package org.blog.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blog.app.common.mapper.PostMapper;
import org.blog.app.entity.paging.Paging;
import org.blog.app.entity.post.PostRequestDto;
import org.blog.app.entity.post.PostResponseDto;
import org.blog.app.service.image.ImageService;
import org.blog.app.service.post.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Valid
@Controller
@RequestMapping
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ImageService imageService;
    private final PostMapper postMapper;

    //ЗДЕСЬ ВСЁ НОРМАЛЬНО
    @GetMapping("/")
    public String postsRedirect() {
        return "redirect:/posts";
    }


    @GetMapping("/posts")
    public String postsPage(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
            Model model
    ) {
        List<PostResponseDto> posts = postService.getAllByParams(search, pageSize, pageNumber);

        boolean hasNext = postService.hasNextPage(search, pageSize, pageNumber);
        boolean hasPrevious = pageNumber > 1;

        model.addAttribute("posts", posts);
        model.addAttribute("search", search);

        Paging paging = new Paging(pageNumber, pageSize, hasNext, hasPrevious);
        model.addAttribute("paging", paging);

        return "posts";
    }

    @GetMapping("/posts/{id}")
    public String get(@PathVariable("id") Long id, Model model) {
        PostResponseDto postRs = postMapper.toPostRsDto(postService.getById(id));
        model.addAttribute("post", postRs);

        return "post";
    }

    @GetMapping("/posts/add")
    public String postAddPage() {
        return "add-post";
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String create(@ModelAttribute @Valid PostRequestDto postRqDto) {
        var createdPostId = postService.create(postRqDto);
        return "redirect:/posts/" + createdPostId;
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImageBytes(@PathVariable("id") Long id) {
        byte[] imageBytes = imageService.getImageBytesByPostId(id);

        if (imageBytes == null || imageBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)  // MediaType.IMAGE_PNG и т.п.
                .body(imageBytes);
    }

    @PostMapping("/posts/{id}/like")
    public String changeRating(@PathVariable("id") Long postId, @RequestParam("like") Boolean isLike) {

        postService.changeRating(isLike, postId);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{id}/edit")
    public String postEdit(@PathVariable Long id) {
        // Просто переводим на изменение поста
        return "redirect:/posts/" + id + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        PostResponseDto post = postMapper.toPostRsDto(postService.getById(id));

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
