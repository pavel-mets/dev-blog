package main.service;

import main.api.responses.StatisticsDTO;
import main.exceptions.NotFoundException;
import main.exceptions.UnauthorizedException;
import main.model.User;
import main.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;


@Service
public class StatisticsService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AuthService authService;

    public StatisticsDTO statisticsAll(){
        //если настройки позволяют показывать статистику всем, то пропускаем следующие проверки
        if (settingService.getGlobalSettings().isStatisticIsPublic()) {}
        //если настройки не позволяют показывать статистику всем, то проверяем на модератора
        //если не удастся получить пользователя, то выбросится UnauthorizedException
        else if (authService.getCurrentUser().isModerator()) {}
        //если пользователь будет получен, но не является модератором, то выбрасываем исключение сами
        else throw new UnauthorizedException();
        //получаем количество постов
        int postCount = postRepository.getPostCount();
        //если нет постов, то выбрасываем исключение и ответ со статусом 404
        if (postCount == 0) throw new NotFoundException();
        //получаем количесвто лайков и дизлайков
        List<Integer[]> votesStatistics = postRepository.getVotesStatistics();
        StatisticsDTO statisticsDTO = StatisticsDTO.builder()
                .postsCount(postCount)
                //голоса могут быть null
                .likesCount(votesStatistics.get(0)[0] == null ? 0 : votesStatistics.get(0)[0])
                .dislikesCount(votesStatistics.get(0)[1] == null ? 0 : votesStatistics.get(0)[1])
                .viewsCount(postRepository.getViewsCount())
                .firstPublication(postRepository.getOldestPostTime().toEpochSecond(OffsetDateTime.now().getOffset()))
                .build();
        return statisticsDTO;
    }

    public StatisticsDTO statisticsMy(){
        //получаем текущего пользователя
        User user = authService.getCurrentUser();
        //получаем количество постов пользователя
        int postCount = postRepository.getPostCount(user);
        //если нет постов, то выбрасываем исключение и ответ со статусом 404
        if (postCount == 0) throw new NotFoundException();
        //получаем количество лайков и дизлайков
        List<Integer[]> votesStatistics = postRepository.getVotesStatistics(user);
        StatisticsDTO statisticsDTO = StatisticsDTO.builder()
                .postsCount(postCount)
                //голоса могут быть null
                .likesCount(votesStatistics.get(0)[0] == null ? 0 : votesStatistics.get(0)[0])
                .dislikesCount(votesStatistics.get(0)[1] == null ? 0 : votesStatistics.get(0)[1])
                .viewsCount(postRepository.getViewsCount(user))
                .firstPublication(postRepository.getOldestPostTime(user).toEpochSecond(OffsetDateTime.now().getOffset()))
                .build();
        return statisticsDTO;
    }
}
