package com.quartztop.bot.tg_bot.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockByCategoryResponse {

    private String category;
    List<StockByProductResponse> productsList;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProductsList(List<StockByProductResponse> productsList) {
        this.productsList = productsList;
    }

    public String getCategory() {
        return category;
    }

    public List<StockByProductResponse> getProductsList() {
        return productsList;
    }
}
