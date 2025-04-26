package com.quartztop.bot.tg_bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    @Getter
    @Setter
    private String token;
    private String urlSearchRequest;
    private String appUrl;

}
