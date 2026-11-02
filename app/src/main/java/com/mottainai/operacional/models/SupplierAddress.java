package com.mottainai.operacional.models;
/** Address contract shared by create/update supplier API requests. */
public class SupplierAddress {
    public String zipCode, street, number, complement, neighborhood, city, state;
    public SupplierAddress(String zipCode, String street, String number, String complement, String neighborhood, String city, String state) {
        this.zipCode=zipCode; this.street=street; this.number=number; this.complement=complement;
        this.neighborhood=neighborhood; this.city=city; this.state=state;
    }
}
