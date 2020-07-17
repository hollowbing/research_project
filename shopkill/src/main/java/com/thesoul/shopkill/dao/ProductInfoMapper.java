package com.thesoul.shopkill.dao;

import com.thesoul.shopkill.entity.ProductInfo;
import com.thesoul.shopkill.entity.SecOrder;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ProductInfoMapper {

    ProductInfo findOne(String productId);
}
