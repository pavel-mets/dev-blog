package main.service;

import lombok.RequiredArgsConstructor;
import main.api.responses.CalendarDTO;
import main.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final PostRepository postRepository;

    public CalendarDTO getCalendar(Integer year){
        if (year == null) year = LocalDate.now().getYear();
        CalendarDTO calendarDTO = new CalendarDTO();
        //извлекаем из строки дату и количество постов за нее ( объект содержит Object[] = {String дата, Long количество})
        for (Object[] entry : postRepository.getDatesAndPostsCountByYear(year)) {
            calendarDTO.getPosts().put(entry[0].toString(), (long) entry[1]);
        }
        calendarDTO.setYears(postRepository.getPostsYears());
        return calendarDTO;
    }
}
