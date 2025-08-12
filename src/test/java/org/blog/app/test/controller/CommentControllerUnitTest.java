package org.blog.app.test.controller;

import org.blog.app.common.exception.GlobalExceptionHandler;
import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.controller.comment.CommentController;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.service.comment.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerUnitTest {

    private AutoCloseable closeable;

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @Mock
    private Validator validator;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("JU: Успешное создание комментария")
    void shouldCreateCommentSuccessfully() throws Exception {
        CommentRequestDto dto = new CommentRequestDto("Hello World! It's new comment!");

        mockMvc.perform(post("/posts/1/comments")
                        .param("text", dto.getText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).create(eq(1L), eq(dto));
    }

    @Test
    @DisplayName("JU: Комментарий не создан. Текст не прошёл валидацию")
    void shouldNotCreateIfTextIsNotValid() {
        CommentRequestDto dto = new CommentRequestDto("");
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "commentRequestDto");
        bindingResult.rejectValue("text", "NotBlank", "Текст комментария не может быть пустым!");

        assertThrows(BindException.class, () -> commentController.create(1L, dto, bindingResult));

        verify(commentService, never()).create(anyLong(), any());
    }

    @Test
    @DisplayName("JU: Успешное обновление комментария")
    void shouldUpdateCommentSuccessfully() throws Exception {
        CommentRequestDto dto = new CommentRequestDto("Hello World! It's new comment!");

        mockMvc.perform(post("/posts/1/comments/1")
                        .param("text", dto.getText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).update(eq(1L), eq(1L), eq(dto));
    }

    @Test
    @DisplayName("JU: Комментарий не обновлён. Текст не прошёл валидацию")
    void shouldNotUpdateIfTextIsNotValid() {
        CommentRequestDto dto = new CommentRequestDto("");
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "commentRequestDto");
        bindingResult.rejectValue("text", "NotBlank", "Текст комментария не может быть пустым!");

        assertThrows(BindException.class, () -> commentController.update(1L, 1L, dto, bindingResult));

        verify(commentService, never()).update(anyLong(), any(), any());
    }

    @Test
    @DisplayName("JU: Комментарий не обновлён. Он не найден")
    void shouldNotUpdateIfCommentNotFound() throws Exception {
        CommentRequestDto dto = new CommentRequestDto("Hello World! It's new comment!");

        doThrow(ObjectNotFoundException.class)
                .when(commentService)
                .update(anyLong(), anyLong(), eq(dto));

        mockMvc.perform(post("/posts/1/comments/1")
                        .param("text", dto.getText()))
                .andExpect(status().is4xxClientError());

        verify(commentService).update(eq(1L), eq(1L), eq(dto));
    }

    @Test
    @DisplayName("JU: Успешное удаление комментария")
    void shouldDeleteCommentSuccessfully() throws Exception {

        mockMvc.perform(post("/posts/1/comments/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).delete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("JU: Комментарий не удалён. Он не найден")
    void shouldNotDeleteIfCommentNotFound() throws Exception {
        doThrow(ObjectNotFoundException.class)
                .when(commentService)
                .delete(anyLong(), anyLong());

        mockMvc.perform(post("/posts/1/comments/1/delete"))
                .andExpect(status().is4xxClientError());

        verify(commentService).delete(anyLong(), anyLong());
    }
}
