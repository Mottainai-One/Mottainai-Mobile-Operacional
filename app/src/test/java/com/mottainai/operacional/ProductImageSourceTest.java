package com.mottainai.operacional;
import com.mottainai.operacional.utils.ProductImageSource;
import org.junit.Test;
import static org.junit.Assert.*;
public class ProductImageSourceTest {
    @Test public void acceptsOnlyNumericProductBarcodes(){assertTrue(ProductImageSource.isBarcode("3017620422003"));assertFalse(ProductImageSource.isBarcode("SKU-301762"));assertFalse(ProductImageSource.isBarcode(null));}
    @Test public void acceptsOfficialHttpsPhoto(){assertTrue(ProductImageSource.isPhotoUrl("https://images.openfoodfacts.org/images/products/301/762/042/2003/front.400.jpg"));}
    @Test public void rejectsOtherSourcesAndImpersonatedHosts(){assertFalse(ProductImageSource.isPhotoUrl("https://images.openfoodfacts.org.evil.test/photo.jpg"));assertFalse(ProductImageSource.isPhotoUrl("https://images.openfoodfacts.org@evil.test/photo.jpg"));assertFalse(ProductImageSource.isPhotoUrl("https://res.cloudinary.com/demo/image/upload/photo.jpg"));}
    @Test public void rejectsInsecureOrMalformedPhotos(){assertFalse(ProductImageSource.isPhotoUrl("http://images.openfoodfacts.org/photo.jpg"));assertFalse(ProductImageSource.isPhotoUrl("not a url"));assertFalse(ProductImageSource.isPhotoUrl(null));}
}
