package com.abubakar.connectify.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pageable createCursorPageable(
            int size
    ) {

        if (size <= 0 ||
                size > PaginationConstants.MAX_PAGE_SIZE) {

            size =
                    PaginationConstants.DEFAULT_PAGE_SIZE;
        }

        return PageRequest.of(
                0,
                size + PaginationConstants.CURSOR_EXTRA_RECORD,
                Sort.by(
                        Sort.Direction.DESC,
                        "id"
                )
        );
    }

}

