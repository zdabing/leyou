package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/12/7
 * @since 1.0.0
 */
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu) {
        // 查询分类
        List<Category> categories = categoryClient.queryCategoryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 搜索字段
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();

        // 查询sku
        List<Sku> skuList = goodsClient.querySkuList(spu.getId());
        // 对sku进行处理
        List<Map<String, Object>> skus = new ArrayList<>();
        // 查询价格
        Set<Long> priceList = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("images", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            skus.add(map);
            priceList.add(sku.getPrice());
        }

        // 查询属性参数
        List<SpecParam> params = specificationClient.querySpecParams(null, spu.getCid3(), true);
        // 查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spu.getId());
        // 获取通用规格
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        // 获取特有规格
        Map<Long, Object> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, Object>>() {
        });

        // 规格参数 key是规格参数的名字,值是规格参数的值
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            // 规格名称
            String key = param.getName();
            Object value = "";
            // 判断是否通用规格
            if (param.getGeneric()){
                value = genericSpec.get(param.getId());
                if (param.getNumeric()){
                    value = chooseSegment(value.toString(),param);
                }
            }else {
                value = specialSpec.get(param.getId());
            }
            specs.put(key,value);
        }
        // 构建goods对象
        Goods goods = new Goods();
        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);   // 搜索字段,包含 标题,分类,品牌,规格等
        goods.setSkus(JsonUtils.toString(skus)); //  所有的sku集合json
        goods.setSpecs(specs); //  所有的可搜索规格参数
        goods.setPrice(priceList); // 所有的spu价格集合
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        Integer page = request.getPage() - 1;// page 从0开始
        Integer size = request.getSize();

        // 1、创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2、查询
        // 2.1、对结果进行筛选
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"}, null));
        // 2.2、基本查询
        QueryBuilder baseKey = buildBasicQueryWithFilter(request);
        queryBuilder.withQuery(baseKey);

        // 2.3、分页
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 2.4 聚合分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 2.4 聚合品牌
        String BrandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(BrandAggName).field("brandId"));

        // 3、返回结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        // 4.1 解析分页结果
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();

        // 4.2 解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categoryList = parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brandList = parseBrandAgg(aggs.get(BrandAggName));

        // 4.3 聚合规格参数
        List<Map<String,Object>> specs = null;
        if (categoryList != null && categoryList.size() == 1) {
            // 如果分类只剩下一个，才进行规格参数过滤
            specs = buildSpecAgg(categoryList.get(0).getId(), baseKey);
        }
        return new SearchResult(total,totalPage,goodsList,categoryList,brandList,specs);
    }

    private List<Map<String, Object>> buildSpecAgg(Long cid, QueryBuilder baseKey) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 1.查询需要聚合的参数
        List<SpecParam> specParams = specificationClient.querySpecParams(null, cid, true);
        // 2.聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.1 带上查询条件
        queryBuilder.withQuery(baseKey);
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        // 获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : specParams) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);

            // 准备map
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets()
                    .stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Brand> brandList = brandClient.queryBrandByIds(ids);
            return brandList;
        } catch (Exception e){
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets()
                    .stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Category> categoryList = categoryClient.queryCategoryListByIds(ids);
            return categoryList;
        } catch (Exception e){
            return null;
        }
    }

    // 构建基本查询条件
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌要特殊处理
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }
}
