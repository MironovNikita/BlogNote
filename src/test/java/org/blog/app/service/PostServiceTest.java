package org.blog.app.service;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.common.mapper.PostMapper;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.entity.tag.Tag;
import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.repository.post.PostRepository;
import org.blog.app.repository.tag.TagRepository;
import org.blog.app.service.post.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("Проверка успешного создания поста")
    void shouldCreatePostWithTags() {
        Long postId = 1L;
        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setTags("тег1 тег2");
        String[] tags = postRequestDto.getTags().split(" ");
        Post mappedPost = new Post();
        mappedPost.setTags(List.of(new Tag(tags[0]), new Tag(tags[1])));

        when(postMapper.toPost(postRequestDto)).thenReturn(mappedPost);
        when(postRepository.create(mappedPost)).thenReturn(postId);

        Long resultedPost = postService.create(postRequestDto);

        assertEquals(postId, resultedPost);
        verify(postRepository).create(mappedPost);
        verify(tagRepository).saveTags(mappedPost.getTags(), postId);
    }

    @Test
    @DisplayName("Проверка успешного создания поста без тегов")
    void shouldCreatePost() {
        Long postId = 1L;
        PostRequestDto postRequestDto = new PostRequestDto();
        Post mappedPost = new Post();

        when(postMapper.toPost(postRequestDto)).thenReturn(mappedPost);
        when(postRepository.create(mappedPost)).thenReturn(postId);

        Long resultedPost = postService.create(postRequestDto);

        assertEquals(postId, resultedPost);
        verify(postRepository).create(mappedPost);
        verify(tagRepository, never()).saveTags(mappedPost.getTags(), postId);
    }

    @Test
    @DisplayName("Проверка успешного обновления поста с тегами")
    void shouldUpdatePostWithTags() {
        Long postId = 1L;
        PostRequestDto postRequestDto = createPostRqDto();
        postRequestDto.setTags("тег1 тег2");
        String[] tags = postRequestDto.getTags().split(" ");

        Post mappedPost = new Post();
        mappedPost.setId(postId);
        mappedPost.setTitle(postRequestDto.getTitle());
        mappedPost.setText(postRequestDto.getText());
        mappedPost.setTags(List.of(new Tag(tags[0]), new Tag(tags[1])));

        Post updatingPost = new Post();
        updatingPost.setTitle("Любой заголовок!");
        updatingPost.setText("Любой текст!");
        updatingPost.setTags(List.of(new Tag(tags[0]), new Tag(tags[1])));

        when(postRepository.getById(postId)).thenReturn(Optional.of(mappedPost));
        when(postMapper.toPost(postRequestDto)).thenReturn(updatingPost);

        postService.update(postId, postRequestDto);

        verify(postRepository).getById(postId);
        verify(postMapper).toPost(postRequestDto);
        verify(postRepository).update(updatingPost, mappedPost);
        verify(tagRepository).updateTags(mappedPost.getTags(), postId);
    }

    @Test
    @DisplayName("Проверка успешного обновления поста")
    void shouldUpdatePost() {
        Long postId = 1L;
        PostRequestDto postRequestDto = createPostRqDto();

        Post mappedPost = new Post();
        mappedPost.setId(postId);
        mappedPost.setTitle(postRequestDto.getTitle());
        mappedPost.setText(postRequestDto.getText());

        Post updatingPost = new Post();
        updatingPost.setTitle("Любой заголовок!");
        updatingPost.setText("Любой текст!");

        when(postRepository.getById(postId)).thenReturn(Optional.of(mappedPost));
        when(postMapper.toPost(postRequestDto)).thenReturn(updatingPost);

        postService.update(postId, postRequestDto);

        verify(postRepository).getById(postId);
        verify(postMapper).toPost(postRequestDto);
        verify(postRepository).update(updatingPost, mappedPost);
        verify(tagRepository, never()).updateTags(mappedPost.getTags(), postId);
    }

    @Test
    @DisplayName("Неуспешное обновление поста - пост не найден")
    void shouldNotUpdatePostIfNotExists() {
        Long postId = 1L;

        when(postRepository.getById(postId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> postService.update(postId, createPostRqDto()));
        verify(postRepository).getById(postId);
        verify(postMapper, never()).toPost(any(PostRequestDto.class));
        verify(postRepository, never()).update(any(Post.class), any(Post.class));
        verify(tagRepository, never()).updateTags(any(), eq(postId));
    }

    @Test
    @DisplayName("Успешное получение поста по ID")
    void shouldFindPostById() {
        Long postId = 1L;
        Post existingPost = new Post();
        existingPost.setId(postId);
        List<Tag> tagList = List.of(new Tag("тег1"));
        existingPost.setTags(tagList);
        List<Comment> commentList = List.of(new Comment());
        existingPost.setComments(commentList);

        when(postRepository.getById(postId)).thenReturn(Optional.of(existingPost));
        when(tagRepository.getTagsByPostId(postId)).thenReturn(tagList);
        when(commentRepository.getAllByPostId(postId)).thenReturn(commentList);

        Post result = postService.getById(postId);

        assertEquals(existingPost, result);
        verify(postRepository).getById(postId);
        verify(tagRepository).getTagsByPostId(postId);
        verify(commentRepository).getAllByPostId(postId);
    }

    @Test
    @DisplayName("Неуспешное получение поста по ID - пост не найден")
    void shouldNotFindPostById() {
        Long postId = 1L;

        when(postRepository.getById(postId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(postId));
    }

    @Test
    @DisplayName("Успешное получение всех постов по параметрам")
    void shouldGetAllPostsByParams() {
        String search = "";
        int pageSize = 10;
        int pageNumber = 1;
        int offset = (pageNumber - 1) * pageSize;

        Post post = new Post();
        PostResponseDto postResponseDto = new PostResponseDto();
        List<Post> postList = List.of(post);

        when(postRepository.getAllByParams(search, pageSize, offset)).thenReturn(postList);
        when(postMapper.toPostRsDto(post)).thenReturn(postResponseDto);

        List<PostResponseDto> result = postService.getAllByParams(search, pageSize, pageNumber);

        assertEquals(postResponseDto, result.getFirst());
        verify(postRepository).getAllByParams(search, pageSize, offset);
        verify(postMapper).toPostRsDto(post);
    }

    @Test
    @DisplayName("Успешное получение параметра наличия следующей страницы - true")
    void shouldGetNextPageParamTrue() {
        when(postRepository.hasNextPage("", 10, 10)).thenReturn(true);
        assertTrue(postService.hasNextPage("", 10, 1));
    }

    @Test
    @DisplayName("Успешное получение параметра наличия следующей страницы - false")
    void shouldGetNextPageParamFalse() {
        when(postRepository.hasNextPage("", 10, 10)).thenReturn(false);
        assertFalse(postService.hasNextPage("", 10, 1));
    }

    @Test
    @DisplayName("Успешное удаление поста")
    void deletePostSuccess() {
        Long postId = 1L;
        Post existingPost = new Post();
        existingPost.setId(postId);

        when(postRepository.getById(postId)).thenReturn(Optional.of(existingPost));

        postService.delete(postId);

        verify(postRepository).delete(postId);
    }

    @Test
    @DisplayName("Неуспешное удаление поста - пост не найден")
    void deletePostNotFound() {
        Long postId = 1L;
        when(postRepository.getById(postId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> postService.delete(postId));

        verify(postRepository, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Успешное изменение рейтинга +1 лайк")
    void changeRatingLike() {
        Long postId = 1L;
        Post existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setLikesCount(5L);

        when(postRepository.getById(postId)).thenReturn(Optional.of(existingPost));

        postService.changeRating(true, postId);

        assertEquals(6L, existingPost.getLikesCount());
        verify(postRepository).updateRating(existingPost);
    }

    @Test
    @DisplayName("Успешное изменение рейтинга -1 лайк")
    void changeRatingDislike() {
        Long postId = 1L;
        Post existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setLikesCount(5L);

        when(postRepository.getById(postId)).thenReturn(Optional.of(existingPost));

        postService.changeRating(false, postId);

        assertEquals(4L, existingPost.getLikesCount());
        verify(postRepository).updateRating(existingPost);
    }

    @Test
    @DisplayName("Неуспешное изменение рейтинга - пост не найден")
    void changeRatingNotFound() {
        when(postRepository.getById(1L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> postService.changeRating(true, 1L));

        verify(postRepository, never()).updateRating(any());
    }

    private PostRequestDto createPostRqDto() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setTitle("Заголовок поста новый");
        postRequestDto.setText("Текст поста новый");
        postRequestDto.setImage(imageFile);
        return postRequestDto;
    }
}
