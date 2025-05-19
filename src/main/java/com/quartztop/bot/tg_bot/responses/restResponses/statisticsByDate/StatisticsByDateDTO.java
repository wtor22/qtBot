package com.quartztop.bot.tg_bot.responses.restResponses.statisticsByDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class StatisticsByDateDTO {

    private long countRegistration;
    private long countClickAction;
    private long countClickNextAction;
    private long countClickPhoto;
    private long countSearchRequest;
    private long countCreateQuestions;

}
