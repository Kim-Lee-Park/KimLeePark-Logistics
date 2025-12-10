package com.klp.common;

import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class PageableArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_SIZE = 10;
    private static final int DEFAULT_PAGE = 0;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String pageParam = webRequest.getParameter("page");
        String sizeParam = webRequest.getParameter("size");
        String sortParam = webRequest.getParameter("sort");

        int page = DEFAULT_PAGE;
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 0) {
                    page = DEFAULT_PAGE;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        int size = DEFAULT_SIZE;
        if (sizeParam != null) {
            try {
                int requested = Integer.parseInt(sizeParam);
                if (ALLOWED_SIZES.contains(requested)) {
                    size = requested;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Sort sort = Sort.unsorted();
        if (sortParam != null) {
            sort = Sort.by(Sort.Order.by(sortParam));
        }

        return PageRequest.of(page, size, sort);
    }
}
