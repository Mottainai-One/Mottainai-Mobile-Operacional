package com.mottainai.operacional.utils;
import android.view.View;
import android.widget.EditText;
import com.mottainai.operacional.R;
import com.mottainai.operacional.models.Supplier;
import com.mottainai.operacional.models.SupplierAddress;
import java.util.Locale;

/** Required address data is entered by the user, never invented to satisfy the API. */
public final class SupplierFormFields {
    private SupplierFormFields(){}
    private static EditText input(View root,int id){return root.findViewById(id);}
    private static String text(View root,int id){return input(root,id).getText().toString().trim();}
    public static String phone(View root){String value=text(root,R.id.et_supplier_phone);return value.isEmpty()?null:value;}
    public static SupplierAddress read(View root){
        int[] ids={R.id.et_supplier_zip_code,R.id.et_supplier_street,R.id.et_supplier_number,R.id.et_supplier_neighborhood,R.id.et_supplier_city,R.id.et_supplier_state};
        for(int id:ids)if(text(root,id).isEmpty()){input(root,id).setError("Campo obrigatório");input(root,id).requestFocus();return null;}
        String zip=text(root,ids[0]).replaceAll("\\D","");
        if(!zip.matches("[0-9]{8}")){input(root,ids[0]).setError("Informe um CEP com 8 dígitos");return null;}
        String state=text(root,ids[5]).toUpperCase(Locale.ROOT);
        if(!state.matches("[A-Z]{2}")){input(root,ids[5]).setError("Informe a UF com duas letras");return null;}
        return new SupplierAddress(zip,text(root,ids[1]),text(root,ids[2]),text(root,R.id.et_supplier_complement),text(root,ids[3]),text(root,ids[4]),state);
    }
    public static void populate(View root,Supplier supplier){
        input(root,R.id.et_supplier_contact).setText(supplier.getEmail());
        input(root,R.id.et_supplier_phone).setText(supplier.getPhone());
        SupplierAddress address=supplier.getAddress();if(address==null)return;
        int[] ids={R.id.et_supplier_zip_code,R.id.et_supplier_street,R.id.et_supplier_number,R.id.et_supplier_complement,R.id.et_supplier_neighborhood,R.id.et_supplier_city,R.id.et_supplier_state};
        String[] values={address.zipCode,address.street,address.number,address.complement,address.neighborhood,address.city,address.state};
        for(int i=0;i<ids.length;i++)input(root,ids[i]).setText(values[i]);
    }
}
