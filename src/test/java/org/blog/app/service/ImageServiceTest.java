package org.blog.app.service;

import org.blog.app.repository.post.PostRepository;
import org.blog.app.service.image.ImageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    @DisplayName("Успешное получение байт изображения, если оно есть у поста")
    void shouldReturnImageBytesIfExists() {
        Long postId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};

        when(postRepository.findImageDataByPostId(postId)).thenReturn(Optional.of(imageBytes));

        byte[] actualBytes = imageService.getImageBytesByPostId(postId);

        assertArrayEquals(imageBytes, actualBytes);
    }

    @Test
    @DisplayName("Успешное получение пустого массива байт при отсутствии изображения")
    void shouldReturnEmptyImageBytesIfNotExists() {
        Long postId = 1L;

        when(postRepository.findImageDataByPostId(postId)).thenReturn(Optional.empty());
        byte[] actualBytes = imageService.getImageBytesByPostId(postId);

        assertArrayEquals(new byte[0], actualBytes);
    }
}
