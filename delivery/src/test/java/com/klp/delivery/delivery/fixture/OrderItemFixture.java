//package com.klp.delivery.delivery.fixture;
//
//import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
//import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest.OrderItem;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class OrderItemFixture {
//
//
//    public static UUID DEFAULT_HUB_ID_UUID_FIRST = DeliveryFixture.DEFAULT_DEPARTURE_ID;                                    // 출발 허브 ID
//    public static UUID DEFAULT_HUB_ID_UUID_SECOND = UUID.fromString(
//        "00000000-0000-0000-0000-000000000007");          // 다른 출발 허브 ID
//
//    public static UUID ORDER_ITEM_ID_FIRST = UUID.fromString(
//        "00000000-0000-0000-0000-000000000008");
//    public static UUID ORDER_ITEM_ID_SECOND = UUID.fromString(
//        "00000000-0000-0000-0000-000000000009");
//    public static UUID ORDER_ITEM_ID_THIRD = UUID.fromString(
//        "00000000-0000-0000-0000-000000000010");
//
//    public static final String ORDER_PRODUCT_NAME = "test-product";
//    public static final int ORDER_QUANTITY = 1;
//
//    // 단일 아이템 기본 생성
//    public static OrderItemCommand OrderItemCommand() {
//        return new OrderItemCommand(ORDER_ITEM_ID_SECOND, DEFAULT_HUB_ID_UUID_FIRST,
//            ORDER_PRODUCT_NAME, UUID.randomUUID(), ORDER_QUANTITY, ORDER_QUANTITY, ORDER_QUANTITY);
//    }
//
//    public static List<OrderItemCommand> orderItemCommandsDefault() {
//        return List.of(
//            OrderItemCommand(ORDER_ITEM_ID_FIRST, DEFAULT_HUB_ID_UUID_FIRST, ORDER_PRODUCT_NAME,
//                ORDER_QUANTITY),
//            OrderItemCommand(ORDER_ITEM_ID_SECOND, DEFAULT_HUB_ID_UUID_FIRST, ORDER_PRODUCT_NAME,
//                ORDER_QUANTITY));
//    }
//
//    // 파라미터 기반 아이템 생성
//    public static OrderItemCommand OrderItemCommand(UUID orderItemId, UUID hubId,
//        String productName, int quantity) {
//        return new OrderItemCommand(orderItemId, hubId, productName, UUID.randomUUID(), quantity,
//            quantity, quantity);
//    }
//
//
//    // 단일 아이템 리스트 (기본)
//    public static List<OrderItemCommand> OrderItemCommandList() {
//        List<OrderItemCommand> orderItems = new ArrayList<>();
//        orderItems.add(OrderItemCommand());
//        return orderItems;
//    }
//
//    public static OrderItem createOrderItem() {
//        return new OrderItem(
//            ORDER_ITEM_ID_FIRST.toString(),
//            DEFAULT_HUB_ID_UUID_FIRST.toString(),
//            ORDER_PRODUCT_NAME,
//            DEFAULT_HUB_ID_UUID_FIRST.toString(),
//            ORDER_QUANTITY,
//            ORDER_QUANTITY,
//            ORDER_QUANTITY
//        );
//    }
//
//
//    // 파라미터 기반 OrderItem 생성
//    public static OrderItem createOrderItem(UUID orderItemId, UUID hubId, String productName,
//        int quantity) {
//        return new OrderItem(orderItemId.toString(), hubId.toString(), productName, "hubId",
//            ORDER_QUANTITY, quantity, quantity);
//    }
//
//    public static List<OrderItem> createOrderItems() {
//        List<OrderItem> items = new ArrayList<>();
//        items.add(createOrderItem());
//        items.add(
//            createOrderItem(ORDER_ITEM_ID_SECOND, DEFAULT_HUB_ID_UUID_FIRST, ORDER_PRODUCT_NAME,
//                ORDER_QUANTITY));
//        items.add(
//            createOrderItem(ORDER_ITEM_ID_THIRD, DEFAULT_HUB_ID_UUID_SECOND, ORDER_PRODUCT_NAME,
//                ORDER_QUANTITY));
//        return items;
//    }
//
//
//    // deliveryId 포함된 OrderItem 리스트 (저장 후 검증용)
//    public static List<OrderItem> createOrderItemListWithDeliveryId() {
//        List<OrderItem> items = new ArrayList<>();
//        items.add(
//            createOrderItem(ORDER_ITEM_ID_FIRST, DEFAULT_HUB_ID_UUID_FIRST, ORDER_PRODUCT_NAME,
//                ORDER_QUANTITY)
//        );
//        return items;
//    }
//
//}
