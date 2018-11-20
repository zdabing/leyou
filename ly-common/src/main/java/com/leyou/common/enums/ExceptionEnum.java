package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    CATEGORY_NOT_FOUND(404,"商品分类为空"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组为空"),
    BRAND_NOT_FOUND(404,"品牌没查到"),
    GOODS_NOT_FOUND(404,"商品没查到"),
    INVALID_FILE_TYPE(400,"文件类型不对"),
    FILE_UPLOAD_ERROR(500,"文件上传失败")
    ;
    private Integer code;
    private String msg;
}
