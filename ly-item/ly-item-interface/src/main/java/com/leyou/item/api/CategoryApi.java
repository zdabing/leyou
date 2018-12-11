package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/12/7
 * @since 1.0.0
 */
public interface CategoryApi {

    @GetMapping("category/list/ids")
    List<Category> queryCategoryListByIds(@RequestParam("ids") List<Long> ids);
}
