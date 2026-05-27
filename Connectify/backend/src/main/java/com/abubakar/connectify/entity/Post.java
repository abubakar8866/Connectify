package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "posts",
        indexes = {

                // USER PROFILE POSTS
                @Index(
                        name = "idx_post_user_deleted",
                        columnList = "user_id, deleted"
                ),

                // CURSOR PAGINATION
                @Index(
                        name = "idx_post_user_deleted_id",
                        columnList = "user_id, deleted, id"
                ),

                // FEED QUERIES
                @Index(
                        name = "idx_post_deleted_id",
                        columnList = "deleted, id"
                ),

                // ADMIN RESTORE REQUESTS
                @Index(
                        name = "idx_post_restore_requested",
                        columnList = "restore_requested"
                ),

                // SOFT DELETE FILTERS
                @Index(
                        name = "idx_post_deleted",
                        columnList = "deleted"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String caption;

    private Long likeCount = 0L;

    private Long commentCount = 0L;

    private Boolean deleted = false;

    @Column(name = "restore_requested")
    private Boolean restoreRequested = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Media> mediaList = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "post_hashtags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags = new ArrayList<>();

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Like> likes = new ArrayList<>();

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Report> reports = new ArrayList<>();

}

