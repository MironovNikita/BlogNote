package org.blog.app.service.post;

import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;

import java.util.List;

public interface PostService {

    Long create(PostRequestDto postRqDto);

    void update(Long id, PostRequestDto postRqDto);

    Post getById(Long id);

    List<PostResponseDto> getAllByParams(String search, int pageSize, int pageNumber);

    boolean hasNextPage(String search, int pageSize, int pageNumber);

    void delete(Long id);

    void changeRating(Boolean like, Long id);
}
