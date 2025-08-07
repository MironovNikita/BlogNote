package org.blog.app.repository.post;

import lombok.RequiredArgsConstructor;
import org.blog.app.entity.post.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

//TODO Вынести @Transactional в слой сервиса!!!
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long create(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection
                            .prepareStatement("INSERT INTO posts (title, text, imageData, likesCount) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

                    ps.setString(1, post.getTitle());
                    ps.setString(2, post.getText());
                    ps.setBytes(3, post.getImageData());
                    ps.setLong(4, post.getLikesCount());
                    return ps;
                }, keyHolder
        );

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    //TODO В методе сервиса будет проверка на существование этого самого Post, поэтому метод будет принимать два поста. Одна сущность - это пост с новыми данными, а вторая со старыми
    @Override
    public void update(Post postToUpdate, Post existingPost) {
        if (postToUpdate.getTitle() != null && !postToUpdate.getTitle().isBlank()) existingPost.setTitle(postToUpdate.getTitle());
        if (postToUpdate.getText() != null && !postToUpdate.getText().isBlank()) existingPost.setText(postToUpdate.getText());
        if (postToUpdate.getImageData() != null) existingPost.setImageData(postToUpdate.getImageData());
        if (postToUpdate.getTags() != null) existingPost.setTags(postToUpdate.getTags());

        jdbcTemplate.update(
                "UPDATE posts SET title = ?, text = ?, imageData = ? WHERE id = ?",
                existingPost.getTitle(),
                existingPost.getText(),
                existingPost.getImageData(),
                existingPost.getId()
        );

        if (postToUpdate.getTags() != null) {
            jdbcTemplate.update("DELETE FROM posts_tags WHERE post_id = ?", existingPost.getId());
        }
    }

    //TODO Дописать комментарии в поиск и теги (в сервисе) + может выбрасывать исключение EmptyResultDataAccessException e
    // 3 отдельных репо: комменты, посты и теги
    @Override
    public Post getById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM posts WHERE id = ?", (rs, rowNum) -> {
                    Post post = new Post();
                    post.setId(rs.getLong("id"));
                    post.setTitle(rs.getString("title"));
                    post.setText(rs.getString("text"));
                    post.setImageData(rs.getBytes("imageData"));
                    post.setLikesCount(rs.getLong("likesCount"));
                    return post;
                }, id);
    }

    //TODO При get-методах добавить Optional для безопасности!
    /**
     *
     * Post existingPost = postRepository.getLikes(id)
     *                               .orElseThrow(() -> new ObjectNotFoundException("Пост", id));
     */
    public Post getLikes(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, likesCount FROM POSTS WHERE id = ?", (rs, rowNum) -> {
                    Post post = new Post();
                    post.setId(rs.getLong("id"));
                    post.setLikesCount(rs.getLong("likesCount"));
                    return post;
                }, id);
    }

    //TODO getall пока под вопросом (пропустил)
    // Добавить удаление комментариев и тегов - естественно перед этим проверять, есть ли такой пост под таким ID
    // Это не нужно, т.к. есть ON DELETE CASCADE
    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public void updateRating(Post post) {
        jdbcTemplate.update("UPDATE posts SET likescount = ? WHERE id = ?", post.getLikesCount(), post.getId());
    }
}

