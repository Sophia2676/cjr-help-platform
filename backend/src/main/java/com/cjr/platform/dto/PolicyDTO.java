package com.cjr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PolicyDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字")
    private String title;

    @Size(max = 255, message = "摘要最长255字")
    private String summary;

    @NotBlank(message = "正文不能为空")
    @Size(max = 20000, message = "正文最长20000字")
    private String content;

    private String coverImage;

    @Size(max = 100, message = "来源最长100字")
    private String source;
}
