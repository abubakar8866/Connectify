package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Message;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.MessageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageAccessValidator {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private ChatAccessValidator chatAccessValidator;

    private static final Logger logger = LoggerFactory.getLogger(
                    MessageAccessValidator.class
            );

    // ================= USER APIs =================
    public Message getActiveMessage(
            Long messageId
    ) {

        logger.debug(
                "Validating active message access | messageId: {}",
                messageId
        );

        Message message =
                getMessage(messageId);

        // CHAT VALIDATION
        chatAccessValidator.getActiveChat(message.getChat().getId());

        // SENDER VALIDATION
        userAccessValidator.validateActiveUser(message.getSender());

        // ADMIN DELETED
        if (Boolean.TRUE.equals(message.getDeletedByAdmin())) {

            logger.warn(
                    "Message access denied | admin deleted | messageId: {}",
                    messageId
            );

            throw new OperationFailException(
                    "Message removed by admin"
            );
        }

        logger.debug(
                "Active message validation successful | messageId: {}",
                messageId
        );

        return message;
    }

    // ================= ADMIN APIs =================
    public Message getMessage(
            Long messageId
    ) {

        logger.debug(
                "Fetching message | messageId: {}",
                messageId
        );

        return messageRepository.findById(messageId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Message not found | messageId: {}",
                            messageId
                    );

                    return new ResourceNotFound(
                            "Message not found with id: " + messageId
                    );
                });
    }

}

