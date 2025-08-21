package org.blog.app.service.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blog.app.repository.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final PostRepository postRepository;

    @Override
    public byte[] getImageBytesByPostId(Long id) {
        var imageData = postRepository.findImageDataByPostId(id).orElse(new byte[0]);

        log.debug("Для поста с ID {} был получен массив байт картинки размером {}", id, imageData.length);
        return imageData;
    }
}
