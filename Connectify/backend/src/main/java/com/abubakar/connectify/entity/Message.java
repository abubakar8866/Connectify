package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "messages",
        indexes = {

                @Index(
                        name = "idx_message_chat_id",
                        columnList = "chat_id"
                ),

                @Index(
                        name = "idx_message_chat_id_id",
                        columnList = "chat_id, id"
                ),

                @Index(
                        name = "idx_message_deleted_admin",
                        columnList = "deletedByAdmin"
                ),

                @Index(
                        name = "idx_message_restore_requested",
                        columnList = "restoreRequested"
                ),

                @Index(
                        name = "idx_message_sender",
                        columnList = "sender_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= MESSAGE CONTENT =================

    @Column(columnDefinition = "TEXT")
    private String content;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    // ================= MESSAGE STATUS =================

    private Boolean isSeen = false;

    private LocalDateTime seenAt;

    private Boolean isEdited = false;

    private LocalDateTime editedAt;

    // ================= USER DELETE =================

    private Boolean deletedForEveryone = false;

    // ================= ADMIN MODERATION =================

    private Boolean deletedByAdmin = false;

    private LocalDateTime deletedByAdminAt;

    // ORIGINAL DATA BACKUP FOR RESTORE
    @Column(columnDefinition = "TEXT")
    private String originalContent;

    private String originalMediaUrl;

    // ================= RESTORE REQUEST =================

    private Boolean restoreRequested = false;

    private LocalDateTime restoreRequestedAt;

    // ================= REPLY MESSAGE =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    // ================= REPLIES =================

    @OneToMany(mappedBy = "replyToMessage")
    private List<Message> replies =
            new ArrayList<>();

    // ================= CHAT =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    // ================= SENDER =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // ================= DELETE FOR ME =================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "deleted_messages",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> deletedForUsers =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "message",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Report> reports =
            new ArrayList<>();

}

