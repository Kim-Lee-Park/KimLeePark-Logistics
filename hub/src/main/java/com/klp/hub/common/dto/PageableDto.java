package com.klp.hub.common.dto;

public record PageableDto(
    Integer page,
    Integer size,
    Integer totalElements,
    Integer totalPages,
    Boolean hasNext,
    Boolean hasPrevious,
    Boolean isFirst,
    Boolean isLast
){}
