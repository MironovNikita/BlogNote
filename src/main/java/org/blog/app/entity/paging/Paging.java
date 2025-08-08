package org.blog.app.entity.paging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Paging {

    private int pageNumber;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
