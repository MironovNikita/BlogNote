package org.blog.app.service;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.common.mapper.CommentMapper;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.entity.post.Post;
import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.service.comment.CommentServiceImpl;
import org.blog.app.service.post.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostService postService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("Успешное создание комментария")
    void shouldCreateComment() {

        Long postId = 1L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("Текст комментария");
        Comment comment = new Comment();
        Post post = new Post();

        when(postService.getById(postId)).thenReturn(post);
        when(commentMapper.toComment(commentRequestDto)).thenReturn(comment);

        commentService.create(postId, commentRequestDto);

        verify(postService).getById(postId);
        verify(commentMapper).toComment(commentRequestDto);
        verify(commentRepository).create(comment, postId);
    }

    @Test
    @DisplayName("Комментарий не создался, если пост не найден")
    void shouldNotCreateCommentIfPostNotFound() {
        Long postId = 1L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("Текст комментария");

        when(postService.getById(postId)).thenThrow(ObjectNotFoundException.class);

        assertThrows(ObjectNotFoundException.class,
                () -> commentService.create(postId, commentRequestDto));

        verify(postService).getById(postId);
        verify(commentMapper, never()).toComment(any());
        verify(commentRepository, never()).create(any(), anyLong());
    }

    @Test
    @DisplayName("Комментарий успешно обновлён")
    void shouldUpdateComment() {
        Long postId = 1L;
        Long commentId = 10L;
        CommentRequestDto dto = new CommentRequestDto("Обновлённый текст");

        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        Post post = new Post();
        post.setComments(List.of(existingComment));

        when(postService.getById(postId)).thenReturn(post);

        commentService.update(postId, commentId, dto);

        verify(commentRepository).update(existingComment, dto);
    }

    @Test
    @DisplayName("Обновление не прошло - комментарий не найден")
    void shouldNotUpdateCommentIfPostNotFound() {
        Long postId = 1L;
        Long commentId = 10L;
        CommentRequestDto dto = new CommentRequestDto("Новый текст");

        Post post = new Post();
        post.setComments(new ArrayList<>());
        when(postService.getById(postId)).thenReturn(post);

        assertThrows(ObjectNotFoundException.class,
                () -> commentService.update(postId, commentId, dto));

        verify(commentRepository, never()).update(any(), any());
    }

    @Test
    @DisplayName("Успешное удаление комментария")
    void shouldDeleteComment() {
        Long postId = 1L;
        Long commentId = 10L;

        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        Post post = new Post();
        post.setComments(List.of(existingComment));

        when(postService.getById(postId)).thenReturn(post);

        commentService.delete(postId, commentId);

        verify(commentRepository).delete(commentId);
    }

    @Test
    @DisplayName("Удаление не произведено - комментарий не найден")
    void shouldNotDeleteCommentIfNotFound() {
        Long postId = 1L;
        Long commentId = 10L;
        Post post = new Post();
        post.setComments(new ArrayList<>());

        when(postService.getById(postId)).thenReturn(post);

        assertThrows(ObjectNotFoundException.class,
                () -> commentService.delete(postId, commentId));

        verify(commentRepository, never()).delete(any());
    }
}
