package com.magazine.backend.repository;

import com.magazine.backend.models.News;
import com.magazine.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByAuthor(User author);

    @Query("SELECT n FROM News n WHERE n.published = true ORDER BY n.creationDate DESC")
    List<News> findLast10News();
    @Query("SELECT n FROM News n WHERE n.newsID NOT IN :readNewsIds and n.published = true ORDER BY n.creationDate DESC")
    List<News> findLast10UnreadNews(List<Long> readNewsIds);

    News findByAuthorAndNewsID(User author, Long Id);
}
