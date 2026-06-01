package com.abubakar.connectify.dto.internal;

import com.abubakar.connectify.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public class CommentMappingData {

    private Map<Long, List<Comment>> repliesMap;

    private Set<Long> likedCommentIds;

}

