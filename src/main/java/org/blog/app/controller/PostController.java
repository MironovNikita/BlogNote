package org.blog.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blog.app.entity.post.Post;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ImageService imageService;

    @GetMapping("/")
    public String postsRedirect() {
        return "redirect:/posts";
    }

    @GetMapping("/posts")
    public String postsPage(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            Model model
    ) {
        List<Post> posts = postService.findPosts(search, pageSize, pageNumber);
        // Для определения есть ли следующая и предыдущая страницы, можно проверить общее число постов или отдать это вместе с постами
        boolean hasNext = postService.hasNextPage(search, pageSize, pageNumber);
        boolean hasPrevious = pageNumber > 1;

        // Добавляем данные в модель для шаблона
        model.addAttribute("posts", posts);
        model.addAttribute("search", search);

        Map<String, Object> paging = new HashMap<>();
        paging.put("pageNumber", pageNumber);
        paging.put("pageSize", pageSize);
        paging.put("hasNext", hasNext);
        paging.put("hasPrevious", hasPrevious);

        model.addAttribute("paging", paging);

        return "posts";
    }

    @GetMapping("/posts/{id}")
    public String get(@PathVariable("id") Long id, Model model) {
        PostResponseDto postRs = postService.getPost(id);
        model.addAttribute("post", postRs);

        return "post";
    }

    @GetMapping("/posts/add")
    public String postAddPage() {
        return "add-post";
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String create(@ModelAttribute @Valid PostRequestDto postRqDto) {
        var createdPostId = postService.createPost(postRqDto);
        return "redirect:/posts/" + createdPostId;
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImageBytes(@PathVariable("id") Long id) {
        byte[] imageBytes = imageService.getImageBytesByPostId(id);

        //TODO Перенести логику в сервис
        //if (imageBytes == null || imageBytes.length == 0) {
        //    return ResponseEntity.notFound().build();
        //}

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)  // MediaType.IMAGE_PNG и т.п.
                .body(imageBytes);
    }

    @PostMapping("/posts/{id}/like")
    public String changeRating(@PathVariable("id") Long postId, @RequestParam("like") Boolean isLike) {

        postService.changeRating(postId, isLike);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{id}/edit")
    public String postEdit(@PathVariable Long id) {
        // Просто переводим на изменение поста
        return "redirect:/posts/" + id + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        PostResponseDto post = postService.getPost(id);

        model.addAttribute("post", post);
        return "post-add";
    }

    @PostMapping(value = "/posts/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String update(@PathVariable("id") Long id, @ModelAttribute @Valid PostRequestDto postRqDto) {
        postService.update(id, postRqDto);

        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        postService.delete(id);

        return "redirect:/posts";
    }
}
