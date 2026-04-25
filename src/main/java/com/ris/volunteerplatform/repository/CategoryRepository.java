package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
