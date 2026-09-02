package com.mottainai.operacional.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "BarcodeAnalyzer";

    private final MultiFormatReader reader = new MultiFormatReader();
    private final BarcodeCallback callback;

    public interface BarcodeCallback {
        void onBarcodeDetected(String rawValue);
    }

    public BarcodeAnalyzer(Context context, BarcodeCallback callback) {
        this.callback = callback;

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        java.util.List<BarcodeFormat> formats = java.util.Arrays.asList(
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39,
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX
        );
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        reader.setHints(hints);
    }

    @Override
    public void analyze(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

        try {
            Result result = reader.decodeWithState(createBinaryBitmap(imageProxy, rotationDegrees));
            if (result != null) {
                String rawValue = result.getText();
                if (rawValue != null && !rawValue.trim().isEmpty()) {
                    String normalized = normalizeBarcode(rawValue);
                    if (isValidBarcodeFormat(normalized)) {
                        callback.onBarcodeDetected(normalized);
                    }
                }
            }
        } catch (NotFoundException e) {
            // Normal - no barcode found in this frame
        } catch (Exception e) {
            Log.w(TAG, "Error analyzing barcode", e);
        } finally {
            imageProxy.close();
            reader.reset();
        }
    }

    private com.google.zxing.BinaryBitmap createBinaryBitmap(ImageProxy imageProxy, int rotationDegrees) {
        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();

        ImageProxy.PlaneProxy yPlane = imageProxy.getPlanes()[0];
        java.nio.ByteBuffer buffer = yPlane.getBuffer();
        int yRowStride = yPlane.getRowStride();
        int yPixelStride = yPlane.getPixelStride();

        byte[] yBuffer = new byte[width * height];
        byte[] rowBuffer = new byte[yRowStride];
        for (int row = 0; row < height; row++) {
            buffer.position(row * yRowStride);
            int len = Math.min(yRowStride, buffer.remaining());
            buffer.get(rowBuffer, 0, len);
            for (int col = 0; col < width; col++) {
                yBuffer[row * width + col] = rowBuffer[col * yPixelStride];
            }
        }

        // Rotaciona Y buffer conforme rotationDegrees do sensor
        if (rotationDegrees != 0) {
            yBuffer = rotateYBuffer(yBuffer, width, height, rotationDegrees);
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                int tmp = width; width = height; height = tmp;
            }
        }

        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                yBuffer, width, height, 0, 0, width, height, false);
        return new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
    }

    private byte[] rotateYBuffer(byte[] src, int width, int height, int rotation) {
        byte[] out = new byte[src.length];
        if (rotation == 90) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    out[x * height + (height - 1 - y)] = src[y * width + x];
                }
            }
        } else if (rotation == 180) {
            for (int i = 0; i < src.length; i++) out[src.length - 1 - i] = src[i];
        } else if (rotation == 270) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    out[(width - 1 - x) * height + y] = src[y * width + x];
                }
            }
        } else {
            return src;
        }
        return out;
    }

    private String normalizeBarcode(String raw) {
        // Remove espaços indevidos, preserva zeros à esquerda
        // Also remove any non-printable characters
        String trimmed = raw.trim();
        // Keep alphanumeric and common barcode characters
        return trimmed.replaceAll("[^\\p{Print}]", "");
    }

    private boolean isValidBarcodeFormat(String barcode) {
        // Validação básica: não muito curto, não muito longo
        // Permite alfanumérico para códigos como ARZ-001, FEI-002
        if (barcode.length() < 3 || barcode.length() > 128) {
            return false;
        }
        return true;
    }
}