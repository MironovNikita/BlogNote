package org.blog.app.common.mapper;

import org.blog.app.common.exception.ObjectCreationException;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.entity.tag.Tag;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PostMapper {

    private static final String TAG_PATTERN = "[^\\p{L}\\p{N}]+";

    public Post toPost(PostRequestDto postRqDto) {
        Post post = new Post();
        post.setTitle(postRqDto.getTitle());
        post.setText(postRqDto.getText());
        try {
            post.setImageData((postRqDto.getImage() != null) ? postRqDto.getImage().getBytes() : null);
        } catch (IOException exception) {
            throw new ObjectCreationException("Пост", "изображение поста");
        }
        post.setLikesCount(0L);

        if (postRqDto.getTags() != null && !postRqDto.getTags().isBlank()) {
            post.setTags(handlePostTags(postRqDto.getTags()));
        }

        return post;
    }

    private Set<Tag> handlePostTags(String tags) {
        return Arrays.stream(tags.split(TAG_PATTERN))
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

    public PostResponseDto toPostRsDto(Post post) {
        PostResponseDto postRsDto = new PostResponseDto();
        postRsDto.setTitle(post.getTitle());
        postRsDto.setText(post.getText());
        postRsDto.setImageBase64(Base64.getEncoder().encodeToString(post.getImageData()));
        postRsDto.setLikesCount(post.getLikesCount());
        return postRsDto;
    }

}
