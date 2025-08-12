package org.blog.app.repository.tag;

import org.blog.app.entity.tag.Tag;

import java.util.List;

public interface TagRepository {

    void saveTags(List<Tag> tags, Long postId);

    void updateTags(List<Tag> tags, Long postId);

    List<Tag> getTagsByPostId(Long postId);
}
