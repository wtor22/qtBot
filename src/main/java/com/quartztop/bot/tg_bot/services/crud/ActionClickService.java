package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.entity.activity.ActionClick;
import com.quartztop.bot.tg_bot.entity.activity.ClickStats;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.activity.ClickType;
import com.quartztop.bot.tg_bot.repositories.ActionClickRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<ClickType, Long> getClickStats(LocalDateTime start, LocalDateTime end) {
        List<ClickStats> rawStats = repositories.countGroupedByClickType(start, end);
        return rawStats.stream().collect(Collectors.toMap(ClickStats::getClickType, ClickStats::getCount));
    }


}
