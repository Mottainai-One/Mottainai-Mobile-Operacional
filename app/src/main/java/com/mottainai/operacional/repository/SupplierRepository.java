package com.mottainai.operacional.repository;
import android.content.Context;
import com.mottainai.operacional.models.*;
import com.mottainai.operacional.network.*;
import com.mottainai.operacional.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

/** Server-backed CRUD; no fallback to local fixtures or synthetic success. */
public class SupplierRepository {
    public interface Callback<T>{void onSuccess(T value);void onError(String message);}
    private final ApiService api;
    private final SessionManager session;
    public SupplierRepository(Context context){api=RetrofitClient.getClient(context).create(ApiService.class); session=new SessionManager(context);}
    private boolean valid(String storeId){return session.getUid()!=null && storeId!=null && storeId.equals(session.getStoreId());}
    private String error(int code){
        switch(code){case 400:return "Confira os campos obrigatórios do fornecedor e endereço.";
            case 401:return "Sessão recusada pela API. Entre novamente.";
            case 403:return "Seu perfil não pode realizar esta operação.";
            case 404:return "Fornecedor não encontrado na API.";
            case 409:return "A API encontrou um conflito no cadastro do fornecedor.";
            default:return "Não foi possível concluir a operação na API (HTTP "+code+").";}
    }
    public void list(String storeId,Callback<List<Supplier>> callback){
        if(!valid(storeId)){callback.onError("Sessão operacional inválida.");return;}
        page(storeId,session.getUid(),0,new ArrayList<>(),callback);
    }
    private void page(String storeId,String uid,int page,List<Supplier> result,Callback<List<Supplier>> callback){
        api.getSuppliers(page,100).enqueue(new retrofit2.Callback<PageResponse<ApiSupplierResponse>>(){
            public void onResponse(Call<PageResponse<ApiSupplierResponse>> call,Response<PageResponse<ApiSupplierResponse>> response){
                if(!valid(storeId)||!uid.equals(session.getUid())){callback.onError("A sessão foi alterada.");return;}
                PageResponse<ApiSupplierResponse> body=response.body();
                if(!response.isSuccessful()||body==null||body.getContent()==null){callback.onError(error(response.code()));return;}
                if(body.getNumber()!=page){callback.onError("A API retornou uma página inesperada.");return;}
                for(ApiSupplierResponse dto:body.getContent())if(dto!=null&&dto.id!=null)result.add(dto.toSupplier());
                if(body.isLast()||page+1>=body.getTotalPages())callback.onSuccess(result);
                else if(body.getContent().isEmpty())callback.onError("Catálogo incompleto retornado pela API.");
                else page(storeId,uid,page+1,result,callback);
            }
            public void onFailure(Call<PageResponse<ApiSupplierResponse>> call,Throwable error){callback.onError("Não foi possível conectar à API de fornecedores.");}
        });
    }
    private void supplierCall(String storeId,Call<ApiSupplierResponse> call,Callback<Supplier> callback){
        if(!valid(storeId)){callback.onError("Sessão operacional inválida.");return;}
        String uid=session.getUid();
        call.enqueue(new retrofit2.Callback<ApiSupplierResponse>(){
            public void onResponse(Call<ApiSupplierResponse> c,Response<ApiSupplierResponse> response){
                if(!valid(storeId)||!uid.equals(session.getUid())){callback.onError("A sessão foi alterada.");return;}
                if(response.isSuccessful()&&response.body()!=null&&response.body().id!=null)callback.onSuccess(response.body().toSupplier());
                else callback.onError(error(response.code()));
            }
            public void onFailure(Call<ApiSupplierResponse> c,Throwable error){callback.onError("Não foi possível conectar à API de fornecedores.");}
        });
    }
    public void findById(String store,String id,Callback<Supplier> cb){supplierCall(store,api.getSupplier(id),cb);}
    public void create(String store,String name,String cnpj,String email,String phone,SupplierAddress address,Callback<Supplier> cb){
        supplierCall(store,api.createSupplier(new SupplierWriteRequest(name,cnpj,email,phone,address,null)),cb);
    }
    public void update(String store,String id,String name,String email,String phone,SupplierAddress address,Callback<Supplier> cb){
        supplierCall(store,api.updateSupplier(id,new SupplierWriteRequest(name,null,email,phone,address,true)),cb);
    }
    public void delete(String store,String id,Callback<Void> callback){
        if(!valid(store)){callback.onError("Sessão operacional inválida.");return;}
        String uid=session.getUid();
        api.deleteSupplier(id).enqueue(new retrofit2.Callback<Void>(){
            public void onResponse(Call<Void> call,Response<Void> response){
                if(!valid(store)||!uid.equals(session.getUid())){callback.onError("A sessão foi alterada.");return;}
                if(response.isSuccessful())callback.onSuccess(null);else callback.onError(error(response.code()));
            }
            public void onFailure(Call<Void> call,Throwable error){callback.onError("Não foi possível conectar à API de fornecedores.");}
        });
    }
}
