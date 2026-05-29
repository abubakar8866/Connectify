package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.ChatSearchRequest;
import com.abubakar.connectify.dto.request.MessageSearchRequest;
import com.abubakar.connectify.dto.response.AdminChatResponse;
import com.abubakar.connectify.dto.response.AdminMessageResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.AdminChatService;
import com.abubakar.connectify.util.PaginationConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/chats")
public class AdminChatController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminChatController.class
            );

    @Autowired
    private AdminChatService adminChatService;

    // ================= GET CHATS =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminChatResponse>
            > getChats(

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
                """
                Admin get chats API called
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

        return ResponseEntity.ok(

                adminChatService.getChats(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= GET MESSAGES =================
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<
            CursorPageResponse<AdminMessageResponse>
            > getMessages(

            @PathVariable
            Long chatId,

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
                """
                Admin get messages API called
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

        return ResponseEntity.ok(

                adminChatService.getMessages(
                        chatId,
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= MODERATE CHAT =================
    @PatchMapping("/{chatId}/moderate")
    public ResponseEntity<String> moderateChat(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Admin moderate chat API called | chatId: {}",
                chatId
        );

        adminChatService.moderateChat(
                chatId
        );

        return ResponseEntity.ok(
                "Chat moderated successfully"
        );
    }

    // ================= MODERATE MESSAGE =================
    @PatchMapping("/messages/{messageId}/moderate")
    public ResponseEntity<String> moderateMessage(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Admin moderate message API called | messageId: {}",
                messageId
        );

        adminChatService.moderateMessage(
                messageId
        );

        return ResponseEntity.ok(
                "Message moderated successfully"
        );
    }

    // ================= APPROVE CHAT RESTORE =================
    @PatchMapping("/{chatId}/restore/approve")
    public ResponseEntity<String> approveChatRestore(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Admin approve chat restore API called | chatId: {}",
                chatId
        );

        adminChatService.approveChatRestore(
                chatId
        );

        return ResponseEntity.ok(
                "Chat restore approved successfully"
        );
    }

    // ================= REJECT CHAT RESTORE =================
    @PatchMapping("/{chatId}/restore/reject")
    public ResponseEntity<String> rejectChatRestore(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Admin reject chat restore API called | chatId: {}",
                chatId
        );

        adminChatService.rejectChatRestore(
                chatId
        );

        return ResponseEntity.ok(
                "Chat restore rejected successfully"
        );
    }

    // ================= APPROVE MESSAGE RESTORE =================
    @PatchMapping("/messages/{messageId}/restore/approve")
    public ResponseEntity<String> approveMessageRestore(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Admin approve message restore API called | messageId: {}",
                messageId
        );

        adminChatService.approveMessageRestore(
                messageId
        );

        return ResponseEntity.ok(
                "Message restore approved successfully"
        );
    }

    // ================= REJECT MESSAGE RESTORE =================
    @PatchMapping("/messages/{messageId}/restore/reject")
    public ResponseEntity<String> rejectMessageRestore(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Admin reject message restore API called | messageId: {}",
                messageId
        );

        adminChatService.rejectMessageRestore(
                messageId
        );

        return ResponseEntity.ok(
                "Message restore rejected successfully"
        );
    }

    // ================= HARD DELETE CHAT =================
    @DeleteMapping("/{chatId}/permanent")
    public ResponseEntity<String> permanentlyDeleteChat(
            @PathVariable Long chatId
    ) {

        logger.info(
                "Admin permanently delete chat API called | chatId: {}",
                chatId
        );

        adminChatService.permanentlyDeleteChat(
                chatId
        );

        return ResponseEntity.ok(
                "Chat permanently deleted successfully"
        );
    }

    // ================= HARD DELETE MESSAGE =================
    @DeleteMapping("/messages/{messageId}/permanent")
    public ResponseEntity<String> permanentlyDeleteMessage(
            @PathVariable Long messageId
    ) {

        logger.info(
                "Admin permanently delete message API called | messageId: {}",
                messageId
        );

        adminChatService.permanentlyDeleteMessage(
                messageId
        );

        return ResponseEntity.ok(
                "Message permanently deleted successfully"
        );
    }

}

