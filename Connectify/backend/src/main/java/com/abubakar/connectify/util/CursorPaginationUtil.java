package com.abubakar.connectify.util;

import com.abubakar.connectify.dto.response.CursorPageResponse;

import java.util.List;
import java.util.function.Function;

public class CursorPaginationUtil {

    private CursorPaginationUtil() {
    }

    public static <T, R> CursorPageResponse<R> buildResponse(
            List<T> entities,
            int size,
            Function<T, Long> cursorExtractor,
            Function<T, R> mapper
    ) {

        boolean hasNext = entities.size() > size;

        // REMOVE EXTRA RECORD
        if (hasNext) {
            entities = entities.subList(0, size);
        }

        Long nextCursor = null;

        if (hasNext && !entities.isEmpty()) {

            T lastEntity =
                    entities.get(
                            entities.size() - 1
                    );

            nextCursor = cursorExtractor.apply(lastEntity);
        }

        List<R> content =
                entities.stream()
                        .map(mapper)
                        .toList();

        return CursorPageResponse.<R>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .pageSize(size)
                .currentPageData(content.size())
                .build();
    }

}

