package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.MessageMediaResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.service.AdminChatService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.specification.ChatSpecification;
import com.abubakar.connectify.specification.MessageSpecification;
import com.abubakar.connectify.dto.request.ChatSearchRequest;
import com.abubakar.connectify.dto.request.MessageSearchRequest;
import com.abubakar.connectify.dto.response.AdminChatResponse;
import com.abubakar.connectify.dto.response.AdminMessageResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.repository.ChatRepository;
import com.abubakar.connectify.repository.MessageRepository;
import com.abubakar.connectify.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AdminChatServiceImpl
        implements AdminChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private ChatAccessValidator chatAccessValidator;

    @Autowired
    private MessageAccessValidator messageAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(AdminChatServiceImpl.class);

    // ================= GET CHATS =================
    @Override
    public CursorPageResponse<AdminChatResponse> getChats(
            ChatSearchRequest request,
            Long cursor,
            int size
    ) {

        logger.info(
                """
                Fetching chats
                | keyword: {}
                | deletedByAdmin: {}
                | restoreRequested: {}
                | reportedOnly: {}
                | cursor: {}
                | size: {}
                """,
                request.getKeyword(),
                request.getDeletedByAdmin(),
                request.getRestoreRequested(),
                request.getReportedOnly(),
                cursor,
                size
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<Chat> specification =
                ChatSpecification.searchChats(
                        request.getKeyword(),
                        request.getDeletedByAdmin(),
                        request.getRestoreRequested(),
                        request.getReportedOnly(),
                        cursor
                );

        List<Chat> chats =
                chatRepository.findAll(
                        specification,
                        pageable
                ).getContent();

        logger.info(
                "Chats fetched successfully | totalChats: {}",
                chats.size()
        );

        return CursorPaginationUtil.buildResponse(
                chats,
                size,
                Chat::getId,
                this::mapToAdminChatResponse
        );
    }

    // ================= GET MESSAGES =================
    @Override
    public CursorPageResponse<AdminMessageResponse> getMessages(
            Long chatId,
            MessageSearchRequest request,
            Long cursor,
            int size
    ) {

        logger.info(
                """
                Fetching messages
                | chatId: {}
                | keyword: {}
                | username: {}
                | messageType: {}
                | deletedByAdmin: {}
                | restoreRequested: {}
                | reportedOnly: {}
                | cursor: {}
                | size: {}
                """,
                chatId,
                request.getKeyword(),
                request.getUsername(),
                request.getMessageType(),
                request.getDeletedByAdmin(),
                request.getRestoreRequested(),
                request.getReportedOnly(),
                cursor,
                size
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        chatAccessValidator.getChat(chatId);

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<Message> specification =
                MessageSpecification.searchMessages(
                        request.getKeyword(),
                        request.getUsername(),
                        request.getMessageType(),
                        request.getDeletedByAdmin(),
                        request.getRestoreRequested(),
                        request.getReportedOnly(),
                        cursor
                ).and(
                        MessageSpecification.hasChatId(
                                chatId
                        )
                );

        List<Message> messages =
                messageRepository.findAll(
                        specification,
                        pageable
                ).getContent();

        logger.info(
                "Messages fetched successfully | totalMessages: {}",
                messages.size()
        );

        return CursorPaginationUtil.buildResponse(
                messages,
                size,
                Message::getId,
                this::mapToAdminMessageResponse
        );
    }

    // ================= MODERATE CHAT =================
    @Override
    public void moderateChat(
            Long chatId
    ) {

        logger.info(
                "Moderating chat | chatId: {}",
                chatId
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Chat chat =
                chatAccessValidator.getActiveChat(chatId);

        if (Boolean.TRUE.equals(
                chat.getDeletedByAdmin()
        )) {

            logger.warn("Chat already moderated");

            throw new OperationFailException(
                    "Chat already moderated"
            );
        }

        chat.setDeletedByAdmin(true);

        chat.setDeletedByAdminAt(
                LocalDateTime.now()
        );

        chat.setRestoreRequested(false);

        chat.setRestoreRequestedAt(null);

        chatRepository.save(chat);

        for (ChatParticipant participant
                : chat.getParticipants()) {

            notificationService.createSystemNotification(
                    participant.getUser().getId(),
                    admin.getId(),
                    "A chat was removed by admin",
                    NotificationType.CHAT_DELETED_BY_ADMIN
            );
        }

        logger.info(
                "Chat moderated successfully | chatId: {}",
                chatId
        );
    }

    // ================= APPROVE RESTORE CHAT =================
    @Override
    public void approveChatRestore(
            Long chatId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Chat chat =
                chatAccessValidator.getChat(chatId);

        if (!Boolean.TRUE.equals(
                chat.getDeletedByAdmin()
        )) {

            logger.warn("Chat is not moderated");

            throw new OperationFailException(
                    "Chat is not moderated"
            );
        }

        if (!Boolean.TRUE.equals(
                chat.getRestoreRequested()
        )) {

            logger.warn("No restore request found");

            throw new OperationFailException(
                    "No restore request found"
            );
        }

        chat.setDeletedByAdmin(false);

        chat.setDeletedByAdminAt(null);

        chat.setRestoreRequested(false);

        chat.setRestoreRequestedAt(null);

        chatRepository.save(chat);

        for (ChatParticipant participant
                : chat.getParticipants()) {

            notificationService.createSystemNotification(
                    participant.getUser().getId(),
                    admin.getId(),
                    "Your chat was restored by admin",
                    NotificationType.CHAT_RESTORED
            );
        }

        logger.info(
                "Chat restore request is approved successfully by admin | chatId: {}",
                chatId
        );

    }

    // ================= REJECT RESTORE CHAT =================
    @Override
    public void rejectChatRestore(
            Long chatId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Chat chat =
                chatAccessValidator.getChat(chatId);

        if (!Boolean.TRUE.equals(
                chat.getRestoreRequested()
        )) {

            logger.warn("No restore request found when rejecting chat restore request.");

            throw new OperationFailException(
                    "No restore request found"
            );
        }

        chat.setRestoreRequested(false);

        chat.setRestoreRequestedAt(null);

        chatRepository.save(chat);

        for (ChatParticipant participant
                : chat.getParticipants()) {

            notificationService.createSystemNotification(
                    participant.getUser().getId(),
                    admin.getId(),
                    "Your chat restore request was rejected",
                    NotificationType.CHAT_RESTORE_REJECTED
            );

        }

        logger.info(
                "Chat restore request is rejected successfully by admin | chatId: {}",
                chatId
        );

    }

    // ================= MODERATE MESSAGE =================
    @Override
    public void moderateMessage(
            Long messageId
    ) {

        logger.info(
                "Moderating message | messageId: {}",
                messageId
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Message message =
                messageAccessValidator.getActiveMessage(messageId);

        if (Boolean.TRUE.equals(
                message.getDeletedByAdmin()
        )) {

            logger.warn("Message already moderated");

            throw new OperationFailException(
                    "Message already moderated"
            );
        }

        message.setDeletedByAdmin(true);

        message.setDeletedByAdminAt(
                LocalDateTime.now()
        );

        message.setDeletedForEveryone(false);

        message.setRestoreRequested(false);

        message.setRestoreRequestedAt(null);

        for (Message reply : message.getReplies()) {

            reply.setReplyToMessage(null);
        }

        message.setReplyToMessage(null);

        message.setIsEdited(false);

        message.setEditedAt(null);

        messageRepository.save(message);

        notificationService.createSystemNotification(
                message.getSender().getId(),
                admin.getId(),
                "One of your messages was removed by admin",
                NotificationType.MESSAGE_DELETED_BY_ADMIN
        );

        logger.info(
                "Message moderated successfully | messageId: {}",
                messageId
        );
    }

    // ================= APPROVE RESTORE MESSAGE =================
    @Override
    public void approveMessageRestore(
            Long messageId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Message message =
                messageAccessValidator.getMessage(messageId);

        if (!Boolean.TRUE.equals(
                message.getDeletedByAdmin()
        )) {

            logger.warn("Message is not moderated");

            throw new OperationFailException(
                    "Message is not moderated"
            );
        }

        if (!Boolean.TRUE.equals(
                message.getRestoreRequested()
        )) {

            logger.warn("No restore request found for restoring message.");

            throw new OperationFailException(
                    "No restore request found"
            );
        }

        message.setDeletedByAdmin(false);

        message.setDeletedByAdminAt(null);

        message.setRestoreRequested(false);

        message.setRestoreRequestedAt(null);

        messageRepository.save(message);

        notificationService.createSystemNotification(
                message.getSender().getId(),
                admin.getId(),
                "Your message was restored by admin",
                NotificationType.MESSAGE_RESTORED
        );

        logger.info("Your Restore message is approved by admin " +
                "with MessageId: {}",messageId);

    }

    // ================= REJECT RESTORE MESSAGE =================
    @Override
    public void rejectMessageRestore(
            Long messageId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Message message =
                messageAccessValidator.getMessage(messageId);

        if (!Boolean.TRUE.equals(
                message.getRestoreRequested()
        )) {

            logger.warn("No restore request found for rejecting restoring message request.");

            throw new OperationFailException(
                    "No restore request found"
            );
        }

        message.setRestoreRequested(false);

        message.setRestoreRequestedAt(null);

        messageRepository.save(message);

        notificationService.createSystemNotification(
                message.getSender().getId(),
                admin.getId(),
                "Your message restore request was rejected",
                NotificationType.MESSAGE_RESTORE_REJECTED
        );

        logger.info("Your Restore message is rejected by admin " +
                "with MessageId: {}",messageId);

    }

    // ================= DTO MAPPERS =================
    private AdminChatResponse mapToAdminChatResponse(
            Chat chat
    ) {

        List<User> users = chat.getParticipants()
                .stream()
                .map(ChatParticipant::getUser)
                .toList();

        User firstUser = users.get(0);

        User secondUser = users.get(1);

        return AdminChatResponse.builder()
                .chatId(chat.getId())
                .firstUsername(firstUser.getUname())
                .secondUsername(secondUser.getUname())
                .lastMessage(chat.getLastMessage())
                .lastMessageAt(chat.getLastMessageAt())
                .totalMessages(
                        chat.getTotalMessages()
                )
                .deletedByAdmin(
                        chat.getDeletedByAdmin()
                )
                .restoreRequested(
                        chat.getRestoreRequested()
                )
                .reportCount(
                        (long) chat.getReports().size()
                )
                .createdAt(chat.getCreatedAt())
                .build();
    }

    private AdminMessageResponse mapToAdminMessageResponse(
            Message message
    ) {

        return AdminMessageResponse.builder()
                .messageId(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderUsername(
                        message.getSender().getUname()
                )
                .content(message.getContent())
                .mediaFiles(
                        mapMediaFiles(
                                message.getMediaFiles()
                        )
                )
                .messageType(message.getMessageType())
                .isSeen(message.getIsSeen())
                .isEdited(message.getIsEdited())
                .deletedForEveryone(
                        message.getDeletedForEveryone()
                )
                .deletedByAdmin(
                        message.getDeletedByAdmin()
                )
                .restoreRequested(
                        message.getRestoreRequested()
                )
                .reportCount(
                        (long) message.getReports().size()
                )
                .createdAt(message.getCreatedAt())
                .build();
    }

    private List<MessageMediaResponse> mapMediaFiles(
            List<MessageMedia> mediaFiles
    ) {

        if (
                mediaFiles == null
                        || mediaFiles.isEmpty()
        ) {

            return Collections.emptyList();
        }

        return mediaFiles.stream()
                .sorted(
                        Comparator.comparing(
                                MessageMedia::getOrderIndex
                        )
                )
                .map(media ->
                        MessageMediaResponse.builder()
                                .id(media.getId())
                                .mediaUrl(media.getMediaUrl())
                                .mediaType(media.getMediaType())
                                .orderIndex(
                                        media.getOrderIndex()
                                )
                                .build()
                )
                .toList();
    }

}

