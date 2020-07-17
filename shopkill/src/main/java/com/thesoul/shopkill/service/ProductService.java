package com.thesoul.shopkill.service;

import com.thesoul.shopkill.entity.ProductInfo;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {

    ProductInfo findOne(String productId);
}
