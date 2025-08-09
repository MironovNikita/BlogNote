package org.blog.app.repository.comment;

import lombok.RequiredArgsConstructor;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    //TODO Тут по идее postId из запроса должен приходить
    public void create(Comment comment, Long postId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection
                            .prepareStatement("INSERT INTO comments (text) VALUES (?)", Statement.RETURN_GENERATED_KEYS);

                    ps.setString(1, comment.getText());
                    return ps;
                }, keyHolder
        );

        Long commentId = (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");

        jdbcTemplate.update("INSERT INTO posts_comments (post_id, comment_id) VALUES (?, ?)",
                postId,
                commentId);
    }

    //TODO В методе сервиса будет проверка на существование этого самого Comment, поэтому метод будет принимать два комментария.
    // Одна сущность - это пост с новыми данными, а вторая со старыми. Хотя возможно это не нужно.
    @Override
    public void update(Comment existingComment, CommentRequestDto updateCommentDto) {
        var text = updateCommentDto.getText();
        if (text != null && !text.isBlank()) existingComment.setText(text);

        jdbcTemplate.update("UPDATE comments SET text = ? WHERE id = ?",
                existingComment.getText(),
                existingComment.getId());
    }

    @Override
    public List<Comment> getAllByPostId(Long postId) {
        return jdbcTemplate.query(
                "SELECT c.id, c.text FROM comments c JOIN posts_comments pc ON pc.comment_id = c.id WHERE pc.post_id = ? ORDER BY c.id",
                (rs, rowNum) -> {
                    Comment comment = new Comment();
                    comment.setId(rs.getLong("id"));
                    comment.setText(rs.getString("text"));
                    return comment;
                }, postId);
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?", id);
    }
}
