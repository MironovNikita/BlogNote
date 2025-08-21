package org.blog.app.controller;

import org.blog.app.common.mapper.PostMapper;
import org.blog.app.service.comment.CommentService;
import org.blog.app.service.image.ImageService;
import org.blog.app.service.post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {PostController.class, CommentController.class})
public abstract class BaseMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected PostService postService;

    @MockitoBean
    protected ImageService imageService;

    @MockitoBean
    protected PostMapper postMapper;

    @MockitoBean
    protected CommentService commentService;
}
