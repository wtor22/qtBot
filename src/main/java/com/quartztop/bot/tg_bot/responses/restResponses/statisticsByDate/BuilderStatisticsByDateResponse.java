package com.quartztop.bot.tg_bot.responses.restResponses.statisticsByDate;

import com.quartztop.bot.tg_bot.entity.activity.ClickType;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserStatus;
import com.quartztop.bot.tg_bot.services.crud.ActionClickService;
import com.quartztop.bot.tg_bot.services.crud.BotUserService;
import com.quartztop.bot.tg_bot.services.crud.SearchRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuilderStatisticsByDateResponse {

    private final BotUserService botUserService;
    private final ActionClickService actionClickService;
    private final SearchRequestService searchRequestService;

    public StatisticsByDateDTO getResponse(LocalDate start, LocalDate end) {

        LocalDateTime startPeriod = start.atStartOfDay();
        LocalDateTime endPeriod = end.atTime(LocalTime.MAX);

        long countUserRegistrationByPeriod = botUserService.getCountUserByStatusAndPeriod(BotUserStatus.ACTIVE, startPeriod, endPeriod);

        long countSearchRequest = searchRequestService.getCountByPeriod(startPeriod, endPeriod);

        Map<ClickType, Long> stats = actionClickService.getClickStats(startPeriod, endPeriod);
        long countCreateQuestion =  (stats.get(ClickType.CREATE_QUESTION) == null) ? 0: stats.get(ClickType.CREATE_QUESTION);
        long countClickPhoto = (stats.get(ClickType.GET_PHOTO) == null) ? 0 : stats.get(ClickType.GET_PHOTO);
        long countClickNextAction = (stats.get(ClickType.NEXT) == null) ? 0 : stats.get(ClickType.NEXT);
        long countActionClick = (stats.get(ClickType.ACTION_CLICK) == null) ? 0 : stats.get(ClickType.ACTION_CLICK);

        return StatisticsByDateDTO.builder()
                .countClickAction(countActionClick)
                .countClickNextAction(countClickNextAction)
                .countCreateQuestions(countCreateQuestion)
                .countRegistration(countUserRegistrationByPeriod)
                .countSearchRequest(countSearchRequest)
                .countClickPhoto(countClickPhoto)
                .build();
    }
}
