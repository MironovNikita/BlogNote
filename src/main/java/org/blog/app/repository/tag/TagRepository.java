package org.blog.app.repository.tag;

import org.blog.app.entity.tag.Tag;

import java.util.Set;

public interface TagRepository {

    void saveTags(Set<Tag> tags, Long postId);

    void updateTags(Set<Tag> tags, Long postId);

    Set<Tag> getTagsByPostId(Long postId);
}
