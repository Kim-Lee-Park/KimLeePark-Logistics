package com.klp.delivery.delivery.application.command;

public record CompanyCommand(
    String companyId,
    String hubId,
    String type,
    String name,
    String address
) {

}