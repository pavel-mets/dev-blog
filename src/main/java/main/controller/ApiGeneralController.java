package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.requests.CommentRequest;
import main.api.requests.ModerationRequest;
import main.api.requests.ProfileRequest;
import main.api.responses.*;
import main.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ApiGeneralController {

    private final InitDTO initDTO;
    private final SettingService settingService;
    private final CalendarService calendarService;
    private final StatisticsService statisticsService;
    private final TagsService tagsService;
    private final CommentService commentService;
    private final ModerationService moderationService;
    private final ImageFileService imageFileService;
    private final AuthService authService;

    @GetMapping("/api/init")
    public ResponseEntity<InitDTO> init() {
        return ResponseEntity.ok(initDTO);
    }

    @GetMapping("/api/settings")
    public ResponseEntity<SettingsDTO> settings(){
        return ResponseEntity.ok(settingService.getGlobalSettings());
    }

    @GetMapping("/api/calendar")
    public ResponseEntity<CalendarDTO> calendar(@RequestParam(value = "year", defaultValue = "") Integer year) {
        return ResponseEntity.ok(calendarService.getCalendar(year));
    }

    @GetMapping("/api/statistics/all")
    public ResponseEntity<StatisticsDTO> statisticAll(){
        return ResponseEntity.ok(statisticsService.statisticsAll());
    }

    @GetMapping("/api/statistics/my")
    public ResponseEntity<StatisticsDTO> statisticMy(){
        return ResponseEntity.ok(statisticsService.statisticsMy());
    }

    @GetMapping("/api/tag")
    public ResponseEntity<TagsDTO> tagsWeight(@RequestParam(value = "query", defaultValue = "") String query){
        return ResponseEntity.ok(tagsService.getTagsByQuery(query));
    }

    @PostMapping("/api/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<GenericResponseObject> addComment(@RequestBody CommentRequest commentRequest){
        GenericResponseObject response = commentService.addComment(commentRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/image")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {
        //получаем ответ от сервиса и оборачиваем в ResponseEntity
        String response = imageFileService.uploadFile(file);
        //возвращаем успешный ответ, отрицательный ответ сюда не доходит - обрабатывается исключениями в сервисе
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/api/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<GenericResponseObject> setModerationStatus(@RequestBody ModerationRequest moderationRequest){
        GenericResponseObject response = new GenericResponseObject();
        response.addField("result", moderationService.setModerationStatus(moderationRequest));
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/api/profile/my",
                    method = RequestMethod.POST,
                    consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<RegisterDTO> changeProfileFormData(@RequestParam(value = "photo", required = false) MultipartFile photoFile,
                                                     @RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "email", required = false) String email,
                                                     @RequestParam(value = "password", required = false) String password,
                                                     @RequestParam(value = "removePhoto", required = false) Integer removePhoto){
        ProfileRequest profileRequest = ProfileRequest.builder()
                    .photoFile(photoFile)
                    .email(email)
                    .name(name)
                    .password(password)
                    .removePhoto(removePhoto)
                    .build();
        RegisterDTO registerDTO = authService.changeProfile(profileRequest);
        return ResponseEntity.ok(registerDTO);
    }

    @RequestMapping(value = "/api/profile/my",
                    method = RequestMethod.POST,
                    consumes = "application/json")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<RegisterDTO> changeProfileJSON(@RequestBody ProfileRequest request){
        RegisterDTO registerDTO = authService.changeProfile(request);
        return ResponseEntity.ok(registerDTO);
    }

    @PutMapping("/api/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public void setSettings(@RequestBody SettingsDTO request){
        settingService.setGlobalSettings(request);
    }
}