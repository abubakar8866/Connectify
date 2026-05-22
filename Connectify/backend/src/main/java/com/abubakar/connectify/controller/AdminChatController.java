package com.abubakar.connectify.controller;

import com.abubakar.connectify.service.AdminChatService;
import com.abubakar.connectify.dto.request.ChatSearchRequest;
import com.abubakar.connectify.dto.request.MessageSearchRequest;
import com.abubakar.connectify.dto.response.AdminChatResponse;
import com.abubakar.connectify.dto.response.AdminMessageResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

import com.abubakar.connectify.util.PaginationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/chats")
public class AdminChatController {

    @Autowired
    private AdminChatService adminChatService;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminChatController.class
            );

    // ================= GET ALL CHATS =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminChatResponse>
            > getAllChats(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Admin get all chats API called | cursor: {} | size: {}",
                cursor,
                size
        );

        return ResponseEntity.ok(

                adminChatService.getAllChats(
                        cursor,
                        size
                )
        );
    }

    // ================= GET CHAT MESSAGES =================
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<
            CursorPageResponse<AdminMessageResponse>
            > getChatMessages(

            @PathVariable Long chatId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Admin get chat messages API called | chatId: {} | cursor: {} | size: {}",
                chatId,
                cursor,
                size
        );


        return ResponseEntity.ok(

                adminChatService.getChatMessages(
                        chatId,
                        cursor,
                        size
                )
        );
    }

    // ================= SEARCH CHATS =================
    @PostMapping("/search")
    public ResponseEntity<
            CursorPageResponse<AdminChatResponse>
            > searchChats(

            @RequestBody
            ChatSearchRequest request,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Admin search chats API called | keyword: {} | deletedByAdmin: {} | cursor: {} | size: {}",
                request.getKeyword(),
                request.getDeletedByAdmin(),
                cursor,
                size
        );

        return ResponseEntity.ok(

                adminChatService.searchChats(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= SEARCH MESSAGES =================
    @PostMapping("/messages/search")
    public ResponseEntity<
            CursorPageResponse<AdminMessageResponse>
            > searchMessages(

            @RequestBody
            MessageSearchRequest request,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Admin search messages API called | keyword: {} | username: {} | messageType: {} | deletedByAdmin: {} | cursor: {} | size: {}",
                request.getKeyword(),
                request.getUsername(),
                request.getMessageType(),
                request.getDeletedByAdmin(),
                cursor,
                size
        );

        return ResponseEntity.ok(

                adminChatService.searchMessages(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= DELETE CHAT =================
    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> adminDeleteChat(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Admin delete chat API called | chatId: {}",
                chatId
        );

        adminChatService.adminDeleteChat(chatId);

        return ResponseEntity.ok(
                "Chat deleted successfully"
        );
    }

    // ================= DELETE MESSAGE =================
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<String> adminDeleteMessage(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Admin delete message API called | messageId: {}",
                messageId
        );

        adminChatService.adminDeleteMessage(messageId);

        return ResponseEntity.ok(
                "Message deleted successfully"
        );
    }

}

