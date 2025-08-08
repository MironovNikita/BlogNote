package org.blog.app.repository.post;

import lombok.RequiredArgsConstructor;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.tag.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        return (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
    }

    @Override
    public void update(Post postToUpdate, Post existingPost) {
        if (postToUpdate.getTitle() != null && !postToUpdate.getTitle().isBlank())
            existingPost.setTitle(postToUpdate.getTitle());
        if (postToUpdate.getText() != null && !postToUpdate.getText().isBlank())
            existingPost.setText(postToUpdate.getText());
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

    @Override
    public Optional<Post> getById(Long id) {
        return Optional.of(jdbcTemplate.queryForObject(
                "SELECT * FROM posts WHERE id = ?", (rs, rowNum) -> {
                    Post post = new Post();
                    post.setId(rs.getLong("id"));
                    post.setTitle(rs.getString("title"));
                    post.setText(rs.getString("text"));
                    post.setImageData(rs.getBytes("imageData"));
                    post.setLikesCount(rs.getLong("likesCount"));
                    return post;
                }, id));
    }

    public Post getLikes(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, likesCount FROM POSTS WHERE id = ?", (rs, rowNum) -> {
                    Post post = new Post();
                    post.setId(rs.getLong("id"));
                    post.setLikesCount(rs.getLong("likesCount"));
                    return post;
                }, id);
    }


    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public void updateRating(Post post) {
        jdbcTemplate.update("UPDATE posts SET likescount = ? WHERE id = ?", post.getLikesCount(), post.getId());
    }

    @Override
    public List<Post> getAllByParams(String search, int limit, int offset) {
        List<Post> posts = jdbcTemplate.query(
                "SELECT DISTINCT p.id, p.title, p.text, p.imageData, p.likesCount " +
                        "FROM posts p " +
                        (search.isEmpty() ? "" :
                                "JOIN posts_tags pt ON p.id = pt.post_id " +
                                        "JOIN tags t ON pt.tag_id = t.id " +
                                        "WHERE t.name LIKE ? ") +
                        "ORDER BY p.id DESC LIMIT ? OFFSET ?",
                ps -> {
                    int index = 1;
                    if (!search.isEmpty()) {
                        ps.setString(index++, "%" + search + "%");
                    }
                    ps.setInt(index++, limit);
                    ps.setInt(index, offset);
                }, (rs, rowNum) -> {
                    Post post = new Post();
                    post.setId(rs.getLong("id"));
                    post.setTitle(rs.getString("title"));
                    post.setText(rs.getString("text"));
                    post.setImageData(rs.getBytes("imageData"));
                    post.setLikesCount(rs.getLong("likesCount"));
                    post.setTags(new HashSet<>());
                    post.setComments(new ArrayList<>());
                    return post;
                });

        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        if (!postMap.isEmpty()) {
            putTags(postMap);
            putComments(postMap);
        }

        return new ArrayList<>(postMap.values());
    }

    @Override
    public boolean hasNextPage(String search, int limit, int offset) {
        String countQuery =
                "SELECT COUNT(DISTINCT p.id) " +
                        "FROM posts p " +
                        (search.isEmpty() ? "" :
                                "JOIN posts_tags pt ON p.id = pt.post_id " +
                                        "JOIN tags t ON pt.tag_id = t.id " +
                                        "WHERE t.name ILIKE ?");

        Integer total = jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(countQuery);
            if (!search.isEmpty()) {
                ps.setString(1, "%" + search + "%");
            }
            return ps;
        }, rs -> rs.next() ? rs.getInt(1) : 0);

        return (offset + limit) < total;
    }

    private void putTags(Map<Long, Post> postMap) {
        if (postMap.isEmpty()) return;

        String inClause = postMap.keySet().stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = "SELECT pt.post_id, t.id, t.name " +
                "FROM posts_tags pt " +
                "JOIN tags t ON pt.tag_id = t.id " +
                "WHERE pt.post_id IN (" + inClause + ")";

        jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            int index = 1;
            for (Long id : postMap.keySet()) {
                ps.setLong(index++, id);
            }
            return ps;
        }, (ResultSet rs) -> {
            while (rs.next()) {
                Long postId = rs.getLong("post_id");
                Post post = postMap.get(postId);
                if (post != null) {
                    Tag tag = new Tag(rs.getLong("id"), rs.getString("name"));
                    post.getTags().add(tag);
                }
            }
        });
    }

    private void putComments(Map<Long, Post> postMap) {
        if (postMap.isEmpty()) return;

        String inClause = postMap.keySet().stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = "SELECT pc.post_id, c.id, c.text " +
                "FROM posts_comments pc " +
                "JOIN comments c ON pc.comment_id = c.id " +
                "WHERE pc.post_id IN (" + inClause + ")";

        jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            int index = 1;
            for (Long id : postMap.keySet()) {
                ps.setLong(index++, id);
            }
            return ps;
        }, (ResultSet rs) -> {
            while (rs.next()) {
                Long postId = rs.getLong("post_id");
                Post post = postMap.get(postId);
                if (post != null) {
                    Comment comment = new Comment(rs.getLong("id"), rs.getString("text"));
                    post.getComments().add(comment);
                }
            }
        });
    }
}

