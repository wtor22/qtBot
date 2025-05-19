package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.dto.ButtonDto;
import com.quartztop.bot.tg_bot.entity.buttons.Button;
import com.quartztop.bot.tg_bot.entity.buttons.ButtonType;
import com.quartztop.bot.tg_bot.repositories.ButtonsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ButtonsService {

    private final ButtonsRepository buttonsRepository;

    public void createImageLinkButton(ButtonDto buttonDto) {
        Button button = mapToEntity(buttonDto);
        
        button.setButtonType(ButtonType.LINK_FOLDER_IMAGE);
        buttonsRepository.save(button);
        log.info("Создана кнопка \"" + buttonDto.getTextButton() + " \" c URL: " + buttonDto.getButtonValue());
    }

    public String update(ButtonDto buttonDto) {
        if(buttonDto.getId() == null || !buttonsRepository.existsById(buttonDto.getId()))
            return "Элемент не найден в БД";
        Button existingButton = buttonsRepository.findByTextButton(buttonDto.getTextButton());
        if( existingButton != null && !existingButton.getId().equals(buttonDto.getId()) )
            return "Элемент с таким названием уже существует в БД";
        Button updatedButton = buttonsRepository.save(mapToEntity(buttonDto));
        log.info("Обновлена кнопка \"" + updatedButton.getTextButton() + " \" c URL: " + updatedButton.getButtonValue());
        return "Элемент обновлен";
    }

    public List<ButtonDto> getAll() {
        return buttonsRepository.findAllByOrderByOrderInBotIndexAsc()
                .stream()
                .map(ButtonsService::mapToDto)
                .toList();
    }
    public void delete(Long id) {
        buttonsRepository.deleteById(id);
    }

    public List<ButtonDto> setOrder(List<Long> ids) {
        List<Button> buttonList = buttonsRepository.findAll();
        for(Button button : buttonList) {
            if(ids.contains(button.getId())) {
                int buttonOrder = ids.indexOf(button.getId());
                log.error("GRINT ORDER id = " + button.getId() + " order " + buttonOrder);
                button.setOrderInBotIndex(ids.indexOf(button.getId()));
            } else {
                button.setOrderInBotIndex(null);
            }
        }
        buttonsRepository.saveAll(buttonList);
        List<Button> savedList = buttonsRepository.findAllByOrderByOrderInBotIndexAsc();
        return savedList.stream().map(ButtonsService::mapToDto).toList();
    }

    public static Button mapToEntity(ButtonDto button) {

        Button buttonEntity = new Button();
        buttonEntity.setId(button.getId());
        buttonEntity.setButtonValue(button.getButtonValue());
        buttonEntity.setTextButton(button.getTextButton());
        buttonEntity.setOrderInBotIndex(button.getOrderInBotIndex());
        return buttonEntity;
    }
    public static ButtonDto mapToDto(Button button) {

        ButtonDto buttonDto = new ButtonDto();
        buttonDto.setId(button.getId());
        buttonDto.setButtonValue(button.getButtonValue());
        buttonDto.setTextButton(button.getTextButton());
        buttonDto.setOrderInBotIndex(button.getOrderInBotIndex());
        return buttonDto;
    }
}
