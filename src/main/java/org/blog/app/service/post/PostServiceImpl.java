package org.blog.app.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.common.mapper.PostMapper;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.repository.post.PostRepository;
import org.blog.app.repository.tag.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public Long create(PostRequestDto postRqDto) {
        Post post = postMapper.toPost(postRqDto);
        post.setLikesCount(0L);
        Long postId = postRepository.create(post);

        log.info("Выполнен запрос на создание поста. ID поста: {}. Заголовок поста: \"{}\"", postId, post.getTitle());

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            tagRepository.saveTags(post.getTags(), postId);
            log.info("Выполнено сохранение тегов для поста с ID {}", postId);
        }

        return postId;
    }

    @Override
    @Transactional
    public void update(Long id, PostRequestDto postRqDto) {

        Post existingPost = postRepository.getById(id).orElseThrow(() -> {
            log.error("Обновление поста с ID {} не выполнено. Пост не был найден!", id);
            return new ObjectNotFoundException("Пост", id);
        });
        Post postToUpdate = postMapper.toPost(postRqDto);
        postRepository.update(postToUpdate, existingPost);

        log.info("Выполнен запрос на обновление поста. ID поста: {}. Новый заголовок поста: {}", id, postToUpdate.getTitle());

        if (postRqDto.getTags() != null && !postRqDto.getTags().isEmpty()) {
            tagRepository.updateTags(postToUpdate.getTags(), existingPost.getId());
            log.info("Выполнено обновление тегов для поста с ID {} и заголовком {}", id, postToUpdate.getTitle());
        }
    }

    @Override
    public Post getById(Long id) {
        Post existingPost = postRepository.getById(id).orElseThrow(() -> {
            log.error("Получение поста по ID {} не выполнено. Пост не был найден!", id);
            return new ObjectNotFoundException("Пост", id);
        });
        existingPost.setTags(tagRepository.getTagsByPostId(id));
        existingPost.setComments(commentRepository.getAllByPostId(id));
        log.info("Пост с ID {} был найден и успешно извлечён из базы данных. Заголовок поста: \"{}\"", id, existingPost.getTitle());
        return existingPost;
    }

    public List<PostResponseDto> getAllByParams(String search, int pageSize, int pageNumber) {
        int offset = (pageNumber - 1) * pageSize;
        List<PostResponseDto> foundPosts = postRepository.getAllByParams(search, pageSize, offset)
                .stream()
                .map(postMapper::toPostRsDto)
                .toList();

        log.info("Главная страница. Найдено постов: {}", foundPosts.size());
        return foundPosts;
    }

    public boolean hasNextPage(String search, int pageSize, int pageNumber) {
        int offset = pageNumber * pageSize;
        return postRepository.hasNextPage(search, pageSize, offset);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Post existingPost = postRepository.getById(id).orElseThrow(() -> {
            log.error("Ошибка удаления поста с ID {}. Пост не был найден!", id);
            return new ObjectNotFoundException("Пост", id);
        });
        postRepository.delete(id);
        log.info("Пост с ID {} и заголовком \"{}\" был удалён!", id, existingPost.getTitle());
    }

    @Override
    @Transactional
    public void changeRating(Boolean like, Long id) {
        Post existingPost = postRepository.getById(id).orElseThrow(() -> {
            log.error("Ошибка изменения рейтинга поста с ID {}. Пост не был найден!", id);
            return new ObjectNotFoundException("Пост", id);
        });

        var likes = existingPost.getLikesCount();
        existingPost.setLikesCount(Boolean.TRUE.equals(like) ? likes + 1 : likes - 1);
        postRepository.updateRating(existingPost);
        log.info("Рейтинг поста с ID {} и заголовком \"{}\" был успешно изменён!", id, existingPost.getTitle());
    }
}
