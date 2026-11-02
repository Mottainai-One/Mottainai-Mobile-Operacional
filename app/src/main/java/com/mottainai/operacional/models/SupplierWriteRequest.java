package com.mottainai.operacional.models;
/** Create contains CNPJ; updates preserve the immutable CNPJ by omitting it. */
public class SupplierWriteRequest {
    public final String tradeName,cnpj,email,phone;
    public final SupplierAddress address;
    public final Boolean active;
    public SupplierWriteRequest(String name,String cnpj,String email,String phone,SupplierAddress address,Boolean active) {
        this.tradeName=name; this.cnpj=cnpj; this.email=email; this.phone=phone; this.address=address; this.active=active;
    }
}
