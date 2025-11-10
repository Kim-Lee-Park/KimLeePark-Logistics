package com.klp.delivery.delivery.domain;

public record Company(
    String companyId,
    String hubId,
    String type,
    String name,
    String address
) {

}