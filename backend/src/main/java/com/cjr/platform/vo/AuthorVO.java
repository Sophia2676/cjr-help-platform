package com.cjr.platform.vo;

import lombok.Data;

/**
 * 作者简要信息(列表/详情中内嵌)
 */
@Data
public class AuthorVO {

    private Long id;
    private String nickname;
    private String avatar;
    private String disabilityType;
}
