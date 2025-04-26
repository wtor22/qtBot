package com.quartztop.bot.tg_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockByProductResponse {

    private String productName;
    private String category;
    private String article;
    private String sizeProduct;
    private String sortProduct;
    private String surfaceProduct;
    private String formatProduct;
    private String thicknessProduct;
    private String recommendedPrice;


    private List<StockByStoreResponse> byStoreResponseList;
}
