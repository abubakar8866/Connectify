package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.LikeResponse;

public interface LikeService {

    LikeResponse togglePostLike(Long postId);

    LikeResponse toggleCommentLike(Long commentId);
}
