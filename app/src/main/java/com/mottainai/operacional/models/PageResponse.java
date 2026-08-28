package com.mottainai.operacional.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Envelope de paginação da API Spring (Spring Data Page).
 * Usado quando GET /api/v1/products retorna Page<ProductResponse>.
 */
public class PageResponse<T> {

    @SerializedName("content")
    private List<T> content;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("number")
    private int number;

    @SerializedName("size")
    private int size;

    @SerializedName("first")
    private boolean first;

    @SerializedName("last")
    private boolean last;

    @SerializedName("empty")
    private boolean empty;

    public List<T> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
    public boolean isEmpty() { return empty; }
}