package org.blog.app.entity.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blog.app.entity.comment.CommentResponseDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.blog.app.common.constants.BlogNoteConstants.POST_TEXT_PREVIEW_SYMBOLS_LIMIT;

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

    private List<CommentResponseDto> comments;

    public String getTextPreview() {
        if (text == null) {
            return "";
        }

        int limit = POST_TEXT_PREVIEW_SYMBOLS_LIMIT;
        if (text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit) + "...";
    }

    public List<String> getTextParts() {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return Arrays.asList(text.split("\\n"));
    }

    public String getTagsAsText() {
        if (tags == null || tags.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append(tag);
            sb.append(" ");
        }
        return sb.toString();
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
