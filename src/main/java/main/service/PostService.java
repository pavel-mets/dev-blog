package main.service;

import main.api.responses.CommentDTO;
import main.api.responses.GenericResponseObject;
import main.api.responses.PostsDTO;
import main.exceptions.NotFoundException;
import main.exceptions.UnauthorizedException;
import main.model.*;
import main.repository.PostRepository;
import main.repository.Tag2PostRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private Tag2PostRepository tag2PostRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TagsService tagsService;

    //метод получения всех видимых постов (is_active = 1; time < текущей даты и времени; moderation_satus = ACCEPTED)
    public PostsDTO getVisiblePosts(Integer offset, Integer limit, String mode) {
        Page<Post> posts = null;
        if (mode.equals("recent")) posts = postRepository.findPostsOrderByTime(PageRequest.of(offset / limit, limit, Sort.by("time").descending()));
        if (mode.equals("popular")) posts = postRepository.findPostsOrderByCommentCount(PageRequest.of(offset / limit, limit));
        if (mode.equals("best")) posts = postRepository.findPostsOrderByLikes(PageRequest.of(offset / limit, limit));
        if (mode.equals("early")) posts = postRepository.findPostsOrderByTime(PageRequest.of(offset / limit, limit, Sort.by("time").ascending()));
        return convertToPostsDTO(posts);
    }

    //метод получения видимых постов по запросу query
    public PostsDTO getPostsByQuery(Integer offset, Integer limit, String query) {
        Page<Post> posts = postRepository.findPostsByQuery(PageRequest.of(offset / limit, limit), query);
        return convertToPostsDTO(posts);
    }

    //метод получения постов по дате
    public PostsDTO getPostsByDate(Integer offset, Integer limit, Date date) {
        Page<Post> posts = postRepository.findPostsByDate(PageRequest.of(offset / limit, limit), date);
        return convertToPostsDTO(posts);
    }

    //метод получения постов по тегу
    public PostsDTO getPostsByTag(Integer offset, Integer limit, String tag) {
        Page<Post> posts = postRepository.findPostsByTag(PageRequest.of(offset / limit, limit), tag);
        return convertToPostsDTO(posts);
    }

    //метод получения постов, требующих модерации, а так же "отмодерированных лично мной"
    public PostsDTO findModerationPosts(Integer offset, Integer limit, String status) {
        //получаем текущего пользователя
        User user = authService.getCurrentUser();
        //получаем список постов из БД
        Page<Post> posts = postRepository.findModerationPosts(PageRequest.of(offset / limit, limit), Post.ModStatus.valueOf(status.toUpperCase()), user);
        return convertToPostsDTO(posts);
    }

    //метод получения "моих" постов
    public PostsDTO findMyPosts(Integer offset, Integer limit, String status) {
        //получаем текущего пользователя
        User user = authService.getCurrentUser();
        boolean isActive = false;
        Post.ModStatus moderationStatus = null;
        if (status.equals("inactive")) {isActive = false; moderationStatus = Post.ModStatus.NEW;}
        if (status.equals("pending")) {isActive = true; moderationStatus = Post.ModStatus.NEW;}
        if (status.equals("declined")) {isActive = true; moderationStatus = Post.ModStatus.DECLINED;}
        if (status.equals("published")) {isActive = true; moderationStatus = Post.ModStatus.ACCEPTED;}
        Page<Post> posts = postRepository.findMyPosts(PageRequest.of(offset / limit, limit), moderationStatus, isActive, user);
        return convertToPostsDTO(posts);
    }

    //метод приведения списка полученных постов из БД к формату вывода
    private PostsDTO convertToPostsDTO(Page<Post> posts) {
        //приведение полученного списка к формату ответа
        PostsDTO postsDTO = new PostsDTO();
        for (Post post: posts) {
            //подготовка анонса для поста, удаляем html теги
            String announce = post.getText().replaceAll("<.+?>", "");
            //заполняем user'a у поста
            PostsDTO.User userDTO = PostsDTO.User.builder()
                    .id(post.getUserId().getId())
                    .name(post.getUserId().getName())
                    .build();
            PostsDTO.Post postDTO = PostsDTO.Post.builder()
                    .id(post.getId())
                    .timestamp(post.getTime().toEpochSecond(OffsetDateTime.now().getOffset()))
                    .title(post.getTitle())
                    .announce(announce.length() > 146 ? announce.substring(0, 146) + "..." : announce)
                    .likeCount(post.getPostVotes().stream().filter(postVote -> postVote.getValue() == 1).count())
                    .dislikeCount(post.getPostVotes().stream().filter(postVote -> postVote.getValue() == -1).count())
                    .commentCount(post.getPostComments().size())
                    .viewCount(post.getViewCount())
                    .user(userDTO)
                    .build();
            //добавляем пост в список для ответа
            postsDTO.getPosts().add(postDTO);
        }
        //устанавливаем поле count у ответа
        postsDTO.setCount(posts.getTotalElements());
        return postsDTO;
    }

    //метод получения одного поста и приведения его к формату вывода
    public PostsDTO.Post getPostById (int id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new NotFoundException());
        //проверяем опубликован пост или нет для установки соответствующего поля в ответе
        boolean postActive = false;
        if (post.isActive() &&
            post.getModerationStatus() == Post.ModStatus.ACCEPTED &&
            post.getTime().isBefore(LocalDateTime.now())) postActive = true;
        //получаем текущего пользователя
        User user = null;
        try {
            //если не выбросится исключение UnauthorizedException, то пользователь авторизован
            user = authService.getCurrentUser();
            //если пользователь не авторизованный модератор или не автор поста, то счетчик просмотров увеличиваем на 1
            if (!(user.isModerator() || user.getId() == post.getUserId().getId())) {
                post.setViewCount(post.getViewCount() + 1);
                postRepository.save(post);
            }
        }
        catch (UnauthorizedException ex) {
            //если не получили пользователя, то он не авторизован
            //прибавляем к счетчику просмотров 1
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }
        //подготовка ответа
        PostsDTO.User userDTO = PostsDTO.User.builder()
                .id(post.getUserId().getId())
                .name(post.getUserId().getName())
                .build();
        PostsDTO.Post postDTO = PostsDTO.Post.builder()
                .id(post.getId())
                .timestamp(post.getTime().toEpochSecond(OffsetDateTime.now().getOffset()))
                .active(postActive)
                .title(post.getTitle())
                .text(post.getText())
                .likeCount(post.getPostVotes().stream().filter(postVote -> postVote.getValue() == 1).count())
                .dislikeCount(post.getPostVotes().stream().filter(postVote -> postVote.getValue() == -1).count())
                .comments(getCommentsByPost(post))
                .viewCount(post.getViewCount())
                .user(userDTO)
                .tags(post.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()))
                .build();

        return postDTO;
    }

    //метод получения комментариев к постам
    public List<CommentDTO> getCommentsByPost(Post post) {
        List<PostComments> comments = post.getPostComments();
        List<CommentDTO> commentsDTO = new ArrayList<>();
        for (PostComments comment : comments) {
            CommentDTO.User user = CommentDTO.User.builder()
                    .id(comment.getUser().getId())
                    .name(comment.getUser().getName())
                    .photo(comment.getUser().getPhoto())
                    .build();
            CommentDTO commentDTO = CommentDTO.builder()
                    .id(comment.getId())
                    .timestamp(comment.getTime().toEpochSecond(OffsetDateTime.now().getOffset()))
                    .text(comment.getText())
                    .user(user)
                    .build();
            commentsDTO.add(commentDTO);
        }
        return commentsDTO;
    }

    //метод записи поста в БД, а так же тегов и связывающей таблицы
    public GenericResponseObject checkAndSavePost(PostsDTO.Post postDTO){
        //проверка на полноту заполнения поста
        GenericResponseObject errors = postCheck(postDTO);
        if (errors != null) return errors;
        //если ошибок нет, то готовим пост к записи
        //Переменная для хранения времени публикации, для краткости дальнейшей записи
        LocalDateTime postTime = LocalDateTime.ofEpochSecond(postDTO.getTimestamp(), 0, UTC);
        //Формируем пост
        Post post = Post.builder()
                .isActive(postDTO.getActive())
                .moderationStatus(Post.ModStatus.NEW)
                .text(postDTO.getText())
                .time(postTime.isAfter(LocalDateTime.now()) ? postTime : LocalDateTime.now())
                .title(postDTO.getTitle())
                .viewCount(0)
                .userId(authService.getCurrentUser())
                .build();
        //Записываем пост в БД
        postRepository.save(post);
        //Формируем сущности Tag2post (список объектов {Post, Tag})
        List<Tag2Post> tag2PostList = new ArrayList<>();
        for (String tagName : postDTO.getTags()) {
            Optional<Tag> optionalTag = tagRepository.findByName(tagName.toLowerCase());
            Tag tag;
            //ищем в БД Tag или создаем новый, если он не существует
            if (optionalTag.isEmpty()) {
                tag = new Tag(tagName.toLowerCase());
                //записываем в БД тег
                tagRepository.save(tag);
            }
            else {
                tag = optionalTag.get();
                }
            //создаем новую запись о связи поста и тега
            Tag2Post tag2Post = new Tag2Post(tag, post);
            tag2PostList.add(tag2Post);
        }
        tag2PostRepository.saveAll(tag2PostList);
        //ошибок нет - возвращаем null
        return null;
    }

    //метод редактирования поста
    public GenericResponseObject checkAndUpdatePost(PostsDTO.Post updatedPostDTO, int updatablePostId){
        //проверка поста на корректность
        //проверка на полноту заполнения поста
        GenericResponseObject errors = postCheck(updatedPostDTO);
        if (errors != null) return errors;
        //если ошибок нет, то готовим пост к записи
        //получаем текущего пользователя
        User user = authService.getCurrentUser();
        //получаем модерируемый пост из БД
        Post updatablePost;
        if (postRepository.existsById(updatablePostId)) {
            updatablePost = postRepository.getOne(updatablePostId);
        }
        else {
            throw new NotFoundException();
        }
        //получаем текущий статус модерации
        Post.ModStatus modStatus = updatablePost.getModerationStatus();
        //устанавливаем статус модерации NEW для автора поста либо оставляем прежний для модератора
        if (!user.isModerator()) modStatus = Post.ModStatus.NEW;
        //Переменная для хранения времени публикации, для краткости дальнейшей записи
        LocalDateTime postTime = LocalDateTime.ofEpochSecond(updatedPostDTO.getTimestamp(), 0, OffsetDateTime.now().getOffset());
        //вносим изменения в пост
        updatablePost.setActive(updatedPostDTO.getActive());
        updatablePost.setModerationStatus(modStatus);
        updatablePost.setText(updatedPostDTO.getText());
        updatablePost.setTime(postTime.isAfter(LocalDateTime.now()) ? postTime : LocalDateTime.now());
        updatablePost.setTitle(updatedPostDTO.getTitle());
        updatablePost.setViewCount(0);
        //Записываем пост в БД
        postRepository.save(updatablePost);
        //список новых связей между постом и тегами
        List<Tag2Post> newTag2PostList = new ArrayList<>();
        //создаем список старых тегов поста, в виде списка String, для удобства сравнения
        List<String> oldTagsNames = updatablePost.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toList());
        //находим новые теги и записываем в таблицу Tag
        for (String newTagName : updatedPostDTO.getTags()){
            if (!oldTagsNames.contains(newTagName.toLowerCase())) {
                //получаем тег из БД
                Tag tag = tagsService.getTag(newTagName);
                //создаем новую запись о связи поста и тега
                Tag2Post tag2Post = new Tag2Post(tag, updatablePost);
                newTag2PostList.add(tag2Post);
            }
        }
        tag2PostRepository.saveAll(newTag2PostList);

        //поиск в старых тегах кандидатов на удаление
        for (String oldTagName : oldTagsNames) {
            //если старый тег не содержится в списке новых, то он кандидат на удаление
            if (!updatedPostDTO.getTags().contains(oldTagName)) {
                Tag tag = tagRepository.findByName(oldTagName).get();
                //удаляем связи в tag2post для удаляемого поста
                tag2PostRepository.delete(tag2PostRepository.findByPostAndTag(updatablePost, tag));
                //если найденный тег больше не связан ни с каким другим постом, то его можно удалить
                //определяем по количеству связей
                if (tag.getTag2posts().size() == 0) {
                    tagRepository.delete(tag);
                }
            }
        }
        //ответ без ошибок - null
        return null;
    }

    //метод проверки поста на корректность
    private GenericResponseObject postCheck(PostsDTO.Post postDTO){
        //Объект для ответа
        GenericResponseObject error = new GenericResponseObject();
        //проверки на полноту заполнения поста
        if (postDTO.getTitle().length() < 3) {
            error.addField("title", "Заголовок слишком короткий");
            return error;
        }
        if (postDTO.getText().length() < 50) {
            error.addField("text", "Текст публикации слишком короткий");
            return error;
        }
        return null;
    }

}