package org.blog.app.test.repository;

import org.blog.app.entity.tag.Tag;
import org.blog.app.repository.tag.TagRepository;
import org.blog.app.test.configuration.TestDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(TestDataSourceConfig.class)
public class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void fillInPosts() {
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM posts_tags");

        jdbcTemplate.execute("ALTER TABLE tags ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.update("INSERT INTO posts (title, text, imageData, likesCount) VALUES (?, ?, ?, ?)",
                "Test Title Первый", "Test Text Первый", "1", 0);
    }

    @Test
    @DisplayName("Успешное создание тегов для поста")
    void shouldCreateTagsForPost() {
        Long postId = 1L;
        Tag tag1 = new Tag("тег1");
        Tag tag2 = new Tag("тег2");
        List<Tag> tags = List.of(tag1, tag2);

        tagRepository.saveTags(tags, postId);

        List<Tag> savedTags = tagRepository.getTagsByPostId(postId);
        assertEquals(2, savedTags.size());
        assertEquals(tag1.getName(), savedTags.get(0).getName());
        assertEquals(tag2.getName(), savedTags.get(1).getName());
    }

    @Test
    @DisplayName("Успешное обновление тегов для поста")
    void shouldUpdateTagsForPost() {
        Long postId = 1L;
        Tag tag1 = new Tag("тег1");
        Tag tag2 = new Tag("тег2");
        List<Tag> tags = List.of(tag1, tag2);

        tagRepository.saveTags(tags, postId);

        Tag tag3 = new Tag("тег3");
        Tag tag4 = new Tag("тег4");
        List<Tag> tagsToUpdate = List.of(tag3, tag4);

        tagRepository.updateTags(tagsToUpdate, postId);
        List<Tag> updatedTags = tagRepository.getTagsByPostId(postId);

        assertEquals(2, updatedTags.size());
        assertEquals(tag3.getName(), updatedTags.get(0).getName());
        assertEquals(tag4.getName(), updatedTags.get(1).getName());
    }

    @Test
    @DisplayName("Успешное получение списка тегов для поста по его ID")
    void shouldGetTagListForPostById() {
        Long postId = 1L;
        Tag tag1 = new Tag("тег1");
        Tag tag2 = new Tag("тег2");
        Tag tag3 = new Tag("тег3");
        Tag tag4 = new Tag("тег4");

        tagRepository.saveTags(List.of(tag1, tag2, tag3, tag4), postId);

        List<Tag> tags = tagRepository.getTagsByPostId(postId);
        assertEquals(4, tags.size());
        assertEquals(tag1.getName(), tags.get(0).getName());
        assertEquals(tag2.getName(), tags.get(1).getName());
        assertEquals(tag3.getName(), tags.get(2).getName());
        assertEquals(tag4.getName(), tags.get(3).getName());
    }
}
