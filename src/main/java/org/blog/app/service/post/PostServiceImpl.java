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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//TODO Добавить логи и интерфейс!
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

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            tagRepository.saveTags(post.getTags(), postId);
        }

        return postId;
    }

    @Override
    @Transactional
    public void update(Long id, PostRequestDto postRqDto) {

        Post existingPost = postRepository.getById(id).orElseThrow(() -> new ObjectNotFoundException("Пост", id));
        Post postToUpdate = postMapper.toPost(postRqDto);
        postRepository.update(postToUpdate, existingPost);

        if (postRqDto.getTags() != null && !postRqDto.getTags().isEmpty()) {
            tagRepository.updateTags(postToUpdate.getTags(), existingPost.getId());
        }
    }

    @Override
    public Post getById(Long id) {
        Post existingPost = postRepository.getById(id).orElseThrow(() -> new ObjectNotFoundException("Пост", id));
        existingPost.setTags(tagRepository.getTagsByPostId(id));
        existingPost.setComments(commentRepository.getAllByPostId(id));
        return existingPost;
    }

    public List<PostResponseDto> getAllByParams(String search, int pageSize, int pageNumber) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            postRepository.getById(id);
            postRepository.delete(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("Пост", id);
        }
    }

    @Override
    @Transactional
    public void changeRating(Boolean like, Long id) {
        Post existingPost = postRepository.getById(id).orElseThrow(() -> new ObjectNotFoundException("Пост", id));

        var likes = existingPost.getLikesCount();
        existingPost.setLikesCount(Boolean.TRUE.equals(like) ? likes + 1 : likes - 1);
        postRepository.updateRating(existingPost);
    }
}
