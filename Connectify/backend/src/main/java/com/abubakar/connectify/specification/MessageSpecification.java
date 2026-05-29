package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.Message;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.MessageType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MessageSpecification {

    public static Specification<Message> searchMessages(
            String keyword,
            String username,
            MessageType messageType,
            Boolean deletedByAdmin,
            Boolean restoreRequested,
            Boolean reportedOnly,
            Long cursor
    ) {

        return (root, query, cb) -> {

            if (query != null && query.getResultType() != Long.class) {

                root.fetch("sender", JoinType.LEFT);
                root.fetch("chat", JoinType.LEFT);
                root.fetch("reports", JoinType.LEFT);
                query.distinct(true);
            }

            query.orderBy(
                    cb.desc(root.get("id"))
            );

            List<Predicate> predicates =
                    new ArrayList<>();

            Join<Message, User> sender =
                    root.join("sender");

            if (
                    keyword != null
                            && !keyword.isBlank()
            ) {

                predicates.add(

                        cb.like(
                                cb.lower(root.get("content")),
                                "%" + keyword.toLowerCase() + "%"
                        )
                );
            }

            if (
                    username != null
                            && !username.isBlank()
            ) {

                predicates.add(

                        cb.like(
                                cb.lower(sender.get("uname")),
                                "%" + username.toLowerCase() + "%"
                        )
                );
            }

            if (messageType != null) {

                predicates.add(

                        cb.equal(
                                root.get("messageType"),
                                messageType
                        )
                );
            }

            if (deletedByAdmin != null) {

                predicates.add(

                        cb.equal(
                                root.get("deletedByAdmin"),
                                deletedByAdmin
                        )
                );
            }

            if (restoreRequested != null) {

                predicates.add(

                        cb.equal(
                                root.get("restoreRequested"),
                                restoreRequested
                        )
                );
            }

            if (Boolean.TRUE.equals(reportedOnly)) {

                predicates.add(
                        cb.greaterThan(
                                cb.size(root.get("reports")),
                                0
                        )
                );
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
                    predicates.toArray(new Predicate[0])
            );
        };

    }

    public static Specification<Message> hasChatId(
            Long chatId
    ) {

        return (root, query, cb) -> {

            if (chatId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("chat").get("id"),
                    chatId
            );
        };
    }

}

