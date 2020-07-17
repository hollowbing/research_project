package com.thesoul.shopkill.service.Impl;

import com.thesoul.shopkill.dao.ProductInfoMapper;
import com.thesoul.shopkill.entity.ProductInfo;
import com.thesoul.shopkill.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Override
    public ProductInfo findOne(String productId) {
        return productInfoMapper.findOne(productId);
    }
}
