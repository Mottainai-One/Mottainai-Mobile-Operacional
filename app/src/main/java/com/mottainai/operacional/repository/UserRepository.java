package com.mottainai.operacional.repository;

import com.mottainai.operacional.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    public interface TeamCallback {
        void onSuccess(List<User> users);
        void onError(Exception error);
    }

    public void listTeam(android.content.Context context, TeamCallback callback) {
        com.mottainai.operacional.utils.SessionManager session = new com.mottainai.operacional.utils.SessionManager(context);
        String uid = session.getUid();
        String storeId = session.getStoreId();
        if (uid == null || storeId == null || !com.mottainai.operacional.utils.RoleHelper.canRegisterProduct(session.getRole())) {
            callback.onError(new SecurityException("Sem permissão para consultar a equipe."));
            return;
        }
        com.mottainai.operacional.network.RetrofitClient.getClient(context)
                .create(com.mottainai.operacional.network.ApiService.class).getStoreTeam()
                .enqueue(new retrofit2.Callback<List<com.mottainai.operacional.models.TeamMemberResponse>>() {
                    @Override public void onResponse(retrofit2.Call<List<com.mottainai.operacional.models.TeamMemberResponse>> call,
                            retrofit2.Response<List<com.mottainai.operacional.models.TeamMemberResponse>> response) {
                        if (!uid.equals(session.getUid()) || !storeId.equals(session.getStoreId())) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError(new IllegalStateException(response.code() == 403
                                    ? "Seu perfil não tem acesso à equipe." : "Não foi possível consultar a equipe (HTTP " + response.code() + ")."));
                            return;
                        }
                        List<User> members = new ArrayList<>();
                        for (com.mottainai.operacional.models.TeamMemberResponse member : response.body()) {
                            if (member == null || member.storeId == null || !storeId.equals(String.valueOf(member.storeId))) {
                                callback.onError(new SecurityException("A API retornou uma equipe fora da loja da sessão."));
                                return;
                            }
                            members.add(new User(String.valueOf(member.employeeId), member.name, member.role, String.valueOf(member.storeId)));
                        }
                        callback.onSuccess(members);
                    }
                    @Override public void onFailure(retrofit2.Call<List<com.mottainai.operacional.models.TeamMemberResponse>> call, Throwable error) {
                        if (!uid.equals(session.getUid()) || !storeId.equals(session.getStoreId())) return;
                        callback.onError(new IllegalStateException("Sem conexão com a API de equipe. Tente novamente."));
                    }
                });
    }

}
