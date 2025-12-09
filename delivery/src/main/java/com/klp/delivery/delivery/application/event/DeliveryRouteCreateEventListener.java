package com.klp.delivery.delivery.application.event;

import com.klp.delivery.delivery.application.command.DeliveryRouteCommand;
import com.klp.delivery.delivery.application.facade.DeliveryRouteFacade;
import com.klp.delivery.delivery.domain.event.DeliveryRouteCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryRouteCreateEventListener {

    private final DeliveryRouteFacade deliveryRouteFacade;

    // 배송생성 후 배송경로 생성 (비동기)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeliveryRouteCreateEvent(DeliveryRouteCreateEvent event) {
        log.info("배송 경로 생성 이벤트 수신: deliveryId={}, departureId={}, arrivalId={}, vendorDrvierId={} ",
            event.deliveryId(), event.departureId(), event.arrivalId(), event.drvierId());

        DeliveryRouteCommand command = new DeliveryRouteCommand(
            event.deliveryId(),
            event.departureId(),
            event.departureName(),
            event.arrivalId(),
            event.arrivalName(),
            event.drvierId()
        );
        deliveryRouteFacade.CreateDeliveryRoute(command, event);
    }

}
