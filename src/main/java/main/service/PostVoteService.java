package main.service;

import main.api.responses.GenericResponseObject;
import main.model.Post;
import main.model.PostVote;
import main.model.User;
import main.repository.PostVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostVoteService {

    @Autowired
    private PostVoteRepository postVoteRepository;

    @Autowired
    private AuthService authService;

    //метод для установки лайка или дизлайка посту от определенного пользователя
    public boolean likeTrigger(Post post, int trigger){
        //определяем существование оценки (лайка или дизлайка) от данного пользователя
        Optional<PostVote> optionalPostVote = postVoteRepository.findByPostAndUser(post, authService.getCurrentUser());
        PostVote postVote;
        boolean result = false;
        //если оценка существует, и отличается от запрашиваемой, то обновляем оценку
        if (optionalPostVote.isPresent()) {
            postVote = optionalPostVote.get();
            if (postVote.getValue() != trigger) {
                postVote.setValue(trigger);
                postVote.setTime(LocalDateTime.now());
                result = true;
            }

        } else {
            postVote = PostVote.builder()
                    .post(post)
                    .user(authService.getCurrentUser())
                    .time(LocalDateTime.now())
                    .value(trigger)
                    .build();
            result = true;
        }
        if (result) postVoteRepository.save(postVote);
        return result;
    }

}
