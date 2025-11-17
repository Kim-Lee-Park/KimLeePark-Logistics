package com.klp.delivery.delivery.fixture;

import static com.klp.delivery.delivery.fixture.OrderItemFixture.orderItemCommands;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.orderItemCommandsDefault;

import com.klp.delivery.delivery.application.command.CompanyCommand;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest.OrderItem;
import java.util.List;
import java.util.UUID;

public class DeliveryFixture {

  public static UUID DEFAULT_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");               // 주문 ID
  public static UUID DEFAULT_DEPARTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");           // 출발 허브 ID
  public static UUID DEFAULT_ARRIVAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");             // 도착 허브 ID
  public static UUID DEFAULT_RECEIVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");            // 수령업체 ID
  public static UUID DEFAULT_SENDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");              // 발송업체 ID
  public static Long DEFAULT_VENDOR_DRIVER_ID = 1234L;


  public static UUID DEFAULT_DELIVERY_ID_FIRST = UUID.fromString("00000000-0000-0000-0000-000000000006");      // 배송 ID
  public static UUID DEFAULT_DELIVERY_ID_SECOND  = UUID.fromString("00000000-0000-0000-0000-000000000007");    // 다른 배송 ID

  public static String DEFAULT_COMPANY_NAME = "테스트업체";
  public static String DEFAULT_COMPANY_ADDRESS = "서울특별시 강남구 테헤란로 123";
  public static String DEFAULT_HUB_ID = DEFAULT_DEPARTURE_ID.toString();                                                // 기본 허브 ID
  public static String DEFAULT_RECEIVER_SLACK_ID = "U123456";
  public static Long DEFAULT_VENDOR_DRIVER_ID_STR = DEFAULT_VENDOR_DRIVER_ID;

  public static UUID DEFAULT_SUPPLIER_ID =  DEFAULT_SENDER_ID;                                                         // 공급업체 ID
  public static UUID DEFAULT_CUSTOMER_ID = DEFAULT_RECEIVER_ID;                                                        // 수령업체 ID
  public static String DEFAULT_IDEMPOTENCY_KEY = "멱등키123";


  public static CompanyCommand createCompany() {
    return new CompanyCommand(
        DEFAULT_RECEIVER_ID.toString(),
        DEFAULT_HUB_ID,
        "CUSTOMER",
        DEFAULT_COMPANY_NAME,
        DEFAULT_COMPANY_ADDRESS
    );
  }

  public static CompanyCommand createCompany(UUID receiverId, String hubId, String name, String address) {
    return new CompanyCommand(
        receiverId.toString(),
        hubId,
        "CUSTOMER",
        name,
        address
    );
  }

  public static DriverCommand createDriver() {
    return new DriverCommand(DEFAULT_VENDOR_DRIVER_ID_STR, DEFAULT_RECEIVER_SLACK_ID);
  }

  public static DriverCommand createDriver(Long vendorDriverId, String receiverSlackId) {
    return new DriverCommand(vendorDriverId, receiverSlackId);
  }


    private static Delivery buildDelivery(
        Long vendorDriverId,
        UUID orderId,
        UUID departureId,
        UUID arrivalId,
        UUID senderId,
        UUID receiverId,
        String companyName,
        String companyAddress,
        String receiverSlackId
    ) {
        return Delivery.create(
            vendorDriverId,
            orderId,
            departureId,
            arrivalId,
            senderId,
            receiverId,
            companyName,
            companyAddress,
            receiverSlackId,
            orderItemCommandsDefault()
        );
    }
  // 아이템2개 같은 허브
  public static Delivery defaultDelivery() {
    return Delivery.create(
        DEFAULT_VENDOR_DRIVER_ID,
        DEFAULT_ORDER_ID,
        DEFAULT_DEPARTURE_ID,
        DEFAULT_ARRIVAL_ID,
        DEFAULT_SENDER_ID,
        DEFAULT_RECEIVER_ID,
        DEFAULT_COMPANY_NAME,
        DEFAULT_COMPANY_ADDRESS,
        DEFAULT_RECEIVER_SLACK_ID,
        orderItemCommandsDefault()
    );
  }

    public static Delivery multiHubDelivery() {
        return Delivery.create(
            DEFAULT_VENDOR_DRIVER_ID,
            DEFAULT_ORDER_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_SENDER_ID,
            DEFAULT_RECEIVER_ID,
            DEFAULT_COMPANY_NAME,
            DEFAULT_COMPANY_ADDRESS,
            DEFAULT_RECEIVER_SLACK_ID,
            orderItemCommands()
        );
    }


    //  기본 배송 객체 생성 후, 리플렉션으로 deliveryId를 지정
  public static Delivery createDelivery(UUID deliveryId) {
    Delivery delivery = defaultDelivery();
    try {
      var field = Delivery.class.getDeclaredField("deliveryId");
      field.setAccessible(true);
      field.set(delivery, deliveryId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return delivery;
  }


     // 주소만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithAddress(String companyAddress) {
        return buildDelivery(
            DEFAULT_VENDOR_DRIVER_ID,
            DEFAULT_ORDER_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_SENDER_ID,
            DEFAULT_RECEIVER_ID,
            DEFAULT_COMPANY_NAME,
            companyAddress,
            DEFAULT_RECEIVER_SLACK_ID
        );
    }


    // 슬랙 ID만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithSlackId(String slackId) {
        return buildDelivery(
            DEFAULT_VENDOR_DRIVER_ID,
            DEFAULT_ORDER_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_SENDER_ID,
            DEFAULT_RECEIVER_ID,
            DEFAULT_COMPANY_NAME,
            DEFAULT_COMPANY_ADDRESS,
            slackId
        );
    }

    // 배송자 ID만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithVendorDriverId(Long vendorDriverId) {
        return buildDelivery(
            vendorDriverId,
            DEFAULT_ORDER_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_SENDER_ID,
            DEFAULT_RECEIVER_ID,
            DEFAULT_COMPANY_NAME,
            DEFAULT_COMPANY_ADDRESS,
            DEFAULT_RECEIVER_SLACK_ID
        );
    }

    // 수령업체 이름만 다르게 (유효성 테스트용)
    public static Delivery createDeliveryWithReceiverName(String receiverName) {
        return buildDelivery(
            DEFAULT_VENDOR_DRIVER_ID,
            DEFAULT_ORDER_ID,
            DEFAULT_DEPARTURE_ID,
            DEFAULT_ARRIVAL_ID,
            DEFAULT_SENDER_ID,
            DEFAULT_RECEIVER_ID,
            receiverName,
            DEFAULT_COMPANY_ADDRESS,
            DEFAULT_RECEIVER_SLACK_ID
        );
    }

    public static DeliveryCreateRequest createDeliveryRequest(List<OrderItem> orderItems) {
        return new DeliveryCreateRequest(
            DEFAULT_ORDER_ID.toString(),
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_SUPPLIER_ID.toString(),
            DEFAULT_CUSTOMER_ID.toString(),
            orderItems
        );
    }


}
