package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.Report;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.enums.ReportTargetType;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {

    public static Specification<Report> searchReports(
            ReportStatus status,
            ReportTargetType targetType,
            String reporterKeyword,
            Boolean resolvedOnly,
            Long cursor
    ) {

        return (root, query, cb) -> {

            if (query != null &&
                    query.getResultType() != Long.class) {

                root.fetch(
                        "reportedBy",
                        JoinType.LEFT
                );

                root.fetch(
                        "resolvedBy",
                        JoinType.LEFT
                );

                query.distinct(true);
            }

            query.orderBy(
                    cb.desc(root.get("id"))
            );

            List<Predicate> predicates =
                    new ArrayList<>();

            if (status != null) {

                predicates.add(

                        cb.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (Boolean.TRUE.equals(resolvedOnly)) {

                predicates.add(

                        cb.isNotNull(
                                root.get("resolvedAt")
                        )
                );
            }

            Join<Report, User> reporter =
                    root.join(
                            "reportedBy",
                            JoinType.LEFT
                    );

            if (
                    reporterKeyword != null &&
                            !reporterKeyword.isBlank()
            ) {

                predicates.add(

                        cb.like(
                                cb.lower(
                                        reporter.get("uname")
                                ),
                                reporterKeyword.toLowerCase() + "%"
                        )
                );
            }

            if (targetType != null) {

                switch (targetType) {

                    case POST ->
                            predicates.add(
                                    cb.isNotNull(
                                            root.get("post")
                                    )
                            );

                    case COMMENT ->
                            predicates.add(
                                    cb.isNotNull(
                                            root.get("comment")
                                    )
                            );

                    case USER ->
                            predicates.add(
                                    cb.isNotNull(
                                            root.get("reportedUser")
                                    )
                            );

                    case STORY ->
                            predicates.add(
                                    cb.isNotNull(
                                            root.get("story")
                                    )
                            );

                    case CHAT ->
                            predicates.add(
                                    cb.isNotNull(
                                            root.get("chat")
                                    )
                            );

                    case MESSAGE ->
                            predicates.add(
                                    cb.isNotNull(
                                            root.get("message")
                                    )
                            );
                }
            }

            if (cursor != null) {

                predicates.add(

                        cb.lessThan(
                                root.get("id"),
                                cursor
                        )
                );
            }

            return cb.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }

}

