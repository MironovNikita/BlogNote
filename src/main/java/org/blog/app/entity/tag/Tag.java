package org.blog.app.entity.tag;

import lombok.*;

@Data
@AllArgsConstructor
public class Tag {

    private Long id;

    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
