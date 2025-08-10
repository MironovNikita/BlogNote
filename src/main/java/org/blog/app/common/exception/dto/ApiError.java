package org.blog.app.common.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiError {
    private String errorMessage;

    private int statusCode;

    private String time;
}
