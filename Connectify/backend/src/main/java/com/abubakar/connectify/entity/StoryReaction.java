package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "story_reactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"story_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    // STORY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    // USER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}

