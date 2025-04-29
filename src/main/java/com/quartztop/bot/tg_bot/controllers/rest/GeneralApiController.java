package com.quartztop.bot.tg_bot.controllers.rest;

import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.responses.restResponses.BuilderStatisticsResponse;
import com.quartztop.bot.tg_bot.responses.restResponses.StatisticsResponses;
import com.quartztop.bot.tg_bot.services.crud.ActionClickService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Slf4j
public class GeneralApiController {

    private final BotUserRepositories botUserRepositories;
    private final BuilderStatisticsResponse statisticsResponse;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponses> getBotStatistics(){
        log.error("START CONTROLLER");
        ResponseEntity.ok(statisticsResponse.getStatisticsResponses());
        return ResponseEntity.ok(statisticsResponse.getStatisticsResponses());
    }

}
