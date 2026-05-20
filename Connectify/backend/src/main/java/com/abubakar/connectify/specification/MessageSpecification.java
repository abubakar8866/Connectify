package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.Message;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.MessageType;
import jakarta.persistence.criteria.Join;
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
            Long cursor
    ) {

        return (root, query, cb) -> {

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

