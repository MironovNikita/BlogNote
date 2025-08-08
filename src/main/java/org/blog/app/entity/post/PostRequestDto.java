package org.blog.app.entity.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 10, max = 150, message = "Размер заголовка должен составлять от 10 до 150 символов")
    private String title;

    @NotBlank(message = "Текст поста не может быть пустым")
    @Size(min = 10, max = 5000, message = "Размер поста должен составлять от 10 до 5000 символов")
    private String text;

    private MultipartFile image;

    private String tags;
}
