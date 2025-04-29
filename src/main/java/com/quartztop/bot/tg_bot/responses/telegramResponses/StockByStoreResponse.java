package com.quartztop.bot.tg_bot.responses.telegramResponses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockByStoreResponse {
    private String nameStore;
    private float stock;

    public void setNameStore(String nameStore) {
        this.nameStore = nameStore;
    }

    public void setStock(float stock) {
        this.stock = stock;
    }

    public void setReserve(float reserve) {
        this.reserve = reserve;
    }

    public void setInTransit(float inTransit) {
        this.inTransit = inTransit;
    }

    public String getNameStore() {
        return nameStore;
    }

    public float getStock() {
        return stock;
    }

    public float getReserve() {
        return reserve;
    }

    public float getInTransit() {
        return inTransit;
    }

    private float reserve;
    private float inTransit;
}
