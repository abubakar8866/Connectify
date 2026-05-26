package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.ChatSearchRequest;
import com.abubakar.connectify.dto.request.MessageSearchRequest;
import com.abubakar.connectify.dto.response.AdminChatResponse;
import com.abubakar.connectify.dto.response.AdminMessageResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface AdminChatService {

    CursorPageResponse<AdminChatResponse> getChats(
            ChatSearchRequest request,
            Long cursor,
            int size
    );

    CursorPageResponse<AdminMessageResponse> getMessages(
            Long chatId,
            MessageSearchRequest request,
            Long cursor,
            int size
    );

    // MODERATION
    void moderateChat(Long chatId);

    void moderateMessage(Long messageId);

    // RESTORE APPROVAL
    void approveChatRestore(Long chatId);

    void rejectChatRestore(Long chatId);

    void approveMessageRestore(Long messageId);

    void rejectMessageRestore(Long messageId);

    // HARD DELETE
    void permanentlyDeleteChat(Long chatId);

    void permanentlyDeleteMessage(Long messageId);

}

