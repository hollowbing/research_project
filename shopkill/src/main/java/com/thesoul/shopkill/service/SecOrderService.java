package com.thesoul.shopkill.service;

import com.thesoul.shopkill.entity.SecOrder;

import java.util.List;

public interface SecOrderService {

    List<SecOrder> findByProductId(String productId);

    int save(SecOrder secOrder);

}