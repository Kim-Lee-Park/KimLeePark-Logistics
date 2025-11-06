package com.klp.hub.hub.application.service;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubService {
    private final HubRepository hubRepository;

    @CachePut(cacheNames = "hub", key="#result.hubId")
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

    @Transactional(readOnly = true)
    public GetHubDetailResponse getHubDetail(UUID hubId){
        return null;
    }

    @Transactional(readOnly = true)
    public GetHubDetailResponse getHubs(Pageable pageable){
        return null;
    }

    @Transactional
    public UpdatedHubResponse updateHub(UUID hubId, UpdateHubCommand request){
        return null;
    }

    @Transactional
    public void deleteHub(UUID hubId){

    }

}
