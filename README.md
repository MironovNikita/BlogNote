
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
1) Установить [**Apache Tomcat**](https://tomcat.apache.org/download-90.cgi);
2) Установить БД [**PostgreSQL**](https://www.postgresql.org/download/);
3) Установить Maven;
4) Скачать проект;
5) Через PgAdmin или др. программу выполнить скрипт [**schema.sql**](https://github.com/MironovNikita/BlogNote/blob/main/src/main/resources/schema.sql);
6) В консоли Maven выполнить команду **`mvn clean package`**;
7) Поместить собранный war-файл в папку Tomcat **`webapps`**.
8) Запустить файл startup.bat (Windows) из папки bin Tomcat.
9) Перейти на **http://localhost:8080/**;
10) Зайти в пункт **`Manager App`**;
11) Выбрать приложение :)

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

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-add-form.png">

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

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-full.png">

</p>

Так выглядит сортировка по тегу:

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/post-sort.png">

</p>

### ⚠️ Важно ⚠️

В случае удаления поста комментарии удаляются полностью вместе с постом, а также связи тегов с удалённым постом. Но сами теги остаются доступными в БД. Это сделано для того, чтобы не дублировать теги, которые могут повторяться для разных постов. В таком случае просто два поста будут ссылаться на один и тот же тег.

## 🗒️ Логирование 🔍

В приложении также предусмотрено логирование. Логи пишутся в файл в корневой папке Tomcat -> blogNote-logs. Ниже приведён пример логов:
```java
2025-08-13 01:26:16 [http-nio-8080-exec-1] INFO  o.b.a.s.comment.CommentServiceImpl - Для поста с ID 1 и заголовком На 3-ей форме Реала вышита фраза "90 минут на «Бернабеу» – это очень долго" был создан комментарий длиной 17 символов
2025-08-13 01:26:16 [http-nio-8080-exec-3] INFO  o.b.app.service.post.PostServiceImpl - Пост с ID 1 был найден и успешно извлечён из базы данных. Заголовок поста: На 3-ей форме Реала вышита фраза "90 минут на «Бернабеу» – это очень долго"
2025-08-13 01:26:16 [http-nio-8080-exec-10] INFO  o.b.a.service.image.ImageServiceImpl - Для поста с ID 1 был получен массив байт картинки размером 581438
2025-08-13 01:26:21 [http-nio-8080-exec-2] INFO  o.b.app.service.post.PostServiceImpl - Главная страница. Найдено постов: 1
2025-08-13 01:26:21 [http-nio-8080-exec-4] INFO  o.b.a.service.image.ImageServiceImpl - Для поста с ID 1 был получен массив байт картинки размером 581438
2025-08-13 01:29:13 [http-nio-8080-exec-1] INFO  o.b.app.service.post.PostServiceImpl - Выполнен запрос на создание поста. ID поста: 2. Заголовок поста: Музыкальные сервисы начали отказываться от созданной нейросетями музыки
2025-08-13 01:29:13 [http-nio-8080-exec-1] INFO  o.b.app.service.post.PostServiceImpl - Выполнено сохранение тегов для поста с ID 2 и заголовком: Музыкальные сервисы начали отказываться от созданной нейросетями музыки
2025-08-13 01:29:13 [http-nio-8080-exec-3] INFO  o.b.app.service.post.PostServiceImpl - Пост с ID 2 был найден и успешно извлечён из базы данных. Заголовок поста: Музыкальные сервисы начали отказываться от созданной нейросетями музыки
2025-08-13 01:29:13 [http-nio-8080-exec-10] INFO  o.b.a.service.image.ImageServiceImpl - Для поста с ID 2 был получен массив байт картинки размером 0
2025-08-13 01:29:20 [http-nio-8080-exec-2] INFO  o.b.app.service.post.PostServiceImpl - Рейтинг поста с ID 2 и заголовком Музыкальные сервисы начали отказываться от созданной нейросетями музыки был успешно изменён!
```

## ✅ Тестирование 🐞

