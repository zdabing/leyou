package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>, SelectByIdListMapper<Brand,Long> {

    @Select("SELECT b.`name`,b.letter,b.image,b.id FROM tb_brand b LEFT JOIN tb_category_brand cb on b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    List<Brand> queryByCategoryId(@Param("cid") Long cid);
}
