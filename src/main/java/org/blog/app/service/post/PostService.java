package org.blog.app.service.post;

import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;

public interface PostService {

    Long create(PostRequestDto postRqDto);

    void update(Long id, PostRequestDto postRqDto);

    Post getById(Long id);

    void delete(Long id);

    void changeRating(Boolean like, Long id);
}
