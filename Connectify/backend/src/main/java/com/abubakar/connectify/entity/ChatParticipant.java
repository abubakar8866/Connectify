package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "chat_id",
                                "user_id"
                        }
                )
        },
        indexes = {

                @Index(
                        name = "idx_chat_participant_user_deleted",
                        columnList = "user_id, deleted"
                ),

                @Index(
                        name = "idx_chat_participant_chat_user",
                        columnList = "chat_id, user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private Long unreadCount = 0L;

    @Builder.Default
    private Boolean isArchived = false;

    @Builder.Default
    private Boolean isMuted = false;

    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    private LocalDateTime lastSeenAt;

    // CHAT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    // USER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {

        if (unreadCount == null) {
            unreadCount = 0L;
        }

        if (isArchived == null) {
            isArchived = false;
        }

        if (isMuted == null) {
            isMuted = false;
        }

        if (deleted == null) {
            deleted = false;
        }
    }

}

