package org.blog.app.test.configuration;

import org.blog.app.common.mapper.CommentMapper;
import org.blog.app.common.mapper.PostMapper;
import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.repository.post.PostRepository;
import org.blog.app.repository.tag.TagRepository;
import org.blog.app.service.comment.CommentService;
import org.blog.app.service.comment.CommentServiceImpl;
import org.blog.app.service.image.ImageService;
import org.blog.app.service.image.ImageServiceImpl;
import org.blog.app.service.post.PostService;
import org.blog.app.service.post.PostServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class TestServiceConfig {

    @Bean
    public CommentMapper commentMapper() {
        return mock(CommentMapper.class);
    }

    @Bean
    public PostMapper postMapper(CommentMapper commentMapper) {
        return new PostMapper(commentMapper);
    }

    @Bean
    public PostService postService(PostRepository postRepository,
                                   TagRepository tagRepository,
                                   CommentRepository commentRepository,
                                   PostMapper postMapper) {
        return new PostServiceImpl(postRepository, tagRepository, commentRepository, postMapper);
    }

    @Bean
    public CommentService commentService(CommentRepository commentRepository,
                                         CommentMapper commentMapper,
                                         PostService postService) {
        return new CommentServiceImpl(commentRepository, commentMapper, postService);
    }

    @Bean
    public ImageService imageService(PostRepository postRepository) {
        return new ImageServiceImpl(postRepository);
    }
}
