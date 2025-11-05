package com.klp.hub.hub.presentation.dto.request;

public record UpdateHubRequest(
    String name,
    Long latitude,
    Long longitude,
    String address
) {

}
