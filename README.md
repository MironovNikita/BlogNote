
<p align="center">

  <img width="128" height="128" src="https://github.com/MironovNikita/BlogNote/blob/main/images/logo.png">

</p>

# 📝 Blog Note
Это веб-приложение — блог, реализованный на Java 21 с использованием Spring Framework версии 6.1 и выше (без Spring Boot), которое может работать в любом современном сервлет-контейнере (Jetty или Tomcat). Проект управляется с помощью системы сборки Maven.

## 📝 Описание
Основные функциональные возможности:

- Блог состоит из двух веб-страниц: лента постов и страница отдельного поста. Страницы реализованы с помощью HTML и JavaScript.
- Лента постов отображает посты сверху вниз с отображением превью (название, картинка, первый абзац до трёх строк), количество комментариев, лайков и тегов.
- Лента поддерживает фильтрацию по тегам и пагинацию с выбором количества постов на странице (10, 20, 50).
- На ленте есть кнопка добавления нового поста — открывается форма для ввода названия, загрузки картинки, текста и выбора тегов.
- На странице поста отображается полный контент: название, картинка, текст разбитый на абзацы, теги, а также кнопки для редактирования, удаления, добавления комментариев и проставления лайков (с автоматическим увеличением счетчика).
- Комментарии отображаются списком, без вложенности, с возможностью редактирования и удаления прямо на странице. Редактирование происходит по клику на комментарий с сохранением по Ctrl+Enter.
- Для хранения данных используется БД PostgreSQL. Встроенная база H2 применяется для интеграционного тестирования.

Приложение покрыто unit и интеграционными тестами с использованием JUnit 5 и Spring TestContext Framework, с применением кэширования контекстов.

### 🧩 Основные сущности 📇
Для реализации функционала приложения были приняты следующие сущности:
- [**Post**](https://github.com/MironovNikita/BlogNote/blob/main/src/main/java/org/blog/app/entity/post/Post.java) - отвечает за содержание основной информации о посте в приложении.
- [**Comment**](https://github.com/MironovNikita/BlogNote/blob/main/src/main/java/org/blog/app/entity/comment/Comment.java) - отвечает за содержание информации о комментарии к посту
- [**Tag**](https://github.com/MironovNikita/BlogNote/blob/main/src/main/java/org/blog/app/entity/tag/Tag.java) - отвечает за содержание информации о теге поста

Структура таблиц базы данных представлена на схеме:

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/bdschema.png">

</p>

Для запуска программы необходимо:
1) Установить БД [**PostgreSQL**](https://www.postgresql.org/download/);
2) Установить Gradle;
3) Скачать проект;
4) В консоли Gradle выполнить команду **`gradle clean build`**;
5) Перейти на **http://localhost:8080/blog-note/**;
6) Создавать посты :)

###
Начальная страница на данный момент не содержит никаких данных
<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/posts-empty.png">

</p>

Давайте создадим первый пост!

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-add-form.png">

</p>

Сразу же попадаем на страницу поста:

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-page.png">

</p>

Здесь можно менять рейтинг поста, оставлять комментарии, редактировать как сам пост, так и комментарии к нему. Также можно и удалить пост, если вдруг он вам надоел :)

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-comment.png">

</p>

Как видите, у поста появились лайки и комментарии. Также изменился счётчик комментариев. А что же происходит в БД?

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-db.png">

</p>

В БД всё отлично. Все сущности заполнились, связи между таблицами сформировались.
Так выглядит главная страница с превью созданного нами поста:

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-preview.png">

</p>

Создадим ещё несколько постов:

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/posts-full.png">

</p>

Так выглядит сортировка по тегу:

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/posts-sort.png">

</p>

### ⚠️ Важно ⚠️

В случае удаления поста комментарии удаляются полностью вместе с постом, а также связи тегов с удалённым постом. Но сами теги остаются доступными в БД. Это сделано для того, чтобы не дублировать теги, которые могут повторяться для разных постов. В таком случае просто два поста будут ссылаться на один и тот же тег.

## 🗒️ Логирование 🔍

