package com.magazine.backend.repository;

import com.magazine.backend.models.News;
import com.magazine.backend.models.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
    @Query("SELECT rs.news.newsID FROM ReadStatus rs WHERE rs.user_id.id = :userId")
    List<Long> findReadNewsIdsByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReadStatus rs WHERE rs.news.newsID = :newsId")
    void deleteByNewsId(Long newsId);
}
