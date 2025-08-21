package org.blog.app.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.common.mapper.CommentMapper;
import org.blog.app.entity.comment.Comment;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.entity.post.Post;
import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.service.post.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostService postService;

    @Override
    public void create(Long postId, CommentRequestDto commentDto) {

        Post post = postService.getById(postId);
        commentRepository.create(commentMapper.toComment(commentDto), postId);
        log.info("Для поста с ID {} и заголовком \"{}\" был создан комментарий длиной {} символов", postId, post.getTitle(), commentDto.getText().length());
    }

    @Override
    public void update(Long postId, Long commentId, CommentRequestDto updateCommentDto) {

        Post post = postService.getById(postId);
        Comment commentToUpdate = post.getComments().stream()
                .filter(com -> com.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Комментарий с ID {} не был найден у поста с ID {} и заголовком \"{}\". Обновление не выполнено!", postId, commentId, post.getTitle());
                    return new ObjectNotFoundException("Комментарий", commentId);
                });

        commentRepository.update(commentToUpdate, updateCommentDto);
        log.info("Комментарий с ID {} для поста с ID {} и заголовком \"{}\" был успешно обновлён. Количество символов в новом комментарии: {}",
                postId, commentId, post.getTitle(), updateCommentDto.getText().length());
    }

    @Override
    public void delete(Long postId, Long commentId) {
        Post post = postService.getById(postId);
        Comment commentToDelete = post.getComments().stream()
                .filter(com -> com.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Ошибка удаления комментария с ID {} для поста с ID {}. Комментарий не был найден у данного поста!", commentId, postId);
                    return new ObjectNotFoundException("Комментарий", commentId);
                });

        commentRepository.delete(commentToDelete.getId());
        log.info("Комментарий с ID {} для поста с ID {} и заголовком \"{}\" был успешно удалён!", commentId, postId, post.getTitle());
    }
}
