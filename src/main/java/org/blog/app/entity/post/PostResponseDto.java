package org.blog.app.entity.post;

import lombok.*;
import org.blog.app.entity.comment.Comment;

import java.util.List;

@ToString
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

    private List<String> tags;

    private List<Comment> comments;

    public String getTextPreview() {
        if (text == null) {
            return "";
        }
        int limit = 200;
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit) + "...";
    }
}
