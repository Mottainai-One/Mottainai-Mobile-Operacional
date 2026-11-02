package com.mottainai.operacional.utils;

import java.net.URI;
import java.util.Locale;

/** Public product photos are obtained from Open Food Facts, never guessed from a product name. */
public final class ProductImageSource {
    private ProductImageSource() { }
    public static boolean isBarcode(String value) {
        return value != null && value.matches("[0-9]{8,14}");
    }
    public static boolean isPhotoUrl(String value) {
        if (value == null) return false;
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getUserInfo() == null && host != null
                    && (host.toLowerCase(Locale.ROOT).equals("images.openfoodfacts.org")
                    || host.toLowerCase(Locale.ROOT).equals("static.openfoodfacts.org"));
        } catch (IllegalArgumentException error) { return false; }
    }
}
