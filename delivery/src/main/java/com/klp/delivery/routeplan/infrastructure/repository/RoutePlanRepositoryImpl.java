package com.klp.delivery.routeplan.infrastructure.repository;

import com.klp.delivery.routeplan.domain.model.QRoutePlan;
import com.klp.delivery.routeplan.domain.model.RoutePlan;
import com.klp.delivery.routeplan.domain.repository.RoutePlanRepository;
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
public class RoutePlanRepositoryImpl implements RoutePlanRepository {

    private final RoutePlanJpaRepository routePlanJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public RoutePlan save(RoutePlan routePlan) {
        return routePlanJpaRepository.save(routePlan);
    }

    @Override
    public void softDeleteByDepartureAndArrival(UUID departureId, UUID arrivalId) {
        routePlanJpaRepository.softDeleteByDepartureAndArrival(departureId, arrivalId);
    }

    @Override
    public Optional<RoutePlan> findByDepartureIdAndArrivalIdAndDeletedAtIsNull(UUID departureId,
        UUID arrivalId) {
        return routePlanJpaRepository.findByDepartureIdAndArrivalIdAndDeletedAtIsNull(departureId,
            arrivalId);
    }

    @Override
    public boolean existsByDepartureIdAndArrivalId(UUID departureId, UUID arrivalId) {
        return routePlanJpaRepository.existsByDepartureIdAndArrivalId(departureId, arrivalId);
    }

    @Override
    public Optional<RoutePlan> getRoutePlanById(UUID routePlanId) {
        return routePlanJpaRepository.findByRoutePlanIdAndDeletedAtIsNull(routePlanId);
    }

    @Override
    public Optional<RoutePlan> findByRoutePlanIdAndDeletedAtIsNull(UUID routePlanId) {
        return routePlanJpaRepository.findByRoutePlanIdAndDeletedAtIsNull(routePlanId);
    }

    @Override
    public Page<RoutePlan> findAll(UUID depId, UUID arrId, Pageable pageable) {
        QRoutePlan qRoutePlan = QRoutePlan.routePlan;

        JPQLQuery<RoutePlan> query = queryFactory.selectFrom(qRoutePlan)
            .where(
                depId != null ? qRoutePlan.departureId.eq(depId) : null,
                arrId != null ? qRoutePlan.arrivalId.eq(arrId) : null,
                qRoutePlan.deletedAt.isNull()
            );

        if (pageable.getSort().isSorted()) {
            PathBuilder<QRoutePlan> entityPath = new PathBuilder<>(QRoutePlan.class,
                qRoutePlan.getMetadata());
            for (Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                query.orderBy(new OrderSpecifier<>(direction, entityPath.getComparable(property,
                    Comparable.class)));
            }
        } else {
            query.orderBy(qRoutePlan.createdAt.desc());
        }

        long total = query.fetchCount();
        List<RoutePlan> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
