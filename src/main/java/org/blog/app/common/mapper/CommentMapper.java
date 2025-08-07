package org.blog.app.common.mapper;

import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toComment(CommentDto commentDto);

    CommentDto toCommentDto(Comment comment);
}
