package com.mottainai.operacional.models;

public abstract class DamageUiState {
    public static class LoadingProduct extends DamageUiState {}
    public static class FormReady extends DamageUiState { public Product product; public FormReady(Product p){product=p;} }
    public static class NoProduct extends DamageUiState {}
    public static class Uploading extends DamageUiState {}
    public static class Submitting extends DamageUiState {}
    public static class Success extends DamageUiState { public Damage damage; public Success(Damage d){damage=d;} }
    public static class Error extends DamageUiState { public String message; public Error(String m){message=m;} }
}