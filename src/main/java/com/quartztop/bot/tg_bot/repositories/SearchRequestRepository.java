package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.activity.SearchRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchRequestRepository extends JpaRepository<SearchRequestEntity, Long>  {

    long countByTimeRequestBetween(LocalDateTime start, LocalDateTime end);
    List<String> findRequestByTimeRequestBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t.request FROM SearchRequestEntity t WHERE t.timeRequest BETWEEN :start AND :end")
    List<String> findTextsByTimeRequestBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
