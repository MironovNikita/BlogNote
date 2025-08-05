package org.blog.app.repository.post;

import org.blog.app.entity.post.Post;

public interface PostRepository {

    Long create(Post post);

    void update(Post postToUpdate, Post existingPost);

    Post getById(Long id);

    void delete(Long id);

    void updateRating(Post post);
}
