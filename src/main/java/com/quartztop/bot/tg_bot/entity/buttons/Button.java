package com.quartztop.bot.tg_bot.entity.buttons;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Button {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text_button")
    private String textButton;

    @Column(name = "button_value")
    private String buttonValue;

    @Column(name = "order_index")
    private Integer orderInBotIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "button_type")
    private ButtonType buttonType;

}
