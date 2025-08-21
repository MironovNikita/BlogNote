package org.blog.app.controller;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerMvcTest extends BaseMvcTest {

    @Test
    @DisplayName("MVC: Успешное создание комментария")
    void shouldCreateCommentSuccessfully() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                        .param("text", "Hello World! It's new comment!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).create(eq(1L), any());
    }

    @Test
    @DisplayName("MVC: Комментарий не создан. Текст не прошёл валидацию")
    void shouldNotCreateIfTextIsNotValid() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                        .param("text", "!"))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).create(anyLong(), any());
    }

    @Test
    @DisplayName("MVC: Успешное обновление комментария")
    void shouldUpdateCommentSuccessfully() throws Exception {

        mockMvc.perform(post("/posts/1/comments/1")
                        .param("text", "Hello World! It's new comment!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).update(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("MVC: Комментарий не обновлён. Текст не прошёл валидацию")
    void shouldNotUpdateIfTextIsNotValid() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                        .param("text", "!"))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).update(anyLong(), any(), any());
    }

    @Test
    @DisplayName("MVC: Комментарий не обновлён. Он не найден")
    void shouldNotUpdateIfCommentNotFound() throws Exception {
        doThrow(ObjectNotFoundException.class)
                .when(commentService)
                .update(anyLong(), anyLong(), any());

        mockMvc.perform(post("/posts/1/comments/1")
                        .param("text", "Hello World! It's new comment!"))
                .andExpect(status().is4xxClientError());

        verify(commentService).update(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("MVC: Успешное удаление комментария")
    void shouldDeleteCommentSuccessfully() throws Exception {

        mockMvc.perform(post("/posts/1/comments/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).delete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("MVC: Комментарий не удалён. Он не найден")
    void shouldNotDeleteIfCommentNotFound() throws Exception {
        doThrow(ObjectNotFoundException.class)
                .when(commentService)
                .delete(anyLong(), anyLong());

        mockMvc.perform(post("/posts/1/comments/1/delete"))
                .andExpect(status().is4xxClientError());

        verify(commentService).delete(anyLong(), anyLong());
    }
}