В приложении также предусмотрено логирование. Логи пишутся непосредственно в консоль Tomcat, встроенного в наш Spring Boot. Ниже приведён пример логов:
```java
//Как видим, Tomcat стартует с нужным путём:
2025-08-21 - 22:25:35.479 (+03:00)  INFO 20764 --- [main] o.s.b.w.e.tomcat.TomcatWebServer : Tomcat started on port 8080 (http) with context path '/blog-note'
```
```java
2025-08-21 - 22:34:18.626 (+03:00)  INFO 7144 --- [http-nio-8080-exec-1] o.b.app.service.post.PostServiceImpl : Главная страница. Найдено постов: 9
2025-08-21 - 22:35:02.467 (+03:00)  INFO 7144 --- [http-nio-8080-exec-6] o.b.app.service.post.PostServiceImpl : Выполнен запрос на создание поста. ID поста: 14. Заголовок поста: "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca"
2025-08-21 - 22:35:02.472 (+03:00)  INFO 7144 --- [http-nio-8080-exec-6] o.b.app.service.post.PostServiceImpl : Выполнено сохранение тегов для поста с ID 14
2025-08-21 - 22:35:02.484 (+03:00)  INFO 7144 --- [http-nio-8080-exec-7] o.b.app.service.post.PostServiceImpl : Пост с ID 14 был найден и успешно извлечён из базы данных. Заголовок поста: "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca"
2025-08-21 - 22:35:15.288 (+03:00)  INFO 7144 --- [http-nio-8080-exec-5] o.b.app.service.post.PostServiceImpl : Рейтинг поста с ID 14 и заголовком "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca" был успешно изменён!
2025-08-21 - 22:35:15.295 (+03:00)  INFO 7144 --- [http-nio-8080-exec-2] o.b.app.service.post.PostServiceImpl : Пост с ID 14 был найден и успешно извлечён из базы данных. Заголовок поста: "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca"
2025-08-21 - 22:35:32.661 (+03:00)  INFO 7144 --- [http-nio-8080-exec-7] o.b.app.service.post.PostServiceImpl : Пост с ID 14 был найден и успешно извлечён из базы данных. Заголовок поста: "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca"
2025-08-21 - 22:35:32.663 (+03:00)  INFO 7144 --- [http-nio-8080-exec-7] o.b.a.s.comment.CommentServiceImpl : Для поста с ID 14 и заголовком "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca" был создан комментарий длиной 15 символов
2025-08-21 - 22:35:32.682 (+03:00)  INFO 7144 --- [http-nio-8080-exec-4] o.b.app.service.post.PostServiceImpl : Пост с ID 14 был найден и успешно извлечён из базы данных. Заголовок поста: "«Вильярреал» планирует укрепить вратарскую позицию голкипером «ПСЖ» — Marca"
2025-08-21 - 22:35:36.787 (+03:00)  INFO 7144 --- [http-nio-8080-exec-2] o.b.app.service.post.PostServiceImpl : Главная страница. Найдено постов: 10
2025-08-21 - 22:35:38.479 (+03:00)  INFO 7144 --- [http-nio-8080-exec-6] o.b.app.service.post.PostServiceImpl : Главная страница. Найдено постов: 5
2025-08-21 - 22:35:46.020 (+03:00)  INFO 7144 --- [http-nio-8080-exec-4] o.b.app.service.post.PostServiceImpl : Главная страница. Найдено постов: 2
```

