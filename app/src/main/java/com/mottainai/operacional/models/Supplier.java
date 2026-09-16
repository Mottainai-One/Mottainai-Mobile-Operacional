package com.mottainai.operacional.models;

/** Fornecedor persistido localmente enquanto a API relacional não está disponível no app. */
public class Supplier {
    private final String id;
    private final String tradeName;
    private final String cnpj;
    private final String contact;

    public Supplier(String id, String tradeName, String cnpj, String contact) {
        this.id = id;
        this.tradeName = tradeName;
        this.cnpj = cnpj;
        this.contact = contact;
    }

    public String getId() { return id; }
    public String getTradeName() { return tradeName; }
    public String getCnpj() { return cnpj; }
    public String getContact() { return contact; }
}
