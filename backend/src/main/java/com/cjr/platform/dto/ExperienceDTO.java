package com.cjr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ExperienceDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题最长50字")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容最长10000字")
    private String content;

    @NotBlank(message = "请选择分类")
    private String category;

    /** 图片路径列表 */
    private List<String> images;
}
