package org.blog.app.repository.comment;

import org.blog.app.entity.comment.Comment;

import java.util.List;

public interface CommentRepository {
    void create(Comment comment, Long postId);

    void update(Comment commentToUpdate, Comment existingComment);

    List<Comment> getAllByPostId(Long postId);

    void delete(Long id);
}
