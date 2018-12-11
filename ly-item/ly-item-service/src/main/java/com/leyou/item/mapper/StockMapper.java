package com.leyou.item.mapper;

import com.leyou.item.pojo.Stock;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.ids.DeleteByIdsMapper;

/**
 * @Author: cuzz
 * @Date: 2018/11/7 19:18
 * @Description: 必须导入tk.mybatis.mapper.additional.insert.InsertListMapper这个包
 */
public interface StockMapper extends Mapper<Stock>, DeleteByIdsMapper<Stock> , InsertListMapper<Stock> {
}
