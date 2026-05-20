package com.abubakar.connectify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
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

    private Boolean isActive = true;

    private Boolean deletedByAdmin = false;

    private LocalDateTime deletedByAdminAt;

    private Long totalMessages = 0L;

    private Boolean restoreRequested = false;

    private LocalDateTime restoreRequestedAt;

    // PARTICIPANTS
    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChatParticipant> participants =
            new ArrayList<>();

    // MESSAGES
    @OneToMany(
            mappedBy = "chat",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Message> messages =
            new ArrayList<>();

}

