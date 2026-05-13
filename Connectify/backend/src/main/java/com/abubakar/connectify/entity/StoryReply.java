package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryReply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    // STORY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    // SENDER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

}

