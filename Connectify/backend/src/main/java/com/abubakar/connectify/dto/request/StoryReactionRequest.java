package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoryReactionRequest {

    @NotNull(message = "Reaction type required")
    private ReactionType reactionType;

}

