package com.klp.delivery.routeplan.fixture;

import com.klp.delivery.routeplan.application.command.HubInfo;
import java.util.UUID;

public class HubFixture {
    public static UUID HUB_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    public static String NAME = "testHub";
    public static Double LATITUDE = 11.;
    public static Double LONGITUDE = 12.;
    public static String ADDRESS = "testAddress";
    public static String STATUS = "ACTIVE";

    public static HubInfo createHub(){
        return new HubInfo(
            HUB_ID,
            NAME,
            LATITUDE,
            LONGITUDE,
            ADDRESS,
            STATUS
        );
    }

    public static HubInfo createHubWithId(UUID id){
        return new HubInfo(
            id,
            NAME,
            LATITUDE,
            LONGITUDE,
            ADDRESS,
            STATUS
        );
    }
}
