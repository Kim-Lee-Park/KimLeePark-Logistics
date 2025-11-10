package com.klp.delivery.delivery.fixture;

import com.klp.delivery.common.DeliveryStatus;
import com.klp.delivery.delivery.domain.Company;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.domain.Driver;
import com.klp.delivery.delivery.presentation.dto.OrderItemDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeliveryFixture {

  public static UUID DEFAULT_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static UUID DEFAULT_DEPARTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  public static UUID DEFAULT_RECEIVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  public static UUID DEFAULT_VENDOR_DRIVER_ID = UUID.fromString(
      "00000000-0000-0000-0000-000000000004");
  public static UUID DEFAULT_ARRIVAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
  public static UUID DEFAULT_DELIVERY_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
  public static UUID DEFAULT_HUB_ID_UUID = UUID.fromString("00000000-0000-0000-0000-000000000007");
  public static UUID DEFAULT_ORDER_ITEM_ID = UUID.fromString(
      "00000000-0000-0000-0000-000000000008");
  public static UUID DEFAULT_PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");

  public static String DEFAULT_COMPANY_NAME = "테스트업체";
  public static String DEFAULT_COMPANY_ADDRESS = "서울특별시 강남구 테헤란로 123";
  public static String DEFAULT_HUB_ID = "00000000-0000-0000-0000-000000000007";
  public static String DEFAULT_RECEIVER_SLACK_ID = "U123456";
  public static String DEFAULT_VENDOR_DRIVER_ID_STR = DEFAULT_VENDOR_DRIVER_ID.toString();

  public static Long DEFAULT_SUPPLIER_ID = 1L;
  public static Long DEFAULT_CUSTOMER_ID = 2L;
  public static String DEFAULT_IDEMPOTENCY_KEY = "멱등키123";
  public static Integer DEFAULT_QUANTITY = 10;

  public static Company createCompany() {
    return new Company(
        DEFAULT_RECEIVER_ID.toString(),
        DEFAULT_HUB_ID,
        "CUSTOMER",
        DEFAULT_COMPANY_NAME,
        DEFAULT_COMPANY_ADDRESS
    );
  }

  public static Company createCompany(UUID receiverId, String hubId, String name, String address) {
    return new Company(
        receiverId.toString(),
        hubId,
        "CUSTOMER",
        name,
        address
    );
  }

  public static Driver createDriver() {
    return new Driver(DEFAULT_VENDOR_DRIVER_ID_STR, DEFAULT_RECEIVER_SLACK_ID);
  }

  public static Driver createDriver(String vendorDriverId, String receiverSlackId) {
    return new Driver(vendorDriverId, receiverSlackId);
  }

  public static Delivery createDelivery() {
    return Delivery.create(
        DEFAULT_VENDOR_DRIVER_ID,
        DEFAULT_ORDER_ID,
        DEFAULT_DEPARTURE_ID,
        DEFAULT_ARRIVAL_ID,
        DEFAULT_RECEIVER_ID,
        DEFAULT_COMPANY_NAME,
        DEFAULT_COMPANY_ADDRESS,
        DEFAULT_RECEIVER_SLACK_ID
    );
  }

  public static Delivery createDelivery(UUID deliveryId) {
    Delivery delivery = createDelivery();
    try {
      var field = Delivery.class.getDeclaredField("deliveryId");
      field.setAccessible(true);
      field.set(delivery, deliveryId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return delivery;
  }

  public static Delivery createDeliveryWithStatus(DeliveryStatus status) {
    Delivery delivery = createDelivery();
    delivery.updateStatus(status);
    return delivery;
  }

  public static OrderItemDto createOrderItemDto() {
    return new OrderItemDto(
        DEFAULT_ORDER_ITEM_ID,
        DEFAULT_HUB_ID_UUID,
        DEFAULT_PRODUCT_ID,
        null, // deliveryId는 null (새로 생성)
        DEFAULT_QUANTITY
    );
  }

  public static OrderItemDto createOrderItemDto(UUID orderItemId, UUID hubId, UUID productId,
      UUID deliveryId, Integer quantity) {
    return new OrderItemDto(
        orderItemId,
        hubId,
        productId,
        deliveryId,
        quantity
    );
  }

  public static List<OrderItemDto> createOrderItemDtoList() {
    List<OrderItemDto> orderItems = new ArrayList<>();
    orderItems.add(createOrderItemDto());
    return orderItems;
  }

  public static List<OrderItemDto> createOrderItemDtoListWithDeliveryId() {
    List<OrderItemDto> orderItems = new ArrayList<>();
    orderItems.add(
        createOrderItemDto(DEFAULT_ORDER_ITEM_ID, DEFAULT_HUB_ID_UUID, DEFAULT_PRODUCT_ID,
            DEFAULT_DELIVERY_ID, DEFAULT_QUANTITY));
    return orderItems;
  }
}
