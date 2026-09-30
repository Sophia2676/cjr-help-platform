package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.HelpRequestDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.HelpRequest;
import com.cjr.platform.entity.User;
import com.cjr.platform.vo.HelpRequestVO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface HelpRequestService extends IService<HelpRequest> {

    /** 公开求助分页(仅募捐中) */
    PageResult<HelpRequestVO> page(PageQuery query);

    HelpRequestVO detail(Long id);

    Long create(Long userId, HelpRequestDTO dto);

    void update(Long userId, Long id, HelpRequestDTO dto);

    void delete(Long userId, Long id);

    PageResult<HelpRequestVO> myPage(Long userId, PageQuery query);

    /** 社区工作者面板：全部求助列表(含求助人账号手机号) */
    PageResult<HelpRequestVO> workerPage(PageQuery query);

    // ---------- 管理端 ----------
    PageResult<HelpRequestVO> adminPage(PageQuery query, String keyword, Integer status);

    void audit(Long id, AuditDTO dto);

    void adminDelete(Long id);

    /** 实体转 VO(含进度百分比) */
    static HelpRequestVO toVO(HelpRequest r, User u) {
        HelpRequestVO vo = new HelpRequestVO();
        vo.setId(r.getId());
        vo.setTitle(r.getTitle());
        vo.setDescription(r.getDescription());
        vo.setTargetAmount(r.getTargetAmount());
        vo.setRaisedAmount(r.getRaisedAmount());
        vo.setDonateCount(r.getDonateCount());
        int progress = 0;
        if (r.getTargetAmount() != null && r.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = r.getRaisedAmount().multiply(BigDecimal.valueOf(100))
                    .divide(r.getTargetAmount(), 0, RoundingMode.DOWN).intValue();
        }
        vo.setProgress(Math.min(progress, 100));
        vo.setImages(r.getImages() == null || r.getImages().isBlank()
                ? java.util.Collections.emptyList()
                : java.util.Arrays.stream(r.getImages().split(",")).filter(s -> !s.isBlank()).toList());
        vo.setContactPhone(r.getContactPhone());
        vo.setStatus(r.getStatus());
        vo.setRejectReason(r.getRejectReason());
        vo.setCompleteTime(r.getCompleteTime());
        vo.setCreateTime(r.getCreateTime());
        vo.setUser(com.cjr.platform.service.UserService.toAuthorVO(u));
        return vo;
    }
}
