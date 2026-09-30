package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.PolicyDTO;
import com.cjr.platform.entity.Policy;
import com.cjr.platform.vo.PolicyDetailVO;
import com.cjr.platform.vo.PolicyVO;

public interface PolicyService extends IService<Policy> {

    /** 公开政策分页(仅已发布, 按发布时间倒序) */
    PageResult<PolicyVO> page(PageQuery query);

    PolicyDetailVO detail(Long id);

    // ---------- 管理端 ----------
    PageResult<Policy> adminPage(PageQuery query, String keyword);

    Long create(Long adminId, PolicyDTO dto);

    void update(Long id, PolicyDTO dto);

    void status(Long id, Integer status);

    void adminDelete(Long id);
}
