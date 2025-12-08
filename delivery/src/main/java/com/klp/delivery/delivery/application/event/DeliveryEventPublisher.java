package com.klp.delivery.delivery.application.event;

import com.klp.delivery.delivery.domain.event.DeliveryNotificationEvent;
import com.klp.delivery.delivery.domain.event.OrderDeliveryEvent;

public interface DeliveryEventPublisher {

    void publishCreatedEvent(OrderDeliveryEvent event);

    void publishShippingEvent(OrderDeliveryEvent event);

    void publishArrivedEvent(OrderDeliveryEvent event);

    void publishNotificationEvent(DeliveryNotificationEvent event);
}
