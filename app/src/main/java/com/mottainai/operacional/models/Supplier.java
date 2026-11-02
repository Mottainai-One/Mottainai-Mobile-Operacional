package com.mottainai.operacional.models;
/** Supplier returned by the operational API; no seeded or device-only records. */
public class Supplier {
    private final String id, tradeName, cnpj, email, phone;
    private final SupplierAddress address;
    private final Boolean active;
    public Supplier(String id,String tradeName,String cnpj,String contact) {
        this(id,tradeName,cnpj,contact!=null && contact.contains("@")?contact:null,
                contact!=null && !contact.contains("@")?contact:null,null,true);
    }
    public Supplier(String id,String tradeName,String cnpj,String email,String phone,SupplierAddress address,Boolean active) {
        this.id=id; this.tradeName=tradeName; this.cnpj=cnpj; this.email=email; this.phone=phone;
        this.address=address; this.active=active;
    }
    public String getId(){return id;}
    public String getTradeName(){return tradeName;}
    public String getCnpj(){return cnpj;}
    public String getContact(){return email!=null && !email.isEmpty()?email:phone;}
    public String getEmail(){return email;}
    public String getPhone(){return phone;}
    public SupplierAddress getAddress(){return address;}
    public Boolean getActive(){return active;}
}
