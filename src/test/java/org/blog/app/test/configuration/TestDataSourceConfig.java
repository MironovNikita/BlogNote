package org.blog.app.test.configuration;

import org.blog.app.repository.comment.CommentRepository;
import org.blog.app.repository.comment.CommentRepositoryImpl;
import org.blog.app.repository.post.PostRepository;
import org.blog.app.repository.post.PostRepositoryImpl;
import org.blog.app.repository.tag.TagRepository;
import org.blog.app.repository.tag.TagRepositoryImpl;
import org.h2.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;

@Configuration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@PropertySource("classpath:test-application.properties")
public class TestDataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource("test-schema.sql"));

        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
        return dataSourceInitializer;
    }

    @Bean
    public CommentRepository commentRepository(JdbcTemplate jdbcTemplate) {
        return new CommentRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public PostRepository postRepository(JdbcTemplate jdbcTemplate) {
        return new PostRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public TagRepository tagRepository(JdbcTemplate jdbcTemplate) {
        return new TagRepositoryImpl(jdbcTemplate);
    }
}
