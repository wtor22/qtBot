package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.entity.ActionClick;
import com.quartztop.bot.tg_bot.entity.BotUser;
import com.quartztop.bot.tg_bot.entity.ClickType;
import com.quartztop.bot.tg_bot.repositories.ActionClickRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActionClickService {

    private final ActionClickRepositories repositories;

    public void create(Long actionId, BotUser user, ClickType clickType) {
        ActionClick actionClick = ActionClick.builder()
                .actionId(actionId)
                .clickType(clickType)
                .clickTime(LocalDateTime.now())
                .botUser(user)
                .build();

        repositories.save(actionClick);
    }

    public List<ActionClick> getActionClickList() {

        return repositories.findAllByClickType(ClickType.MORE_DETAILS);
    }

}
