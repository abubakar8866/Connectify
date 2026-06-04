package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "message_media",
        indexes = {
                @Index(
                        name = "idx_message_media_message",
                        columnList = "message_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MessageType mediaType;

    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

}

