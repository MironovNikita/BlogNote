package org.blog.app.common.mapper;

import lombok.RequiredArgsConstructor;
import org.blog.app.common.exception.ObjectCreationException;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.entity.tag.Tag;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private static final String TAG_PATTERN = "[^\\p{L}\\p{N}]+";

    private final CommentMapper commentMapper;

    public Post toPost(PostRequestDto postRqDto) {
        Post post = new Post();
        post.setTitle(postRqDto.getTitle());
        post.setText(postRqDto.getText());
        try {
            post.setImageData((postRqDto.getImage() != null) ? postRqDto.getImage().getBytes() : null);
        } catch (IOException exception) {
            throw new ObjectCreationException("Пост", "изображение поста");
        }

        if (postRqDto.getTags() != null && !postRqDto.getTags().isBlank()) {
            post.setTags(handlePostTags(postRqDto.getTags()));
        }

        return post;
    }

    private List<Tag> handlePostTags(String tags) {
        return Arrays.stream(tags.split(TAG_PATTERN))
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .map(Tag::new)
                .toList();
    }

    public PostResponseDto toPostRsDto(Post post) {
        PostResponseDto postRsDto = new PostResponseDto();
        postRsDto.setId(post.getId());
        postRsDto.setTitle(post.getTitle());
        postRsDto.setText(post.getText());
        postRsDto.setImageBase64(Base64.getEncoder().encodeToString(post.getImageData()));
        postRsDto.setLikesCount(post.getLikesCount());
        postRsDto.setTags(post.getTags() == null
                ? new ArrayList<>()
                : post.getTags().stream().map(Tag::getName).toList());
        postRsDto.setComments(post.getComments() == null
                ? new ArrayList<>()
                : post.getComments().stream().map(commentMapper::toCommentRsDto).toList());
        return postRsDto;
    }

}
