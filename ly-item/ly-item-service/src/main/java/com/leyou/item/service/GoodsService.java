package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    public PageResult<Spu> queryGoodsByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 开始分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        // 搜索字段定义
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().andLike("title", "%" + key + "%");
        }
        // 上下架过滤
        if (saleable != null) {
            example.createCriteria().andEqualTo("saleable", saleable);
        }
        // 排序
        example.setOrderByClause("last_update_time DESC");
        // 查询
        List<Spu> list = spuMapper.selectByExample(example);
        // 封装品牌跟分裂名称
        loadCategoryAndBrandName(list);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        PageInfo<Spu> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    private void loadCategoryAndBrandName(List<Spu> list) {
        for (Spu spu : list) {
            List<String> names = categoryService.queryCategoryListByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    public SpuDetail querySpuDetailByid(Long id){
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.SPU_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkusBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        return skus;
    }

    public Spu querySpuByid(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new  LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spu;
    }

    private void saveSkuAndStock(Spu spu) {
        List<Stock> list = new ArrayList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            // 保存sku
            sku.setSpuId(spu.getId());
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            list.add(stock);
        }
        // 批量新增库存
        int count = stockMapper.insertList(list);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }
}
