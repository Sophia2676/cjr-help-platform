package com.cjr.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 饼图数据项 {name, value}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameValueVO {

    private String name;
    private Long value;
}
