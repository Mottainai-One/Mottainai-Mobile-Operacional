package com.mottainai.operacional.utils;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * Traduz exceções do Firebase Auth para mensagens em português — sem isto o
 * usuário via o texto em inglês do próprio SDK (ex: "The password is invalid...").
 */
public final class AuthErrorTranslator {

    private AuthErrorTranslator() {}

    public static String toUserMessage(Exception e) {
        if (e == null) return "Erro desconhecido. Tente novamente.";

        if (e instanceof FirebaseNetworkException) {
            return "Sem conexão com a internet. Verifique sua rede e tente novamente.";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Muitas tentativas. Aguarde um momento e tente novamente.";
        }
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            if (code != null) {
                switch (code) {
                    case "ERROR_INVALID_EMAIL":
                        return "E-mail inválido.";
                    case "ERROR_USER_DISABLED":
                        return "Esta conta foi desativada. Contate o administrador.";
                    case "ERROR_USER_NOT_FOUND":
                    case "ERROR_WRONG_PASSWORD":
                    case "ERROR_INVALID_CREDENTIAL":
                        return "E-mail ou senha incorretos.";
                    case "ERROR_TOO_MANY_REQUESTS":
                        return "Muitas tentativas. Aguarde um momento e tente novamente.";
                    case "ERROR_USER_TOKEN_EXPIRED":
                        return "Sessão expirada. Faça login novamente.";
                    default:
                        break;
                }
            }
        }
        if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
            return "E-mail ou senha incorretos.";
        }
        return "Não foi possível entrar. Tente novamente.";
    }
}
