package org.blog.app.entity.post;

import lombok.*;
import org.blog.app.entity.comment.Comment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    //TODO Переделать на CommentResponseDto
    private List<Comment> comments;

    public String getTextPreview() {
        if (text == null) {
            return "";
        }
        //TODO Вынести в константу
        int limit = 200;
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit) + "...";
    }

    public List<String> getTextParts() {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return Arrays.asList(text.split("\\n"));
    }

    @Override
    public String toString() {
        return "PostResponseDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", tags=" + tags +
                ", comments=" + comments +
                ", likesCount=" + likesCount +
                '}';
    }
}
