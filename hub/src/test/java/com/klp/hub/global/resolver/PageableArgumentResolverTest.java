package com.klp.hub.global.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class PageableArgumentResolverTest {

    private PageableArgumentResolver resolver;
    private ModelAndViewContainer mavContainer;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        resolver = new PageableArgumentResolver();
        mavContainer = new ModelAndViewContainer();
        request = new MockHttpServletRequest();
    }

    private Pageable resolve(NativeWebRequest webRequest) throws Exception {
        return (Pageable) resolver.resolveArgument(
            null, mavContainer, webRequest, null
        );
    }

    @Test
    @DisplayName("기본값 테스트 - page=0, size=10, sort=unsorted")
    void default_values() throws Exception {
        NativeWebRequest webRequest = new ServletWebRequest(request);

        Pageable pageable = resolve(webRequest);

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().isUnsorted()).isTrue();
    }

    @Test
    @DisplayName("허용된 size값은 정상 반영된다 (10, 30, 50)")
    void allowed_sizes() throws Exception {
        request.setParameter("page", "1");
        request.setParameter("size", "30");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Pageable pageable = resolve(webRequest);

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(30);
    }

    @Test
    @DisplayName("허용되지 않은 size 값은 기본값(10)으로 처리된다")
    void invalid_size() throws Exception {
        request.setParameter("size", "777");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Pageable pageable = resolve(webRequest);

        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("page 음수 → 0으로 교정된다")
    void negative_page() throws Exception {
        request.setParameter("page", "-5");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Pageable pageable = resolve(webRequest);

        assertThat(pageable.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("sort 파라미터는 그대로 PageRequest에 반영된다")
    void sort_param() throws Exception {
        request.setParameter("sort", "createdAt");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Pageable pageable = resolve(webRequest);

        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("page, size가 숫자가 아니면 기본값을 사용한다")
    void invalid_number() throws Exception {
        request.setParameter("page", "abc");
        request.setParameter("size", "xyz");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Pageable pageable = resolve(webRequest);

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }
}
