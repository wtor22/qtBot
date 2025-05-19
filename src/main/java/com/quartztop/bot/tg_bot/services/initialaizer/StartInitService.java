package com.quartztop.bot.tg_bot.services.initialaizer;

import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
import com.quartztop.bot.tg_bot.repositories.BotUserRoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartInitService {

    private final BotUserRoleRepository botUserRoleRepository;


    @PostConstruct
    public void rolesStartInit() {
        List<Roles> rolesList = List.of(Roles.values());
        List<BotUserRole> botUserRoleList = new ArrayList<>();
        for (Roles role: rolesList) {
            if(botUserRoleRepository.existsByRole(role)) continue;
            BotUserRole botUserRole = new BotUserRole();
            botUserRole.setRole(role);
            switch (role) {
                case ADMIN -> botUserRole.setName("Админ");
                case USER -> botUserRole.setName("Посетитель");
                case MANAGER -> botUserRole.setName("Менеджер");
            }
            botUserRoleList.add(botUserRole);
        }
        botUserRoleRepository.saveAll(botUserRoleList);
    }
}
