package main.controller;

import lombok.AllArgsConstructor;
import main.api.requests.LoginRequest;
import main.api.requests.ProfileRequest;
import main.api.requests.RegisterRequest;
import main.api.responses.CaptchaDTO;
import main.api.responses.GenericResponseObject;
import main.api.responses.LoginDTO;
import main.api.responses.RegisterDTO;
import main.service.AuthService;
import main.service.SettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@AllArgsConstructor
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final SettingService settingService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginDTO> login(@RequestBody LoginRequest loginRequest){
        Authentication auth;
        //попытка аутентифицировать пользователя
        try {
            auth = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        }
        catch (BadCredentialsException ex) {
            //если не прошла аутентификация, то отдаем отрицательный ответ
            return ResponseEntity.ok(new LoginDTO());
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        return ResponseEntity.ok(authService.getLoginResponse(loginRequest.getEmail()));
    }

    @PostMapping("/api/auth/password")
    public ResponseEntity<GenericResponseObject> changePassword(@RequestBody RegisterRequest registerRequest){
        GenericResponseObject response = new GenericResponseObject();
        response.addField("result", authService.changePassword(registerRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<RegisterDTO> register(@RequestBody RegisterRequest registerRequest){
        //согласно ТЗ необходимо возвращать статус 404 при обращении сюда и значении глобального параметра MULTIUSER_MODE = false
        if (!settingService.getGlobalSettings().isMultiuserMode()) return ResponseEntity.notFound().build();
        RegisterDTO registerDTO = authService.registerNewUser(registerRequest);
        return ResponseEntity.ok(registerDTO);
    }

    @PostMapping("/api/auth/restore")
    public ResponseEntity<GenericResponseObject> restore(@RequestBody ProfileRequest profileRequest,
                                                         @RequestHeader("origin") String origin)
    {
        GenericResponseObject response = new GenericResponseObject();
        response.addField("result", authService.codeGenerationAndEmail(profileRequest.getEmail(), origin));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/auth/captcha")
    public ResponseEntity<CaptchaDTO> getCaptcha(){
        CaptchaDTO captchaDTO = authService.getCaptchaCodes();
        return ResponseEntity.ok(captchaDTO);
    }

    @GetMapping("/api/auth/check")
    public ResponseEntity<LoginDTO> authCheck(Principal principal){
        if (principal == null) {return ResponseEntity.ok(new LoginDTO());}
        return ResponseEntity.ok(authService.getLoginResponse(principal.getName()));
    }

    @GetMapping("/api/auth/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<LoginDTO> logout(){
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setResult(true);
        SecurityContextHolder.clearContext();
        SecurityContextHolder.createEmptyContext();
        return ResponseEntity.ok(loginDTO);
    }

}