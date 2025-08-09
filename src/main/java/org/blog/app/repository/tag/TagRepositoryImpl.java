package org.blog.app.repository.tag;

import lombok.RequiredArgsConstructor;
import org.blog.app.entity.tag.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveTags(List<Tag> tags, Long postId) {

        if (tags == null || tags.isEmpty()) return;
        List<Tag> tagList = new ArrayList<>(tags);

        jdbcTemplate.batchUpdate(
                "INSERT INTO tags (name) VALUES (?) ON CONFLICT DO NOTHING",
                tagList,
                tagList.size(),
                ((ps, tag) -> ps.setString(1, tag.getName()))
        );

        String placeholders = String.join(",", Collections.nCopies(tags.size(), "?"));
        List<Long> tagIds = jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement("SELECT id FROM tags WHERE name IN (" + placeholders + ")");
                    for (int i = 0; i < tagList.size(); i++) {
                        ps.setString(i + 1, tagList.get(i).getName());
                    }
                    return ps;
                },
                (rs, rowNum) -> rs.getLong("id")
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO posts_tags (post_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                tagIds,
                tagIds.size(),
                ((ps, tagId) -> {
                    ps.setLong(1, postId);
                    ps.setLong(2, tagId);
                })
        );
    }

    @Override
    public void updateTags(List<Tag> tags, Long postId) {
        jdbcTemplate.update("DELETE FROM posts_tags WHERE post_id = ?", postId);
        saveTags(tags, postId);
    }

    @Override
    public List<Tag> getTagsByPostId(Long postId) {
        return jdbcTemplate.query(
                "SELECT t.id, t.name FROM tags t JOIN posts_tags pt ON t.id = pt.tag_id WHERE pt.post_id = ? ORDER BY t.id ",
                (rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name")),
                postId
        );
    }
}