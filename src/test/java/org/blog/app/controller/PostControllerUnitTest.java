package org.blog.app.controller;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.common.mapper.PostMapper;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentResponseDto;
import org.blog.app.entity.paging.Paging;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.entity.tag.Tag;
import org.blog.app.service.image.ImageService;
import org.blog.app.service.post.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerUnitTest {

    @Mock
    private PostService postService;

    @Mock
    private ImageService imageService;

    @Mock
    private PostMapper postMapper;

    @Mock
    Model model;

    @InjectMocks
    private PostController postController;

    @Test
    @DisplayName("JU: Редирект с / на /posts")
    void shouldRedirectFromRootToPosts() {

        String url = postController.postsRedirect();
        assertEquals("redirect:/posts", url);
    }

    @Test
    @DisplayName("JU: Успешная загрузка страницы постов")
    void shouldReturnPostPageWithData() {

        List<PostResponseDto> posts = List.of(createPostRsDto());
        Paging paging = createPaging();

        when(postService.getAllByParams(eq(""), eq(5), eq(1))).thenReturn(posts);
        when(postService.hasNextPage(eq(""), eq(5), eq(1))).thenReturn(false);

        String view = postController.postsPage("", 5, 1, model);

        assertEquals("posts", view);

        verify(postService).getAllByParams("", 5, 1);
        verify(postService).hasNextPage("", 5, 1);
        verify(model).addAttribute("posts", posts);
        verify(model).addAttribute("search", "");
        verify(model).addAttribute("paging", paging);
    }

    @Test
    @DisplayName("JU: Успешная загрузка страницы постов с поиском по тегу")
    void shouldReturnPostPageWithDataByTag() {

        List<PostResponseDto> posts = List.of(createPostRsDto());
        Paging paging = createPaging();

        when(postService.getAllByParams(eq("тестовый"), eq(5), eq(1))).thenReturn(posts);
        when(postService.hasNextPage(eq("тестовый"), eq(5), eq(1))).thenReturn(false);

        String view = postController.postsPage("тестовый", 5, 1, model);

        assertEquals("posts", view);
        verify(postService).getAllByParams("тестовый", 5, 1);
        verify(postService).hasNextPage("тестовый", 5, 1);
        verify(model).addAttribute("posts", posts);
        verify(model).addAttribute("search", "тестовый");
        verify(model).addAttribute("paging", paging);
    }

    @Test
    @DisplayName("JU: Успешная загрузка страницы постов при отсутствии постов")
    void shouldReturnPostPageEmpty() {

        List<PostResponseDto> posts = Collections.emptyList();
        Paging paging = createPaging();

        when(postService.getAllByParams(eq(""), eq(5), eq(1))).thenReturn(posts);
        when(postService.hasNextPage(eq(""), eq(5), eq(1))).thenReturn(false);

        String view = postController.postsPage("", 5, 1, model);

        assertEquals("posts", view);
        verify(postService).getAllByParams("", 5, 1);
        verify(postService).hasNextPage("", 5, 1);
        verify(model).addAttribute("posts", posts);
        verify(model).addAttribute("search", "");
        verify(model).addAttribute("paging", paging);
    }

    @Test
    @DisplayName("JU: Получение поста по ID возвращает страницу с постом")
    void shouldFindPostById() {
        Long postId = 1L;
        PostResponseDto dto = createPostRsDto();
        Post post = createPost();

        when(postService.getById(postId)).thenReturn(post);
        when(postMapper.toPostRsDto(post)).thenReturn(dto);

        String view = postController.get(postId, model);

        assertEquals("post", view);
        verify(postService).getById(postId);
        verify(postMapper).toPostRsDto(post);
        verify(model).addAttribute("post", dto);
    }

    @Test
    @DisplayName("JU: Получение поста с несуществующим ID возвращает 404")
    void shouldReturn404ForNonExistingPost() {
        Long nonExistingId = 9999L;

        when(postService.getById(nonExistingId)).thenThrow(ObjectNotFoundException.class);

        assertThrows(ObjectNotFoundException.class, () -> postController.get(nonExistingId, model));
        verify(postService).getById(nonExistingId);
    }

    @Test
    @DisplayName("JU: Переход по /posts/add возвращает страницу добавления поста")
    void shouldReturnAddPostPage() {

        String url = postController.postAddPage();
        assertEquals("add-post", url);
    }

    @Test
    @DisplayName("JU: Успешное создание поста с multipart/form-data")
    void shouldCreatePostAndRedirect() throws Exception {
        Long postId = 1L;
        PostRequestDto dto = createPostRqDto();
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "postRequestDto");

        when(postService.create(dto)).thenReturn(postId);

        String view = postController.create(dto, bindingResult);

        assertEquals("redirect:/posts/" + postId, view);
        verify(postService).create(any(PostRequestDto.class));
    }

    @Test
    @DisplayName("JU: Неуспешное создание поста с невалидным текстом")
    void shouldReturn400IfTextIsIncorrect() {
        PostRequestDto dto = createPostRqDto();
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "postRequestDto");
        bindingResult.rejectValue("text", "NotBlank", "Текст поста не может быть пустым!");

        assertThrows(BindException.class, () -> postController.create(dto, bindingResult));

        verify(postService, never()).create(any());
    }

    @Test
    @DisplayName("JU: Успешное получение изображения по ID поста")
    void shouldReturnImageBytes() {
        Long existingPostId = 1L;
        byte[] expected = new byte[]{1, 2, 3, 4, 5};

        when(imageService.getImageBytesByPostId(existingPostId)).thenReturn(expected);

        ResponseEntity<byte[]> gotBytes = postController.getImageBytes(existingPostId);

        assertEquals(expected.length, Objects.requireNonNull(gotBytes.getBody()).length);
        assertTrue(gotBytes.getStatusCode().is2xxSuccessful());
        assertEquals(expected, gotBytes.getBody());
        verify(imageService).getImageBytesByPostId(existingPostId);
    }

    @Test
    @DisplayName("JU: Получение изображения по несуществующему ID поста возвращает 404")
    void shouldReturn404WhenImageNotFound() {
        Long nonExistingPostId = 9999L;

        when(imageService.getImageBytesByPostId(nonExistingPostId)).thenReturn(new byte[0]);

        ResponseEntity<byte[]> gotBytes = postController.getImageBytes(nonExistingPostId);

        assertTrue(gotBytes.getStatusCode().is4xxClientError());
        verify(imageService).getImageBytesByPostId(nonExistingPostId);
    }

    @Test
    @DisplayName("JU: Успешное изменение рейтинга поста и редирект")
    void shouldChangePostRating() {
        Long existingPostId = 1L;

        String view = postController.changeRating(existingPostId, true);

        assertEquals("redirect:/posts/" + existingPostId, view);
        verify(postService).changeRating(true, existingPostId);
    }

    @Test
    @DisplayName("JU: Неуспешное изменение рейтинга поста, если пост отсутствует")
    void shouldReturn404IfPostNotExists() {
        Long nonExistingPostId = 9999L;

        doThrow(ObjectNotFoundException.class).when(postService).changeRating(true, nonExistingPostId);

        assertThrows(ObjectNotFoundException.class, () -> postController.changeRating(nonExistingPostId, true));
        verify(postService).changeRating(true, nonExistingPostId);
    }

    @Test
    @DisplayName("JU: Успешное обновление поста с multipart/form-data")
    void shouldUpdatePostAndRedirect() throws Exception {

        Long existingPostId = 1L;
        PostRequestDto dto = new PostRequestDto("Test Title Тестовый", "Test Text Тестовый", null, null);
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "postRequestDto");

        doNothing().when(postService).update(existingPostId, dto);

        String view = postController.postEdit(existingPostId, dto, bindingResult);

        assertEquals(view, "redirect:/posts/" + existingPostId);
        assertFalse(bindingResult.hasErrors());
        verify(postService).update(existingPostId, dto);
    }

    @Test
    @DisplayName("JU: Неуспешное обновление поста, если пост отсутствует")
    void shouldNotUpdatePostIfPostNotExists() {
        Long updatingPostId = 9999L;
        PostRequestDto dto = new PostRequestDto("Test Title Тестовый", "Test Text Тестовый", null, null);
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "postRequestDto");

        doThrow(ObjectNotFoundException.class).when(postService).update(eq(updatingPostId), any(PostRequestDto.class));

        assertThrows(ObjectNotFoundException.class, () -> postController.postEdit(updatingPostId, dto, bindingResult));
        verify(postService).update(eq(updatingPostId), any(PostRequestDto.class));
    }

    @Test
    @DisplayName("JU: Успешный показ формы для редактирования поста по ID")
    void shouldShowPostEditForm() {
        Long postId = 1L;
        Post post = createPost();
        PostResponseDto dto = createPostRsDto();

        when(postService.getById(postId)).thenReturn(post);
        when(postMapper.toPostRsDto(post)).thenReturn(dto);

        String view = postController.showEditForm(postId, model);

        assertEquals("add-post", view);
        verify(postService).getById(postId);
        verify(postMapper).toPostRsDto(post);
        verify(model).addAttribute("post", dto);
    }

    @Test
    @DisplayName("JU: Неуспешный показ формы для редактирования поста по ID, если пост не найден")
    void shouldNotShowPostEditFormIfPostNotExists() {
        Long existingPostId = 1L;

        when(postService.getById(existingPostId)).thenThrow(ObjectNotFoundException.class);

        assertThrows(ObjectNotFoundException.class, () -> postController.showEditForm(existingPostId, model));
        verify(postService).getById(existingPostId);
    }

    @Test
    @DisplayName("JU: Успешное удаление поста по ID")
    void shouldDeletePostById() {
        Long existingPostId = 1L;

        doNothing().when(postService).delete(existingPostId);

        String view = postController.delete(existingPostId);

        assertEquals(view, "redirect:/posts");
        verify(postService).delete(existingPostId);
    }

    @Test
    @DisplayName("JU: Неуспешное удаление поста по ID, если такого поста не существует")
    void shouldReturn404IfPostDoesNotExist() {
        Long nonExistingPostId = 9999L;

        doThrow(ObjectNotFoundException.class).when(postService).delete(nonExistingPostId);

        assertThrows(ObjectNotFoundException.class, () -> postController.delete(nonExistingPostId));
        verify(postService).delete(nonExistingPostId);
    }

    private Paging createPaging() {
        Paging paging = new Paging();
        paging.setPageNumber(1);
        paging.setPageSize(5);
        paging.setHasNext(false);
        paging.setHasPrevious(false);
        return paging;
    }

    private PostRequestDto createPostRqDto() {
        return new PostRequestDto(
                "Test Title Тестовый",
                "Test Text Тестовый",
                new MockMultipartFile("2", new byte[0]),
                "тестовый");
    }

    private PostResponseDto createPostRsDto() {
        return new PostResponseDto(1L,
                "Test Title Тестовый",
                "Test Text Тестовый",
                "Mg==",
                0L,
                List.of("тестовый"),
                List.of(new CommentResponseDto(1L, "тестовый комментарий!!!")));
    }

    private Post createPost() {
        return new Post(1L,
                "Test Title Тестовый",
                "Test Text Тестовый",
                new byte[0],
                0L,
                List.of(new Tag(1L, "тестовый")),
                List.of(new Comment(1L, "тестовый комментарий!!!")));
    }
}
