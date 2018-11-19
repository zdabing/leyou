package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author Administrator
 * @create 2018/11/17
 * @since 1.0.0
 */
@Data
public class ExceptionResult {
    private Integer status;
    private String msg;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum e) {
        this.msg = e.getMsg();
        this.status = e.getCode();
        this.timestamp = System.currentTimeMillis();
    }
}
