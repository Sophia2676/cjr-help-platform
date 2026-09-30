package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.ExperienceDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Experience;
import com.cjr.platform.vo.ExperienceDetailVO;
import com.cjr.platform.vo.ExperienceVO;

public interface ExperienceService extends IService<Experience> {

    PageResult<ExperienceVO> page(PageQuery query, String category);

    ExperienceDetailVO detail(Long id);

    Long create(Long userId, ExperienceDTO dto);

    void update(Long userId, Long id, ExperienceDTO dto);

    void delete(Long userId, Long id);

    PageResult<ExperienceVO> myPage(Long userId, PageQuery query, Integer status);

    // ---------- 管理端 ----------
    PageResult<ExperienceVO> adminPage(PageQuery query, String keyword, Integer status);

    void audit(Long id, AuditDTO dto);

    void adminDelete(Long id);
}
