package com.uepb.DesafioKnex.dto.request;

import com.uepb.DesafioKnex.model.enums.PaymentMethod;

public record FinishPurchaseRequest(
        PaymentMethod paymentMethod
) {
}
