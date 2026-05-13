package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "story_views",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"story_id", "viewer_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryView extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // STORY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    // VIEWER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id")
    private User viewer;

}
