package org.blog.app.entity.comment;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    @Size(min = 10, max = 2000, message = "Размер комментария должен составлять от 10 до 2000 символов!")
    private String text;
}
