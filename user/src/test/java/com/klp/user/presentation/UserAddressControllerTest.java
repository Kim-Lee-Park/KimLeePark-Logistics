package com.klp.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.global.authorization.CustomWithMockUser;
import com.klp.global.exception.GlobalExceptionHandler;
import com.klp.global.security.config.SecurityConfig;
import com.klp.global.security.filter.AuthorizationFilter;
import com.klp.user.application.UserFacade;
import com.klp.user.presentation.dto.request.UserAddressCreateRequest;
import com.klp.user.presentation.dto.response.UserAddressListResponse;
import com.klp.user.presentation.dto.response.UserAddressResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAddressController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class UserAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserFacade userFacade;

    @Nested
    @DisplayName("주소 단건 조회 테스트")
    class GetUserAddressTest {

        @Test
        @DisplayName("MASTER 권한으로 다른 사용자의 주소를 조회할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "MASTER")
        void getUserAddress_asMaster_success() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();

            UserAddressResponse response = new UserAddressResponse(
                addressId,
                hubId,
                "테스트 주소",
                "101동 101호",
                true,
                37.123456,
                127.123456
            );

            when(userFacade.getUserAddress(addressId)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/users/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userAddressId").value(addressId.toString()))
                .andExpect(jsonPath("$.hubId").value(hubId.toString()))
                .andExpect(jsonPath("$.address").value("테스트 주소"))
                .andExpect(jsonPath("$.detail").value("101동 101호"))
                .andExpect(jsonPath("$.isDefault").value(true));

            verify(userFacade, times(1)).getUserAddress(addressId);
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 본인의 주소를 조회할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void getUserAddress_asCustomerOwn_success() throws Exception {
            // given
            Long userId = 1L;
            UUID addressId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();

            UserAddressResponse response = new UserAddressResponse(
                addressId,
                hubId,
                "테스트 주소",
                "101동 101호",
                true,
                37.123456,
                127.123456
            );

            when(userFacade.getUserAddress(addressId)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/users/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).getUserAddress(addressId);
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 다른 사용자의 주소를 조회하면 403 에러가 발생한다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void getUserAddress_asCustomerOther_forbidden() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/v1/users/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isForbidden());

            verify(userFacade, times(0)).getUserAddress(any());
        }
    }

    @Nested
    @DisplayName("주소 목록 조회 테스트")
    class GetUserAddressListTest {

        @Test
        @DisplayName("MASTER 권한으로 다른 사용자의 주소 목록을 조회할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "MASTER")
        void getUserAddressList_asMaster_success() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();

            UserAddressResponse addressResponse = new UserAddressResponse(
                addressId,
                hubId,
                "테스트 주소",
                "101동 101호",
                true,
                37.123456,
                127.123456
            );

            UserAddressListResponse response = new UserAddressListResponse(
                List.of(addressResponse)
            );

            when(userFacade.getUserAddressList(userId)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/users/address/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.addresses[0].address").value("테스트 주소"));

            verify(userFacade, times(1)).getUserAddressList(userId);
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 본인의 주소 목록을 조회할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void getUserAddressList_asCustomerOwn_success() throws Exception {
            // given
            Long userId = 1L;

            UserAddressListResponse response = new UserAddressListResponse(List.of());

            when(userFacade.getUserAddressList(userId)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/v1/users/address/{userId}", userId))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).getUserAddressList(userId);
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 다른 사용자의 주소 목록을 조회하면 403 에러가 발생한다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void getUserAddressList_asCustomerOther_forbidden() throws Exception {
            // given
            Long userId = 2L;

            // when & then
            mockMvc.perform(get("/v1/users/address/{userId}", userId))
                .andExpect(status().isForbidden());

            verify(userFacade, times(0)).getUserAddressList(any());
        }
    }

    @Nested
    @DisplayName("주소 생성 테스트")
    class CreateUserAddressTest {

        @Test
        @DisplayName("MASTER 권한으로 다른 사용자의 주소를 생성할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "MASTER")
        void createUserAddress_asMaster_success() throws Exception {
            // given
            Long userId = 2L;
            UUID hubId = UUID.randomUUID();

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                hubId,
                "테스트 주소",
                "101동 101호",
                true,
                37.123456,
                127.123456
            );

            doNothing().when(userFacade).createUserAddress(any());

            // when & then
            mockMvc.perform(post("/v1/users/address/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).createUserAddress(any());
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 본인의 주소를 생성할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void createUserAddress_asCustomerOwn_success() throws Exception {
            // given
            Long userId = 1L;
            UUID hubId = UUID.randomUUID();

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                hubId,
                "테스트 주소",
                "101동 101호",
                true,
                37.123456,
                127.123456
            );

            doNothing().when(userFacade).createUserAddress(any());

            // when & then
            mockMvc.perform(post("/v1/users/address/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).createUserAddress(any());
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 다른 사용자의 주소를 생성하면 403 에러가 발생한다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void createUserAddress_asCustomerOther_forbidden() throws Exception {
            // given
            Long userId = 2L;
            UUID hubId = UUID.randomUUID();

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                hubId,
                "테스트 주소",
                "101동 101호",
                true,
                37.123456,
                127.123456
            );

            // when & then
            mockMvc.perform(post("/v1/users/address/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

            verify(userFacade, times(0)).createUserAddress(any());
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400 에러가 발생한다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void createUserAddress_withInvalidRequest_badRequest() throws Exception {
            // given
            Long userId = 1L;

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                null, // hubId 누락
                "", // address 빈 문자열
                "101동 101호",
                true,
                null, // latitude 누락
                127.123456
            );

            // when & then
            mockMvc.perform(post("/v1/users/address/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(userFacade, times(0)).createUserAddress(any());
        }
    }

    @Nested
    @DisplayName("주소 수정 테스트")
    class UpdateUserAddressTest {

        @Test
        @DisplayName("MASTER 권한으로 다른 사용자의 주소를 수정할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "MASTER")
        void updateUserAddress_asMaster_success() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                hubId,
                "테스트 주소",
                "102동 102호",
                false,
                37.654321,
                127.654321
            );

            doNothing().when(userFacade).updateUserAddress(eq(addressId), any());

            // when & then
            mockMvc.perform(patch("/v1/users/address/{userId}/{addressId}", userId, addressId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).updateUserAddress(eq(addressId), any());
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 본인의 주소를 수정할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void updateUserAddress_asCustomerOwn_success() throws Exception {
            // given
            Long userId = 1L;
            UUID addressId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                hubId,
                "테스트 주소",
                "102동 102호",
                false,
                37.654321,
                127.654321
            );

            doNothing().when(userFacade).updateUserAddress(eq(addressId), any());

            // when & then
            mockMvc.perform(patch("/v1/users/address/{userId}/{addressId}", userId, addressId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).updateUserAddress(eq(addressId), any());
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 다른 사용자의 주소를 수정하면 403 에러가 발생한다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void updateUserAddress_asCustomerOther_forbidden() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();

            UserAddressCreateRequest request = new UserAddressCreateRequest(
                hubId,
                "테스트 주소",
                "102동 102호",
                false,
                37.654321,
                127.654321
            );

            // when & then
            mockMvc.perform(patch("/v1/users/address/{userId}/{addressId}", userId, addressId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

            verify(userFacade, times(0)).updateUserAddress(any(), any());
        }
    }

    @Nested
    @DisplayName("주소 삭제 테스트")
    class DeleteUserAddressTest {

        @Test
        @DisplayName("MASTER 권한으로 다른 사용자의 주소를 삭제할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "MASTER")
        void deleteUserAddress_asMaster_success() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();

            doNothing().when(userFacade).deleteUserAddress(addressId, userId);

            // when & then
            mockMvc.perform(delete("/v1/users/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).deleteUserAddress(addressId, userId);
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 본인의 주소를 삭제할 수 있다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void deleteUserAddress_asCustomerOwn_success() throws Exception {
            // given
            Long userId = 1L;
            UUID addressId = UUID.randomUUID();

            doNothing().when(userFacade).deleteUserAddress(addressId, userId);

            // when & then
            mockMvc.perform(delete("/v1/users/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isOk());

            verify(userFacade, times(1)).deleteUserAddress(addressId, userId);
        }

        @Test
        @DisplayName("CUSTOMER 권한으로 다른 사용자의 주소를 삭제하면 403 에러가 발생한다")
        @CustomWithMockUser(userId = 1L, authority = "CUSTOMER")
        void deleteUserAddress_asCustomerOther_forbidden() throws Exception {
            // given
            Long userId = 2L;
            UUID addressId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/v1/users/address/{userId}/{addressId}", userId, addressId))
                .andExpect(status().isForbidden());

            verify(userFacade, times(0)).deleteUserAddress(any(), any());
        }
    }
}
