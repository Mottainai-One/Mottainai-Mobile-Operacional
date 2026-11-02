package com.mottainai.operacional.utils;
/** Cancels an operational API subscription/request without a Firestore dependency. */
@FunctionalInterface
public interface DataSubscription { void remove(); }
