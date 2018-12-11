package com.leyou.search.web;

import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/12/8
 * @since 1.0.0
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest request){
        return ResponseEntity.ok(searchService.search(request));
    }
}
