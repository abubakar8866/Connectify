package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private Boolean isSeen = false;

    private LocalDateTime seenAt;

    private Boolean isEdited = false;

    private LocalDateTime editedAt;

    private Boolean deletedForEveryone = false;

    private Boolean deletedByAdmin = false;

    private LocalDateTime deletedByAdminAt;

    private Boolean restoreRequested = false;

    private LocalDateTime restoreRequestedAt;

    // REPLY MESSAGE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    // REPLIES
    @OneToMany(mappedBy = "replyToMessage")
    private List<Message> replies =
            new ArrayList<>();

    // CHAT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    // SENDER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // DELETE FOR ME
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "deleted_messages",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> deletedForUsers =
            new ArrayList<>();

}

