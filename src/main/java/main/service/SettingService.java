package main.service;

import lombok.RequiredArgsConstructor;
import main.api.responses.SettingsDTO;
import main.model.GlobalSettings;
import main.repository.GlobalSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final GlobalSettingsRepository globalSettingsRepository;

    //метод получения глобальных параметров
    public SettingsDTO getGlobalSettings(){
        SettingsDTO settingsDTO = new SettingsDTO();
        //если ничего не прочитаем из БД, то значения по умолчанию:
        settingsDTO.setMultiuserMode(true);
        settingsDTO.setPostPremoderation(true);
        settingsDTO.setPostPremoderation(true);
        //читаем значения из БД
        List<GlobalSettings> settingsList = globalSettingsRepository.findAll();
        for (GlobalSettings setting : settingsList) {
            switch (setting.getCode()){
                case "MULTIUSER_MODE": {
                    settingsDTO.setMultiuserMode(setting.getValue().equals("YES"));
                    break;
                }
                case "POST_PREMODERATION": {
                    settingsDTO.setPostPremoderation(setting.getValue().equals("YES"));
                    break;
                }
                case "STATISTICS_IS_PUBLIC": {
                    settingsDTO.setStatisticIsPublic(setting.getValue().equals("YES"));
                    break;
                }
            }
        }
        return settingsDTO;
    }

    //метод установки глобальных параметров
    public void setGlobalSettings(SettingsDTO settings){
        //список для записи в БД
        List<GlobalSettings> settingsList = new ArrayList<>();
        //формируем первый параметр
        GlobalSettings globalSettingsItem = new GlobalSettings();
        globalSettingsItem.setCode("MULTIUSER_MODE");
        globalSettingsItem.setName("Многопользовательский режим");
        if (settings.isMultiuserMode()) globalSettingsItem.setValue("YES"); else globalSettingsItem.setValue("NO");
        settingsList.add(globalSettingsItem);
        //формируем второй параметр
        globalSettingsItem = new GlobalSettings();
        globalSettingsItem.setCode("POST_PREMODERATION");
        globalSettingsItem.setName("Премодерация постов");
        if (settings.isPostPremoderation()) globalSettingsItem.setValue("YES"); else globalSettingsItem.setValue("NO");
        settingsList.add(globalSettingsItem);
        //формируем третий параметр
        globalSettingsItem = new GlobalSettings();
        globalSettingsItem.setCode("STATISTICS_IS_PUBLIC");
        globalSettingsItem.setName("Показывать всем статистику блога");
        if (settings.isStatisticIsPublic()) globalSettingsItem.setValue("YES"); else globalSettingsItem.setValue("NO");
        settingsList.add(globalSettingsItem);
        //пишем в БД
        globalSettingsRepository.deleteAll();
        globalSettingsRepository.saveAll(settingsList);
    }

}
