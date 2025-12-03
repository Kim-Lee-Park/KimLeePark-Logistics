package com.klp.delivery.delivery.fixture;

import static com.klp.delivery.delivery.fixture.OrderItemFixture.orderItemCommandsDefault;

import com.klp.delivery.delivery.application.command.HubInfoCommand;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.infrastructure.client.dto.HubInfoResponse;
import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest.OrderItem;
import com.klp.delivery.routeplan.application.command.HubInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DeliveryFixture {

    public static UUID DEFAULT_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");              // 주문 ID
    public static UUID DEFAULT_DEPARTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");          // 출발 허브 ID
    public static String DEFAULT_DEPARTURE_NAME = "서울센터";                                                           // 출발 허브 이름
    public static UUID DEFAULT_ARRIVAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");            // 도착 허브 ID
    public static String DEFAULT_ARRIVAL_NAME = "부산센터";                                                             // 도착 허브 이름
    public static Long DEFAULT_USER_DRIVER_ID = 1234L;                                                                // 고객 배송 담당자 ID
    public static Long NEW_USER_DRIVER_ID = 4567L;                                                                    // 새로운 고객 배송 담당자 ID
    public static String DEFAULT_USER_DRIVER_NAME = "고객배송담당자";                                                     // 고객 배송 담당자 이름
    public static String DEFAULT_USER_DRIVER_SLACK_ID = "슬랙아이디";                                                    // 고객 배송 담당자 슬랙 ID

    public static UUID DEFAULT_DELIVERY_ID_FIRST = UUID.fromString("00000000-0000-0000-0000-000000000006");      // 배송 ID
    public static UUID DEFAULT_DELIVERY_ID_SECOND = UUID.fromString("00000000-0000-0000-0000-000000000007");     // 다른 배송 ID

//    public static UUID DEFAULT_HUB_ID = DEFAULT_DEPARTURE_ID;                                                // 기본 허브 ID
    public static String DEFAULT_RECEIVER_SLACK_ID = "U123456";

    public static final Double DEFALT_HUB_LATITUDE = 0.0;
    public static final Double DEFALT_HUB_LONGITUDE = 0.0;
    public static final String DEFALT_HUB_ADDRESS = "경기도";

    public static String CUSTOMER_NAME = "김이박";
    public static String CUSTOMER_ADDRESS = "경기도 남양주시";
    public static String CUSTOMER_EMAIL = "abc@gmail.com";


    public static String DEFAULT_IDEMPOTENCY_KEY = "멱등키123";


    public static final Long DRIVER_USER_ID = 1L;
    public static final String DRIVER_USERNAME = "test-username";
    public static final String DRIVER_SLACK_ID = "U1234567890";
    public static final String DRIVER_PHONE = "010-1234-5678";
    public static final String DRIVER_EMAIL = "driver@test.com";
    public static final String DEFAULT_HUB_STATUS = "hub";


    public static UUID DEFAULT_USER_ADDRESS_HUB_ID = DEFAULT_ARRIVAL_ID;
    public static LocalDateTime DEFAULT_ORDER_CREATED_AT = LocalDateTime.now();
    public static String COMMENT = "빨리주세요";



    public static HubInfoCommand createDeparutreInfoCommand() {
        return new HubInfoCommand(
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFALT_HUB_LATITUDE,
            DEFALT_HUB_LONGITUDE,
            DEFALT_HUB_ADDRESS,
            DEFAULT_HUB_STATUS
        );
    }

    public static HubInfoCommand createArrivalInfoCommand() {
        return new HubInfoCommand(
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            DEFALT_HUB_LATITUDE,
            DEFALT_HUB_LONGITUDE,
            DEFALT_HUB_ADDRESS,
            DEFAULT_HUB_STATUS
        );
    }


    public static HubInfoResponse createDeparutreInfoResponse() {
        return new HubInfoResponse(
            DEFAULT_DEPARTURE_ID.toString(),
            DEFAULT_DEPARTURE_NAME,
            DEFALT_HUB_LATITUDE,
            DEFALT_HUB_LONGITUDE,
            DEFALT_HUB_ADDRESS,
            DEFAULT_HUB_STATUS
        );
    }

    public static HubInfoResponse createArrivalInfoResponse() {
        return new HubInfoResponse(
            DEFAULT_ARRIVAL_ID.toString(),
            DEFAULT_ARRIVAL_NAME,
            DEFALT_HUB_LATITUDE,
            DEFALT_HUB_LONGITUDE,
            DEFALT_HUB_ADDRESS,
            DEFAULT_HUB_STATUS
        );
    }

    public static HubInfo createDepartureHubInfo() {
        return new HubInfo(
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFALT_HUB_LATITUDE,
            DEFALT_HUB_LONGITUDE,
            DEFALT_HUB_ADDRESS,
            DEFAULT_HUB_STATUS
        );
    }

    public static HubInfo createArrivalHubInfo() {
        return new HubInfo(
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            DEFALT_HUB_LATITUDE,
            DEFALT_HUB_LONGITUDE,
            DEFALT_HUB_ADDRESS,
            DEFAULT_HUB_STATUS
        );
    }


    public static DriverCommand createDriver() {
        return DriverCommand.of(createDriversResponse());
    }

    public static List<DriverCommand> createDrivers() {
        return DriverCommand.from(createDriversResponses());
    }


    public static DriverResponse createDriversResponse() {
        return new DriverResponse(DRIVER_USER_ID, DRIVER_USERNAME, DRIVER_SLACK_ID, DRIVER_PHONE, DRIVER_EMAIL);
    }

    public static List<DriverResponse> createDriversResponses() {
        return List.of(new DriverResponse(DRIVER_USER_ID, DRIVER_USERNAME, DRIVER_SLACK_ID, DRIVER_PHONE, DRIVER_EMAIL));
    }


    private static Delivery buildDelivery(
        UUID orderId,
        Long userDriverId,
        String userDriverSlackId,
        UUID departureId,
        String departureName,
        UUID arrivalId,
        String arrivalName,
        String userName,
        String userAddress
    ) {
        return Delivery.create(
            orderId,
            userDriverId,
            userDriverSlackId,
            departureId,
            departureName,
            arrivalId,
            arrivalName,
            userName,
            userAddress,
            orderItemCommandsDefault()
        );
    }

    // 아이템2개 같은 허브
    public static Delivery defaultDelivery() {
        return Delivery.create(
            DEFAULT_ORDER_ID,
            DEFAULT_USER_DRIVER_ID,
            DEFAULT_USER_DRIVER_SLACK_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            CUSTOMER_NAME,
            CUSTOMER_ADDRESS,
            orderItemCommandsDefault()
        );
    }

    // 여러 개의 Delivery를 포함한 리스트 (테스트용)
    public static List<Delivery> deliveryList() {
        Delivery delivery1 = defaultDelivery();
        Delivery delivery2 = defaultDelivery();
        setDeliveryId(delivery1, DEFAULT_DELIVERY_ID_FIRST);
        setDeliveryId(delivery2, DEFAULT_DELIVERY_ID_SECOND);
        return List.of(delivery1, delivery2);
    }


    public static Delivery deliveryWithCustomHubId(List<OrderItemCommand> items) {
        return Delivery.create(
            DEFAULT_ORDER_ID,
            DEFAULT_USER_DRIVER_ID,
            DEFAULT_USER_DRIVER_SLACK_ID,
            items.get(0).hubId(), //(출발 허브),
            DEFAULT_DEPARTURE_NAME,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            CUSTOMER_NAME,
            CUSTOMER_ADDRESS,
            items
        );
    }


    // 리플렉션으로 deliveryId 설정 (테스트용)
    private static void setDeliveryId(Delivery delivery, UUID deliveryId) {
        try {
            var field = Delivery.class.getDeclaredField("deliveryId");
            field.setAccessible(true);
            field.set(delivery, deliveryId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //  기본 배송 객체 생성 후, 리플렉션으로 deliveryId를 지정
    public static Delivery createDelivery(UUID deliveryId) {
        Delivery delivery = defaultDelivery();
        setDeliveryId(delivery, deliveryId);
        return delivery;
    }

    // orderItems와 deliveryId를 받아서 Delivery 생성 (테스트용)
    public static Delivery createDeliveryWithItems(UUID deliveryId,
        List<OrderItemCommand> orderItems) {
        Delivery delivery = deliveryWithCustomHubId(orderItems);
        setDeliveryId(delivery, deliveryId);
        return delivery;
    }

    // DeliveryCommand와 orderItems를 받아서 hubId에 따라 적절한 deliveryId를 가진 Delivery 생성 (테스트용)
    public static Delivery createDeliveryFromCommand(DeliveryCommand cmd,
        List<OrderItemCommand> orderItems) {
        UUID deliveryId = cmd.departureId().equals(DEFAULT_DEPARTURE_ID)
            ? DEFAULT_DELIVERY_ID_FIRST
            : DEFAULT_DELIVERY_ID_SECOND;
        return createDeliveryWithItems(deliveryId, orderItems);
    }


    // 주소만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithAddress(String companyAddress) {
        return buildDelivery(
            DEFAULT_ORDER_ID,
            DEFAULT_USER_DRIVER_ID,
            DEFAULT_USER_DRIVER_SLACK_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            CUSTOMER_NAME,
            companyAddress
        );
    }


    // 슬랙 ID만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithSlackId(String slackId) {
        return buildDelivery(
            DEFAULT_ORDER_ID,
            DEFAULT_USER_DRIVER_ID,
            slackId,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            CUSTOMER_NAME,
            CUSTOMER_ADDRESS
        );
    }

    // 배송자 ID만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithVendorDriverId(Long vendorDriverId) {
        return buildDelivery(
            DEFAULT_ORDER_ID,
            vendorDriverId,
            DEFAULT_USER_DRIVER_SLACK_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            CUSTOMER_NAME,
            CUSTOMER_ADDRESS
        );
    }

    // 수령업체 이름만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithReceiverName(String receiverName) {
        return buildDelivery(
            DEFAULT_ORDER_ID,
            DEFAULT_USER_DRIVER_ID,
            DEFAULT_USER_DRIVER_SLACK_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_DEPARTURE_NAME,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_ARRIVAL_NAME,
            receiverName,
            CUSTOMER_ADDRESS
        );
    }

    public static DeliveryCreateRequest createDeliveryRequest(List<OrderItem> orderItems) {
        return new DeliveryCreateRequest(
            DEFAULT_ORDER_ID.toString(),
            DEFAULT_IDEMPOTENCY_KEY,
            CUSTOMER_NAME,
            CUSTOMER_EMAIL,
            CUSTOMER_ADDRESS,
            DEFAULT_USER_ADDRESS_HUB_ID.toString(),
            DEFAULT_ORDER_CREATED_AT,
            COMMENT,
            orderItems
        );
    }

    public static DeliveryCreateRequest createDeliveryRequest(UUID orderId, List<OrderItem> orderItems) {
        return new DeliveryCreateRequest(
            orderId.toString(),
            DEFAULT_IDEMPOTENCY_KEY + "-" + orderId,
            CUSTOMER_NAME,
            CUSTOMER_EMAIL,
            CUSTOMER_ADDRESS,
            DEFAULT_USER_ADDRESS_HUB_ID.toString(),
            DEFAULT_ORDER_CREATED_AT,
            COMMENT,
            orderItems
        );
    }


}
