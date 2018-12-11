package com.leyou.search.repository;


import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void testIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData() {
        int page = 1;
        int rows = 5;
        int size = 0;
        do {
            //  查询spu信息
            List<Spu> result = goodsClient.queryBrandByPage(page, rows, true, null).getItems();
            if (CollectionUtils.isEmpty(result)) {
                break;
            }
            // 构建成goods
            List<Goods> goodsList = result.stream().map(searchService::buildGoods).collect(Collectors.toList());
            // 存入数据库
            goodsRepository.saveAll(goodsList);
            //翻页
            page++;
            size = result.size();
        } while (size == 5);

    }
}
