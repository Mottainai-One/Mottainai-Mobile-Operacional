package com.mottainai.operacional.models;
public class ApiSupplierResponse {
    public Integer id;
    public String tradeName,cnpj,email,phone;
    public Boolean active;
    public SupplierAddress address;
    public Supplier toSupplier(){return new Supplier(String.valueOf(id),tradeName,cnpj,email,phone,address,active);}
}
