package com.klp.order.review.application.command;

public record UpdateReviewCommand(
    int rating,
    String content
) {

}
