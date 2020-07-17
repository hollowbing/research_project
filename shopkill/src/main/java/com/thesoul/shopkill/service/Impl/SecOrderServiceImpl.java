package com.thesoul.shopkill.service.Impl;

import com.thesoul.shopkill.dao.SecOrderMapper;
import com.thesoul.shopkill.entity.SecOrder;
import com.thesoul.shopkill.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecOrderServiceImpl implements SecOrderService {

    @Autowired
    private SecOrderMapper secOrderMapper;

    @Override
    public List<SecOrder> findByProductId(String productId) {

        return secOrderMapper.findByProductId(productId);
    }

    public int save(SecOrder secOrder) {

        return secOrderMapper.save(secOrder);
    }

}