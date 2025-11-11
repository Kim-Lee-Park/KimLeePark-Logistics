package com.klp.delivery.delivery.fixture;

import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest.OrderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderItemFixture {


    public static UUID DEFAULT_HUB_ID_UUID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    public static UUID DEFAULT_ORDER_ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");
    public static UUID DEFAULT_PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000008");



    // 단일 아이템 기본 생성
    public static OrderItemCommand OrderItemCommand() {
        return new OrderItemCommand(DEFAULT_ORDER_ITEM_ID, DEFAULT_HUB_ID_UUID);
    }


    // 파라미터 기반 아이템 생성
    public static OrderItemCommand OrderItemCommand(UUID orderItemId, UUID hubId) {
        return new OrderItemCommand(orderItemId, hubId);
    }


    // 단일 아이템 리스트 (기본)
    public static List<OrderItemCommand> OrderItemCommandList() {
        List<OrderItemCommand> orderItems = new ArrayList<>();
        orderItems.add(OrderItemCommand());
        return orderItems;
    }


    // deliveryId 포함된 아이템 리스트 (저장 후 검증용)
    public static List<OrderItemCommand> OrderItemCommandListWithDeliveryId() {
        List<OrderItemCommand> orderItems = new ArrayList<>();
        orderItems.add(OrderItemCommand(DEFAULT_ORDER_ITEM_ID, DEFAULT_HUB_ID_UUID));
        return orderItems;
    }

    public static OrderItem createOrderItem() {
        return new OrderItem(
            DEFAULT_ORDER_ITEM_ID.toString(),
            DEFAULT_HUB_ID_UUID.toString()
        );
    }


    // 파라미터 기반 OrderItem 생성
    public static OrderItem createOrderItem(UUID orderItemId, UUID hubId) {
        return new OrderItem(orderItemId.toString(), hubId.toString());
    }


    // 단일 OrderItem 리스트 (기본)
    public static List<OrderItem> createOrderItemList() {
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem());
        return items;
    }


    // deliveryId 포함된 OrderItem 리스트 (저장 후 검증용)
    public static List<OrderItem> createOrderItemListWithDeliveryId() {
        List<OrderItem> items = new ArrayList<>();
        items.add(
            createOrderItem(DEFAULT_ORDER_ITEM_ID, DEFAULT_HUB_ID_UUID)
        );
        return items;
    }

}
