package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.dto.BotUserDTO;
import com.quartztop.bot.tg_bot.entity.BotUser;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotUserService {

    private final BotUserRepositories repositories;

    public BotUserDTO create(BotUserDTO botUserDTO) {

        Optional<BotUser> optionalBotUser = repositories.findById(botUserDTO.getTelegramId());
        if(optionalBotUser.isPresent()) throw new RuntimeException("Пользователь с таким id еже зарегистрирован");
        BotUser botUser = mapToEntity(botUserDTO);

        return mapToDto(repositories.save(botUser));
    }

    public static BotUserDTO mapToDto(BotUser botUser) {

        BotUserDTO botUserDTO = new BotUserDTO();
        botUserDTO.setTelegramId(botUser.getTelegramId());
        botUserDTO.setPhoneNumber(botUser.getPhoneNumber());
        botUserDTO.setUsername(botUser.getUsername());
        botUserDTO.setFirstName(botUser.getFirstName());
        botUserDTO.setLastName(botUser.getLastName());
        return botUserDTO;
    }

    public static BotUser mapToEntity(BotUserDTO botUser) {

        BotUser botUserEntity = new BotUser();
        botUserEntity.setTelegramId(botUserEntity.getTelegramId());
        botUserEntity.setPhoneNumber(botUserEntity.getPhoneNumber());
        botUserEntity.setUsername(botUserEntity.getUsername());
        botUserEntity.setFirstName(botUserEntity.getFirstName());
        botUserEntity.setLastName(botUserEntity.getLastName());
        return botUserEntity;
    }
}
