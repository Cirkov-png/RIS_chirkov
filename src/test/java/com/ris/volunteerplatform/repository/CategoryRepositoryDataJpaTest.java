package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2-jpa")
class CategoryRepositoryDataJpaTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Сохранение и чтение категории через JPA")
    void saveAndFind() {
        Category saved = categoryRepository.save(
                Category.builder().name("Разовые акции").description("Кратковременная помощь").build());
        assertThat(saved.getId()).isNotNull();

        Category loaded = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Разовые акции");
        assertThat(loaded.getDescription()).contains("Кратко");
    }
}
