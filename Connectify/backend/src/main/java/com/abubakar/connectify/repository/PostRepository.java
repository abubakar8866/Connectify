package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>,
        JpaSpecificationExecutor<Post> {

    List<Post> findAllByOrderByIdDesc(
            Pageable pageable
    );

    List<Post> findByIdLessThanOrderByIdDesc(
            Long cursor,
            Pageable pageable
    );

    List<Post>
    findByDeletedFalseOrderByLikeCountDescCommentCountDescIdDesc(
            Pageable pageable
    );

    List<Post>
    findByDeletedFalseAndIdLessThanOrderByLikeCountDescCommentCountDescIdDesc(
            Long cursor,
            Pageable pageable
    );

    Long countByCreatedAtAfter(LocalDateTime time);

    Long countByDeletedTrue();

    Long countByUser(User user);

}

