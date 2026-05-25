package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Post;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

    // SEARCH BY CAPTION
    public static Specification<Post> keyword(
            String keyword
    ) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("caption")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    // SEARCH BY USERNAME
    public static Specification<Post> username(
            String username
    ) {

        return (root, query, cb) -> {

            if (username == null || username.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(
                            root.get("user").get("uname")
                    ),
                    "%" + username.toLowerCase() + "%"
            );
        };
    }

    // SEARCH BY HASHTAG
    public static Specification<Post> hashtag(
            String hashtag
    ) {

        return (root, query, cb) -> {

            if (hashtag == null || hashtag.isBlank()) {
                return cb.conjunction();
            }

            query.distinct(true);

            Join<Post, Hashtag> hashtags =
                    root.join("hashtags");

            return cb.like(
                    cb.lower(hashtags.get("name")),
                    "%" + hashtag.toLowerCase() + "%"
            );
        };
    }

    // REPORTED POSTS
    public static Specification<Post> reportedOnly() {

        return (root, query, cb) -> {

            if (query != null && query.getResultType() != Long.class) {
                query.distinct(true);
            }

            return cb.isNotEmpty(
                    root.get("reports")
            );
        };
    }

    // RESTORE REQUESTED POSTS
    public static Specification<Post> restoreRequested(
            Boolean restoreRequested
    ) {

        return (root, query, cb) -> {

            if (restoreRequested == null) {

                return cb.conjunction();
            }

            return cb.equal(
                    root.get("restoreRequested"),
                    restoreRequested
            );
        };
    }

    // DELETED
    public static Specification<Post> deleted(
            Boolean deleted
    ) {

        return (root, query, cb) -> {

            if (deleted == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("deleted"),
                    deleted
            );
        };
    }

    // CURSOR
    public static Specification<Post> cursor(
            Long cursor
    ) {

        return (root, query, cb) -> {

            if (cursor == null) {

                return cb.conjunction();
            }

            return cb.lessThan(
                    root.get("id"),
                    cursor
            );
        };
    }

}

