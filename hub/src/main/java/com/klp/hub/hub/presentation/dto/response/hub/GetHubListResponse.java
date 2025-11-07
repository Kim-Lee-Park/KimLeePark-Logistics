package com.klp.hub.hub.presentation.dto.response.hub;

import com.klp.hub.hub.domain.model.Hub;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record GetHubListResponse(
    List<HubSummaryResponse> hubs,
    PageableDto pageable
) {
    public record HubSummaryResponse(
        UUID hubId,
        String name,
        Long latitude,
        Long longitude,
        String address
    ){}

    public record PageableDto(
        Integer page,
        Integer size,
        Integer totalElements,
        Integer totalPages,
        Boolean hasNext,
        Boolean hasPrevious,
        Boolean isFirst,
        Boolean isLast
    ) {

    }

    public static GetHubListResponse from(Page<Hub> page) {
        List<HubSummaryResponse> hubs = page.getContent().stream()
            .map(hub -> new HubSummaryResponse(
                hub.getHubId(),
                hub.getName(),
                hub.getLatitude(),
                hub.getLongitude(),
                hub.getAddress()
            ))
            .toList();

        PageableDto pageableDto = new PageableDto(
            page.getNumber(),
            page.getSize(),
            (int) page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious(),
            page.isFirst(),
            page.isLast()
        );
        return new GetHubListResponse(hubs, pageableDto);
    }
}
