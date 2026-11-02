package com.mottainai.operacional.fragments;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.PosApproval;
import com.mottainai.operacional.models.PosApprovalDecisionRequest;
import com.mottainai.operacional.network.ApiService;
import com.mottainai.operacional.network.RetrofitClient;
import com.mottainai.operacional.utils.RoleHelper;
import com.mottainai.operacional.utils.SessionManager;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Reads and decides actual store-scoped POS requests through the operational API. */
public class PosApprovalsFragment extends Fragment {
    private ApiService api;
    private SessionManager session;
    private LinearLayout requests;
    private TextView status;
    private MaterialButton reload;
    private ProgressBar progress;
    private Call<?> activeCall;
    private int generation;
    private boolean writing;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state){
        generation++;
        session=new SessionManager(requireContext());
        api=RetrofitClient.getClient(requireContext()).create(ApiService.class);
        LinearLayout root=new LinearLayout(requireContext());root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(12),dp(20),dp(16));
        TextView title=text("Aprovações do PDV",20);root.addView(title);
        status=text("Carregando solicitações da loja...",15);root.addView(status);
        progress=new ProgressBar(requireContext());root.addView(progress,new LinearLayout.LayoutParams(-1,dp(36)));
        reload=new MaterialButton(requireContext());reload.setText("Atualizar");reload.setAllCaps(false);root.addView(reload);
        reload.setOnClickListener(v->load());
        ScrollView scroll=new ScrollView(requireContext());requests=new LinearLayout(requireContext());requests.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(requests);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        return root;
    }

    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state){super.onViewCreated(view,state);load();}

    private void load(){
        if(writing||getView()==null)return;
        if(!RoleHelper.canApprovePosRequests(session.getRole())){progress.setVisibility(View.GONE);status.setText("Seu perfil não pode acessar as aprovações do PDV.");reload.setEnabled(false);return;}
        if(activeCall!=null)activeCall.cancel();
        final int expected=generation;final String uid=session.getUid();
        progress.setVisibility(View.VISIBLE);status.setText("Consultando solicitações da loja...");
        Call<List<PosApproval>> call=api.getPosApprovals("PENDING",50);activeCall=call;
        call.enqueue(new Callback<List<PosApproval>>(){
            @Override public void onResponse(Call<List<PosApproval>> call,Response<List<PosApproval>> response){
                if(!accepts(expected,uid))return;progress.setVisibility(View.GONE);
                if(!response.isSuccessful()||response.body()==null){status.setText(error(response.code()));return;}
                requests.removeAllViews();
                status.setText(response.body().isEmpty()?"Nenhuma solicitação pendente nesta loja.":"Solicitações pendentes da loja "+session.getStoreId());
                for(PosApproval request:response.body())if(request!=null&&request.id!=null)requests.addView(card(request));
            }
            @Override public void onFailure(Call<List<PosApproval>> call,Throwable failure){
                if(!accepts(expected,uid)||call.isCanceled())return;progress.setVisibility(View.GONE);status.setText("Não foi possível conectar ao serviço de aprovações. Tente atualizar.");
            }
        });
    }

    private View card(PosApproval request){
        MaterialCardView card=new MaterialCardView(requireContext());card.setRadius(dp(12));
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.op_surface));
        LinearLayout.LayoutParams bounds=new LinearLayout.LayoutParams(-1,-2);bounds.setMargins(0,dp(10),0,dp(4));card.setLayoutParams(bounds);
        LinearLayout content=new LinearLayout(requireContext());content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(16),dp(12),dp(16),dp(12));
        content.addView(text(type(request.requestType),18));content.addView(text("Venda #"+request.saleId+" · Colaborador #"+request.employeeId,14));
        if(request.saleItemId!=null)content.addView(text("Item #"+request.saleItemId,14));
        if(request.reason!=null)content.addView(text(request.reason,16));
        if(request.requestedAmount!=null)content.addView(text(NumberFormat.getCurrencyInstance(new Locale("pt","BR")).format(request.requestedAmount),16));
        if(request.discountPercent!=null)content.addView(text("Desconto solicitado: "+request.discountPercent.stripTrailingZeros().toPlainString()+"%",15));
        MaterialButton approve=new MaterialButton(requireContext());approve.setText("Aprovar");approve.setAllCaps(false);approve.setOnClickListener(v->confirm(request,true));content.addView(approve);
        MaterialButton reject=new MaterialButton(requireContext());reject.setText("Recusar");reject.setAllCaps(false);reject.setOnClickListener(v->confirm(request,false));content.addView(reject);
        card.addView(content);return card;
    }

    private void confirm(PosApproval request,boolean approve){
        if(writing)return;
        EditText observation=new EditText(requireContext());observation.setHint("Observação (opcional)");observation.setMaxLines(3);observation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(250)});
        new MaterialAlertDialogBuilder(requireContext()).setTitle(approve?"Aprovar solicitação?":"Recusar solicitação?")
                .setMessage(type(request.requestType)+" — venda #"+request.saleId).setView(observation)
                .setNegativeButton("Voltar",null).setPositiveButton("Confirmar",(dialog,which)->decide(request,approve,observation.getText().toString().trim())).show();
    }

    private void decide(PosApproval request,boolean approve,String observation){
        if(writing)return;writing=true;reload.setEnabled(false);progress.setVisibility(View.VISIBLE);
        final int expected=generation;final String uid=session.getUid();
        Call<PosApproval> call=api.decidePosApproval(request.id,new PosApprovalDecisionRequest(approve?"APPROVED":"REJECTED",observation.isEmpty()?null:observation));activeCall=call;
        call.enqueue(new Callback<PosApproval>(){
            @Override public void onResponse(Call<PosApproval> call,Response<PosApproval> response){
                if(!accepts(expected,uid))return;writing=false;reload.setEnabled(true);progress.setVisibility(View.GONE);
                if(!response.isSuccessful()||response.body()==null){status.setText(error(response.code()));return;}
                Toast.makeText(requireContext(),"EXECUTED".equals(response.body().status)?"Cancelamento executado pela API.":"Decisão registrada pela API.",Toast.LENGTH_SHORT).show();load();
            }
            @Override public void onFailure(Call<PosApproval> call,Throwable failure){
                if(!accepts(expected,uid)||call.isCanceled())return;writing=false;reload.setEnabled(true);progress.setVisibility(View.GONE);status.setText("Não foi possível confirmar a decisão. Atualize a fila antes de tentar novamente.");
            }
        });
    }
    private boolean accepts(int expected,String uid){return getView()!=null&&generation==expected&&uid!=null&&uid.equals(session.getUid());}
    private String error(int code){
        if(code==401)return "Sessão recusada. Entre novamente.";
        if(code==403)return "Seu perfil não pode acessar esta operação.";
        if(code==409)return "A solicitação foi alterada ou já decidida. Atualize a fila.";
        if(code==404)return "Solicitação não encontrada. Atualize a fila.";
        if(code>=500)return "Serviço de aprovações temporariamente indisponível. Tente novamente mais tarde.";
        return "Não foi possível concluir a operação (HTTP "+code+").";
    }
    private String type(String value){
        if(value==null)return "Solicitação do PDV";
        switch(value){case "CANCEL_PURCHASE":return "Cancelamento da compra";case "CANCEL_PRODUCT":return "Cancelamento do produto";case "REFUND":return "Reembolso";case "RETURN":return "Devolução";case "SPECIAL_DISCOUNT":return "Desconto especial";default:return "Solicitação do PDV";}
    }
    private TextView text(String value,int size){TextView view=new TextView(requireContext());view.setText(value);view.setTextSize(size);view.setTextColor(ContextCompat.getColor(requireContext(),R.color.op_text));view.setPadding(0,dp(5),0,dp(5));return view;}
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density);}
    @Override public void onDestroyView(){generation++;if(activeCall!=null)activeCall.cancel();writing=false;requests=null;status=null;progress=null;reload=null;super.onDestroyView();}
}
