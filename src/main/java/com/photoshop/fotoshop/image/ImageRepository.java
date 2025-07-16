package com.photoshop.fotoshop.image;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ImageRepository extends JpaRepository<Image, Long> {
    Page<Image> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Image findById(long id);

    List<Image> findTop4ByIdNotOrderByCreatedAtDesc(Long id);

    List<Image> findTop3ByFeaturedTrueOrderByCreatedAtDesc();
}
