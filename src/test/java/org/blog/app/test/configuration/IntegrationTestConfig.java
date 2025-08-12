package org.blog.app.test.configuration;

import org.blog.app.controller.comment.CommentController;
import org.blog.app.controller.post.PostController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackageClasses = {CommentController.class, PostController.class})
@Import({TestDataSourceConfig.class, TestServiceConfig.class, TestWebMvcConfig.class})
public class IntegrationTestConfig {
}
