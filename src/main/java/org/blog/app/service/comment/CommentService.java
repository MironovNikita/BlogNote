package org.blog.app.service.comment;

import org.blog.app.entity.comment.CommentRequestDto;

public interface CommentService {

    void create(Long postId, CommentRequestDto commentDto);

    void update(Long postId, Long commentId, CommentRequestDto commentDto);

    void delete(Long postId, Long commentId);
}
