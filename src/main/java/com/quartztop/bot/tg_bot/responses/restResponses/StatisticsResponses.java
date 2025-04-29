package com.quartztop.bot.tg_bot.responses.restResponses;

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
    private long clickTabNextActionCount;
    private long clickTabActions;
    private List<ActionStatistics> actionStatisticsList;
}
