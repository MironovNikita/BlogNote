package org.blog.app.common.exception;

public class ObjectCreationException extends RuntimeException {
    public ObjectCreationException(String object, String field) {
        super(String.format("Ошибка создания %s: поле \"%s\" некорректно!", object, field));
    }
}
