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

    private Boolean isActive = true;

    private Boolean deleted = false;

    private Boolean restoreRequested = false;

    private Long viewCount = 0L;

    private Long reactionCount = 0L;

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
            mappedBy = "story"
    )
    private List<Report> reports = new ArrayList<>();

}

