package com.abubakar.connectify.util;

import com.abubakar.connectify.exception.InvalidJsonException;
import com.abubakar.connectify.exception.ValidationFailException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JsonRequestParser {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    private static final Logger logger =
            LoggerFactory.getLogger(JsonRequestParser.class);

    public <T> T parseAndValidate(
            String json,
            Class<T> clazz
    )  {

        try {

            T object =
                    objectMapper.readValue(
                            json,
                            clazz
                    );

            Set<ConstraintViolation<T>> violations =
                    validator.validate(object);

            if (!violations.isEmpty()) {

                throw new ValidationFailException(
                        violations.iterator()
                                .next()
                                .getMessage()
                );
            }

            return object;

        } catch (JsonProcessingException ex) {

            logger.error(
                    "JSON parsing failed for class: {}",
                    clazz.getSimpleName(),
                    ex
            );

            throw new InvalidJsonException(
                    ex.getMessage()
            );
        }

    }

}

