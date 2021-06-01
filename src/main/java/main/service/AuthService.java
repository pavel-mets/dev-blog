package main.service;

import com.github.cage.Cage;
import com.github.cage.image.EffectConfig;
import com.github.cage.image.Painter;
import com.github.cage.image.ScaleConfig;
import com.github.cage.token.RandomTokenGenerator;
import main.api.requests.ProfileRequest;
import main.api.requests.RegisterRequest;
import main.api.responses.CaptchaDTO;
import main.api.responses.LoginDTO;
import main.api.responses.RegisterDTO;
import main.exceptions.RegisterException;
import main.exceptions.UnauthorizedException;
import main.model.CaptchaCodes;
import main.model.User;
import main.repository.CaptchaCodesRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private ImageFileService imageFileService;

    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Value("${blog.captcha.expire}")
    int captchaExpire;

    @Value("${blog.restore.code.expire}")
    int restoreCodeExpire;

    @Value("${blog.image.upload.path}")
    String uploadPath;

    //метод получения ответа при логине пользователя
    public LoginDTO getLoginResponse(String email){
        main.model.User currentUser =
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(email));
        int moderationCount = 0;
        if (currentUser.isModerator()) moderationCount = postRepository.getModerationCount();
        LoginDTO.User user = LoginDTO.User.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .photo(currentUser.getPhoto())
                .email(currentUser.getEmail())
                .moderationCount(moderationCount)
                .moderation(currentUser.isModerator())
                .settings(currentUser.isModerator())
                .build();
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setResult(true);
        loginDTO.setUser(user);
        return loginDTO;
    }

    //метод генерирует code и secret_code, а также картинку
    public CaptchaDTO getCaptchaCodes() {
        //объекту Painter устанавливаем параметры прорисовки капчи 100 x 35
        Painter painter = new Painter(100, 35, Color.WHITE, Painter.Quality.MAX,
                new EffectConfig(true, true, false, true,
                new ScaleConfig(0.9f, 0.9f)), null);
        //создаем генератор кода с длиной в 4 символа
        RandomTokenGenerator generator = new RandomTokenGenerator(new Random(), 4);
        Cage cage = new Cage(painter, null, null, "png", 1F, null, null);
        String token = generator.next();
        String secret = Base64.encodeBase64String(token.getBytes());
        String image = "data:image/png;base64, " + Base64.encodeBase64String(cage.draw(token));
        CaptchaDTO captchaDTO = new CaptchaDTO(secret, image);
        CaptchaCodes captchaCodes = CaptchaCodes.builder()
                .code(token)
                .secretCode(secret)
                .time(LocalDateTime.now())
                .build();
        captchaCodesRepository.save(captchaCodes);
        return captchaDTO;
    }

    //метод регистрации нового пользователя, запись в БД
    public RegisterDTO registerNewUser(RegisterRequest registerRequest){
        //проверка капчи, выбрасываем исключение при отрицательной проверке и отдаем ответ со статусом 200
        checkCaptcha(registerRequest.getCaptcha(), registerRequest.getCaptchaSecret());
        //проверка email на корректность
        checkEmail(registerRequest.getEmail());
        //проверка имени
        checkName(registerRequest.getName());
        //проверка длины пароля
        checkPassword(registerRequest.getPassword());
        //проверка наличия уже существующей регистрации по email
        checkEmailBusy(registerRequest.getEmail());

        //если ошибок нет, то записываем юзера в БД и формируем положительный ответ
        User user = User.builder()
            .email(registerRequest.getEmail())
            .name(registerRequest.getName())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .isModerator(false)
            .regTime(LocalDateTime.now())
            .build();
        userRepository.save(user);
        //подготовка положительного ответа
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setResult(true);
        return registerDTO;
    }

    //проверка устаревшей капчи каждые 60 секунд, период устаревания берем из properties
    @Scheduled(fixedRate = 60000)
    private void captchaAndRestoreCodeClear(){
        //установка времени по умолчанию (1 час согласно ТЗ), если оно не задано в application.properties
        if (captchaExpire == 0) captchaExpire = 60;
        //установка времени по умолчанию для устаревшего кода восстановления, если оно не задано
        if (restoreCodeExpire == 0) restoreCodeExpire = 180;
        //поиск и удаление устаревшей капчи
        List<CaptchaCodes> codesList = captchaCodesRepository.getExpiredCaptcha(captchaExpire);
        captchaCodesRepository.deleteAll(codesList);
        //поиск и удаление устаревших ссылок на восстановление пароля
        List<User> userList = userRepository.getUsersByExpiredCode(restoreCodeExpire);
        for (User user : userList) {
            user.setCode(null);
            user.setCodeTime(null);
        }
        userRepository.saveAll(userList);
    }

    //метод изменения своего профиля
    public RegisterDTO changeProfile(ProfileRequest profileRequest){
        User currentUser = getCurrentUser();
        //для всех проверок, в случае отрицательной проверки выбросится исключение и ответ с ошибками и http кодом 200
        checkName(profileRequest.getName());
        //проверка email на корректность
        checkEmail(profileRequest.getEmail());
        //проверка email на занятость
        checkEmailBusy(profileRequest.getEmail());
        //записываем изменения текущего пользователя
        currentUser.setName(profileRequest.getName());
        currentUser.setEmail(profileRequest.getEmail());
        //изменение пароля если он задан в запросе
        if (profileRequest.getPassword() != null) {
            //проверка на длину в 6 и более символов
            checkPassword(profileRequest.getPassword());
            currentUser.setPassword(passwordEncoder.encode(profileRequest.getPassword()));
        }
        //замена картинки, если она обновляется, т.е. присутствует параметр removePhoto = 0
        if (profileRequest.getRemovePhoto() != null && profileRequest.getRemovePhoto() == 0 && profileRequest.getPhotoFile() != null) {
            //загрузка и масштабирование картинки и аватарки
            String uploadImagePath = imageFileService.uploadFile(profileRequest.getPhotoFile()).replaceAll(uploadPath + "/", "");
            //удаление старой картинки с сервера
            imageFileService.fileAndDirectoriesDelete(uploadPath + "/" + currentUser.getPhoto());
            //удаление старой аватарки с сервера
            imageFileService.fileAndDirectoriesDelete(uploadPath + "/avatars/" + currentUser.getPhoto());
            //запись пути к новой картинке
            currentUser.setPhoto(uploadImagePath);
        }
        //удаление картинки, если установлен соответствующий флаг и photo = ""
        if (profileRequest.getRemovePhoto() != null && profileRequest.getRemovePhoto() == 1) {
            //удаление старой картинки с сервера
            imageFileService.fileAndDirectoriesDelete(uploadPath + "/" + currentUser.getPhoto());
            //удаление старой аватарки с сервера
            imageFileService.fileAndDirectoriesDelete(uploadPath + "/avatars/" + currentUser.getPhoto());
            //удлаление из данных пользователя ссылки на картинку
            currentUser.setPhoto(null);
        }
        //сохранение пользователя в БД
        userRepository.save(currentUser);
        //подготовка положительного ответа
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setResult(true);
        return registerDTO;
    }

    //проверка имени на символы, кроме буквенного, цифрового или знака подчёркивания
    private void checkName(String name) {
        if (!name.matches("^[А-Яа-яA-Za-z0-9_-]+")) {
            RegisterDTO.Errors errors = new RegisterDTO.Errors();
            errors.setName("Имя указано неверно");
            throw new RegisterException(errors);
        }
    }

    //проверка email на корректность
    private void checkEmail(String email) {
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+.+.[A-Za-z]+")) {
            RegisterDTO.Errors errors = new RegisterDTO.Errors();
            errors.setEmail("Email указан некорректно");
            throw new RegisterException(errors);
        }
    }

    //проверка длины пароля
    private void checkPassword(String password) {
        if (password.length() < 6) {
            RegisterDTO.Errors errors = new RegisterDTO.Errors();
            errors.setPassword("Пароль короче 6 символов");
            throw new RegisterException(errors);
        }
    }

    //проверка на существование такого же email у другого зарегистрированного пользователя
    private void checkEmailBusy(String email) {
        Optional<User> otherUser = userRepository.findByEmail(email);
        User currentUser = null;
        try {
            currentUser = getCurrentUser();
        }
        catch (UnauthorizedException exception){
            exception.printStackTrace();
           //если пользователь не авторизован, то не выбрасываем исключение, а продолжаем регистрацию нового пользователя
        }
        if (otherUser.isPresent() && otherUser.get().getId() != currentUser.getId()) {
            RegisterDTO.Errors errors = new RegisterDTO.Errors();
            errors.setEmail("Этот email уже зарегистрирован");
            throw new RegisterException(errors);
        }
    }

    //метод проверки капчи
    private void checkCaptcha(String captcha, String captchaSecret) {
        Optional<CaptchaCodes> captchaCode = captchaCodesRepository.findBySecretCode(captchaSecret);
        if (!captchaCode.isPresent() || !captchaCode.get().getCode().equals(captcha)) {
            RegisterDTO.Errors errors = new RegisterDTO.Errors();
            errors.setCaptcha("Код с картинки введен неверно");
            throw new RegisterException(errors);
        }
    }

    //метод получения текущего авторизованного пользователя
    public User getCurrentUser() throws UnauthorizedException {
        try {
            String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            User user = userRepository.findByEmail(email).get();
            return user;
        }
        catch (Exception ex) {
            throw new UnauthorizedException();
        }
    }

    //метод генерации хеш кода и отправки по email ссылки для смены пароля
    public boolean codeGenerationAndEmail(String email, String origin){
        try {
        //проверка на существование пользователя с данным email в БД
        User user = userRepository.findByEmail(email).get();
            //генерируем хеш код
            String code = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replaceAll("-", "");
            //записываем в БД
            user.setCode(code);
            user.setCodeTime(LocalDateTime.now());
            userRepository.save(user);
            //отправляем ссылку по email
            mailService.sendMessage(user.getEmail(), "Ссылка на восстановление пароля",
                    "<p><a href=\"" + origin + "/login/change-password/" +
                    code + "\">Нажмите на ссылку для восстановления пароля на ресурсе DevPub</a></p>");
            }
        catch (Exception e) {
            //при любой ошибке возвращаем false
            return false;
        }
        return true;
    }

    //метод смены пароля
    public boolean changePassword(RegisterRequest registerRequest){
        //все проверки выбрасывают исключения в случае непрохождения и ответ сервера с кодом 200 и ошибкой
        //проверка правильности капчи
        checkCaptcha(registerRequest.getCaptcha(), registerRequest.getCaptchaSecret());
        //проверка пароля на длину
        checkPassword(registerRequest.getPassword());
        try {
            //поиск пользователя по коду восстановления, если не найден то получим исключение
            User user = userRepository.findByCode(registerRequest.getCode()).get();
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            //очистка кода восстановления
            user.setCode(null);
            user.setCodeTime(null);
            userRepository.save(user);
        }
        catch (Exception ex)
        {
            //пользователь не найден по коду, или код неверный или устарел
            RegisterDTO.Errors errors = new RegisterDTO.Errors();
            errors.setCode("Ссылка для восстановления пароля устарела.\n" +
                    "<a href=\"/login/restore-password\">Запросить ссылку снова</a>");
            throw new RegisterException(errors);
        }
        return true;
    }
}