package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.EditMessageRequest;
import com.abubakar.connectify.dto.request.SendMessageRequest;
import com.abubakar.connectify.dto.response.*;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.MessageType;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.ChatParticipantRepository;
import com.abubakar.connectify.repository.ChatRepository;
import com.abubakar.connectify.repository.MessageRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.ChatService;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private ChatAccessValidator chatAccessValidator;

    @Autowired
    private MessageAccessValidator messageAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final long ONLINE_THRESHOLD_MINUTES = 2;

    // ================= CREATE CHAT =================
    @Override
    public ChatResponse createChat(
            Long userId
    ) {

        logger.info(
                "Creating or fetching chat | userId: {}",
                userId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Create chat request received | currentUserId: {} | targetUserId: {}",
                currentUser.getId(),
                userId
        );

        updateUserLastSeen(currentUser);

        User otherUser =
                userAccessValidator.getValidUser(userId);

        if (currentUser.getId().equals(otherUser.getId())) {

            logger.warn(
                    "You cannot chat with yourself"
            );

            throw new OperationFailException(
                    "You cannot chat with yourself"
            );
        }

        Chat existingChat =
                chatRepository.findPrivateChatBetweenUsers(
                        currentUser.getId(),
                        otherUser.getId()
                ).orElse(null);

        if (existingChat != null) {

            logger.info(
                    "Existing private chat found | chatId: {} | currentUserId: {} | targetUserId: {}",
                    existingChat.getId(),
                    currentUser.getId(),
                    otherUser.getId()
            );

            return mapToChatResponse(
                    existingChat,
                    currentUser
            );
        }

        Chat chat = Chat.builder()
                .build();

        Chat savedChat =
                chatRepository.save(chat);

        ChatParticipant currentParticipant =
                ChatParticipant.builder()
                        .chat(savedChat)
                        .user(currentUser)
                        .build();

        ChatParticipant otherParticipant =
                ChatParticipant.builder()
                        .chat(savedChat)
                        .user(otherUser)
                        .build();

        chatParticipantRepository.save(currentParticipant);

        chatParticipantRepository.save(otherParticipant);

        // IMPORTANT: KEEP BOTH PARTICIPANTS IN MEMORY
        savedChat.getParticipants()
                .add(currentParticipant);

        savedChat.getParticipants()
                .add(otherParticipant);

        logger.info(
                "Private chat created successfully | chatId: {} | participantOneId: {} | participantTwoId: {}",
                savedChat.getId(),
                currentUser.getId(),
                otherUser.getId()
        );

        return mapToChatResponse(
                savedChat,
                currentUser
        );
    }

    // ================= SEND MESSAGE =================
    @Override
    public MessageResponse sendMessage(
            Long chatId,
            SendMessageRequest request,
            List<MultipartFile> mediaFiles
    ) {

        logger.info(
                "Sending message | chatId: {}",
                chatId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Send message request received | chatId: {} | senderId: {} | messageType: {}",
                chatId,
                currentUser.getId(),
                request.getMessageType()
        );

        Chat chat =
                chatAccessValidator.getActiveChat(chatId);

        validateChatParticipant(
                chat,
                currentUser
        );

        String content =
                request.getContent();

        MessageType messageType =
                request.getMessageType();

        // ================= VALIDATION =================

        if (
                messageType == MessageType.TEXT
                        &&
                        (content == null || content.isBlank())
        ) {

            logger.warn(
                    "Text message content required"
            );

            throw new OperationFailException(
                    "Text message content required"
            );
        }

        if (
                (messageType == MessageType.IMAGE
                        || messageType == MessageType.VIDEO)
                        &&
                        (mediaFiles == null || mediaFiles.isEmpty())
        ) {

            logger.warn(
                    "Media files are required"
            );

            throw new OperationFailException(
                    "Media files are required"
            );
        }

        // ================= REPLY MESSAGE =================

        Message replyMessage = null;

        if (request.getReplyToMessageId() != null) {

            replyMessage =
                    messageAccessValidator.getActiveMessage(
                            request.getReplyToMessageId()
                    );

            if (
                    !replyMessage.getChat()
                            .getId()
                            .equals(chat.getId())
            ) {

                logger.error(
                        "Reply message does not belong to this chat"
                );

                throw new OperationFailException(
                        "Reply message does not belong to this chat"
                );
            }

            if (
                    Boolean.TRUE.equals(
                            replyMessage.getDeletedForEveryone()
                    )
            ) {

                logger.error(
                        "Cannot reply to deleted message"
                );

                throw new OperationFailException(
                        "Cannot reply to deleted message"
                );
            }
        }

        // ================= CREATE MESSAGE =================

        Message message =
                Message.builder()
                        .chat(chat)
                        .sender(currentUser)
                        .content(content)
                        .messageType(messageType)
                        .replyToMessage(replyMessage)
                        .build();

        // ================= MEDIA UPLOAD =================

        if (
                mediaFiles != null
                        &&
                        !mediaFiles.isEmpty()
        ) {

            FileUploadResponse uploadResponse =
                    fileService.uploadMultipleFiles(
                            mediaFiles,
                            currentUser.getId(),
                            null,
                            "messages"
                    );

            List<String> uploadedFiles =
                    uploadResponse.getUploadedFiles();

            if (
                    uploadedFiles == null
                            ||
                            uploadedFiles.isEmpty()
            ) {

                logger.error(
                        "Media upload failed | chatId: {}",
                        chatId
                );

                throw new OperationFailException(
                        "Media upload failed"
                );
            }

            int orderIndex = 0;

            for (String mediaUrl : uploadedFiles) {

                MessageMedia media =
                        MessageMedia.builder()
                                .message(message)
                                .mediaUrl(mediaUrl)
                                .mediaType(messageType)
                                .orderIndex(orderIndex++)
                                .build();

                message.getMediaFiles()
                        .add(media);
            }
        }

        Message savedMessage =
                messageRepository.save(message);

        // ================= UPDATE CHAT =================

        chat.setLastMessage(
                buildLastMessage(
                        messageType,
                        content
                )
        );

        chat.setLastMessageAt(
                LocalDateTime.now()
        );

        chat.setTotalMessages(
                chat.getTotalMessages() == null
                        ? 1L
                        : chat.getTotalMessages() + 1
        );

        chatRepository.save(chat);

        // ================= UPDATE PARTICIPANTS =================

        List<ChatParticipant> participants =
                chatParticipantRepository.findByChat(chat);

        for (ChatParticipant participant : participants) {

            if (Boolean.TRUE.equals(
                    participant.getDeleted()
            )) {

                participant.setDeleted(false);
                participant.setDeletedAt(null);
            }

            if (
                    !participant.getUser()
                            .getId()
                            .equals(currentUser.getId())
            ) {

                participant.setUnreadCount(
                        participant.getUnreadCount() + 1
                );

                notificationService.createNotification(
                        participant.getUser().getId(),
                        currentUser.getId(),
                        buildNotificationMessage(
                                currentUser.getUname(),
                                messageType
                        ),
                        NotificationType.MESSAGE,
                        null,
                        null
                );
            }
        }

        chatParticipantRepository.saveAll(
                participants
        );

        logger.info(
                "Message sent successfully | messageId: {} | chatId: {} | senderId: {} | messageType: {}",
                savedMessage.getId(),
                chatId,
                currentUser.getId(),
                messageType
        );

        return mapToMessageResponse(
                savedMessage,
                currentUser
        );
    }

    // ================= GET MY CHATS =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<ChatResponse> getMyChats(
            Long cursor,
            int size
    ) {

        logger.info("Fetching user chats");

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Fetching chats for user | userId: {} | cursor: {} | size: {}",
                currentUser.getId(),
                cursor,
                size
        );

        updateUserLastSeen(currentUser);

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        List<ChatParticipant> participants;

        List<Long> ids;

        if (cursor == null) {

            ids =
                    chatParticipantRepository
                            .findChatParticipantIds(
                                    currentUser,
                                    pageable
                            );

        } else {

            ids =
                    chatParticipantRepository
                            .findChatParticipantIdsWithCursor(
                                    currentUser,
                                    cursor,
                                    pageable
                            );
        }

        if (ids.isEmpty()) {

            return CursorPageResponse.<ChatResponse>builder()
                    .content(List.of())
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        participants =
                chatParticipantRepository
                        .findChatsWithParticipants(ids);

        logger.debug(
                "Chats fetched successfully | userId: {} | totalChats: {}",
                currentUser.getId(),
                participants.size()
        );

        return CursorPaginationUtil.buildResponse(
                participants,
                size,
                participant ->
                        participant.getChat().getId(),
                participant ->
                        mapToChatResponse(
                                participant.getChat(),
                                currentUser
                        )
        );
    }

    // ================= GET CHAT MESSAGES =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<MessageResponse> getMessages(
            Long chatId,
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching messages | chatId: {}",
                chatId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Fetching chat messages | chatId: {} | userId: {} | cursor: {} | size: {}",
                chatId,
                currentUser.getId(),
                cursor,
                size
        );

        updateUserLastSeen(currentUser);

        Chat chat = chatAccessValidator.getActiveChat(chatId);

        validateChatParticipant(
                chat,
                currentUser
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        List<Message> messages;

        if (cursor == null) {

            messages =
                    messageRepository
                            .findVisibleMessages(
                                    chat,
                                    currentUser.getId(),
                                    pageable
                            );

        } else {

            messages =
                    messageRepository
                            .findVisibleMessagesWithCursor(
                                    chat,
                                    cursor,
                                    currentUser.getId(),
                                    pageable
                            );
        }

        logger.debug(
                "Messages fetched successfully | chatId: {} | fetchedCount: {}",
                chatId,
                messages.size()
        );

        return CursorPaginationUtil.buildResponse(
                messages,
                size,
                Message::getId,
                message ->
                        mapToMessageResponse(
                                message,
                                currentUser
                        )
        );
    }

    // ================= MARK AS SEEN =================
    @Override
    public void markMessagesAsSeen(
            Long chatId
    ) {

        logger.info(
                "Marking messages as seen | chatId: {}",
                chatId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Mark messages as seen request received | chatId: {} | userId: {}",
                chatId,
                currentUser.getId()
        );

        updateUserLastSeen(currentUser);

        Chat chat = chatAccessValidator.getActiveChat(chatId);

        validateChatParticipant(
                chat,
                currentUser
        );

        List<Message> unseenMessages =
                messageRepository
                        .findByChatAndSenderIdNotAndIsSeenFalseAndDeletedByAdminFalse(
                                chat,
                                currentUser.getId()
                        );

        for (Message message : unseenMessages) {

            message.setIsSeen(true);

            message.setSeenAt(
                    LocalDateTime.now()
            );
        }

        messageRepository.saveAll(unseenMessages);

        ChatParticipant participant =
                chatParticipantRepository
                        .findByChatAndUser(
                                chat,
                                currentUser
                        )
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Participant not found"
                                )
                        );

        participant.setUnreadCount(0L);

        participant.setLastSeenAt(
                LocalDateTime.now()
        );

        chatParticipantRepository.save(participant);

        logger.info(
                "Messages marked as seen successfully | chatId: {} | userId: {} | updatedMessages: {}",
                chatId,
                currentUser.getId(),
                unseenMessages.size()
        );
    }

    // ================= EDIT MESSAGE =================
    @Override
    public MessageResponse editMessage(
            Long messageId,
            EditMessageRequest request
    ) {

        logger.info(
                "Editing message | messageId: {}",
                messageId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Edit message request received | messageId: {} | userId: {}",
                messageId,
                currentUser.getId()
        );

        Message message = messageAccessValidator.getActiveMessage(messageId);

        if (
                !message.getSender()
                        .getId()
                        .equals(currentUser.getId())
        ) {

            logger.error(
                    "You can edit only your own messages"
            );

            throw new OperationFailException(
                    "You can edit only your own messages"
            );
        }

        if (message.getMessageType() != MessageType.TEXT) {

            logger.error(
                    "Only text messages can be edited"
            );

            throw new OperationFailException(
                    "Only text messages can be edited"
            );
        }

        if (Boolean.TRUE.equals(
                message.getDeletedForEveryone()
        )) {

            logger.error(
                    "Deleted message cannot be edited"
            );

            throw new OperationFailException(
                    "Deleted message cannot be edited"
            );
        }

        message.setContent(request.getContent());

        message.setIsEdited(true);

        message.setEditedAt(
                LocalDateTime.now()
        );

        Message updatedMessage =
                messageRepository.save(message);

        logger.info(
                "Message edited successfully | messageId: {} | userId: {}",
                messageId,
                currentUser.getId()
        );

        return mapToMessageResponse(
                updatedMessage,
                currentUser
        );
    }

    // ================= DELETE MESSAGE FOR ME =================
    @Override
    public void deleteMessageForMe(
            Long messageId
    ) {

        logger.info(
                "Deleting message for me | messageId: {}",
                messageId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Delete message for self request received | messageId: {} | userId: {}",
                messageId,
                currentUser.getId()
        );

        Message message = messageAccessValidator.getActiveMessage(messageId);

        validateChatParticipant(
                message.getChat(),
                currentUser
        );

        // ALREADY DELETED
        boolean alreadyDeleted =
                message.getDeletedForUsers()
                        .stream()
                        .anyMatch(user ->
                                user.getId()
                                        .equals(currentUser.getId())
                        );

        if (alreadyDeleted) {

            logger.warn(
                    "Message already deleted for user | messageId: {} | userId: {}",
                    messageId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Message already deleted for you"
            );
        }

        message.getDeletedForUsers()
                .add(currentUser);

        messageRepository.save(message);

        logger.info(
                "Message deleted for current user successfully | messageId: {} | userId: {}",
                messageId,
                currentUser.getId()
        );
    }

    // ================= DELETE MESSAGE FOR EVERYONE =================
    @Override
    public void deleteMessageForEveryone(
            Long messageId
    ) {

        logger.info(
                "Deleting message for everyone | messageId: {}",
                messageId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Delete message for everyone request received | messageId: {} | senderId: {}",
                messageId,
                currentUser.getId()
        );

        Message message = messageAccessValidator.getActiveMessage(messageId);

        // ONLY SENDER CAN DELETE FOR EVERYONE
        if (
                !message.getSender()
                        .getId()
                        .equals(currentUser.getId())
        ) {

            logger.warn(
                    "You can delete only your own messages attempt | messageId: {} | requestedBy: {}",
                    messageId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "You can delete only your own messages"
            );
        }

        if (Boolean.TRUE.equals(
                message.getDeletedForEveryone()
        )) {

            logger.warn(
                    "Unauthorized delete for everyone attempt | messageId: {} | requestedBy: {}",
                    messageId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Message already deleted"
            );
        }

        message.setDeletedForEveryone(true);

        message.setContent(
                "This message was deleted"
        );

        // CLEAR CHILD REPLIES
        for (Message reply : message.getReplies()) {

            reply.setReplyToMessage(null);
        }

        // CLEAR PARENT REPLY
        message.setReplyToMessage(null);

        message.setIsEdited(false);
        message.setEditedAt(null);

        messageRepository.save(message);

        logger.info(
                "Message deleted for everyone successfully | messageId: {} | senderId: {}",
                messageId,
                currentUser.getId()
        );
    }

    // ================= DELETE CHAT FOR ME =================
    @Override
    public void deleteChatForMe(
            Long chatId
    ) {

        logger.info(
                "Deleting chat for current user | chatId: {}",
                chatId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Chat chat = chatAccessValidator.getChat(chatId);

        ChatParticipant participant =
                chatParticipantRepository
                        .findByChatAndUser(
                                chat,
                                currentUser
                        )
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Participant not found"
                                )
                        );

        if (Boolean.TRUE.equals(
                participant.getDeleted()
        )) {

            throw new OperationFailException(
                    "Chat already deleted for you"
            );
        }

        participant.setDeleted(true);

        participant.setDeletedAt(
                LocalDateTime.now()
        );

        participant.setUnreadCount(0L);

        chatParticipantRepository.save(participant);

        logger.info(
                "Chat deleted for current user successfully | chatId: {} | userId: {}",
                chatId,
                currentUser.getId()
        );

    }

    // ================= REQUEST RESTORE MESSAGE =================
    @Override
    public void requestRestoreMessage(
            Long messageId
    ) {

        logger.info(
                "Requesting message restore | messageId: {}",
                messageId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Message restore request received | messageId: {} | userId: {}",
                messageId,
                currentUser.getId()
        );

        Message message = messageAccessValidator.getMessage(messageId);

        if (Boolean.TRUE.equals(
                message.getDeletedForEveryone()
        )) {

            logger.warn(
                    "Deleted message restore request detected | messageId: {} | userId: {}",
                    messageId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Deleted message cannot be restored"
            );
        }

        // MESSAGE MUST BE ADMIN DELETED
        if (!Boolean.TRUE.equals(
                message.getDeletedByAdmin()
        )) {

            logger.warn(
                    "This message was not deleted by admin request detected | messageId: {} | role: {}",
                    messageId,
                    currentUser.getRole()
            );

            throw new OperationFailException(
                    "This message was not deleted by admin"
            );
        }

        // ONLY SENDER CAN REQUEST
        if (
                !message.getSender()
                        .getId()
                        .equals(currentUser.getId())
        ) {

            logger.warn(
                    "request restore only for your own message request detected | messageId: {} | userId: {}",
                    messageId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "You can request restore only for your own message"
            );
        }

        // PREVENT DUPLICATE REQUEST
        if (Boolean.TRUE.equals(
                message.getRestoreRequested()
        )) {

            logger.warn(
                    "Duplicate message restore request detected | messageId: {} | userId: {}",
                    messageId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Restore request already submitted"
            );
        }

        message.setRestoreRequested(true);

        message.setRestoreRequestedAt(
                LocalDateTime.now()
        );

        messageRepository.save(message);

        // NOTIFY ADMIN
        userRepository.findByRole(Role.ADMIN)
                .ifPresent(admin ->

                        notificationService.createNotification(
                                admin.getId(),
                                currentUser.getId(),
                                currentUser.getUname() +
                                " requested message restoration",
                                NotificationType.RESTORE_REQUEST,
                                null,
                                message.getId()
                        )
                );

        logger.info(
                "Message restore request submitted successfully | messageId: {} | userId: {}",
                messageId,
                currentUser.getId()
        );
    }

    // ================= REQUEST RESTORE CHAT =================
    @Override
    public void requestRestoreChat(
            Long chatId
    ) {

        logger.info(
                "Requesting chat restore | chatId: {}",
                chatId
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Chat restore request received | chatId: {} | userId: {}",
                chatId,
                currentUser.getId()
        );

        Chat chat = chatAccessValidator.getChat(chatId);

        // CHAT MUST BE ADMIN DELETED
        if (!Boolean.TRUE.equals(
                chat.getDeletedByAdmin()
        )) {

            logger.warn(
                    "This chat was not deleted by admin request detected | chatId: {} | role: {}",
                    chat.getId(),
                    currentUser.getRole()
            );

            throw new OperationFailException(
                    "This chat was not deleted by admin"
            );
        }

        // USER MUST BELONG TO CHAT
        validateChatParticipantWithoutAdminCheck(
                chat,
                currentUser
        );

        // PREVENT DUPLICATE REQUEST
        if (Boolean.TRUE.equals(
                chat.getRestoreRequested()
        )) {

            logger.warn(
                    "Restore request already submitted request detected | chatId: {} | chat status: {}",
                    chat.getId(),
                    chat.getRestoreRequested()
            );

            throw new OperationFailException(
                    "Restore request already submitted"
            );
        }

        chat.setRestoreRequested(true);

        chat.setRestoreRequestedAt(
                LocalDateTime.now()
        );

        chatRepository.save(chat);

        // NOTIFY ADMIN
        userRepository.findByRole(Role.ADMIN)
                .ifPresent(admin ->

                        notificationService.createNotification(
                                admin.getId(),
                                currentUser.getId(),
                                currentUser.getUname() +
                                " requested chat restoration",
                                NotificationType.RESTORE_REQUEST,
                                chat.getId(),
                                null
                        )
                );

        logger.info(
                "Chat restore request submitted successfully | chatId: {} | userId: {}",
                chatId,
                currentUser.getId()
        );
    }

    // ================= PRIVATE METHODS =================
    private void validateChatParticipant(
            Chat chat,
            User currentUser
    ) {

        if (Boolean.TRUE.equals(chat.getDeletedByAdmin())) {
            throw new ResourceNotFound("Chat not found");
        }

        boolean exists =
                chatParticipantRepository
                        .existsByChatAndUser(
                                chat,
                                currentUser
                        );

        if (!exists) {

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }
    }

    private void updateUserLastSeen(
            User user
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        if (
                user.getLastSeenAt() == null
                        ||
                        user.getLastSeenAt()
                                .isBefore(now.minusMinutes(1))
        ) {

            user.setLastSeenAt(now);

            userRepository.save(user);
        }
    }

    private boolean isUserOnline(
            User user
    ) {

        if (user.getLastSeenAt() == null) {
            return false;
        }

        return user.getLastSeenAt()
                .isAfter(
                        LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES)
                );
    }

    private String buildNotificationMessage(
            String username,
            MessageType messageType
    ) {

        return switch (messageType) {

            case IMAGE ->
                    username + " sent you an image";

            case VIDEO ->
                    username + " sent you a video";

            default ->
                    username + " sent you a message";
        };
    }

    private String buildLastMessage(
            MessageType messageType,
            String content
    ) {

        return switch (messageType) {

            case TEXT ->
                    content;

            case IMAGE ->
                    "Image";

            case VIDEO ->
                    "Video";
        };
    }

    private ChatResponse mapToChatResponse(
            Chat chat,
            User currentUser
    ) {

        logger.info(
                "Participants loaded: {}",
                chat.getParticipants().size()
        );

        ChatParticipant currentParticipant =
                chat.getParticipants()
                        .stream()
                        .filter(participant ->
                                participant.getUser()
                                        .getId()
                                        .equals(currentUser.getId())
                        )
                        .findFirst()
                        .orElse(null);

        ChatParticipant otherParticipant =
                chat.getParticipants()
                        .stream()
                        .filter(participant ->
                                !participant.getUser()
                                        .getId()
                                        .equals(currentUser.getId())
                        )
                        .findFirst()
                        .orElse(null);

        if (otherParticipant == null) {

            throw new ResourceNotFound(
                    "Other participant not found"
            );
        }

        User otherUser =
                otherParticipant.getUser();

        boolean online =
                isUserOnline(otherUser);

        return ChatResponse.builder()
                .chatId(chat.getId())
                .otherUserId(otherUser.getId())
                .username(otherUser.getUname())
                .profileImageUrl(
                        otherUser.getProfileImageUrl()
                )
                .isVerified(
                        otherUser.getIsVerified()
                )
                .lastMessage(chat.getLastMessage())
                .lastMessageAt(chat.getLastMessageAt())
                .isOnline(online)
                .lastSeenAt(
                        online
                                ? null
                                : otherUser.getLastSeenAt()
                )
                .unreadCount(
                        Objects.requireNonNull(currentParticipant).getUnreadCount()
                )
                .build();
    }

    private MessageResponse mapToMessageResponse(
            Message message,
            User currentUser
    ) {

        boolean deletedForMe =
                messageRepository.isMessageDeletedForUser(
                        message.getId(),
                        currentUser.getId()
                );

        MessageResponse.MessageResponseBuilder builder =
                MessageResponse.builder()

                        .id(message.getId())

                        .messageType(
                                message.getMessageType()
                        )

                        .senderId(
                                message.getSender().getId()
                        )

                        .senderUsername(
                                message.getSender().getUname()
                        )

                        .senderProfileImage(
                                message.getSender()
                                        .getProfileImageUrl()
                        )

                        .isSeen(
                                message.getIsSeen()
                        )

                        .seenAt(
                                message.getSeenAt()
                        )

                        .isDeletedForMe(
                                deletedForMe
                        )

                        .deletedForEveryone(
                                message.getDeletedForEveryone()
                        )

                        .createdAt(
                                message.getCreatedAt()
                        );

        // MESSAGE DELETED FOR EVERYONE
        if (
                Boolean.TRUE.equals(
                        message.getDeletedForEveryone()
                )
        ) {

            return builder
                    .content(
                            "This message was deleted"
                    )
                    .mediaFiles(
                            Collections.emptyList()
                    )
                    .isEdited(false)
                    .editedAt(null)
                    .replyMessageId(null)
                    .replyMessageContent(null)
                    .replySenderUsername(null)
                    .replyMessageType(null)
                    .replyMediaFiles(
                            Collections.emptyList()
                    )
                    .build();
        }

        Message replyMessage =
                message.getReplyToMessage();

        String replyContent = null;

        if (replyMessage != null) {

            replyContent =
                    Boolean.TRUE.equals(
                            replyMessage.getDeletedForEveryone()
                    )
                            ? "This message was deleted"
                            : replyMessage.getContent();
        }

        return builder

                .content(
                        message.getContent()
                )

                .mediaFiles(
                        mapMediaFiles(
                                message.getMediaFiles()
                        )
                )

                .isEdited(
                        message.getIsEdited()
                )

                .editedAt(
                        message.getEditedAt()
                )

                .replyMessageId(
                        replyMessage != null
                                ? replyMessage.getId()
                                : null
                )

                .replyMessageContent(
                        replyContent
                )

                .replySenderUsername(
                        replyMessage != null
                                ? replyMessage
                                .getSender()
                                .getUname()
                                : null
                )

                .replyMessageType(
                        replyMessage != null
                                ? replyMessage.getMessageType()
                                : null
                )

                .replyMediaFiles(
                        replyMessage != null
                                ? mapMediaFiles(
                                replyMessage.getMediaFiles()
                        )
                                : Collections.emptyList()
                )

                .build();
    }

    private void validateChatParticipantWithoutAdminCheck(
            Chat chat,
            User currentUser
    ) {

        boolean exists =
                chatParticipantRepository
                        .existsByChatAndUser(
                                chat,
                                currentUser
                        );

        if (!exists) {

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }
    }

    private List<MessageMediaResponse> mapMediaFiles(
            List<MessageMedia> mediaFiles
    ) {

        if (
                mediaFiles == null
                        ||
                        mediaFiles.isEmpty()
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
                                .mediaUrl(
                                        media.getMediaUrl()
                                )
                                .mediaType(
                                        media.getMediaType()
                                )
                                .orderIndex(
                                        media.getOrderIndex()
                                )
                                .build()
                )
                .toList();
    }

}