Как говорилось ранее, функционал приложения был протестирован как с помощью unit-тестов, так и с помощью интеграционных. Полностью проверен путь от контроллеров до репозиториев и БД. Также отдельно протестированы классы репозиториев и их взаимодействие с БД. Сервисный слой ограничился unit-тестами.
Для тестирования была выбраза база данных Н2. С её настройками можно ознакомиться [**здесь**](https://github.com/MironovNikita/BlogNote/blob/main/src/test/resources/test-application.properties).

<p align="center">

  <img src="https://github.com/MironovNikita/BlogNote/blob/main/images/tests.png">

</p>

## 🛠️ Зависимости проекта ⚙️
Основные зависимости проекта:

1) **`spring-webmvc (org.springframework:spring-webmvc)`**
Основная библиотека Spring MVC. Позволяет создавать веб-приложения с использованием паттерна MVC, обрабатывать HTTP-запросы, управлять контроллерами, рендерить представления и работать с REST.

2) **`spring-test (org.springframework:spring-test)`**
Предоставляет средства для интеграционного и модульного тестирования компонентов Spring. Позволяет загружать Spring-контекст в тестах, предоставляет поддержку MockMvc и другие инструменты для тестирования.

3) **`jakarta.servlet-api (jakarta.servlet:jakarta.servlet-api)`**
Библиотека с API сервлетов, необходимых для работы веб-приложений на сервлетах (например, в Tomcat или Jetty). Обычно используется в режиме provided, так как контейнер сервлетов сам предоставляет её.

4) **`junit-jupiter-api и junit-jupiter-engine (org.junit.jupiter)`**
Библиотеки JUnit 5 — ядро и движок тестирования. С их помощью пишут и запускают юнит и интеграционные тесты на Java с использованием новой архитектуры JUnit 5.

5) **`mockito-core (org.mockito:mockito-core)`**
Фреймворк для создания моков (заглушек) в тестах. Позволяет изолировать тестируемые компоненты, подменяя зависимости имитациями для удобного тестирования.

6) **`hamcrest (org.hamcrest:hamcrest)`**
Библиотека для написания читаемых и выразительных утверждений (assertions) в тестах. В сочетании с JUnit или Mockito позволяет делать проверки более наглядными.

7) **`jakarta.validation-api и hibernate-validator`**
API и реализация спецификации Bean Validation (JSR 380). Позволяют валидировать Java-объекты с помощью аннотаций, например, @NotNull, @Size и др., что полезно для проверки данных на уровне модели.

8) **`thymeleaf и thymeleaf-spring6`**
Шаблонизатор для генерации HTML-страниц на сервере. Интегрируется со Spring (версия spring6 compatible), позволяет удобно создавать и рендерить UI страницы из шаблонов с поддержкой выражений и условной логики.

9) **`lombok (org.projectlombok:lombok)`**
Утилита для сокращения шаблонного кода в Java. Позволяет генерировать геттеры, сеттеры, конструкторы, equals/hashCode и др. аннотациями, уменьшая объём кода и улучшая читаемость.

10) **`h2 (com.h2database:h2)`**
Встраиваемая реляционная база данных в памяти. Используется в тестах или для простых приложений, когда не требуется полноценная внешняя база. Поддерживает режим совместимости с PostgreSQL и другие.

А также:
- **`postgresql`** — драйвер для работы с базой PostgreSQL.

- **`spring-data-jdbc`** — Spring Data для удобной работы с JDBC (без JPA).

- **`mapstruct + mapstruct-processor`** — библиотека и процессор для генерации мапперов (преобразования DTO в сущности и обратно).

- **`logback-classic`** — библиотека для логирования, реализует логирование по стандарту SLF4J с поддержкой конфигураций на XML или Groovy.

С полным перечнем зависимостей можно ознакомиться [**здесь**](https://github.com/MironovNikita/BlogNote/blob/main/pom.xml).

Результаты сборки проекта:
```java
[INFO] Results:
[INFO] 
[INFO] Tests run: 90, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- war:3.4.0:war (default-war) @ BlogNote ---
[INFO] Packaging webapp
[INFO] Assembling webapp [BlogNote] in [C:*\BlogNote\target\blog-note]
[INFO] Processing war project
[INFO] Copying webapp resources [C:*\BlogNote\src\main\webapp]
[INFO] Building war: C:*\BlogNote\target\blog-note.war
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.883 s
[INFO] Finished at: 2025-08-13T02:05:29+03:00
[INFO] ------------------------------------------------------------------------

```

