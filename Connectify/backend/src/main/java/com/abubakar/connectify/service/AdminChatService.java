package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.ChatSearchRequest;
import com.abubakar.connectify.dto.request.MessageSearchRequest;
import com.abubakar.connectify.dto.response.AdminChatResponse;
import com.abubakar.connectify.dto.response.AdminMessageResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface AdminChatService {

    CursorPageResponse<AdminChatResponse> getAllChats(
            Long cursor,
            int size
    );

    CursorPageResponse<AdminMessageResponse> getChatMessages(
            Long chatId,
            Long cursor,
            int size
    );

    CursorPageResponse<AdminChatResponse> searchChats(
            ChatSearchRequest request,
            Long cursor,
            int size
    );

    CursorPageResponse<AdminMessageResponse> searchMessages(
            MessageSearchRequest request,
            Long cursor,
            int size
    );

    void adminDeleteChat(
            Long chatId
    );

    void adminDeleteMessage(
            Long messageId
    );

}

