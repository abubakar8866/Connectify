package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Chat;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.ChatRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatAccessValidator {

    @Autowired
    private ChatRepository chatRepository;

    private static final Logger logger = LoggerFactory.getLogger(
                    ChatAccessValidator.class
            );

    // ================= USER APIs =================
    public Chat getActiveChat(
            Long chatId
    ) {

        logger.debug(
                "Validating active chat access | chatId: {}",
                chatId
        );

        Chat chat = getChat(chatId);

        if (Boolean.TRUE.equals(chat.getDeletedByAdmin())) {

            logger.warn(
                    "Chat access denied | admin deleted | chatId: {}",
                    chatId
            );

            throw new OperationFailException(
                    "Chat is unavailable"
            );
        }

        if (Boolean.FALSE.equals(chat.getIsActive())) {

            logger.warn(
                    "Chat access denied | inactive chat | chatId: {}",
                    chatId
            );

            throw new OperationFailException(
                    "Chat is inactive"
            );
        }

        logger.debug(
                "Active chat validation successful | chatId: {}",
                chatId
        );

        return chat;
    }

    // ================= ADMIN APIs =================
    public Chat getChat(
            Long chatId
    ) {

        logger.debug(
                "Fetching chat | chatId: {}",
                chatId
        );

        return chatRepository.findById(chatId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Chat not found | chatId: {}",
                            chatId
                    );

                    return new ResourceNotFound(
                            "Chat not found with id: " + chatId
                    );
                });
    }

}

