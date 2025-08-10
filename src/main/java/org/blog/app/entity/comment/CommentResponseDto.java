package org.blog.app.entity.comment;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

    private Long id;

    private String text;
}
