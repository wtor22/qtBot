package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.activity.SearchRequestEntity;
import com.quartztop.bot.tg_bot.repositories.SearchRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchRequestService {

    private final SearchRequestRepository searchRequestRepository;

    public void create(String request, BotUser user) {
        SearchRequestEntity searchRequestEntity = new SearchRequestEntity();
        searchRequestEntity.setRequest(request);
        searchRequestEntity.setBotUser(user);
        searchRequestEntity.setTimeRequest(LocalDateTime.now());
        searchRequestRepository.save(searchRequestEntity);
        log.info("Request: {} saved", request);
    }

    public long getCountByPeriod(LocalDateTime start, LocalDateTime end) {
        return searchRequestRepository.countByTimeRequestBetween(start,end);
    }
}
