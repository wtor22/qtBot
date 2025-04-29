package com.quartztop.bot.tg_bot.responses.restResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class ActionStatistics {

    private Long actionId;
    private int countMoreDetailsClick;
}
