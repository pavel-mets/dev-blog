package main.service;

import main.api.requests.ModerationRequest;
import main.api.responses.GenericResponseObject;
import main.exceptions.NotFoundException;
import main.model.Post;
import main.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ModerationService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthService authService;

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
