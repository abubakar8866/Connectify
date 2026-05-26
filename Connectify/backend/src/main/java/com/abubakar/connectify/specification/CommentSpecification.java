package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Report;
import com.abubakar.connectify.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CommentSpecification {

    public static Specification<Comment> searchComments(

            String keyword,
            Boolean reportedOnly,
            Boolean restoreRequested,
            Boolean deleted,
            Long cursor
    ) {

        return (root, query, cb) -> {

            if (
                    query != null
                            &&
                            query.getResultType() != Long.class
            ) {

                query.distinct(true);
            }

            query.orderBy(
                    cb.desc(root.get("id"))
            );

            List<Predicate> predicates =
                    new ArrayList<>();

            // ================= KEYWORD =================

            if (
                    keyword != null
                            &&
                            !keyword.isBlank()
            ) {

                Join<Comment, User> user =
                        root.join(
                                "user",
                                JoinType.LEFT
                        );

                predicates.add(

                        cb.or(

                                cb.like(
                                        cb.lower(root.get("content")),
                                        keyword.toLowerCase() + "%"
                                ),

                                cb.like(
                                        cb.lower(user.get("uname")),
                                        keyword.toLowerCase() + "%"
                                )
                        )
                );
            }

            // ================= REPORTED =================

            if (Boolean.TRUE.equals(reportedOnly)) {

                Join<Comment, Report> reports =
                        root.join(
                                "reports",
                                JoinType.LEFT
                        );

                predicates.add(
                        cb.isNotNull(
                                reports.get("id")
                        )
                );
            }

            // ================= RESTORE REQUESTED =================

            if (restoreRequested != null) {

                predicates.add(

                        cb.equal(
                                root.get("restoreRequested"),
                                restoreRequested
                        )
                );
            }

            // ================= DELETED =================

            if (deleted != null) {

                predicates.add(

                        cb.equal(
                                root.get("deleted"),
                                deleted
                        )
                );
            }

            // ================= CURSOR =================

            if (cursor != null) {

                predicates.add(

                        cb.lessThan(
                                root.get("id"),
                                cursor
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

}

