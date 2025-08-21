package org.blog.app.controller;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentResponseDto;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.entity.tag.Tag;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PostControllerMvcTest extends BaseMvcTest {

    @Test
    @DisplayName("MVC: Редирект с / на /posts")
    void shouldRedirectFromRootToPosts() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("MVC: Успешная загрузка страницы постов")
    void shouldReturnPostPageWithData() throws Exception {

        List<PostResponseDto> posts = List.of(createPostRsDto());

        when(postService.getAllByParams(eq(""), eq(5), eq(1))).thenReturn(posts);
        when(postService.hasNextPage(eq(""), eq(5), eq(1))).thenReturn(false);

        MvcResult result = mockMvc.perform(get("/posts")
                        .param("search", "")
                        .param("pageSize", "5")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("paging", Matchers.hasProperty("pageNumber", Matchers.is(1))))
                .andExpect(model().attribute("paging", Matchers.hasProperty("pageSize", Matchers.is(5))))
                .andExpect(model().attribute("paging", Matchers.hasProperty("hasNext", Matchers.is(false))))
                .andExpect(model().attribute("paging", Matchers.hasProperty("hasPrevious", Matchers.is(false))))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();

        @SuppressWarnings("unchecked")
        List<PostResponseDto> gotPosts = (List<PostResponseDto>) model.get("posts");
        assertEquals(gotPosts.size(), posts.size());
        assertEquals(gotPosts.getFirst().getTitle(), posts.getFirst().getTitle());

        verify(postService).getAllByParams("", 5, 1);
        verify(postService).hasNextPage("", 5, 1);
    }

    @Test
    @DisplayName("MVC: Успешная загрузка страницы постов с поиском по тегу")
    void shouldReturnPostPageWithDataByTag() throws Exception {

        List<PostResponseDto> posts = List.of(createPostRsDto());
        when(postService.getAllByParams(eq("тестовый"), eq(5), eq(1))).thenReturn(posts);
        when(postService.hasNextPage(eq("тестовый"), eq(5), eq(1))).thenReturn(false);

        MvcResult result = mockMvc.perform(get("/posts")
                        .param("search", "тестовый")
                        .param("pageSize", "5")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("search", "тестовый"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("paging", Matchers.hasProperty("pageNumber", Matchers.is(1))))
                .andExpect(model().attribute("paging", Matchers.hasProperty("pageSize", Matchers.is(5))))
                .andExpect(model().attribute("paging", Matchers.hasProperty("hasNext", Matchers.is(false))))
                .andExpect(model().attribute("paging", Matchers.hasProperty("hasPrevious", Matchers.is(false))))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();

        @SuppressWarnings("unchecked")
        List<PostResponseDto> gotPosts = (List<PostResponseDto>) model.get("posts");
        assertEquals(gotPosts.size(), posts.size());
        assertEquals(gotPosts.getFirst().getTitle(), posts.getFirst().getTitle());

        verify(postService).getAllByParams("тестовый", 5, 1);
        verify(postService).hasNextPage("тестовый", 5, 1);
    }

    @Test
    @DisplayName("MVC: Успешная загрузка страницы постов при отсутствии постов")
    void shouldReturnPostPageEmpty() throws Exception {

        MvcResult result = mockMvc.perform(get("/posts")
                        .param("search", "тестовый")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();

        @SuppressWarnings("unchecked")
        List<PostResponseDto> postsFromModel = (List<PostResponseDto>) model.get("posts");

        assertTrue(postsFromModel.isEmpty());
        verify(postService).getAllByParams("тестовый", 10, 1);
        verify(postService).hasNextPage("тестовый", 10, 1);
    }

    @Test
    @DisplayName("MVC: Получение поста по ID возвращает страницу с постом")
    void shouldFindPostById() throws Exception {
        Long postId = 1L;
        PostResponseDto dto = createPostRsDto();
        Post post = createPost();

        when(postService.getById(postId)).thenReturn(post);
        when(postMapper.toPostRsDto(post)).thenReturn(dto);

        MvcResult result = mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("post", dto))
                .andReturn();

        PostResponseDto gotPost = (PostResponseDto) Objects.requireNonNull(result.getModelAndView()).getModel().get("post");

        assertEquals(gotPost, dto);
        verify(postService).getById(postId);
        verify(postMapper).toPostRsDto(post);
    }

    @Test
    @DisplayName("MVC: Получение поста с несуществующим ID возвращает 404")
    void shouldReturn404ForNonExistingPost() throws Exception {
        Long nonExistingId = 9999L;

        when(postService.getById(nonExistingId)).thenThrow(ObjectNotFoundException.class);

        mockMvc.perform(get("/posts/{id}", nonExistingId))
                .andExpect(status().is4xxClientError());

        verify(postService).getById(nonExistingId);
    }

    @Test
    @DisplayName("MVC: Переход по /posts/add возвращает страницу добавления поста")
    void shouldReturnAddPostPage() throws Exception {
        mockMvc.perform(get("/posts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"));
    }

    @Test
    @DisplayName("MVC: Успешное создание поста с multipart/form-data")
    void shouldCreatePostAndRedirect() throws Exception {
        Long postId = 1L;
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        when(postService.getById(postId)).thenReturn(createPost());

        mockMvc.perform(multipart("/posts")
                        .file(imageFile)
                        .param("title", "Test Title Тестовый")
                        .param("text", "Test Text Тестовый")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));

        Post dto = postService.getById(postId);
        assertEquals(dto.getTitle(), "Test Title Тестовый");
        assertEquals(dto.getText(), "Test Text Тестовый");
        verify(postService).create(any(PostRequestDto.class));
    }

    @Test
    @DisplayName("MVC: Неуспешное создание поста с невалидным текстом")
    void shouldReturn400IfTextIsIncorrect() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts")
                        .file(imageFile)
                        .param("title", "Test Title Тестовый")
                        .param("text", "!")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(postService, never()).create(any());
    }

    @Test
    @DisplayName("MVC: Успешное получение изображения по ID поста")
    void shouldReturnImageBytes() throws Exception {
        Long existingPostId = 1L;

        when(imageService.getImageBytesByPostId(existingPostId)).thenReturn(new byte[]{1, 2, 3});

        MvcResult mvcResult = mockMvc.perform(get("/images/{id}", existingPostId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andReturn();

        byte[] imageBytes = mvcResult.getResponse().getContentAsByteArray();
        assertTrue(imageBytes.length > 0);
        verify(imageService).getImageBytesByPostId(existingPostId);
    }

    @Test
    @DisplayName("MVC: Получение изображения по несуществующему ID поста возвращает 404")
    void shouldReturn404WhenImageNotFound() throws Exception {
        Long nonExistingPostId = 9999L;

        when(imageService.getImageBytesByPostId(nonExistingPostId)).thenReturn(new byte[0]);

        mockMvc.perform(get("/images/{id}", nonExistingPostId))
                .andExpect(status().isNotFound());
        verify(imageService).getImageBytesByPostId(nonExistingPostId);
    }

    @Test
    @DisplayName("MVC: Успешное изменение рейтинга поста и редирект")
    void shouldChangePostRating() throws Exception {
        Long existingPostId = 1L;

        doNothing().when(postService).changeRating(true, existingPostId);

        mockMvc.perform(post("/posts/{id}/like", existingPostId)
                        .param("like", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId));

        verify(postService).changeRating(true, existingPostId);
    }

    @Test
    @DisplayName("INT: Неуспешное изменение рейтинга поста, если пост отсутствует")
    void shouldReturn404IfPostNotExists() throws Exception {
        Long nonExistingPostId = 9999L;

        doThrow(ObjectNotFoundException.class).when(postService).changeRating(true, nonExistingPostId);

        mockMvc.perform(post("/posts/{id}/like", nonExistingPostId)
                        .param("like", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());

        verify(postService).changeRating(true, nonExistingPostId);
    }

    @Test
    @DisplayName("MVC: Успешное обновление поста с multipart/form-data")
    void shouldUpdatePostAndRedirect() throws Exception {

        Long existingPostId = 1L;
        PostRequestDto dto = new PostRequestDto("Test Title Тестовый", "Test Text Тестовый", null, null);
        doNothing().when(postService).update(existingPostId, dto);

        mockMvc.perform(multipart("/posts/{id}/edit", existingPostId)
                        .param("title", "Test Title Тестовый")
                        .param("text", "Test Text Тестовый")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId));

        verify(postService).update(existingPostId, dto);
    }

    @Test
    @DisplayName("MVC: Неуспешное обновление поста, если пост отсутствует")
    void shouldNotUpdatePostIfPostNotExists() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );
        Long updatingPostId = 1L;

        doThrow(ObjectNotFoundException.class).when(postService).update(eq(updatingPostId), any(PostRequestDto.class));

        mockMvc.perform(multipart("/posts/{id}/edit", updatingPostId)
                        .file(imageFile)
                        .param("title", "Новый Тестовый заголовок")
                        .param("text", "Новый Тестовый текст")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is4xxClientError());

        verify(postService).update(eq(updatingPostId), any(PostRequestDto.class));
    }

    @Test
    @DisplayName("MVC: Успешный показ формы для редактирования поста по ID")
    void shouldShowPostEditForm() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );
        Long updatingPostId = 1L;

        mockMvc.perform(multipart("/posts/{id}/edit", updatingPostId)
                        .file(imageFile)
                        .param("title", "Новый Тестовый заголовок")
                        .param("text", "Новый Тестовый текст")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + updatingPostId));

        verify(postService).update(eq(updatingPostId), any());
    }

    @Test
    @DisplayName("MVC: Неуспешный показ формы для редактирования поста по ID, если пост не найден")
    void shouldNotShowPostEditFormIfPostNotExists() throws Exception {
        Long existingPostId = 1L;

        when(postService.getById(existingPostId)).thenThrow(ObjectNotFoundException.class);

        mockMvc.perform(get("/posts/{id}/edit", existingPostId))
                .andExpect(status().is4xxClientError());

        verify(postService).getById(existingPostId);
    }

    @Test
    @DisplayName("MVC: Успешное удаление поста по ID")
    void shouldDeletePostById() throws Exception {
        Long existingPostId = 1L;

        doNothing().when(postService).delete(existingPostId);

        mockMvc.perform(post("/posts/{id}/delete", existingPostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).delete(existingPostId);
    }

    @Test
    @DisplayName("MVC: Неуспешное удаление поста по ID, если такого поста не существует")
    void shouldReturn404IfPostDoesNotExist() throws Exception {
        Long nonExistingPostId = 9999L;

        doThrow(ObjectNotFoundException.class).when(postService).delete(nonExistingPostId);

        mockMvc.perform(post("/posts/{id}/delete", nonExistingPostId))
                .andExpect(status().is4xxClientError());

        verify(postService).delete(nonExistingPostId);
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
