package com.mottainai.operacional;

import com.google.gson.Gson;
import com.mottainai.operacional.models.OpenFoodFactsResponse;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpenFoodFactsImageTest {
    private OpenFoodFactsResponse.ProductData product(String json) {
        return new Gson().fromJson(json, OpenFoodFactsResponse.ProductData.class);
    }
    @Test public void prefersFrontImage() {
        assertEquals("https://front", product("{\"image_front_url\":\"https://front\",\"image_url\":\"https://general\"}").getImageUrl());
    }
    @Test public void fallsBackToGeneralImage() {
        assertEquals("https://general", product("{\"image_front_url\":\"\",\"image_url\":\"https://general\"}").getImageUrl());
    }
    @Test public void missingImageIsNotInvented() {
        assertNull(product("{}").getImageUrl());
    }
}
