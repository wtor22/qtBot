package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.dto.BotUserDTO;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserStatus;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.repositories.BotUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotUserService {

    private final BotUserRepositories repositories;
    private final BotUserRoleRepository botUserRoleRepository;

    public BotUserDTO create(BotUserDTO botUserDTO) {

        Optional<BotUser> optionalBotUser = repositories.findById(botUserDTO.getTelegramId());
        if(optionalBotUser.isPresent()) throw new RuntimeException("Пользователь с таким id еже зарегистрирован");
        BotUser botUser = mapToEntity(botUserDTO);

        return mapToDto(repositories.save(botUser));
    }
    public List<BotUserDTO> getUsersByPartPhone(String part) {
        BotUserRole roleUser = botUserRoleRepository.findByRole(Roles.USER);
        List<BotUser> botUserList = repositories.findByPhoneNumberContainingAndBotUserRole(part, roleUser);
        return botUserList.stream().map(BotUserService::mapToDto).toList();
    }

    public long getCountUserByStatusAndPeriod(BotUserStatus botUserStatus, LocalDateTime start, LocalDateTime end) {
        return repositories.countByStatusAndRegisteredAtBetween(botUserStatus,start,end);
    }


    public String setUserRole(Long telegramId, BotUserRole botUserRole ) {
        Optional<BotUser> optionalBotUser = repositories.findById(telegramId);
        if(optionalBotUser.isEmpty()) return "Пользователь не найден в БД";
        BotUser botUser = optionalBotUser.get();
        botUser.setBotUserRole(botUserRole);
        repositories.save(botUser);
        log.warn("⚠\uFE0F Пользователь {} установлен как {}} " , botUser.getPhoneNumber(), botUserRole.getName());
        return "Пользователю с номером телефона " + botUser.getPhoneNumber() + " назначена роль " + botUserRole.getName();

    }

    public List<BotUserDTO> getUsersDTOByRole(BotUserRole role) {
        List<BotUser> botUserList = repositories.findByBotUserRole(role);
        return botUserList.stream().map(BotUserService::mapToDto).toList();
    }

    public static BotUserDTO mapToDto(BotUser botUser) {

        BotUserDTO botUserDTO = new BotUserDTO();
        botUserDTO.setTelegramId(botUser.getTelegramId());
        botUserDTO.setPhoneNumber(botUser.getPhoneNumber());
        botUserDTO.setUsername(botUser.getUsername());
        botUserDTO.setFirstName(botUser.getFirstName());
        botUserDTO.setLastName(botUser.getLastName());
        botUserDTO.setRegisteredAt(botUser.getRegisteredAt());
        return botUserDTO;
    }

    public static BotUser mapToEntity(BotUserDTO botUser) {

        BotUser botUserEntity = new BotUser();
        botUserEntity.setTelegramId(botUser.getTelegramId());
        botUserEntity.setPhoneNumber(botUser.getPhoneNumber());
        botUserEntity.setUsername(botUser.getUsername());
        botUserEntity.setFirstName(botUser.getFirstName());
        botUserEntity.setLastName(botUser.getLastName());
        botUserEntity.setRegisteredAt(botUser.getRegisteredAt());
        return botUserEntity;
    }
}
