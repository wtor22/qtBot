package com.quartztop.bot.tg_bot.responses.restResponses.statisticsAction;

import com.quartztop.bot.tg_bot.entity.activity.ActionClick;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserStatus;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.repositories.SearchRequestRepository;
import com.quartztop.bot.tg_bot.services.crud.ActionClickService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuilderStatisticsResponse {

    private final ActionClickService actionClickService;
    private final BotUserRepositories botUserRepositories;
    private final SearchRequestRepository searchRequestRepository;

    public StatisticsResponses getStatisticsResponses() {

        List<String> listRequest = searchRequestRepository.findTextsByTimeRequestBetween(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

        Map<Long, ActionStatistics> statisticsMap = new HashMap<>();

        List<ActionClick> clickList = actionClickService.getActionClickList();

        for(ActionClick actionClick: clickList) {
            ActionStatistics stats = statisticsMap.get(actionClick.getActionId());
            if(stats == null) {
                ActionStatistics actionStatistics = ActionStatistics.builder()
                        .actionId(actionClick.getActionId())
                        .countMoreDetailsClick(1)
                        .build();
                statisticsMap.put(actionClick.getActionId(), actionStatistics);
            } else {
                stats.setCountMoreDetailsClick(stats.getCountMoreDetailsClick() + 1);
            }
        }

        List<ActionStatistics> actionStatisticsList = new ArrayList<>(statisticsMap.values());

        long botUserCount = botUserRepositories.count();
        long botUserCountNotActive = botUserRepositories.countByStatus(BotUserStatus.REGISTERED);

        return StatisticsResponses.builder()
                .usersCount(botUserCount)
                .usersNotActiveStatusCount(botUserCountNotActive)
                .requestsStockToday(listRequest)
                .actionStatisticsList(actionStatisticsList)
                .build();
    }
}
