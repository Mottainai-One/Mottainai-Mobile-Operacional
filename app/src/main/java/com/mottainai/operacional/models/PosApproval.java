package com.mottainai.operacional.models;
import java.math.BigDecimal;
/** Actual operational POS approval response. */
public class PosApproval {
    public String id,requestType,reason,status,saleDate,createdAt,decidedAt;
    public Integer storeId,employeeId,saleId,saleItemId,cancellationRequestId,decidedByEmployeeId;
    public BigDecimal requestedAmount,discountPercent;
}
