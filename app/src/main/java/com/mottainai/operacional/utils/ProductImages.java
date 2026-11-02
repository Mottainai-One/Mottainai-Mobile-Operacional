package com.mottainai.operacional.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.LruCache;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.Product;
import com.mottainai.operacional.models.OpenFoodFactsResponse;
import com.mottainai.operacional.repository.OpenFoodFactsRepository;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Barcode-based public photos; stock, prices and catalog identity remain SQL API data. */
public final class ProductImages {
    private static final OpenFoodFactsRepository SOURCE = new OpenFoodFactsRepository();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Entry> CACHE = new LruCache<>(256);
    private static final LinkedHashMap<String, List<Target>> PENDING = new LinkedHashMap<>();
    private static final long GAP_MS = 5_000; // 12 product reads/minute, below the public 15/minute limit.
    private static SharedPreferences disk;
    private static long lastStarted;
    private static boolean scheduled;
    private static boolean running;
    private ProductImages() { }

    public static void load(ImageView view, Product product) {
        Glide.with(view).clear(view);
        view.setImageResource(R.drawable.op_box);
        view.setContentDescription(product.getName());
        view.setOnLongClickListener(null);
        Object binding = new Object(); view.setTag(binding);
        String barcode = product.getSku(); // The API mapper puts the actual barcode in this legacy UI field.
        if (!ProductImageSource.isBarcode(barcode)) return;
        if (disk == null) disk = view.getContext().getApplicationContext()
                .getSharedPreferences("open_food_facts_images", Context.MODE_PRIVATE);
        Entry cached = cached(barcode);
        if (cached != null) { if (!cached.url.isEmpty()) display(view,cached.url); return; }
        List<Target> targets = PENDING.get(barcode);
        if (targets == null) { targets = new ArrayList<>(); PENDING.put(barcode,targets); }
        targets.add(new Target(view,binding));
        schedule();
    }

    public static void rememberLookup(Context context,String barcode,String url) {
        if(!ProductImageSource.isBarcode(barcode))return;
        if(disk==null)disk=context.getApplicationContext().getSharedPreferences("open_food_facts_images",Context.MODE_PRIVATE);
        String photo=ProductImageSource.isPhotoUrl(url)?url:"";
        long expires=System.currentTimeMillis()+(photo.isEmpty()?3_600_000L:86_400_000L);
        CACHE.put(barcode,new Entry(photo,expires));
        disk.edit().putString("url_"+barcode,photo).putLong("expires_"+barcode,expires).apply();
    }

    private static Entry cached(String code) {
        long now=System.currentTimeMillis();
        Entry value=CACHE.get(code);
        if(value != null && value.expires>now)return value;
        if(value != null)CACHE.remove(code);
        String url=disk.getString("url_"+code,null);
        long expires=disk.getLong("expires_"+code,0);
        if(url==null || expires<=now || (!url.isEmpty() && !ProductImageSource.isPhotoUrl(url)))return null;
        value=new Entry(url,expires);CACHE.put(code,value);return value;
    }

    private static void schedule() {
        if(running || scheduled || PENDING.isEmpty())return;
        scheduled=true;
        long delay=lastStarted==0?0:Math.max(0,GAP_MS-(SystemClock.elapsedRealtime()-lastStarted));
        MAIN.postDelayed(ProductImages::next,delay);
    }

    private static void next() {
        scheduled=false;
        String code=null;
        var iterator=PENDING.entrySet().iterator();
        while(iterator.hasNext()){
            var candidate=iterator.next();
            candidate.getValue().removeIf(target->!target.current());
            if(candidate.getValue().isEmpty()){iterator.remove();continue;}
            code=candidate.getKey();break;
        }
        if(code==null)return;
        final String requested=code;
        running=true;lastStarted=SystemClock.elapsedRealtime();
        SOURCE.fetchByBarcode(requested,new OpenFoodFactsRepository.CallbackResult(){
            @Override public void onSuccess(OpenFoodFactsResponse.ProductData data){
                String url=data.getImageUrl();complete(requested,ProductImageSource.isPhotoUrl(url)?url:"",true);
            }
            @Override public void onNotFound(){complete(requested,"",true);}
            @Override public void onError(String message){complete(requested,"",false);}
        });
    }

    private static void complete(String code,String url,boolean cacheable) {
        if(cacheable){
            long expires=System.currentTimeMillis()+(url.isEmpty()?3_600_000L:86_400_000L);
            CACHE.put(code,new Entry(url,expires));
            disk.edit().putString("url_"+code,url).putLong("expires_"+code,expires).apply();
        }
        List<Target> targets=PENDING.remove(code);
        if(targets!=null && !url.isEmpty())for(Target target:targets){
            ImageView view=target.view.get();
            if(view!=null && target.current() && view.isAttachedToWindow())display(view,url);
        }
        running=false;schedule();
    }

    private static final class Entry {
        final String url;final long expires;
        Entry(String url,long expires){this.url=url;this.expires=expires;}
    }
    private static final class Target {
        final WeakReference<ImageView> view;final Object binding;
        Target(ImageView view,Object binding){this.view=new WeakReference<>(view);this.binding=binding;}
        boolean current(){ImageView value=view.get();return value!=null && value.getTag()==binding;}
    }
    private static void display(ImageView view,String url) {
        Glide.with(view).load(url).fitCenter().placeholder(R.drawable.op_box).error(R.drawable.op_box).into(view);
        view.setOnLongClickListener(v->{new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setMessage(R.string.product_image_source).setPositiveButton(android.R.string.ok,null).show();return true;});
    }
}
