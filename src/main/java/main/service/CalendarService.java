package main.service;

import main.api.responses.CalendarDTO;
import main.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;

@Service
public class CalendarService {

    @Autowired
    private PostRepository postRepository;

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
