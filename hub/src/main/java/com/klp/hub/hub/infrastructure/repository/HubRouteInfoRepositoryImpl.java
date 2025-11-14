package com.klp.hub.hub.infrastructure.repository;

import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.domain.model.QHubRouteInfo;
import com.klp.hub.hub.domain.repository.HubRouteInfoRepository;
import com.klp.hub.hub.infrastructure.dto.RoutePairDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubRouteInfoRepositoryImpl implements HubRouteInfoRepository {
    private final HubRouteInfoJpaRepository hubRouteInfoJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public HubRouteInfo save(HubRouteInfo hubRouteInfo) {
        return hubRouteInfoJpaRepository.save(hubRouteInfo);
    }

    @Override
    public Optional<HubRouteInfo> getHubRouteInfoById(UUID hubRouteInfoId) {
        return hubRouteInfoJpaRepository.findById(hubRouteInfoId);
    }

    @Override
    public Page<HubRouteInfo> getHubRoutes(UUID depId, UUID arrId, Pageable pageable) {
        QHubRouteInfo qRouteInfo = QHubRouteInfo.hubRouteInfo;

        JPQLQuery<HubRouteInfo> base = queryFactory.selectFrom(qRouteInfo)
            .where(
                depId != null ? qRouteInfo.departureId.eq(depId) : null,
                arrId != null ? qRouteInfo.arrivalId.eq(arrId) : null,
                qRouteInfo.deletedAt.isNull()
            );

        if (pageable.getSort().isSorted()) {
            PathBuilder<QHubRouteInfo> entityPath = new PathBuilder<>(QHubRouteInfo.class, qRouteInfo.getMetadata());
            for (Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                base.orderBy(new OrderSpecifier<>(direction, entityPath.getComparable(property, Comparable.class)));
            }
        } else {
            base.orderBy(qRouteInfo.createdAt.desc());
        }

        long total = base.fetchCount();
        List<HubRouteInfo> content = base
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<RoutePairDto> findExistingParisIn(List<UUID> hubIds) {
        QHubRouteInfo qRouteInfo = QHubRouteInfo.hubRouteInfo;

        return queryFactory
            .select(Projections.constructor(RoutePairDto.class, qRouteInfo.departureId, qRouteInfo.arrivalId))
            .from(qRouteInfo)
            .where(
                qRouteInfo.deletedAt.isNull(),
                qRouteInfo.departureId.in(hubIds),
                qRouteInfo.arrivalId.in(hubIds),
                qRouteInfo.departureId.ne(qRouteInfo.arrivalId)
            )
            .fetch();
    }

    @Override
    public boolean existsByDepartureIdAndArrivalId(UUID departureId, UUID arrivalId) {
        return hubRouteInfoJpaRepository.existsByDepartureIdAndArrivalId(departureId, arrivalId);
    }

    @Override
    public List<HubRouteInfo> getAllHubRouteInfos() {
        return hubRouteInfoJpaRepository.findByDeletedAtIsNull();
    }
}
