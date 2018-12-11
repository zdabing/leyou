package com.leyou.search.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsClientTest {
    
    @Autowired
    private GoodsClient goodsClient;
    
    @Test
    public void queryDetailById(){
        SpuDetail spuDetail = goodsClient.queryDetailById(2l);
        System.out.println("spuDetail = " + spuDetail);
    }

    @Test
    public void queryBrandByPage(){
        PageResult<Spu> spuPageResult = goodsClient.queryBrandByPage(1, 100, true, null);
        System.out.println("spuPageResult = " + spuPageResult);
    }
}
