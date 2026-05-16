package com.abubakar.connectify.specification;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    // ================= KEYWORD SEARCH =================
    public static Specification<User> searchByKeyword(
            String keyword
    ) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern =
                    "%" + keyword.toLowerCase() + "%";

            return cb.or(

                    cb.like(
                            cb.lower(root.get("name")),
                            pattern
                    ),

                    cb.like(
                            cb.lower(root.get("uname")),
                            pattern
                    )
            );
        };
    }

    // ================= VERIFIED FILTER =================
    public static Specification<User> hasVerified(
            Boolean verified
    ) {

        return (root, query, cb) -> {

            if (verified == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("isVerified"),
                    verified
            );
        };
    }

    // ================= PRIVATE FILTER =================
    public static Specification<User> hasPrivateAccount(
            Boolean isPrivate
    ) {

        return (root, query, cb) -> {

            if (isPrivate == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("isPrivate"),
                    isPrivate
            );
        };
    }

    // ================= ACTIVE FILTER =================
    public static Specification<User> hasActive(
            Boolean active
    ) {

        return (root, query, cb) -> {

            if (active == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("isActive"),
                    active
            );
        };
    }

    // ================= ACCOUNT STATUS =================
    public static Specification<User> hasAccountStatus(
            AccountStatus status
    ) {

        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("accountStatus"),
                    status
            );
        };
    }

    // ================= CITY FILTER =================
    public static Specification<User> hasCity(
            String city
    ) {

        return (root, query, cb) -> {

            if (city == null || city.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("city")),
                    "%" + city.toLowerCase() + "%"
            );
        };
    }

    // ================= GENDER FILTER =================
    public static Specification<User> hasGender(
            Gender gender
    ) {

        return (root, query, cb) -> {

            if (gender == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("gender"),
                    gender
            );
        };
    }

    // ================= MIN FOLLOWERS =================
    public static Specification<User> hasMinFollowers(
            Long minFollowers
    ) {

        return (root, query, cb) -> {

            if (minFollowers == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("followersCount"),
                    minFollowers
            );
        };
    }

    // ================= EXCLUDE CURRENT USER =================
    public static Specification<User> excludeCurrentUser(
            Long currentUserId
    ) {

        return (root, query, cb) ->

                cb.notEqual(
                        root.get("id"),
                        currentUserId
                );
    }

    // ================= CURSOR PAGINATION =================
    public static Specification<User> cursor(
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

