package org.blog.app.repository.comment;

import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;

import java.util.List;

public interface CommentRepository {
    void create(Comment comment, Long postId);

    void update(Comment commentToUpdate, CommentRequestDto updateCommentDto);

    List<Comment> getAllByPostId(Long postId);

    void delete(Long id);
}