## 🚀 Последствия перезда на Spring Boot ⚙️
Проект был перенесён со Spring Framework до Spring Boot Framework. В связи с этим, проект подвергся ряду изменений и улучшений:
1) Были убраны конфигурационные файлы, т.к. теперь Spring может это делать за нас.
2) Убрана web-составляющая, т.к. теперь приложение можно запускать из среды разработки, либо из fat-JAR.
3) Был скорректирован файл application.properties:
```java
//Из интересного: 
//Настройка вывода логов в более удобном формате для чтения
logging.pattern.console=%d{yyyy-MM-dd - HH:mm:ss.SSS (XXX)} %highlight(%5p) %clr(${PID}){magenta} --- [%clr(%t){green}] %clr(%c{36}){cyan} : %m%n
spring.output.ansi.enabled=always

//Настройка максимального размера для загружаемых файлов в наше приложение
# max file upload size
spring.servlet.multipart.max-file-size=10MB
//Настройка максимального размера входящего запроса
# max request size
spring.servlet.multipart.max-request-size=20MB
```
4) Был добавлен обработчик исключений по максимальному размеру картинки. Теперь при размерах картинок более 10Мб наше приложение выдаст привычное нам окно ошибки 🙂
```java
@ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxUploadSizeExceededException(Model model, HttpServletRequest request) {
        log.error("Ошибка для запроса: {}. Максимальный размер загружаемого файла {}", request.getRequestURI(), maxFileUploadSize);
        ApiError apiError = new ApiError(
                "Максимальный размер загружаемого файла " + maxFileUploadSize,
                HttpStatus.BAD_REQUEST.value(),
                getDateTime()
        );

        model.addAttribute(ERROR_ATTRIBUTE, apiError);
        addRefererToModel(request, model);
        return ERROR_VIEW;
    }
```
5) Основные изменения коснулись тестов. Теперь существует три контекста, которые выполняют следующие функции:
5.1. [**Контекст**](https://github.com/MironovNikita/BlogNote/blob/main/src/test/java/org/blog/app/controller/BaseIntegrationTest.java) для интеграционных тестов, проверяющих полный путь от контроллера до базы данных;

5.2 [**Контекст**](https://github.com/MironovNikita/BlogNote/blob/main/src/test/java/org/blog/app/controller/BaseMvcTest.java) для интеграционных тестов контроллеров (MVC);

5.3 [**Контекст**](https://github.com/MironovNikita/BlogNote/blob/main/src/test/java/org/blog/app/repository/BaseRepositoryTest.java) для проверки репозиториев.

## ✅ Тестирование 🐞

Как говорилось ранее, функционал приложения был протестирован как с помощью unit-тестов, так и с помощью интеграционных.
Для тестирования была выбраза база данных Н2. С её настройками можно ознакомиться [**здесь**](https://github.com/MironovNikita/BlogNote/blob/main/src/test/resources/application-test.properties).

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/tests.png">

</p>

Как видим, у нас завершается ровно 3 контекста:
```java
2025-08-21 - 22:52:58.675 (+03:00)  INFO 20112 --- [SpringApplicationShutdownHook] com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Shutdown initiated...
2025-08-21 - 22:52:58.677 (+03:00)  INFO 20112 --- [SpringApplicationShutdownHook] com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Shutdown completed.
2025-08-21 - 22:52:58.678 (+03:00)  INFO 20112 --- [SpringApplicationShutdownHook] com.zaxxer.hikari.HikariDataSource : HikariPool-2 - Shutdown initiated...
2025-08-21 - 22:52:58.679 (+03:00)  INFO 20112 --- [SpringApplicationShutdownHook] com.zaxxer.hikari.HikariDataSource : HikariPool-2 - Shutdown completed.
2025-08-21 - 22:52:58.681 (+03:00)  INFO 20112 --- [SpringApplicationShutdownHook] com.zaxxer.hikari.HikariDataSource : HikariPool-3 - Shutdown initiated...
2025-08-21 - 22:52:58.727 (+03:00)  INFO 20112 --- [SpringApplicationShutdownHook] com.zaxxer.hikari.HikariDataSource : HikariPool-3 - Shutdown completed.

Process finished with exit code 0
```

## 🛠️ Зависимости проекта ⚙️
Основные зависимости проекта:

📦 Основные (runtime)

1) **spring-boot-starter-web**
Поднимает Spring MVC + встроенный Tomcat, REST-контроллеры.

2) **spring-boot-starter-thymeleaf**
Поддержка Thymeleaf-шаблонов (рендеринг HTML).

3) **spring-boot-starter-data-jdbc**
Spring Data JDBC — работа с БД через JdbcTemplate и репозитории (без JPA).

4) **spring-boot-starter-validation**
Hibernate Validator (JSR-380, @NotNull, @Size, @Valid и т.д.).

5) **org.mapstruct:mapstruct**
Генерация мапперов (DTO ↔ Entity) во время компиляции.

6) **org.postgresql:postgresql**
JDBC-драйвер для PostgreSQL.

⚡ Компиляция (compile-only / annotation processor)

1) **lombok (compileOnly + annotationProcessor)**
Подключение Lombok-аннотаций (@Getter, @Builder и т.д.), генерирует код на этапе компиляции.

2) **mapstruct-processor (annotationProcessor)**
Аннотационный процессор, генерирующий реализации интерфейсов мапперов.

🧪 Тестирование

1) **lombok (testCompileOnly + testAnnotationProcessor)**
Чтобы и тестовые классы могли использовать Lombok.

2) **mapstruct-processor (testAnnotationProcessor)**
Чтобы MapStruct работал и в тестовых сборках.

3) **spring-boot-starter-test**
Набор для тестирования: JUnit 5, Mockito, AssertJ, Spring TestContext.

4) **com.h2database:h2**
In-memory БД H2 для тестов (чтобы не гонять PostgreSQL).

С полным перечнем зависимостей можно ознакомиться [**здесь**](https://github.com/MironovNikita/BlogNote/blob/main/build.gradle).

Результаты сборки проекта:
```java
> Task :test
--------------------------------------------------
Тестов всего: 117
Успешно:      117
Провалено:    0
Пропущено:    0
Результат:    SUCCESS
--------------------------------------------------

> Task :check
> Task :build

BUILD SUCCESSFUL in 8s
9 actionable tasks: 9 executed
23:05:15: Execution finished 'clean build'.
```

