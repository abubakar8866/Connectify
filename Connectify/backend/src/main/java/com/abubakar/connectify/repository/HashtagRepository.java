package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Hashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    List<Hashtag>
    findByNameContainingIgnoreCaseOrderByIdDesc(
            String keyword,
            Pageable pageable
    );

    List<Hashtag>
    findByNameContainingIgnoreCaseAndIdLessThanOrderByIdDesc(
            String keyword,
            Long cursor,
            Pageable pageable
    );

}

