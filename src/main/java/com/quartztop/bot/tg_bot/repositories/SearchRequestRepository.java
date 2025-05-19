package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.activity.SearchRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SearchRequestRepository extends JpaRepository<SearchRequestEntity, Long>  {

    long countByTimeRequestBetween(LocalDateTime start, LocalDateTime end);
}
