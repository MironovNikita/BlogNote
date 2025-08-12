package org.blog.app.test.controller;

import org.blog.app.common.mapper.CommentMapper;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.entity.post.Post;
import org.blog.app.service.post.PostService;
import org.blog.app.test.configuration.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class CommentControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
    }

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

        when(commentMapper.toComment(dto)).thenReturn(new Comment(1L, "Какой-то тестовый текст"));

        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .param("text", dto.getText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentMapper).toComment(dto);

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
