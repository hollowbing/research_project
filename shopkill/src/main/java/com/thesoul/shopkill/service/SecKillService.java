package com.thesoul.shopkill.service;


import com.thesoul.shopkill.entity.SecOrder;
import com.thesoul.shopkill.entity.SecProductInfo;
import org.springframework.stereotype.Service;

@Service
public interface SecKillService {

    long orderProductMockDiffUser(String productId,SecOrder secOrder)throws Exception;

    SecProductInfo refreshStock(String productId);
}