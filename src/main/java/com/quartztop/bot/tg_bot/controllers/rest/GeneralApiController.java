package com.quartztop.bot.tg_bot.controllers.rest;

import com.quartztop.bot.tg_bot.dto.BotUserDTO;
import com.quartztop.bot.tg_bot.dto.ButtonDto;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
import com.quartztop.bot.tg_bot.repositories.BotUserRoleRepository;
import com.quartztop.bot.tg_bot.repositories.ButtonsRepository;
import com.quartztop.bot.tg_bot.repositories.SearchRequestRepository;
import com.quartztop.bot.tg_bot.responses.restResponses.statisticsAction.BuilderStatisticsResponse;
import com.quartztop.bot.tg_bot.responses.restResponses.statisticsAction.StatisticsResponses;
import com.quartztop.bot.tg_bot.responses.restResponses.statisticsByDate.BuilderStatisticsByDateResponse;
import com.quartztop.bot.tg_bot.responses.restResponses.statisticsByDate.StatisticsByDateDTO;
import com.quartztop.bot.tg_bot.services.crud.BotUserService;
import com.quartztop.bot.tg_bot.services.crud.ButtonsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Slf4j
public class GeneralApiController {

    private final BuilderStatisticsResponse statisticsResponse;
    private final ButtonsRepository buttonsRepository;
    private final ButtonsService buttonsService;
    private final BotUserService botUserService;
    private final BotUserRoleRepository botUserRoleRepository;
    private final BuilderStatisticsByDateResponse builderStatisticsByDateResponse;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponses> getBotStatistics(){
        return ResponseEntity.ok(statisticsResponse.getStatisticsResponses());
    }
    @GetMapping("/statistics-by-period")
    public ResponseEntity<StatisticsByDateDTO> getBotStatisticsByPeriod(@RequestParam LocalDate start,
                                                                        @RequestParam LocalDate end) {
        StatisticsByDateDTO response = builderStatisticsByDateResponse.getResponse(start,end);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/button")
    public ResponseEntity<String> createImageLinkButton(@RequestBody ButtonDto button) {
        if(buttonsRepository.existsByTextButton(button.getTextButton())) return ResponseEntity.badRequest().body("С таким текстом кнопка уже существует");
        buttonsService.createImageLinkButton(button);
        return ResponseEntity.ok("Сохранено успешно");
    }

    @PutMapping("/button")
    public ResponseEntity<String> updateImageLinkButton(@RequestBody ButtonDto button) {
        String response = buttonsService.update(button);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/button")
    public ResponseEntity<List<ButtonDto>> getAllButton(){
        return ResponseEntity.ok(buttonsService.getAll());
    }
    @DeleteMapping("button/{id}")
    public ResponseEntity<String> deleteButton(@PathVariable Long id){
        buttonsService.delete(id);
        return ResponseEntity.ok("Элемент удален");
    }

    @PutMapping("/button/order")
    public ResponseEntity<List<ButtonDto>> setOrderButton(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(buttonsService.setOrder(ids));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<BotUserDTO>> getListUserByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(botUserService.getUsersByPartPhone(phone));
    }

    @PutMapping("/users/set-admin")
    public ResponseEntity<String> setUserAsAdmin(@RequestBody Long telegramId) {
        BotUserRole roleAdmin = botUserRoleRepository.findByRole(Roles.ADMIN);
        String response = botUserService.setUserRole(telegramId, roleAdmin);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/users/unset-admin")
    public ResponseEntity<String> unsetUserAsAdmin(@RequestBody Long telegramId) {
        BotUserRole roleUser = botUserRoleRepository.findByRole(Roles.USER);
        String response = botUserService.setUserRole(telegramId, roleUser);
        return ResponseEntity.ok(response);
    }
    @GetMapping("users")
    public ResponseEntity<List<BotUserDTO>> getBotUsersByRequest(@RequestParam String request){
        BotUserRole role = botUserRoleRepository.findByRole(Roles.USER);
        List<BotUserDTO> list =  botUserService.getUsersDTOByRole(role);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/users/admins")
    public ResponseEntity<List<BotUserDTO>> getListAdmins() {
        BotUserRole role = botUserRoleRepository.findByRole(Roles.ADMIN);
        return ResponseEntity.ok(botUserService.getUsersDTOByRole(role));
    }

}
