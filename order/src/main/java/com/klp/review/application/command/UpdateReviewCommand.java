package com.klp.review.application.command;

public record UpdateReviewCommand(
    int rating,
    String content
) {
}
