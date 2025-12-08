package com.klp.delivery.delivery.application.event;

import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;

public interface DeliveryEventPublisher {

    void publishDeliveryEvent(OrderDeliveryEvent event);
}
