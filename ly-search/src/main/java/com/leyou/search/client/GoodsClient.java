package com.leyou.search.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/12/7
 * @since 1.0.0
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
