package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CursorPageResponse<T> {

    private List<T> content;

    // NEXT CURSOR
    private Long nextCursor;

    // HAS NEXT PAGE
    private Boolean hasNext;

    // REQUEST SIZE
    private Integer pageSize;

    // CURRENT PAGE COUNT
    private Integer currentPageData;

}

