package com.klp.authservice.auth.application.client;

import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataDTO;

public interface UserClient {

    /**
     * 유저 도메인을 통해 닉네임 중복 여부를 확인
     */
    boolean checkUserNameAvailable(String userName);

    /**
     * 회원가입 이후 유저 생성 요청
     */
    void createUser(UserCreateRequest request);

    /**
     * userName으로 유저 정보를 가져오는 요청
     */
    UserDataDTO getUserByUserName(String userName);
}
