package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        List<ShopType> shopTypeList = new ArrayList<>();
        //先查找redis
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
        if(shopTypeJsonList != null && !shopTypeJsonList.isEmpty()){
            for(String shopTypeJson : shopTypeJsonList){
                shopTypeList.add(JSONUtil.toBean(shopTypeJson, ShopType.class));
            }
            return Result.ok(shopTypeList);
        }
        //如果没找到，再去数据库里找
        shopTypeList = query().orderByAsc("sort").list();
        if(shopTypeList == null || shopTypeList.isEmpty()){
            return Result.fail("种类为找到");
        }
        //再存入redis中
        for (ShopType shopType : shopTypeList) {
            stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType));
        }
        return Result.ok(shopTypeList);
    }
}
