package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "notifications",
        indexes = {

                @Index(
                        name = "idx_notification_receiver",
                        columnList = "receiver_id"
                ),

                @Index(
                        name = "idx_notification_receiver_read",
                        columnList = "receiver_id, is_read"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RECEIVER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    // WHO TRIGGERED ACTION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    private Boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

}

