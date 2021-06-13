package main.service;

import main.api.responses.GenericResponseObject;
import main.exception.BadRequestException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageFileService {

    @Value("${blog.image.upload.path}")
    String uploadPath;
    @Value("${blog.image.upload.maxfilesize}")
    int maxFileSize;

    //поле содержащее расширение имени файла
    private String fileExtention;
    //поле с полным путем закачанного файла
    private String resultFileName;

    public String uploadFile(MultipartFile file) {
        //объект для ответа
        GenericResponseObject response = new GenericResponseObject();
        //проверяем входящий файл на ошибки, если их нет, то получим null, если есть, то получим объект с наименованием ошибки
        GenericResponseObject errorFields = fileCheck(file);
        //если ошибок нет, то создаем рандомные каталоги и записываем файл
        if (errorFields == null) {
            resultFileName = createPath();
            if (recordFile(file)) return resultFileName; //возвращаем положительный ответ содержащий путь
            else {
                errorFields = new GenericResponseObject();
                errorFields.addField("image", "Не удалось загрузить " + file.getOriginalFilename());
                response.addField("errors", errorFields);
                throw new BadRequestException(response);
            }
        }
        else { response.addField("result", false);
               response.addField("errors", errorFields);
               throw new BadRequestException(response);
        }
    }

    //метод проверки загружаемого файла, возврат объекта с формулировкой ошибки или null если ошибок нет
    private GenericResponseObject fileCheck(MultipartFile file){
        GenericResponseObject errorFields = new GenericResponseObject();
        //проверка на пустой файл
        if (file.isEmpty()) {
            errorFields.addField("image", "Невозможно загрузить пустой файл " + file.getOriginalFilename());
            return errorFields;
        }
        //проверка расширения имени файла
        fileExtention = file.getOriginalFilename().replaceFirst(".+\\.", "").toLowerCase();
        if (!(fileExtention.equals("jpg") || fileExtention.equals("jpeg") || fileExtention.equals("png"))) {
            errorFields.addField("image", "Файл " + file.getOriginalFilename() + " имеет недопустимый формат");
            return errorFields;
        }
        //проверка на превышение размера файла
        if (file.getSize() > maxFileSize) {
            errorFields.addField("image", "Файл " + file.getOriginalFilename() + " превышает максимальный размер " + maxFileSize / 1024 + " Кб");
            return errorFields;
        }
        //если не выявлено ни одной ошибки, то возвращаем null
        return null;
    }

    //метод создания рандомных каталогов
    private String createPath(){
        String[] subPath = UUID.randomUUID().toString().split("-");
        String uploadDir = uploadPath + "/" + subPath[0] + "/" + subPath[1] + "/" + subPath[2];
        new File(uploadDir).mkdirs();
        //создание каталогов для аватарок
        new File(uploadDir.replaceAll(uploadPath, uploadPath + "/avatars")).mkdirs();
        return uploadDir + "/" + subPath[3] + "." + fileExtention;
    }

    //метод записи файла
    private boolean recordFile(MultipartFile file){
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            //находим меньшую сторону для обрезки до квадрата
            int cutSize = originalImage.getHeight() > originalImage.getWidth() ? originalImage.getWidth() : originalImage.getHeight();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(resultFileName)));
            //обрезаем изображение до квадрата и сжимаем до 90 * 90
            Thumbnails.of(file.getInputStream())
                    .sourceRegion(Positions.CENTER, cutSize, cutSize)
                    .size(90, 90)
                    .toOutputStream(stream);
            stream.flush();
            stream.close();
            //Формируем изображение для аватарки 22 * 22
            BufferedOutputStream avatarStream = new BufferedOutputStream(new FileOutputStream(new File(resultFileName.replaceAll(uploadPath, uploadPath + "/avatars"))));
            Thumbnails.of(file.getInputStream())
                    .sourceRegion(Positions.CENTER, cutSize, cutSize)
                    .size(36, 36)
                    .toOutputStream(avatarStream);
            avatarStream.flush();
            avatarStream.close();

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    //метод удаления файла (картинки) и структуры пустых директорий до него
    public void fileAndDirectoriesDelete(String path){
        try {
            for (;;){
                Files.delete(Paths.get(path));
                //отбрасываем путь после последнего слеша и удаляем последнюю директорию по кругу
                path = path.substring(0, path.lastIndexOf("/"));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            //удаление завершено по причине полной очистки структуры
            //либо по причине невозможности удалить из-за совпадения имен директорий и наличия в них файлов
            //запись в лог операции удаления
        }
    }
}
