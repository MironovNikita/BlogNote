package org.blog.app.entity.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.tag.Tag;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    private Long id;

    private String title;

    private String text;

    private byte[] imageData;

    private Long likesCount;

    private List<Tag> tags;

    private List<Comment> comments;

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", likesCount=" + likesCount +
                ", tags=" + tags +
                ", comments=" + comments +
                '}';
    }
}
