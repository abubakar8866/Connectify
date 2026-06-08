package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.MessageRequest;
import com.abubakar.connectify.dto.response.ChatResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {

    ChatResponse createChat(Long receiverId);

    CursorPageResponse<ChatResponse> getMyChats(
            Long cursor,
            int size
    );

    MessageResponse sendMessage(
            Long chatId,
            MessageRequest request,
            List<MultipartFile> mediaFiles
    );

    CursorPageResponse<MessageResponse> getMessages(
            Long chatId,
            Long cursor,
            int size
    );

    void markMessagesAsSeen(Long chatId);

    public MessageResponse editMessage(
            Long messageId,
            MessageRequest request,
            List<MultipartFile> mediaFiles
    );

    void deleteMessageForMe(Long messageId);

    void deleteMessageForEveryone(Long messageId);

    void deleteChatForMe(Long chatId);

    // ================= RESTORE REQUESTS =================

    void requestRestoreMessage(
            Long messageId
    );

    void requestRestoreChat(
            Long chatId
    );

    void restoreChat(
            Long chatId
    );

}

