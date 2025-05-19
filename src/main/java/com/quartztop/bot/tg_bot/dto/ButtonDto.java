package com.quartztop.bot.tg_bot.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ButtonDto {

    private Long id;
    private String textButton;
    private String buttonValue;
    private Integer orderInBotIndex;
}
