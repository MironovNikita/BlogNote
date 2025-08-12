package org.blog.app.common.mapper;

import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.entity.comment.CommentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    Comment toComment(CommentRequestDto commentDto);

    CommentResponseDto toCommentRsDto(Comment comment);
}
