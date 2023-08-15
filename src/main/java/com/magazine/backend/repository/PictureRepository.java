package com.magazine.backend.repository;

import com.magazine.backend.models.News;
import com.magazine.backend.models.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    Picture findByNews(News news);
}
