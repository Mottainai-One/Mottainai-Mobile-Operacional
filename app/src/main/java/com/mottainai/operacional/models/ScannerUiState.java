package com.mottainai.operacional.models;

import androidx.annotation.NonNull;

public abstract class ScannerUiState {

    public static final class Idle extends ScannerUiState {
        public static final Idle INSTANCE = new Idle();

        private Idle() {}

        @NonNull
        @Override
        public String toString() {
            return "Idle";
        }
    }

    public static final class RequestingPermission extends ScannerUiState {
        public static final RequestingPermission INSTANCE = new RequestingPermission();

        private RequestingPermission() {}

        @NonNull
        @Override
        public String toString() {
            return "RequestingPermission";
        }
    }

    public static final class PermissionDenied extends ScannerUiState {
        public final boolean permanentlyDenied;

        public PermissionDenied(boolean permanentlyDenied) {
            this.permanentlyDenied = permanentlyDenied;
        }

        @NonNull
        @Override
        public String toString() {
            return "PermissionDenied(permanentlyDenied=" + permanentlyDenied + ")";
        }
    }

    public static final class CameraReady extends ScannerUiState {
        public static final CameraReady INSTANCE = new CameraReady();

        private CameraReady() {}

        @NonNull
        @Override
        public String toString() {
            return "CameraReady";
        }
    }

    public static final class Scanning extends ScannerUiState {
        public static final Scanning INSTANCE = new Scanning();

        private Scanning() {}

        @NonNull
        @Override
        public String toString() {
            return "Scanning";
        }
    }

    public static final class LookingUp extends ScannerUiState {
        public final String barcode;

        public LookingUp(String barcode) {
            this.barcode = barcode;
        }

        @NonNull
        @Override
        public String toString() {
            return "LookingUp(barcode=" + barcode + ")";
        }
    }

    public static final class Found extends ScannerUiState {
        public final Product product;

        public Found(Product product) {
            this.product = product;
        }

        @NonNull
        @Override
        public String toString() {
            return "Found(product=" + product.getName() + ")";
        }
    }

    public static final class NotFound extends ScannerUiState {
        public final String barcode;

        public NotFound(String barcode) {
            this.barcode = barcode;
        }

        @NonNull
        @Override
        public String toString() {
            return "NotFound(barcode=" + barcode + ")";
        }
    }

    public static final class Error extends ScannerUiState {
        public final String message;
        public final boolean isNetworkError;
        public final int httpCode;

        public Error(String message, boolean isNetworkError, int httpCode) {
            this.message = message;
            this.isNetworkError = isNetworkError;
            this.httpCode = httpCode;
        }

        @NonNull
        @Override
        public String toString() {
            return "Error(message=" + message + ", network=" + isNetworkError + ", code=" + httpCode + ")";
        }
    }
}