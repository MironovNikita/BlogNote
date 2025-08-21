package org.blog.app.controller;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.service.comment.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerUnitTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    @DisplayName("JU: Успешное создание комментария")
    void shouldCreateCommentSuccessfully() throws Exception {
        CommentRequestDto dto = new CommentRequestDto("Hello World!");
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        commentController.create(1L, dto, bindingResult);

        assertFalse(bindingResult.hasErrors());
        verify(commentService).create(1L, dto);
    }

    @Test
    @DisplayName("JU: Комментарий не создан. Текст не прошёл валидацию")
    void shouldNotCreateIfTextIsNotValid() {
        CommentRequestDto dto = new CommentRequestDto("");
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "commentRequestDto");
        bindingResult.rejectValue("text", "NotBlank", "Текст комментария не может быть пустым!");

        assertThrows(BindException.class, () -> commentController.create(1L, dto, bindingResult));
        assertTrue(bindingResult.hasErrors());
        verify(commentService, never()).create(anyLong(), any());
    }

    @Test
    @DisplayName("JU: Успешное обновление комментария")
    void shouldUpdateCommentSuccessfully() throws BindException {
        CommentRequestDto dto = new CommentRequestDto("Hello World! It's new comment!");
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        commentController.update(1L, 1L, dto, bindingResult);

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
    void shouldNotUpdateIfCommentNotFound() {
        CommentRequestDto dto = new CommentRequestDto("Hello World! It's new comment!");
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        doThrow(ObjectNotFoundException.class)
                .when(commentService)
                .update(anyLong(), anyLong(), eq(dto));

        assertThrows(ObjectNotFoundException.class, () -> commentController.update(1L, 1L, dto, bindingResult));
        verify(commentService).update(eq(1L), eq(1L), eq(dto));
    }

    @Test
    @DisplayName("JU: Успешное удаление комментария")
    void shouldDeleteCommentSuccessfully() {

        commentController.delete(anyLong(), anyLong());
        verify(commentService).delete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("JU: Комментарий не удалён. Он не найден")
    void shouldNotDeleteIfCommentNotFound() {
        doThrow(ObjectNotFoundException.class)
                .when(commentService)
                .delete(anyLong(), anyLong());

        assertThrows(ObjectNotFoundException.class, () -> commentController.delete(1L, 1L));
        verify(commentService).delete(1L, 1L);
    }

}
