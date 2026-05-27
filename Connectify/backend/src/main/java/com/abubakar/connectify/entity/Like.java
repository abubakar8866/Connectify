package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = {

                // PREVENT DUPLICATE POST LIKES
                @UniqueConstraint(
                        name = "uk_like_user_post",
                        columnNames = {
                                "user_id",
                                "post_id"
                        }
                ),

                // PREVENT DUPLICATE COMMENT LIKES
                @UniqueConstraint(
                        name = "uk_like_user_comment",
                        columnNames = {
                                "user_id",
                                "comment_id"
                        }
                )
        },
        indexes = {

                // FAST POST LIKE LOOKUPS
                @Index(
                        name = "idx_like_post",
                        columnList = "post_id"
                ),

                // FAST COMMENT LIKE LOOKUPS
                @Index(
                        name = "idx_like_comment",
                        columnList = "comment_id"
                ),

                // FAST USER LIKE LOOKUPS
                @Index(
                        name = "idx_like_user",
                        columnList = "user_id"
                ),

                // FEED "LIKED POSTS" CHECKS
                @Index(
                        name = "idx_like_user_post",
                        columnList = "user_id, post_id"
                ),

                // COMMENT LIKE CHECKS
                @Index(
                        name = "idx_like_user_comment",
                        columnList = "user_id, comment_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // POST LIKE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // COMMENT LIKE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

}

