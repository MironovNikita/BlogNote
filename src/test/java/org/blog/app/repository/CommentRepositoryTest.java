package org.blog.app.repository;

import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.repository.comment.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void fillInPosts() {
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM posts_comments");

        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.update("INSERT INTO posts (title, text, imageData, likesCount) VALUES (?, ?, ?, ?)",
                "Test Title Первый", "Test Text Первый", new byte[]{1, 2, 3}, 0);
    }

    @Test
    @DisplayName("Успешное создание комментария")
    void shouldCreateComment() {
        Long postId = 1L;
        Comment comment = createCommentForPost(postId);

        List<Comment> comments = commentRepository.getAllByPostId(postId);
        assertEquals(1L, comments.size());
        assertEquals(comment.getText(), comments.getFirst().getText());
    }

    @Test
    @DisplayName("Успешное обновление комментария")
    void shouldUpdateExistingComment() {
        Long postId = 1L;
        Comment comment = createCommentForPost(postId);
        CommentRequestDto dto = new CommentRequestDto("Новый комментарий у 1 поста");
        commentRepository.update(comment, dto);

        List<Comment> comments = commentRepository.getAllByPostId(postId);
        assertEquals(1L, comments.size());
        assertEquals(dto.getText(), comments.getFirst().getText());
    }

    @Test
    @DisplayName("Получение списка всех комментариев поста")
    void shouldGetAllCommentsByPostId() {
        Long postId = 1L;
        Comment comment1 = new Comment();
        comment1.setText("Первый комментарий!");
        Comment comment2 = new Comment();
        comment2.setText("Второй комментарий!");
        Comment comment3 = new Comment();
        comment3.setText("Третий комментарий!");

        commentRepository.create(comment1, postId);
        commentRepository.create(comment2, postId);
        commentRepository.create(comment3, postId);

        List<Comment> comments = commentRepository.getAllByPostId(postId);
        assertEquals(3L, comments.size());
        assertEquals(comment1.getText(), comments.get(0).getText());
        assertEquals(comment2.getText(), comments.get(1).getText());
        assertEquals(comment3.getText(), comments.get(2).getText());
    }

    @Test
    @DisplayName("Успешное удаление комментария")
    void shouldDeleteComment() {
        Long postId = 1L;
        Comment comment1 = new Comment();
        comment1.setText("Первый комментарий!");
        Comment comment2 = new Comment();
        comment2.setText("Второй комментарий!");

        commentRepository.create(comment1, postId);
        commentRepository.create(comment2, postId);

        List<Comment> comments = commentRepository.getAllByPostId(postId);
        assertEquals(2L, comments.size());

        commentRepository.delete(1L);
        comments = commentRepository.getAllByPostId(postId);
        assertEquals(1L, comments.size());
        assertEquals(comment2.getText(), comments.getFirst().getText());
    }

    private Comment createCommentForPost(Long postId) {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Комментарий для 1 поста");
        commentRepository.create(comment, postId);

        return comment;
    }
}
