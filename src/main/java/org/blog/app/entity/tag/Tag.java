package org.blog.app.entity.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
public class Tag {

    private Long id;

    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
