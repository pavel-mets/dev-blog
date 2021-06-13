package main.service;

import lombok.RequiredArgsConstructor;
import main.api.requests.CommentRequest;
import main.api.responses.GenericResponseObject;
import main.exception.BadRequestException;
import main.model.PostComments;
import main.repository.PostCommentsRepository;
import main.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final PostCommentsRepository postCommentsRepository;
    private final AuthService authService;

    public GenericResponseObject addComment(CommentRequest commentRequest){
        //объект ответа
        GenericResponseObject response = new GenericResponseObject();
        //проверка на существование родительского комментария
        if (commentRequest.getParentId() != null && !postCommentsRepository.existsById(commentRequest.getParentId())) {
            response.addField("result", false);
            GenericResponseObject error = new GenericResponseObject();
            error.addField("text", "Не существует родительского комментария");
            error.addField("errors", error);
            throw new BadRequestException(response);
        }
        //проверка на существование поста к которому пишется комментарий
        if (!postRepository.existsById(commentRequest.getPostId())) {
            response.addField("result", false);
            GenericResponseObject error = new GenericResponseObject();
            error.addField("text", "Не существует поста к которому написан комментарий");
            error.addField("errors", error);
            throw new BadRequestException(response);
        }
        //проверка на длину комментария
        if (commentRequest.getText().replaceAll("<.+?>", "").length() < 10) {
            response.addField("result", false);
            GenericResponseObject error = new GenericResponseObject();
            error.addField("text", "Комментарий слишком короткий");
            error.addField("errors", error);
            throw new BadRequestException(response);
        }
        //формируем положительный ответ
        PostComments comment = PostComments.builder()
                .parentId(commentRequest.getParentId() == null ? null : postCommentsRepository.getOne(commentRequest.getParentId()))
                .post(postRepository.getOne(commentRequest.getPostId()))
                .text(commentRequest.getText())
                .time(LocalDateTime.now())
                .user(authService.getCurrentUser())
                .build();
        response.addField("id", postCommentsRepository.save(comment).getId());
        return response;
    }
}
