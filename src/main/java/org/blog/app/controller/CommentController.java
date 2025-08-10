package org.blog.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blog.app.entity.comment.CommentRequestDto;
import org.blog.app.service.comment.CommentService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Valid
@Controller
@RequestMapping
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("posts/{id}/comments")
    public String create(@PathVariable("id") Long id,
                         @ModelAttribute @Valid CommentRequestDto commentDto,
                         BindingResult bindingResult) throws BindException {

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        commentService.create(id, commentDto);

        return "redirect:/posts/" + id;
    }

    @PostMapping("posts/{id}/comments/{commentId}")
    public String update(@PathVariable("id") Long postId,
                         @PathVariable("commentId") Long commentId,
                         @ModelAttribute @Valid CommentRequestDto commentDto,
                         BindingResult bindingResult) throws BindException {

        if (bindingResult.hasErrors()) throw new BindException(bindingResult);
        commentService.update(postId, commentId, commentDto);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("posts/{id}/comments/{commentId}/delete")
    public String delete(@PathVariable("id") Long id,
                         @PathVariable("commentId") Long commentId) {

        commentService.delete(id, commentId);

        return "redirect:/posts/" + id;
    }
}
