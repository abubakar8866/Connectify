package com.abubakar.connectify.specification;

import com.abubakar.connectify.dto.request.AdminStoryFilterRequest;
import com.abubakar.connectify.entity.Story;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StorySpecification {

    public static Specification<Story> filterStories(
            AdminStoryFilterRequest request,
            Long cursor
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (request.getUsername() != null &&
                    !request.getUsername().trim().isEmpty()) {

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("user")
                                                .get("uname")
                                ),
                                "%" +
                                        request.getUsername()
                                                .toLowerCase() +
                                        "%"
                        )
                );
            }

            if (request.getDeleted() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("deleted"),
                                request.getDeleted()
                        )
                );
            }

            if (request.getIsActive() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("isActive"),
                                request.getIsActive()
                        )
                );
            }

            if (request.getRestoreRequested() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("restoreRequested"),
                                request.getRestoreRequested()
                        )
                );
            }

            if (request.getMediaType() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("mediaType"),
                                request.getMediaType()
                        )
                );
            }

            if (request.getExpired() != null) {

                if (request.getExpired()) {

                    predicates.add(
                            criteriaBuilder.lessThan(
                                    root.get("expiresAt"),
                                    LocalDateTime.now()
                            )
                    );

                } else {

                    predicates.add(
                            criteriaBuilder.greaterThan(
                                    root.get("expiresAt"),
                                    LocalDateTime.now()
                            )
                    );
                }
            }

            if (request.getCreatedDate() != null) {

                LocalDateTime start =
                        request.getCreatedDate()
                                .atStartOfDay();

                LocalDateTime end =
                        request.getCreatedDate()
                                .plusDays(1)
                                .atStartOfDay();

                predicates.add(
                        criteriaBuilder.between(
                                root.get("createdAt"),
                                start,
                                end
                        )
                );
            }

            if (Boolean.TRUE.equals(request.getReportedOnly())) {

                predicates.add(
                        criteriaBuilder.isNotEmpty(
                                root.get("reports")
                        )
                );
            }

            // CURSOR PAGINATION
            if (cursor != null) {

                predicates.add(
                        criteriaBuilder.lessThan(
                                root.get("id"),
                                cursor
                        )
                );
            }

            query.orderBy(
                    criteriaBuilder.desc(root.get("id"))
            );

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

}

