package com.mottainai.operacional.models;

import java.math.BigDecimal;

/**
 * Estado editável do formulário. Separa UI de Product (domínio) para validação.
 * Campos quantity/minQuantity/batch/expiryDate/fornecedor são exibidos mas NÃO enviados
 * para /api/v1/products — pendentes em /api/v1/inventory.
 */
public class ProductForm {
    private String barcode;
    private String name;
    private String description;
    private Integer categoryId;
    private Integer taxProfileId;
    private String ncm;
    private String cest;
    private String imageUrl;
    private String categoryName;
    private String brand;
    private String unitMeasure;
    private String weight; // string para validação, convertido para BigDecimal no envio
    private String quantity;
    private String minQuantity;
    private String batch;
    private String expiryDate; // yyyy-MM-dd
    private String supplier;
    private boolean active = true;
    private Integer version; // para PUT (concorrência)

    // Getters/Setters
    public String getBarcode() { return barcode; }
    public void setBarcode(String v) { barcode = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer v) { categoryId = v; }
    public Integer getTaxProfileId() { return taxProfileId; }
    public void setTaxProfileId(Integer v) { taxProfileId = v; }
    public String getNcm() { return ncm; }
    public void setNcm(String v) { ncm = v; }
    public String getCest() { return cest; }
    public void setCest(String v) { cest = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { imageUrl = v; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String v) { categoryName = v; }
    public String getBrand() { return brand; }
    public void setBrand(String v) { brand = v; }
    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String v) { unitMeasure = v; }
    public String getWeight() { return weight; }
    public void setWeight(String v) { weight = v; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String v) { quantity = v; }
    public String getMinQuantity() { return minQuantity; }
    public void setMinQuantity(String v) { minQuantity = v; }
    public String getBatch() { return batch; }
    public void setBatch(String v) { batch = v; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String v) { expiryDate = v; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String v) { supplier = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { active = v; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { version = v; }

    public BigDecimal getWeightAsBigDecimal() {
        try { return new BigDecimal(weight.replace(",", ".")); } catch (Exception e) { return null; }
    }

    /** Converte para payload aceito pelo backend (apenas campos do contrato). */
    public ProductUpsertRequest toUpsertRequest() {
        ProductUpsertRequest r = new ProductUpsertRequest();
        r.setCategoryId(categoryId);
        r.setTaxProfileId(taxProfileId);
        r.setNcm(ncm != null ? ncm.trim() : null);
        r.setCest(cest != null && !cest.trim().isEmpty() ? cest.trim() : null);
        r.setImageUrl(imageUrl);
        r.setBarcode(barcode != null ? barcode.trim() : null);
        r.setName(name != null ? name.trim() : null);
        r.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
        r.setBrand(brand != null && !brand.trim().isEmpty() ? brand.trim() : null);
        r.setUnitMeasure(unitMeasure != null ? unitMeasure.trim() : null);
        r.setWeight(getWeightAsBigDecimal());
        r.setActive(active);
        r.setVersion(version);
        return r;
    }

    public ProductCreateRequest toCreateRequest() {
        return new ProductCreateRequest(categoryId, taxProfileId, barcode == null ? null : barcode.trim(),
                ncm == null ? null : ncm.trim(), cest == null || cest.trim().isEmpty() ? null : cest.trim(),
                name == null ? null : name.trim(), description == null || description.trim().isEmpty() ? null : description.trim(),
                brand == null || brand.trim().isEmpty() ? null : brand.trim(),
                unitMeasure == null ? null : unitMeasure.trim(), getWeightAsBigDecimal(), imageUrl);
    }

    public ProductUpdateRequest toUpdateRequest() {
        return new ProductUpdateRequest(categoryId, taxProfileId, ncm == null ? null : ncm.trim(),
                cest == null || cest.trim().isEmpty() ? null : cest.trim(), name == null ? null : name.trim(),
                description == null || description.trim().isEmpty() ? null : description.trim(),
                brand == null || brand.trim().isEmpty() ? null : brand.trim(),
                unitMeasure == null ? null : unitMeasure.trim(), getWeightAsBigDecimal(), active, version, imageUrl);
    }
}
