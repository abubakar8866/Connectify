package com.abubakar.connectify.entity;

import com.abubakar.connectify.enums.ReportReason;
import com.abubakar.connectify.enums.ReportStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_report_post",
                        columnNames = {
                                "reported_by_id",
                                "post_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_report_comment",
                        columnNames = {
                                "reported_by_id",
                                "comment_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_report_user",
                        columnNames = {
                                "reported_by_id",
                                "reported_user_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_report_story",
                        columnNames = {
                                "reported_by_id",
                                "story_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_report_chat",
                        columnNames = {
                                "reported_by_id",
                                "chat_id"
                        }
                ),

                @UniqueConstraint(
                        name = "uk_report_message",
                        columnNames = {
                                "reported_by_id",
                                "message_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_report_comment",
                        columnList = "comment_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    // USER WHO REPORTED
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id")
    private User reportedBy;

    // REPORTED USER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    // REPORTED POST
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // REPORTED COMMENT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    // REPORTED CHAT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    // REPORTED MESSAGE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    // REPORTED STORY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

}

