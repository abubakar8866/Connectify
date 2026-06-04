package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        indexes = {

                @Index(
                        name = "idx_chat_deleted_active",
                        columnList = "deletedByAdmin, isActive"
                ),

                @Index(
                        name = "idx_chat_last_message_at",
                        columnList = "lastMessageAt"
                ),

                @Index(
                        name = "idx_chat_restore_requested",
                        columnList = "restoreRequested"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    private LocalDateTime lastMessageAt;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean deletedByAdmin = false;

    private LocalDateTime deletedByAdminAt;

    @Builder.Default
    private Long totalMessages = 0L;

    @Builder.Default
    private Boolean restoreRequested = false;

    private LocalDateTime restoreRequestedAt;

    // PARTICIPANTS
    @Builder.Default
    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChatParticipant> participants =
            new ArrayList<>();

    // MESSAGES
    @Builder.Default
    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Message> messages =
            new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Report> reports =
            new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (isActive == null) {
            isActive = true;
        }

        if (deletedByAdmin == null) {
            deletedByAdmin = false;
        }

        if (restoreRequested == null) {
            restoreRequested = false;
        }

        if (totalMessages == null) {
            totalMessages = 0L;
        }
    }

}

