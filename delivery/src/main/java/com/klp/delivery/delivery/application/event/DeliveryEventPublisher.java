package com.klp.delivery.delivery.application.event;

import com.klp.delivery.delivery.domain.event.DeliveryArrivedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryArrivedFailedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryCreatedFailedEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingEvent;
import com.klp.delivery.delivery.domain.event.DeliveryShippingFailedEvent;

public interface DeliveryEventPublisher {

    void publishCreatedEvent(DeliveryCreatedEvent event);

    void publishShippingEvent(DeliveryShippingEvent event);

    void publishArrivedEvent(DeliveryArrivedEvent event);

    void publishCreatedFailedEvent(DeliveryCreatedFailedEvent event);

    void publishShippingFailedEvent(DeliveryShippingFailedEvent event);

    void publishArrivedFailedEvent(DeliveryArrivedFailedEvent event);
}
