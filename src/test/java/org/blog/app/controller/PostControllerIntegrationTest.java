package org.blog.app.controller;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.entity.comment.CommentResponseDto;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostResponseDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PostControllerIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void clearData() {
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM posts_comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts_tags");

        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE tags ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("INT: Редирект с / на /posts")
    void shouldRedirectFromRootToPosts() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("INT: Успешная загрузка страницы постов")
    void shouldReturnPostPageWithData() throws Exception {

        PostResponseDto dto = createPostRsDto();

        MvcResult mvcResult = mockMvc.perform(get("/posts")
                        .param("search", "")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(mvcResult.getModelAndView()).getModel();

        @SuppressWarnings("unchecked")
        List<PostResponseDto> postsFromModel = (List<PostResponseDto>) model.get("posts");
        assertFalse(postsFromModel.isEmpty());
        assertEquals(dto.getTitle(), postsFromModel.getFirst().getTitle());
    }

    @Test
    @DisplayName("INT: Успешная загрузка страницы постов с поиском по тегу")
    void shouldReturnPostPageWithDataByTag() throws Exception {

        PostResponseDto dto = createPostRsDto();

        MvcResult mvcResult = mockMvc.perform(get("/posts")
                        .param("search", "тестовый")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(mvcResult.getModelAndView()).getModel();

        @SuppressWarnings("unchecked")
        List<PostResponseDto> postsFromModel = (List<PostResponseDto>) model.get("posts");
        assertFalse(postsFromModel.isEmpty());
        assertEquals(dto.getTitle(), postsFromModel.getFirst().getTitle());
    }

    @Test
    @DisplayName("INT: Успешная загрузка страницы постов при отсутствии постов")
    void shouldReturnPostPageEmpty() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/posts")
                        .param("search", "тестовый")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        Map<String, Object> model = Objects.requireNonNull(mvcResult.getModelAndView()).getModel();

        @SuppressWarnings("unchecked")
        List<PostResponseDto> postsFromModel = (List<PostResponseDto>) model.get("posts");
        assertTrue(postsFromModel.isEmpty());
    }

    @Test
    @DisplayName("INT: Получение поста по ID возвращает страницу с постом")
    void shouldFindPostById() throws Exception {
        Long postId = 1L;
        PostResponseDto dto = createPostRsDto();

        MvcResult result = mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("post", dto))
                .andReturn();

        PostResponseDto gotPost = (PostResponseDto) Objects.requireNonNull(result.getModelAndView()).getModel().get("post");
        assertEquals(gotPost, dto);
    }

    @Test
    @DisplayName("INT: Получение поста с несуществующим ID возвращает 404")
    void shouldReturn404ForNonExistingPost() throws Exception {
        Long nonExistingId = 9999L;

        mockMvc.perform(get("/posts/{id}", nonExistingId))
                .andExpect(status().is4xxClientError());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(nonExistingId));
    }

    @Test
    @DisplayName("INT: Переход по /posts/add возвращает страницу добавления поста")
    void shouldReturnAddPostPage() throws Exception {
        mockMvc.perform(get("/posts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"));
    }

    @Test
    @DisplayName("INT: Успешное создание поста с multipart/form-data")
    void shouldCreatePostAndRedirect() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts")
                        .file(imageFile)
                        .param("title", "Тестовый заголовок")
                        .param("text", "Тестовый текст")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));

        Post dto = postService.getById(1L);
        assertEquals(dto.getTitle(), "Тестовый заголовок");
    }

    @Test
    @DisplayName("INT: Неуспешное создание поста с невалидным текстом")
    void shouldReturn400IfTextIsIncorrect() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts")
                        .file(imageFile)
                        .param("title", "Тестовый заголовок")
                        .param("text", "")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(1L));
    }

    @Test
    @DisplayName("INT: Успешное получение изображения по ID поста")
    void shouldReturnImageBytes() throws Exception {
        Long existingPostId = 1L;
        createPostRsDto();

        MvcResult mvcResult = mockMvc.perform(get("/images/{id}", existingPostId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andReturn();

        byte[] imageBytes = mvcResult.getResponse().getContentAsByteArray();
        assertTrue(imageBytes.length > 0);
    }

    @Test
    @DisplayName("INT: Получение изображения по несуществующему ID поста возвращает 404")
    void shouldReturn404WhenImageNotFound() throws Exception {
        Long nonExistingPostId = 9999L;

        mockMvc.perform(get("/images/{id}", nonExistingPostId))
                .andExpect(status().isNotFound());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(nonExistingPostId));
    }

    @Test
    @DisplayName("INT: Успешное изменение рейтинга поста и редирект")
    void shouldChangePostRating() throws Exception {
        Long existingPostId = 1L;
        createPostRsDto();

        mockMvc.perform(post("/posts/{id}/like", existingPostId)
                        .param("like", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId));

        Post post = postService.getById(existingPostId);
        assertEquals(1L, post.getLikesCount());
    }

    @Test
    @DisplayName("INT: Неуспешное изменение рейтинга поста, если пост отсутствует")
    void shouldReturn404IfPostNotExists() throws Exception {
        Long nonExistingPostId = 9999L;

        mockMvc.perform(post("/posts/{id}/like", nonExistingPostId)
                        .param("like", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(nonExistingPostId));
    }

    @Test
    @DisplayName("INT: Успешное обновление поста с multipart/form-data")
    void shouldUpdatePostAndRedirect() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageData",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes(StandardCharsets.UTF_8)
        );
        createPostRsDto();
        Long existingPostId = 1L;

        mockMvc.perform(multipart("/posts/{id}/edit", existingPostId)
                        .file(imageFile)
                        .param("title", "Новый Тестовый заголовок")
                        .param("text", "Новый Тестовый текст")
                        .param("likesCount", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + existingPostId));

        Post dto = postService.getById(1L);
        assertEquals(dto.getTitle(), "Новый Тестовый заголовок");
        assertEquals(dto.getText(), "Новый Тестовый текст");
    }

    @Test
    @DisplayName("INT: Неуспешное обновление поста, если пост отсутствует")
    void shouldNotUpdatePostIfPostNotExists() throws Exception {
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
                .andExpect(status().is4xxClientError());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(updatingPostId));
    }

    @Test
    @DisplayName("INT: Успешный показ формы для редактирования поста по ID")
    void shouldShowPostEditForm() throws Exception {
        Long existingPostId = 1L;
        createPostRsDto();

        mockMvc.perform(get("/posts/{id}/edit", existingPostId))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("post", Matchers.hasProperty("id", Matchers.equalTo(existingPostId))));

        assertDoesNotThrow(() -> postService.getById(existingPostId));
    }

    @Test
    @DisplayName("INT: Неуспешный показ формы для редактирования поста по ID, если пост не найден")
    void shouldNotShowPostEditFormIfPostNotExists() throws Exception {
        Long existingPostId = 1L;

        mockMvc.perform(get("/posts/{id}/edit", existingPostId))
                .andExpect(status().is4xxClientError());

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(existingPostId));
    }

    @Test
    @DisplayName("INT: Успешное удаление поста по ID")
    void shouldDeletePostById() throws Exception {
        Long existingPostId = 1L;
        createPostRsDto();

        mockMvc.perform(post("/posts/{id}/delete", existingPostId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        List<PostResponseDto> posts = postService.getAllByParams("", 10, 1);
        assertTrue(posts.isEmpty());
    }

    @Test
    @DisplayName("INT: Неуспешное удаление поста по ID, если такого поста не существует")
    void shouldReturn404IfPostDoesNotExist() throws Exception {
        Long nonExistingPostId = 9999L;

        assertThrows(ObjectNotFoundException.class, () -> postService.getById(nonExistingPostId));

        mockMvc.perform(post("/posts/{id}/delete", nonExistingPostId))
                .andExpect(status().is4xxClientError());
    }

    private PostResponseDto createPostRsDto() {
        jdbcTemplate.update("INSERT INTO posts (title, text, imageData, likesCount) VALUES (?, ?, ?, ?)",
                "Test Title Тестовый", "Test Text Тестовый", new byte[]{1, 2, 3}, 0);

        jdbcTemplate.update("INSERT INTO tags (name) VALUES (?)",
                "тестовый");

        jdbcTemplate.update("INSERT INTO comments (text) VALUES (?)",
                "тестовый комментарий!!!");

        jdbcTemplate.update("INSERT INTO posts_tags (post_id, tag_id) VALUES (?, ?)",
                1L, 1L);

        jdbcTemplate.update("INSERT INTO posts_comments (post_id, comment_id) VALUES (?, ?)",
                1L, 1L);

        return new PostResponseDto(1L,
                "Test Title Тестовый",
                "Test Text Тестовый",
                "AQID",
                0L,
                List.of("тестовый"),
                List.of(new CommentResponseDto(1L, "тестовый комментарий!!!")));
    }
}
