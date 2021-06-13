package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.requests.LikeRequest;
import main.api.responses.GenericResponseObject;
import main.api.responses.PostsDTO;
import main.repository.PostRepository;
import main.service.PostService;
import main.service.PostVoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@RestController
@RequiredArgsConstructor
public class ApiPostController {

    private final PostService postService;
    private final PostVoteService postVoteService;
    private final PostRepository postRepository;

    @GetMapping("/api/post")
    public ResponseEntity<PostsDTO> visiblePosts(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                 @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                                 @RequestParam(value = "mode", defaultValue = "recent") String mode) {
        PostsDTO posts = postService.getVisiblePosts(offset, limit, mode);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/post/{id}")
    public ResponseEntity<PostsDTO.Post> getPostById(@PathVariable int id) {
        PostsDTO.Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/api/post/byDate")
    public ResponseEntity<PostsDTO> getPostsByDate(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                          @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                          @RequestParam(value = "date") Date date) {
        PostsDTO posts = postService.getPostsByDate(offset, limit, date);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/post/byTag")
    public ResponseEntity<PostsDTO> getPostsByTag(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                         @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                         @RequestParam(value = "tag") String tag) {
        PostsDTO posts = postService.getPostsByTag(offset, limit, tag);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/post/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<PostsDTO> getModerationPosts(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                                @RequestParam(value = "status") String status) {
        PostsDTO posts = postService.findModerationPosts(offset, limit, status);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/post/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostsDTO> getMyPosts(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                      @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                      @RequestParam(value = "status") String status) {
        PostsDTO posts = postService.findMyPosts(offset, limit, status);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/post/search")
    public ResponseEntity<PostsDTO> searchPosts(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                       @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                       @RequestParam(value = "query") String query) {
        PostsDTO posts = postService.getPostsByQuery(offset, limit, query);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/post")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<GenericResponseObject> addPost(@RequestBody PostsDTO.Post postRequest){
        GenericResponseObject postCheckResponse = postService.checkAndSavePost(postRequest);
        GenericResponseObject responseBody = new GenericResponseObject();
        if (postCheckResponse == null) {
            responseBody.addField("result", true);
            return ResponseEntity.ok(responseBody);
        }
        responseBody.addField("result", false);
        responseBody.addField("errors", postCheckResponse);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/api/post/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity dislikePost(@RequestBody LikeRequest likeRequest)
    {
        GenericResponseObject response = new GenericResponseObject();
        response.addField("result", postVoteService.likeTrigger(postRepository.findById(likeRequest.getPostId()).get(), -1));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/post/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<GenericResponseObject> likePost(@RequestBody LikeRequest likeRequest)
    {
        GenericResponseObject response = new GenericResponseObject();
        response.addField("result", postVoteService.likeTrigger(postRepository.findById(likeRequest.getPostId()).get(), 1));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/post/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<GenericResponseObject> updatePost(@PathVariable int id, @RequestBody PostsDTO.Post updatablePostDTO) {
        GenericResponseObject postCheckResponse = postService.checkAndUpdatePost(updatablePostDTO, id);
        GenericResponseObject responseBody = new GenericResponseObject();
        if (postCheckResponse == null) {
            responseBody.addField("result", true);
            return ResponseEntity.ok(responseBody);
        }
        responseBody.addField("result", false);
        responseBody.addField("errors", postCheckResponse);
        return ResponseEntity.ok(responseBody);
    }
}