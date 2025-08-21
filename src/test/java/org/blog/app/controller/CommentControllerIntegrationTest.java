package org.blog.app.controller;

import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.entity.post.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void initData() {

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM posts_comments");

        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("INSERT INTO posts (title, text, imageData, likesCount) VALUES ('postTitle_test', 'postText_test', '', '0')");
    }

    @Test
    @DisplayName("INT: Успешное создание комментария")
    void shouldCreateCommentSuccessfully() throws Exception {

        Long postId = 1L;
        CommentRequestDto dto = new CommentRequestDto("Hello World! Я новый комментарий!");

        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .param("text", dto.getText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        Post post = postService.getById(1L);
        List<Comment> comments = post.getComments();

        assertEquals(1, comments.size());
    }

    @Test
    @DisplayName("INT: Комментарий не создан. Текст не прошёл валидацию")
    void shouldNotCreateIfTextIsNotValid() throws Exception {
        Long postId = 1L;
        CommentRequestDto dto = new CommentRequestDto("");

        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .param("text", dto.getText()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(BindException.class, result.getResolvedException()));

        Post post = postService.getById(1L);
        assertTrue(post.getComments().isEmpty());
    }

    @Test
    @DisplayName("INT: Успешное обновление комментария")
    void shouldUpdateCommentSuccessfully() throws Exception {
        String newText = "Hello World! Это новый комментарий!";
        Long postId = 1L;
        Long commentId = 1L;

        jdbcTemplate.update("INSERT INTO comments (id, text) VALUES (?, ?)", commentId, "Текст старого комментария");
        jdbcTemplate.update("INSERT INTO posts_comments (post_id, comment_id) VALUES (?, ?)", postId, commentId);

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .param("text", newText))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        Post post = postService.getById(postId);
        assertEquals(1, post.getComments().size());
        assertEquals(commentId, post.getComments().getFirst().getId());
        assertEquals(post.getComments().getFirst().getText(), newText);
    }

    @Test
    @DisplayName("INT: Комментарий не обновлён. Текст не прошёл валидацию")
    void shouldNotUpdateIfTextIsNotValid() throws Exception {

        String newText = "Hello!";
        String oldText = "Текст старого комментария";
        Long postId = 1L;
        Long commentId = 1L;

        jdbcTemplate.update("INSERT INTO comments (id, text) VALUES (?, ?)", commentId, oldText);
        jdbcTemplate.update("INSERT INTO posts_comments (post_id, comment_id) VALUES (?, ?)", postId, commentId);

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .param("text", newText))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(BindException.class, result.getResolvedException()));

        Post post = postService.getById(postId);
        assertEquals(1, post.getComments().size());
        assertEquals(post.getComments().getFirst().getText(), oldText);
    }

    @Test
    @DisplayName("INT: Комментарий не обновлён. Он не найден")
    void shouldNotUpdateIfCommentNotFound() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        String oldText = "Текст старого комментария";

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .param("text", oldText))
                .andExpect(status().is4xxClientError());

        Post post = postService.getById(postId);
        assertEquals(0, post.getComments().size());
    }

    @Test
    @DisplayName("INT: Успешное удаление комментария")
    void shouldDeleteCommentSuccessfully() throws Exception {

        Long postId = 1L;
        Long commentId = 1L;
        String oldText = "Текст старого комментария";

        jdbcTemplate.update("INSERT INTO comments (id, text) VALUES (?, ?)", commentId, oldText);
        jdbcTemplate.update("INSERT INTO posts_comments (post_id, comment_id) VALUES (?, ?)", postId, commentId);

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/delete", postId, commentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        Post post = postService.getById(postId);
        assertEquals(0, post.getComments().size());
    }

    @Test
    @DisplayName("INT: Комментарий не удалён. Он не найден")
    void shouldNotDeleteIfCommentNotFound() throws Exception {

        Long postId = 1L;
        Long commentId = 1L;

        mockMvc.perform(post("/posts/{postId}/comments/{commentId}/delete", postId, commentId))
                .andExpect(status().is4xxClientError());

        Post post = postService.getById(postId);
        assertEquals(0, post.getComments().size());
    }
}
