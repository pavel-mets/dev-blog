package main.service;

import lombok.RequiredArgsConstructor;
import main.api.requests.ModerationRequest;
import main.model.Post;
import main.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final PostRepository postRepository;
    private final AuthService authService;

    public boolean setModerationStatus(ModerationRequest moderationRequest){
        Optional<Post> optionalPost = postRepository.findById(moderationRequest.getPostId());
        Post.ModStatus moderationStatus;
        if (moderationRequest.getDecision().equals("accept")) {moderationStatus = Post.ModStatus.ACCEPTED;}
        else {moderationStatus = Post.ModStatus.DECLINED;}
            try {
                Post post = optionalPost.get();
                post.setModerationStatus(moderationStatus);
                post.setModeratorId(authService.getCurrentUser());
                postRepository.save(post);
            }
            catch (Exception e){
                //при неудаче модерации по любым причинам возвращаем false
                return false;
            }
        return true;
    }
}
