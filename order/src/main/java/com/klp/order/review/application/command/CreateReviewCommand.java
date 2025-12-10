package com.klp.order.review.application.command;

import java.util.UUID;

public record CreateReviewCommand(
    UUID orderId,
    UUID productId,
    Long userId,
    int rating,
    String content
) {

}
