package org.blog.app.repository;

import org.blog.app.common.exception.ObjectNotFoundException;
import org.blog.app.entity.post.Post;
import org.blog.app.repository.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PostRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM posts_comments");
        jdbcTemplate.execute("DELETE FROM posts_tags");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM comments");

        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE tags ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("Успешное создание поста")
    void shouldCreatePost() {
        Post post = createPost();
        Long postId = postRepository.create(post);
        post.setId(postId);

        Post createdPost = postRepository.getById(postId).orElseThrow(() -> new ObjectNotFoundException("Пост", postId));

        assertEquals(postId, createdPost.getId());
        assertEquals(post.getTitle(), createdPost.getTitle());
        assertEquals(post.getText(), createdPost.getText());
        assertEquals(post.getImageData(), createdPost.getImageData());
        assertEquals(post.getLikesCount(), createdPost.getLikesCount());
    }

    @Test
    @DisplayName("Успешное обновление поста")
    void shouldUpdatePost() {
        Post existingPost = createPost();
        Long postId = postRepository.create(existingPost);
        existingPost.setId(postId);

        Post updatingPost = new Post();
        updatingPost.setText("Новый новейший текст");
        updatingPost.setTitle("Новый новейший заголовок");

        postRepository.update(updatingPost, existingPost);

        existingPost = postRepository.getById(postId).orElseThrow(() -> new ObjectNotFoundException("Пост", postId));
        assertEquals(updatingPost.getTitle(), existingPost.getTitle());
        assertEquals(updatingPost.getText(), existingPost.getText());
        assertEquals(updatingPost.getTags(), existingPost.getTags());
    }

    @Test
    @DisplayName("Успешное получение поста по ID")
    void shouldGetPostById() {
        Post post = createPost();
        Long postId = postRepository.create(post);
        post.setId(postId);

        Post gotPost = postRepository.getById(postId).orElseThrow(() -> new ObjectNotFoundException("Пост", postId));
        assertEquals(post, gotPost);
    }

    @Test
    @DisplayName("Успешное удаление поста по ID")
    void shouldDeletePostById() {
        Post post = createPost();
        Long postId = postRepository.create(post);
        post.setId(postId);

        postRepository.delete(postId);
        assertEquals(postRepository.getById(postId), Optional.empty());
    }

    @Test
    @DisplayName("Успешное обновление рейтинга поста")
    void shouldChangePostRating() {
        Post post = createPost();
        Long postId = postRepository.create(post);
        post.setId(postId);
        post.setLikesCount(post.getLikesCount() + 1);

        postRepository.updateRating(post);

        assertEquals(post.getLikesCount(), postRepository.getById(postId).orElseThrow().getLikesCount());
    }

    @Test
    @DisplayName("Успешное получение постов по параметрам")
    void shouldGetAllByParams() {
        Post post1 = createPost();
        Post post2 = createPost();
        Post post3 = createPost();

        postRepository.create(post1);
        postRepository.create(post2);
        postRepository.create(post3);

        post1 = postRepository.getById(1L).orElseThrow();
        post1.setTags(new ArrayList<>());
        post1.setComments(new ArrayList<>());
        post2 = postRepository.getById(2L).orElseThrow();
        post2.setTags(new ArrayList<>());
        post2.setComments(new ArrayList<>());
        post3 = postRepository.getById(3L).orElseThrow();
        post3.setTags(new ArrayList<>());
        post3.setComments(new ArrayList<>());
        List<Post> originalPosts = List.of(post1, post2, post3);

        List<Post> gotPosts = postRepository.getAllByParams("", 5, 0);
        assertEquals(gotPosts.size(), originalPosts.size());
        assertTrue(gotPosts.containsAll(originalPosts));
    }

    @Test
    @DisplayName("Успешная проверка наличия следующей страницы постов")
    void shouldGetHasNextPageParam() {
        Post post1 = createPost();
        Post post2 = createPost();
        Post post3 = createPost();

        postRepository.create(post1);
        postRepository.create(post2);
        postRepository.create(post3);

        assertTrue(postRepository.hasNextPage("", 1, 0));
    }

    @Test
    @DisplayName("Успешное получение массива байт картинки для поста по его ID")
    void shouldGetImageDataByPostId() {
        Post post = createPost();
        post.setImageData(new byte[]{1, 2, 3});
        Long postId = postRepository.create(post);
        post.setId(postId);

        byte[] gotBytes = postRepository.findImageDataByPostId(postId).orElseThrow();

        assertArrayEquals(post.getImageData(), gotBytes);
    }

    private Post createPost() {
        Post post = new Post();
        post.setTitle("Тестовый заголовок");
        post.setText("Тестовый текст");
        post.setLikesCount(0L);

        return post;
    }
}
