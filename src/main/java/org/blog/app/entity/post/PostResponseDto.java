package org.blog.app.entity.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blog.app.entity.comment.Comment;

import java.util.Queue;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {

    private Long id;

    private String title;

    private String text;

    private String imageBase64;

    private Long likesCount;

    private String tags;

    private Queue<Comment> comments;
}
