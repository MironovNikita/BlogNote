package org.blog.app.service.comment;

import org.blog.app.entity.comment.CommentDto;

public interface CommentService {

    void create(Long postId, CommentDto commentDto);

    void update(Long postId, Long commentId, CommentDto commentDto);

    void delete(Long postId, Long commentId);
}
