package com.klp.hub.hub.application.service;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse.HubSummaryResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubService {
    private final HubRepository hubRepository;

    //허브 등록
    @Transactional
    public RegisterHubResponse registerHub(RegisterHubCommand request){

        if(hubRepository.existsByName(request.name())){
            throw new RuntimeException("hub name duplicated");
        }
        if(hubRepository.existsByAddress(request.address())){
            throw new RuntimeException("hub address duplicated");
        } // TODO: BusinessException으로 변경하기

        Hub hub=Hub.create(request);
        hubRepository.save(hub);

        return new RegisterHubResponse(hub.getHubId());
    }

    //허브 단일 조회
    @Cacheable(cacheNames = "hub",key = "#hubId")
    @Transactional(readOnly = true)
    public GetHubDetailResponse getHubDetail(UUID hubId){
        Hub hub= hubRepository.getHubById(hubId)
            .orElseThrow(()->new RuntimeException("hub not found"));
        return GetHubDetailResponse.from(hub);
    }

    //허브 목록 조회
    @Transactional(readOnly = true)
    public GetHubListResponse getHubs(Pageable pageable){
        Page<Hub> page = hubRepository.getHubs(pageable);

        return GetHubListResponse.from(page);
    }

    @Transactional
    public UpdatedHubResponse updateHub(UUID hubId, UpdateHubCommand request){
        return null;
    }

    @Transactional
    public void deleteHub(UUID hubId){

    }

}
