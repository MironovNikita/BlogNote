package org.blog.app.service.image;

import lombok.RequiredArgsConstructor;
import org.blog.app.entity.post.Post;
import org.blog.app.service.post.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final PostService postService;

    @Override
    public byte[] getImageBytesByPostId(Long id) {
        Post post = postService.getById(id);
        return post.getImageData();
    }
}
