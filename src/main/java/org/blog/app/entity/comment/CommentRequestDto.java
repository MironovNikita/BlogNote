package org.blog.app.entity.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {

    @Size(min = 10, max = 2000, message = "Размер комментария должен составлять от 10 до 2000 символов!")
    @NotBlank(message = "Текст комментария не может быть пустым!")
    private String text;
}
