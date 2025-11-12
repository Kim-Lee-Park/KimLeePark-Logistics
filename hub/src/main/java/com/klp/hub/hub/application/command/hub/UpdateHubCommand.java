package com.klp.hub.hub.application.command.hub;

public record UpdateHubCommand(
    String name,
    Double latitude,
    Double longitude,
    String address
) {
    public Boolean nameIsNotNull(){return name != null;}
    public Boolean latitudeIsNotNull(){return latitude != null;}
    public Boolean longitudeIsNotNull(){return longitude != null;}
    public Boolean addressIsNotNull(){return address != null;}
}
