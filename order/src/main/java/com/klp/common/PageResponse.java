package com.klp.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> data,
    PageableResponse pageable
) {

    public static <T> PageResponse<T> of(List<T> data, Page<?> page) {
        return new PageResponse<>(
            data,
            PageableResponse.from(page)
        );
    }
}
