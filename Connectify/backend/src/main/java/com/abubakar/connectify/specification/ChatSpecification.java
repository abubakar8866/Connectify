package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.Chat;
import com.abubakar.connectify.entity.ChatParticipant;
import com.abubakar.connectify.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChatSpecification {

    public static Specification<Chat> searchChats(
            String keyword,
            Boolean deletedByAdmin,
            Long cursor
    ) {

        return (root, query, cb) -> {

            if (query != null && query.getResultType() != Long.class) {
                query.distinct(true);
            }

            query.orderBy(
                    cb.desc(root.get("id"))
            );

            Join<Chat, ChatParticipant> participants =
                    root.join(
                            "participants",
                            JoinType.LEFT
                    );

            Join<ChatParticipant, User> user =
                    participants.join(
                            "user",
                            JoinType.LEFT
                    );

            List<Predicate> predicates =
                    new ArrayList<>();

            if (
                    keyword != null
                            && !keyword.isBlank()
            ) {

                predicates.add(

                        cb.or(

                                cb.like(
                                        cb.lower(user.get("uname")),
                                         keyword.toLowerCase() + "%"
                                ),

                                cb.like(
                                        cb.lower(user.get("email")),
                                         keyword.toLowerCase() + "%"
                                )
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

