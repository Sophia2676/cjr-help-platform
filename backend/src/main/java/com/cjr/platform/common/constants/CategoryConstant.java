package com.cjr.platform.common.constants;

import java.util.List;

/**
 * 分类与残疾类别字典
 */
public final class CategoryConstant {

    /** 帖子分类 */
    public static final List<String> POST_CATEGORIES =
            List.of("求医问药", "生活求助", "出行交流", "心理互助", "求职就业", "其他");

    /** 康复经验分类 */
    public static final List<String> EXPERIENCE_CATEGORIES =
            List.of("康复训练", "日常护理", "心理疏导", "辅助器具", "饮食营养", "其他");

    /** 残疾类别 */
    public static final List<String> DISABILITY_TYPES =
            List.of("视力残疾", "听力残疾", "言语残疾", "肢体残疾", "智力残疾", "精神残疾", "多重残疾");

    private CategoryConstant() {
    }
}
