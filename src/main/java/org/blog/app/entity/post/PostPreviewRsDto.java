package org.blog.app.entity.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blog.app.entity.tag.Tag;

import java.util.Set;

//TODO Скорее всего понадобится урезанная версия Post в методе getAll
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostPreviewRsDto {

    private String title;

    private String imageBase64;

    //TODO Сократить в маппере
    private String text;

    private Long comments;

    private Long likesCount;

    private Set<Tag> tags;
}
