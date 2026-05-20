package com.abubakar.connectify.service.impl;

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
import com.abubakar.connectify.entity.Chat;
import com.abubakar.connectify.entity.ChatParticipant;
import com.abubakar.connectify.entity.Message;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.ChatRepository;
import com.abubakar.connectify.repository.MessageRepository;
import com.abubakar.connectify.util.AdminValidator;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
import com.abubakar.connectify.util.PaginationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    private static final Logger logger =
            LoggerFactory.getLogger(AdminChatServiceImpl.class);

    // ================= GET ALL CHATS =================
    @Override
    public CursorPageResponse<AdminChatResponse> getAllChats(
            Long cursor,
            int size
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        List<Chat> chats;

        if (cursor == null) {

            chats =
                    chatRepository.findAllByOrderByIdDesc(
                            pageable
                    );

        } else {

            chats =
                    chatRepository.findByIdLessThanOrderByIdDesc(
                    cursor,
                    pageable
            );
        }

        return CursorPaginationUtil.buildResponse(
                chats,
                size,
                Chat::getId,
                this::mapToAdminChatResponse
        );
    }

    // ================= GET CHAT MESSAGES =================
    @Override
    public CursorPageResponse<AdminMessageResponse> getChatMessages(
            Long chatId,
            Long cursor,
            int size
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Chat chat =
                getChat(chatId);

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        List<Message> messages;

        if (cursor == null) {

            messages =
                    messageRepository
                            .findByChatOrderByIdDesc(
                                    chat,
                                    pageable
                            );

        } else {

            messages =
                    messageRepository
                            .findByChatAndIdLessThanOrderByIdDesc(
                                    chat,
                                    cursor,
                                    pageable
                            );
        }

        return CursorPaginationUtil.buildResponse(
                messages,
                size,
                Message::getId,
                this::mapToAdminMessageResponse
        );
    }

    // ================= SEARCH CHATS =================
    @Override
    public CursorPageResponse<AdminChatResponse> searchChats(
            ChatSearchRequest request,
            Long cursor,
            int size
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        Specification<Chat> specification =
                ChatSpecification.searchChats(
                        request.getKeyword(),
                        request.getDeletedByAdmin(),
                        cursor
                );

        List<Chat> chats =
                chatRepository.findAll(
                        specification,
                        pageable
                ).getContent();

        return CursorPaginationUtil.buildResponse(
                chats,
                size,
                Chat::getId,
                this::mapToAdminChatResponse
        );
    }

    // ================= SEARCH MESSAGES =================
    @Override
    public CursorPageResponse<AdminMessageResponse> searchMessages(
            MessageSearchRequest request,
            Long cursor,
            int size
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        Specification<Message> specification =
                MessageSpecification.searchMessages(
                        request.getKeyword(),
                        request.getUsername(),
                        request.getMessageType(),
                        request.getDeletedByAdmin(),
                        cursor
                );

        List<Message> messages =
                messageRepository.findAll(
                        specification,
                        pageable
                ).getContent();

        return CursorPaginationUtil.buildResponse(
                messages,
                size,
                Message::getId,
                this::mapToAdminMessageResponse
        );
    }

    // ================= DELETE CHAT =================
    @Override
    public void adminDeleteChat(
            Long chatId
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Chat chat =
                getChat(chatId);

        chat.setDeletedByAdmin(true);

        chat.setDeletedByAdminAt(
                LocalDateTime.now()
        );

        chat.setIsActive(false);

        // RESET RESTORE REQUEST STATE
        chat.setRestoreRequested(false);

        chat.setRestoreRequestedAt(null);

        chatRepository.save(chat);

        // NOTIFY PARTICIPANTS
        for (ChatParticipant participant
                : chat.getParticipants()) {

            notificationService.createNotification(

                    participant.getUser().getId(),

                    admin.getId(),

                    "A chat was removed by admin",

                    NotificationType.CHAT_DELETED_BY_ADMIN,

                    null,

                    null
            );
        }

        logger.info(
                "Chat deleted by admin successfully | chatId: {}",
                chatId
        );

    }

    // ================= DELETE MESSAGE =================
    @Override
    public void adminDeleteMessage(
            Long messageId
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Message message =
                getMessage(messageId);

        message.setDeletedByAdmin(true);

        message.setDeletedByAdminAt(
                LocalDateTime.now()
        );

        // ADMIN MODERATION OVERRIDES USER DELETE
        message.setDeletedForEveryone(false);

        message.setContent(
                "Message removed by admin"
        );

        message.setMediaUrl(null);

        // RESET RESTORE REQUEST STATE
        message.setRestoreRequested(false);

        message.setRestoreRequestedAt(null);

        // CLEAR CHILD REPLIES
        for (Message reply : message.getReplies()) {

            reply.setReplyToMessage(null);
        }

        // CLEAR PARENT REPLY
        message.setReplyToMessage(null);

        messageRepository.save(message);

        // NOTIFY SENDER
        notificationService.createNotification(

                message.getSender().getId(),

                admin.getId(),

                "One of your messages was removed by admin",

                NotificationType.MESSAGE_DELETED_BY_ADMIN,

                null,

                null
        );

        logger.info(
                "Message deleted by admin successfully | messageId: {}",
                messageId
        );

    }

    // ================= REUSABLE METHODS =================
    private Chat getChat(
            Long chatId
    ) {

        return chatRepository.findById(chatId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Chat not found with id: " + chatId
                        )
                );
    }

    private Message getMessage(
            Long messageId
    ) {

        return messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Message not found with id: " + messageId
                        )
                );
    }

    // ================= DTO MAPPERS =================
    private AdminChatResponse mapToAdminChatResponse(
            Chat chat
    ) {

        List<ChatParticipant> participants =
                chat.getParticipants();

        if (participants.size() < 2) {

            throw new OperationFailException(
                    "Invalid chat participants"
            );
        }

        User firstUser =
                participants.get(0).getUser();

        User secondUser =
                participants.get(1).getUser();

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
                .mediaUrl(message.getMediaUrl())
                .messageType(message.getMessageType())
                .isSeen(message.getIsSeen())
                .isEdited(message.getIsEdited())
                .deletedForEveryone(
                        message.getDeletedForEveryone()
                )
                .deletedByAdmin(
                        message.getDeletedByAdmin()
                )
                .createdAt(message.getCreatedAt())
                .build();
    }

}

