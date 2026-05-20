package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.EditMessageRequest;
import com.abubakar.connectify.dto.request.SendMessageRequest;
import com.abubakar.connectify.dto.response.ChatResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ChatService {

    ChatResponse createChat(Long receiverId);

    CursorPageResponse<ChatResponse> getMyChats(
            Long cursor,
            int size
    );

    MessageResponse sendMessage(
            Long chatId,
            SendMessageRequest request,
            MultipartFile media
    );

    CursorPageResponse<MessageResponse> getMessages(
            Long chatId,
            Long cursor,
            int size
    );

    void markMessagesAsSeen(Long chatId);

    MessageResponse editMessage(
            Long messageId,
            EditMessageRequest request
    );

    void deleteMessageForMe(Long messageId);

    void deleteMessageForEveryone(Long messageId);

    // ================= RESTORE REQUESTS =================

    void requestRestoreMessage(
            Long messageId
    );

    void requestRestoreChat(
            Long chatId
    );

}

