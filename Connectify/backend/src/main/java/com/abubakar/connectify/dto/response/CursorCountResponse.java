package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorCountResponse<T> {

    private CursorPageResponse<T> page;

    private Long totalCount;

}

