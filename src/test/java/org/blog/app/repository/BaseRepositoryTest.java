package org.blog.app.repository;

import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.repository.post.PostRepository;
import org.blog.app.repository.tag.TagRepository;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PostRepository.class, CommentRepository.class, TagRepository.class}))
public abstract class BaseRepositoryTest {
}
