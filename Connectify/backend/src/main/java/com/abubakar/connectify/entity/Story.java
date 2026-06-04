package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean deleted = false;

    @Builder.Default
    private Boolean deletedByAdmin = false;

    private LocalDateTime deletedByAdminAt;

    @Builder.Default
    private Boolean restoreRequested = false;

    private LocalDateTime restoreRequestedAt;

    @Builder.Default
    private Long viewCount = 0L;

    @Builder.Default
    private Long reactionCount = 0L;

    @Builder.Default
    private Long replyCount = 0L;

    // STORY OWNER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // VIEWERS
    @OneToMany(
            mappedBy = "story",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<StoryView> views = new ArrayList<>();

    // REACTIONS
    @OneToMany(
            mappedBy = "story",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<StoryReaction> reactions = new ArrayList<>();

    // REPLIES
    @OneToMany(
            mappedBy = "story",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<StoryReply> replies = new ArrayList<>();

    // REPORTS
    @OneToMany(
            mappedBy = "story",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Report> reports = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (isActive == null) {
            isActive = true;
        }

        if (deleted == null) {
            deleted = false;
        }

        if (deletedByAdmin == null) {
            deletedByAdmin = false;
        }

        if (restoreRequested == null) {
            restoreRequested = false;
        }

        if (viewCount == null) {
            viewCount = 0L;
        }

        if (reactionCount == null) {
            reactionCount = 0L;
        }

        if (replyCount == null) {
            replyCount = 0L;
        }
    }

}

