package org.blog.app.entity.comment;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

    private Long id;

    private String text;
}
