package com.klp.hub.hub.application.service;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubService {

    @Transactional
    public RegisterHubResponse registerHub(RegisterHubCommand request){

        return new RegisterHubResponse(null);
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
