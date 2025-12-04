package com.klp.hub.hub.application.service;

import com.klp.hub.common.model.UserDetailsImpl;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.klp.hub.hub.exception.HubErrorCode;
import com.klp.hub.hub.presentation.dto.response.GetHubByNameResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubService {

    private final HubRepository hubRepository;

    private static final String CACHE_NAME = "hub";

    //허브 등록
    @Transactional
    public RegisterHubResponse registerHub(RegisterHubCommand request, UserDetails userDetails) {

        if (hubRepository.existsByName(request.name())) {
            throw new BusinessException(HubErrorCode.HUB_NAME_DUPLICATED);
        }
        if (hubRepository.existsByAddress(request.address())) {
            throw new BusinessException(HubErrorCode.HUB_ADDRESS_DUPLICATED);
        }

        Hub hub = Hub.create(request.name(), request.latitude(), request.longitude(),
            request.address());
        hubRepository.save(hub);

        return new RegisterHubResponse(hub.getHubId());
    }

    //허브 단일 조회
    @Cacheable(cacheNames = CACHE_NAME, key = "#hubId")
    @Transactional(readOnly = true)
    public GetHubDetailResponse getHubDetail(UUID hubId) {
        Hub hub = getHubById(hubId);
        return GetHubDetailResponse.from(hub);
    }

    //허브 목록 조회
    @Transactional(readOnly = true)
    public GetHubListResponse getHubs(Pageable pageable) {
        Page<Hub> page = hubRepository.getHubs(pageable);

        return GetHubListResponse.from(page);
    }

    //허브 수정
    @CacheEvict(cacheNames = CACHE_NAME, key = "#hubId")
    @Transactional
    public UpdatedHubResponse updateHub(UUID hubId, UpdateHubCommand request,
        UserDetails userDetails) {
        Hub hub = getHubById(hubId);

        if (hubRepository.existsByName(request.name())) {
            throw new BusinessException(HubErrorCode.HUB_NAME_DUPLICATED);
        }
        if (hubRepository.existsByAddress(request.address())) {
            throw new BusinessException(HubErrorCode.HUB_ADDRESS_DUPLICATED);
        }

        hub.update(request.name(), request.latitude(), request.longitude(), request.address());

        return UpdatedHubResponse.from(hub);
    }

    //허브 삭제
    @Transactional(propagation = Propagation.MANDATORY)
    @CacheEvict(cacheNames = CACHE_NAME, key = "#hubId")
    public void markPendingDelete(UUID hubId, UserDetailsImpl userDetails) {
        Hub hub = getHubById(hubId);
        hub.pendingDelete(userDetails.getUserId());
    }

    //허브 ID로 조회
    @Cacheable(cacheNames = CACHE_NAME, key = "#hubId")
    @Transactional(readOnly = true)
    public Hub getHubById(UUID hubId) {
        return hubRepository.getHubById(hubId)
            .orElseThrow(() -> new BusinessException(HubErrorCode.NOT_EXISTS));
    }

    @Transactional(readOnly = true)
    public List<Hub> getHubByIds(List<UUID> hubIds) {
        return hubRepository.getHubsByIds(hubIds);
    }

    //허브 이름으로 조회
    @Transactional(readOnly = true)
    public GetHubByNameResponse getHubByName(String hubName) {
        Hub hub = hubRepository.findByName(hubName)
            .orElseThrow(() -> new BusinessException(HubErrorCode.NOT_EXISTS));
        return GetHubByNameResponse.from(hub);
    }
}
