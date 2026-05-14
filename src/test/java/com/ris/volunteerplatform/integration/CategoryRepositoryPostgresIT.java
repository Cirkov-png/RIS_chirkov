package com.ris.volunteerplatform.integration;

import com.ris.volunteerplatform.entity.Category;
import com.ris.volunteerplatform.repository.CategoryRepository;
import com.ris.volunteerplatform.support.PostgresTestcontainersBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("jpa-slice")
class CategoryRepositoryPostgresIT extends PostgresTestcontainersBase {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Сохранение и чтение категории в PostgreSQL")
    void saveAndFind() {
        Category saved = categoryRepository.save(
                Category.builder().name("IT-категория").description("через Testcontainers").build());
        assertThat(saved.getId()).isNotNull();

        Category loaded = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("IT-категория");
    }
}
