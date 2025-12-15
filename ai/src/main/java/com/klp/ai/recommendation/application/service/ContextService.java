package com.klp.ai.recommendation.application.service;

import com.klp.ai.recommendation.application.dto.HubInfo;
import com.klp.ai.recommendation.application.dto.OrderedProduct;
import com.klp.ai.recommendation.application.dto.ProductCandidate;
import com.klp.ai.recommendation.application.dto.RecommendationContext;
import com.klp.ai.recommendation.application.dto.ReviewStats;
import com.klp.ai.recommendation.application.dto.TimeSlot;
import com.klp.ai.recommendation.application.dto.WeatherInfo;
import com.klp.ai.recommendation.infrastructure.client.feign.HubClient;
import com.klp.ai.recommendation.infrastructure.client.feign.OrderClient;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.HubResponse;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.InventoryResponse;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.ReviewListResponse;
import com.klp.ai.recommendation.infrastructure.client.rest.WeatherClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextService {

    private final HubClient hubClient;
    private final OrderClient orderClient;
    private final WeatherClient weatherClient;

    public HubInfo getHubInfo(UUID hubId) {
        try {
            HubResponse response = hubClient.getHub(hubId);
            return new HubInfo(
                response.hubId(),
                response.name(),
                response.latitude(),
                response.longitude(),
                response.address()
            );
        } catch (Exception e) {
            log.error("허브 정보 조회 실패: hubId={}, error={}", hubId, e.getMessage());
            return null;
        }
    }

    public int getInventory(UUID productId) {
        try {
            InventoryResponse response = hubClient.getInventory(productId);
            return response.quantity() != null ? response.quantity() : 0;
        } catch (Exception e) {
            log.warn("재고 조회 실패: productId={}, error={}", productId, e.getMessage());
            return 0;
        }
    }

    public WeatherInfo getWeather(double latitude, double longitude) {
        return weatherClient.getCurrentWeather(latitude, longitude);
    }

    public TimeSlot getCurrentTimeSlot() {
        return TimeSlot.now();
    }

    public ReviewStats getReviewStats(UUID productId) {
        try {
            ReviewListResponse response = orderClient.getReviews(productId);
            return new ReviewStats(
                productId,
                response.getAverageRating(),
                response.getReviewCount()
            );
        } catch (Exception e) {
            log.warn("리뷰 조회 실패: productId={}, error={}", productId, e.getMessage());
            return ReviewStats.empty(productId);
        }
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.00877;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(earthRadiusKm * c * 100) / 100.0;
    }

    public RecommendationContext buildContext(
        Long userId,
        UUID userHubId,
        List<OrderedProduct> orderedProducts
    ) {
        HubInfo userHub = getHubInfo(userHubId);

        WeatherInfo weather = null;
        if (userHub != null && userHub.latitude() != null && userHub.longitude() != null) {
            weather = getWeather(userHub.latitude(), userHub.longitude());
        }

        TimeSlot timeSlot = getCurrentTimeSlot();

        return new RecommendationContext(
            userId,
            userHub,
            orderedProducts,
            LocalDateTime.now(),
            weather,
            timeSlot
        );
    }

    public ProductCandidate enrichCandidate(
        UUID productId,
        String productName,
        UUID hubId,
        String hubName,
        double similarityScore,
        HubInfo userHub
    ) {
        int inventory = getInventory(productId);
        ReviewStats reviewStats = getReviewStats(productId);

        double distance = 0.0;
        if (userHub != null && userHub.latitude() != null && userHub.longitude() != null) {
            HubInfo productHub = getHubInfo(hubId);
            if (productHub != null && productHub.latitude() != null && productHub.longitude() != null) {
                distance = calculateDistance(
                    userHub.latitude(), userHub.longitude(),
                    productHub.latitude(), productHub.longitude()
                );
            }
        }

        return new ProductCandidate(
            productId,
            productName,
            hubId,
            hubName,
            distance,
            inventory,
            similarityScore,
            reviewStats.averageRating(),
            reviewStats.reviewCount()
        );
    }
}
