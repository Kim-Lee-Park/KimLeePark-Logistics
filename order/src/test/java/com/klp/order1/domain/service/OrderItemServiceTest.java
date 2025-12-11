//package com.klp.order.domain.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//import com.klp.common.exception.BusinessException;
//import com.klp.order.application.command.OrderItemCommand;
//import com.klp.order.application.service.OrderItemService;
//import com.klp.order.domain.entity.order.Order;
//import com.klp.order.domain.entity.orderitem.OrderItem;
//import com.klp.order.domain.repository.OrderItemRepository;
//import com.klp.order.domain.repository.OrderRepository;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("OrderItemService 테스트")
//class OrderItemServiceTest {
//
//    @Mock
//    private OrderItemRepository orderItemRepository;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @InjectMocks
//    private OrderItemService orderItemService;
//
//    private Order order;
//    private OrderItem orderItem1;
//    private OrderItem orderItem2;
//    private UUID orderItemId;
//    private UUID orderId;
//
//    @BeforeEach
//    void setUp() {
//        orderId = UUID.randomUUID();
//        List<OrderItemCommand> itemCommands = List.of(
//            new OrderItemCommand(UUID.randomUUID(), "상품명", UUID.randomUUID(), 10)
//        );
//        order = Order.create(1L, 2L, "테스트 주문", itemCommands);
//
//        orderItemId = UUID.randomUUID();
//        orderItem1 = OrderItem.of(order,
//            new OrderItemCommand(UUID.randomUUID(), "상품명", UUID.randomUUID(), 10));
//        orderItem2 = OrderItem.of(order,
//            new OrderItemCommand(UUID.randomUUID(), "상품명", UUID.randomUUID(), 5));
//    }
//
//    @Test
//    @DisplayName("주문 아이템 조회 - ID로 조회 성공")
//    void findById_Success() {
//        // given
//        given(orderItemRepository.findById(orderItemId))
//            .willReturn(Optional.of(orderItem1));
//
//        // when
//        OrderItem result = orderItemService.findById(orderItemId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result).isEqualTo(orderItem1);
//    }
//
//    @Test
//    @DisplayName("주문 아이템 조회 - 존재하지 않는 ID")
//    void findById_NotFound() {
//        // given
//        UUID nonExistentId = UUID.randomUUID();
//        given(orderItemRepository.findById(nonExistentId))
//            .willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> orderItemService.findById(nonExistentId))
//            .isInstanceOf(BusinessException.class)
//            .hasMessage("주문 아이템을 찾을 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("주문 ID로 주문 아이템 조회")
//    void findByOrderId_Success() {
//        // given
//        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);
//        given(orderItemRepository.findByOrder_OrderId(orderId))
//            .willReturn(orderItems);
//
//        // when
//        List<OrderItem> result = orderItemService.findByOrderId(orderId);
//
//        // then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getQuantity()).isEqualTo(10);
//        assertThat(result.get(1).getQuantity()).isEqualTo(5);
//    }
//
//    @Test
//    @DisplayName("배송 ID가 없는 주문 아이템 조회 - 배송 할당 대기 중")
//    void findUnassignedDeliveryItems_Success() {
//        // given
//        List<OrderItem> unassignedItems = List.of(orderItem1, orderItem2);
//        given(orderItemRepository.findByDeliveryIdIsNull())
//            .willReturn(unassignedItems);
//
//        // when
//        List<OrderItem> result = orderItemService.findUnassignedDeliveryItems();
//
//        // then
//        assertThat(result).hasSize(2);
//
//        assertThat(result.get(0).getQuantity()).isEqualTo(10);
//        assertThat(result.get(0).getDeliveryId()).isNull();
//
//        assertThat(result.get(1).getQuantity()).isEqualTo(5);
//        assertThat(result.get(1).getDeliveryId()).isNull();
//    }
//
//    @Test
//    @DisplayName("삭제되지 않은 주문 아이템만 조회")
//    void findNotDeletedItems_Success() {
//        // given
//        List<OrderItem> activeItems = List.of(orderItem1);
//        given(orderItemRepository.findByDeletedAtIsNull())
//            .willReturn(activeItems);
//
//        // when
//        List<OrderItem> result = orderItemService.findNotDeletedItems();
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0)).isEqualTo(orderItem1);
//        assertThat(result.get(0).getDeletedAt()).isNull();
//    }
//
//    @Test
//    @DisplayName("주문 ID로 삭제되지 않은 아이템만 조회")
//    void findNotDeletedItemsByOrderId_Success() {
//        // given
//        List<OrderItem> activeItems = List.of(orderItem1, orderItem2);
//        given(orderItemRepository.findByOrder_OrderIdAndDeletedAtIsNull(orderId))
//            .willReturn(activeItems);
//
//        // when
//        List<OrderItem> result = orderItemService.findNotDeletedItemsByOrderId(orderId);
//
//        // then
//        assertThat(result).hasSize(2);
//
//        assertThat(result.get(0)).isEqualTo(orderItem1);
//        assertThat(result.get(0).getDeletedAt()).isNull();
//
//        assertThat(result.get(1)).isEqualTo(orderItem2);
//        assertThat(result.get(1).getDeletedAt()).isNull();
//    }
//
//    @Test
//    @DisplayName("주문 아이템 수량 수정 - 정상")
//    void updateQuantity_Success() {
//        // given
//        int newQuantity = 20;
//        given(orderItemRepository.findById(orderItemId))
//            .willReturn(Optional.of(orderItem1));
//        given(orderItemRepository.save(any(OrderItem.class)))
//            .willReturn(orderItem1);
//
//        // when
//        OrderItem result = orderItemService.updateQuantity(orderItemId, newQuantity);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getQuantity()).isEqualTo(newQuantity);
//    }
//
//    @Test
//    @DisplayName("주문 아이템 수량 수정 - 0 이하 수량으로 수정 시도")
//    void updateQuantity_Fail_InvalidQuantity() {
//        // given
//        int invalidQuantity = 0;
//        given(orderItemRepository.findById(orderItemId))
//            .willReturn(Optional.of(orderItem1));
//
//        // when & then
//        assertThatThrownBy(() -> orderItemService.updateQuantity(orderItemId, invalidQuantity))
//            .isInstanceOf(BusinessException.class)
//            .hasMessage("주문 수량은 1개 이상이어야 합니다.");
//
//    }
//
//    @Test
//    @DisplayName("배송 ID 할당 - 정상")
//    void assignDeliveryId_Success() {
//        // given
//        UUID deliveryId = UUID.randomUUID();
//        given(orderItemRepository.findById(orderItemId))
//            .willReturn(Optional.of(orderItem1));
//        given(orderItemRepository.save(any(OrderItem.class)))
//            .willReturn(orderItem1);
//
//        // when
//        OrderItem result = orderItemService.assignDeliveryId(orderItemId, deliveryId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getDeliveryId()).isEqualTo(deliveryId);
//    }
//
//    @Test
//    @DisplayName("배송 ID 할당 - 존재하지 않는 주문 아이템")
//    void assignDeliveryId_Fail_ItemNotFound() {
//        // given
//        UUID nonExistentId = UUID.randomUUID();
//        UUID deliveryId = UUID.randomUUID();
//        given(orderItemRepository.findById(nonExistentId))
//            .willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> orderItemService.assignDeliveryId(nonExistentId, deliveryId))
//            .isInstanceOf(BusinessException.class)
//            .hasMessage("주문 아이템을 찾을 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("주문 아이템 소프트 삭제 - 정상")
//    void deleteOrderItem_Success() {
//        // given
//        Long deletedBy = 100L;
//        given(orderItemRepository.findById(orderItemId))
//            .willReturn(Optional.of(orderItem1));
//        given(orderItemRepository.save(any(OrderItem.class)))
//            .willReturn(orderItem1);
//
//        // when
//        orderItemService.deleteOrderItem(orderItemId, deletedBy);
//
//        // then
//        assertThat(orderItem1.getDeletedBy()).isEqualTo(deletedBy);
//    }
//
//    @Test
//    @DisplayName("주문 아이템 소프트 삭제 - 존재하지 않는 아이템")
//    void deleteOrderItem_Fail_ItemNotFound() {
//        // given
//        UUID nonExistentId = UUID.randomUUID();
//        Long deletedBy = 100L;
//        given(orderItemRepository.findById(nonExistentId))
//            .willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> orderItemService.deleteOrderItem(nonExistentId, deletedBy))
//            .isInstanceOf(BusinessException.class)
//            .hasMessage("주문 아이템을 찾을 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("전체 주문 아이템 조회")
//    void findAll_Success() {
//        // given
//        List<OrderItem> allItems = List.of(orderItem1, orderItem2);
//        given(orderItemRepository.findAll())
//            .willReturn(allItems);
//
//        // when
//        List<OrderItem> result = orderItemService.findAll();
//
//        // then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0)).isEqualTo(orderItem1);
//        assertThat(result.get(1)).isEqualTo(orderItem2);
//    }
//
//    @Test
//    @DisplayName("주문 아이템 배치 배송 ID 할당 - 여러 아이템에 한 번에 할당")
//    void assignDeliveryIdBatch_Success() {
//        // given
//        UUID deliveryId = UUID.randomUUID();
//        List<UUID> orderItemIds = List.of(orderItemId, UUID.randomUUID());
//        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);
//
//        given(orderItemRepository.findById(orderItemIds.get(0)))
//            .willReturn(Optional.of(orderItem1));
//        given(orderItemRepository.findById(orderItemIds.get(1)))
//            .willReturn(Optional.of(orderItem2));
//        given(orderItemRepository.save(any(OrderItem.class)))
//            .willReturn(orderItem1, orderItem2);
//
//        // when
//        List<OrderItem> result = orderItemService.assignDeliveryIdBatch(orderItemIds, deliveryId);
//
//        // then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getDeliveryId()).isNotNull();
//        assertThat(result.get(1).getDeliveryId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("주문 아이템 배치 배송 ID 할당 - 빈 리스트")
//    void assignDeliveryIdBatch_EmptyList() {
//        // given
//        UUID deliveryId = UUID.randomUUID();
//        List<UUID> emptyList = List.of();
//
//        // when & then
//        assertThatThrownBy(() -> orderItemService.assignDeliveryIdBatch(emptyList, deliveryId))
//            .isInstanceOf(BusinessException.class)
//            .hasMessage("주문 아이템이 존재하지 않습니다.");
//    }
//}