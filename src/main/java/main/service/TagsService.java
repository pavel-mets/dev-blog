package main.service;

import lombok.RequiredArgsConstructor;
import main.api.responses.TagsDTO;
import main.exception.NotFoundException;
import main.model.Tag;
import main.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagsService {

    private final TagRepository tagRepository;

    //метод получения тегов и их количеств из БД по запросу query или его отсутствию
    public TagsDTO getTagsByQuery(String query){
        //из БД возвращаем список объектов Object[] = {имя, количество}
        List<Object[]> results;
        if (query.equals("")) { results = tagRepository.getAllTags(); }
        else { results = tagRepository.getTagsByQuery(query); }
        //возврат статуса 404 если ничего не найдено
        if (results.size() == 0) throw new NotFoundException();
        //объект для ответа
        TagsDTO tagsDTO = new TagsDTO();
        //получение общего количества видимых публикаций для расчета веса публикаций
        long postCount = tagRepository.getVisiblePostsCount();
        //получение максимального ненормированного веса
        float maxWeight = results.stream().map(o -> (long) o[1] * 1f / postCount).max(Comparator.comparing(Float::valueOf)).get();
        //заполнение списка тегов по формату ответа
        for (Object[] result: results) {
            TagsDTO.Tag tagWithWeight = TagsDTO.Tag.builder()
                    .name((String) result[0])
                    .weight(Math.round((long) result[1] *1f / postCount / maxWeight * 100) / 100f)
                    .build();
            tagsDTO.getTags().add(tagWithWeight);
        }
        return tagsDTO;
    }

    //метод получения тега из БД по имени, либо создания нового, если его не существует
    public Tag getTag(String tagName){
        Optional<Tag> optionalTag = tagRepository.findByName(tagName.toLowerCase());
        Tag tag;
        //ищем в БД Tag или создаем новый, если он не существует
        if (optionalTag.isEmpty()) {
            tag = new Tag(tagName.toLowerCase());
            //записываем в БД тег
            tagRepository.save(tag);
        }
        else {
            tag = optionalTag.get();
        }
        return tag;
    }

}
