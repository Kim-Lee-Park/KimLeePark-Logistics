package com.klp.delivery.delivery.infrastructure.repository;

import static com.klp.delivery.common.enums.CustomerDeliveryStatus.CREATED;
import static com.klp.delivery.common.enums.CustomerDeliveryStatus.SHIPPING;
import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;

import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.entity.QDelivery;
import com.klp.delivery.delivery.domain.entity.QDeliveryItem;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.global.exception.BusinessException;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository deliveryJpaRepository;
    private final JPAQueryFactory queryFactory;

    QDelivery qDelivery = QDelivery.delivery;
    QDeliveryItem qItem = QDeliveryItem.deliveryItem;

    @Override
    public Delivery save(Delivery delivery) {
        return deliveryJpaRepository.save(delivery);
    }

    @Override
    public Delivery findByDeliveryId(UUID deliveryId) {
        return deliveryJpaRepository.findByDeliveryId(deliveryId)
            .orElseThrow(() -> new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
    }

    @Override
    public List<Delivery> findDeliveryByOrderId(UUID orderId) {

        return queryFactory.selectFrom(qDelivery)
            .leftJoin(qDelivery.deliveryItems, qItem).fetchJoin()
            .where(qDelivery.orderId.eq(orderId))
            .fetch();
    }

    @Override
    public Page<Delivery> findDeliveryAll(Pageable pageable) {

        long total = Optional.ofNullable(
            queryFactory.select(qDelivery.count()).from(qDelivery).fetchOne()).orElse(0L);

        JPQLQuery<Delivery> query = queryFactory.selectFrom(qDelivery);

        if (pageable.getSort().isSorted()) {

            PathBuilder<QDelivery> pathBuilder = new PathBuilder<>(QDelivery.class,
                qDelivery.getMetadata());

            for (Sort.Order order : pageable.getSort()) {

                String property = order.getProperty();

                Order direction = order.isAscending() ? ASC : DESC;

                query.orderBy(
                    new OrderSpecifier<>(
                        direction,
                        pathBuilder.getComparable(property, Comparable.class)));

            }

        }

        List<Delivery> content = query.offset(pageable.getOffset())
            .limit(pageable.getPageSize()).fetch();

        return new PageImpl<>(content, pageable, total);

    }

    @Override
    public boolean existsActiveDeliveryByRoutePlanId(UUID routePlanId) {
        // 존재 여부만 확인하므로 ID만 선택하여 효율적으로 조회
        return queryFactory
            .select(qDelivery.deliveryId)
            .from(qDelivery)
            .where(
                qDelivery.routePlanId.eq(routePlanId)
                    .and(qDelivery.status.in(
                        CREATED,
                        SHIPPING
                    ))
                    .and(qDelivery.deletedAt.isNull())
            )
            .limit(1)
            .fetchFirst() != null;
    }
}
