package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.DonateDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Donation;
import com.cjr.platform.entity.User;
import com.cjr.platform.vo.DonationVO;
import com.cjr.platform.vo.HelpRequestVO;

public interface DonationService extends IService<Donation> {

    /** 发起捐助(核心事务), 返回最新进度 */
    HelpRequestVO donate(Long userId, Long requestId, DonateDTO dto);

    /** 受助人确认收到单笔捐助 */
    void confirm(Long userId, Long donationId);

    /** 受助人手动完成筹款 */
    void complete(Long userId, Long requestId);

    /** 我捐出的记录 */
    PageResult<DonationVO> myDonations(Long userId, PageQuery query);

    /** 我收到的捐助 */
    PageResult<DonationVO> received(Long userId, PageQuery query, Long helpRequestId);

    // ---------- 管理端 ----------
    PageResult<DonationVO> adminPage(PageQuery query, String keyword, Long helpRequestId);

    void adminDelete(Long id);

    /** 实体转 VO(匿名捐助对外显示"爱心人士") */
    static DonationVO toVO(Donation d, User donor, String helpTitle) {
        DonationVO vo = new DonationVO();
        vo.setId(d.getId());
        vo.setHelpRequestId(d.getHelpRequestId());
        vo.setHelpTitle(helpTitle);
        vo.setAmount(d.getAmount());
        vo.setMessage(d.getMessage());
        vo.setIsAnonymous(d.getIsAnonymous());
        vo.setStatus(d.getStatus());
        vo.setConfirmTime(d.getConfirmTime());
        vo.setCreateTime(d.getCreateTime());
        boolean anon = d.getIsAnonymous() != null && d.getIsAnonymous() == 1;
        vo.setDonorNickname(anon ? "爱心人士" : (donor != null ? donor.getNickname() : "该用户已注销"));
        vo.setDonorAvatar(anon ? null : (donor != null ? donor.getAvatar() : null));
        return vo;
    }
}
