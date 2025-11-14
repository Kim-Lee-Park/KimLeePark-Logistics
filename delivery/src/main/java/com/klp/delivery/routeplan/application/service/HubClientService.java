package com.klp.delivery.routeplan.application.service;

import com.klp.delivery.routeplan.application.command.HubInfo;
import java.util.UUID;

public interface HubClientService {
    HubInfo getHubById(UUID hubId);
}
