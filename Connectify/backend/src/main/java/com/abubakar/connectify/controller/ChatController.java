package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.EditMessageRequest;
import com.abubakar.connectify.dto.request.SendMessageRequest;
import com.abubakar.connectify.dto.response.ChatResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.MessageResponse;
import com.abubakar.connectify.service.ChatService;

import com.abubakar.connectify.util.JsonRequestParser;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JsonRequestParser jsonRequestParser;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ChatController.class
            );

    // ================= CREATE CHAT =================
    @PostMapping("/{receiverId}")
    public ResponseEntity<ChatResponse>
    createChat(
            @PathVariable Long receiverId
    ) {

        logger.info(
                "Create chat API request received | receiverId: {}",
                receiverId
        );

        ChatResponse response =
                chatService.createChat(receiverId);

        return ResponseEntity.ok(
                        response
        );
    }

    // ================= GET MY CHATS =================
    @GetMapping
    public ResponseEntity<
                    CursorPageResponse<ChatResponse>
            >
    getMyChats(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        logger.info(
                "Get chats API request received | cursor: {} | size: {}",
                cursor,
                size
        );

        CursorPageResponse<ChatResponse> response =
                chatService.getMyChats(
                        cursor,
                        size
                );

        return ResponseEntity.ok(
                        response
        );
    }

    // ================= SEND MESSAGE =================
    @PostMapping(
            value = "/{chatId}/messages",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MessageResponse>
    sendMessage(

            @PathVariable Long chatId,

            @RequestPart("request")
            String requestJson,

            @RequestPart(
                    value = "media",
                    required = false
            )
            MultipartFile media
    ) {

        logger.info(
                """
                Send message API request received
                | chatId: {}
                | mediaPresent: {}
                """,
                chatId,
                media != null
        );

        SendMessageRequest request =
                jsonRequestParser.parseAndValidate(
                        requestJson,
                        SendMessageRequest.class
                );

        MessageResponse response =
                chatService.sendMessage(
                        chatId,
                        request,
                        media
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ================= GET MESSAGES =================
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<
                    CursorPageResponse<MessageResponse>
            >
    getMessages(

            @PathVariable Long chatId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        logger.info(
                "Get messages API request received | chatId: {} | cursor: {} | size: {}",
                chatId,
                cursor,
                size
        );

        CursorPageResponse<MessageResponse> response =
                chatService.getMessages(
                        chatId,
                        cursor,
                        size
                );

        return ResponseEntity.ok(
                        response
        );
    }

    // ================= MARK AS SEEN =================
    @PutMapping("/{chatId}/seen")
    public ResponseEntity<String>
    markMessagesAsSeen(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Mark messages as seen API request received | chatId: {}",
                chatId
        );

        chatService.markMessagesAsSeen(chatId);

        return ResponseEntity.ok(
                        "Messages marked as seen"
        );
    }

    // ================= EDIT MESSAGE =================
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse>
    editMessage(

            @PathVariable Long messageId,

            @Valid
            @RequestBody
            EditMessageRequest request
    ) {

        logger.info(
                "Edit message API request received | messageId: {}",
                messageId
        );

        MessageResponse response =
                chatService.editMessage(
                        messageId,
                        request
                );

        return ResponseEntity.ok(
                        response
        );
    }

    // ================= DELETE MESSAGE FOR ME =================
    @DeleteMapping("/messages/{messageId}/me")
    public ResponseEntity<String>
    deleteMessageForMe(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Delete message for current user API request received | messageId: {}",
                messageId
        );

        chatService.deleteMessageForMe(messageId);

        return ResponseEntity.ok(
                        "Message deleted for you"
        );
    }

    // ================= DELETE MESSAGE FOR EVERYONE =================
    @DeleteMapping("/messages/{messageId}/everyone")
    public ResponseEntity<String>
    deleteMessageForEveryone(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Delete message for everyone API request received | messageId: {}",
                messageId
        );

        chatService.deleteMessageForEveryone(messageId);

        return ResponseEntity.ok(
                        "Message deleted for everyone"
        );
    }

    @DeleteMapping("/{chatId}/me")
    public ResponseEntity<String> deleteChatForMe(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Delete chat for current user API request received | chatId: {}",
                chatId
        );

        chatService.deleteChatForMe(chatId);

        return ResponseEntity.ok(
                "Chat deleted successfully"
        );
    }

    @PostMapping("/messages/{messageId}/restore-request")
    public ResponseEntity<String> requestRestoreMessage(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Restore message request API received | messageId: {}",
                messageId
        );

        chatService.requestRestoreMessage(
                messageId
        );

        return ResponseEntity.ok(
                "Restore request submitted successfully"
        );
    }

    @PostMapping("/{chatId}/restore-request")
    public ResponseEntity<String> requestRestoreChat(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Restore chat request API received | chatId: {}",
                chatId
        );

        chatService.requestRestoreChat(
                chatId
        );

        return ResponseEntity.ok(
                "Restore request submitted successfully"
        );
    }

}

