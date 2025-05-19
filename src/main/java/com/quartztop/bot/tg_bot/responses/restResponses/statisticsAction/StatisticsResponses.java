package com.quartztop.bot.tg_bot.responses.restResponses.statisticsAction;

import com.quartztop.bot.tg_bot.responses.restResponses.statisticsAction.ActionStatistics;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class StatisticsResponses {
    private long usersCount;
    private long usersNotActiveStatusCount;
    private List<ActionStatistics> actionStatisticsList;
}
