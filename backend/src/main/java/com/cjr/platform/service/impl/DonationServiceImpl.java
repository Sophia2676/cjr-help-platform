package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.constants.HelpStatus;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.dto.DonateDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Donation;
import com.cjr.platform.entity.HelpRequest;
import com.cjr.platform.entity.User;
import com.cjr.platform.mapper.DonationMapper;
import com.cjr.platform.mapper.HelpRequestMapper;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.service.DonationService;
import com.cjr.platform.service.HelpRequestService;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.vo.DonationVO;
import com.cjr.platform.vo.HelpRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl extends ServiceImpl<DonationMapper, Donation> implements DonationService {

    private final HelpRequestMapper helpRequestMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HelpRequestVO donate(Long userId, Long requestId, DonateDTO dto) {
        HelpRequest request = helpRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("求助不存在");
        }
        if (request.getStatus() != HelpStatus.RAISING) {
            throw new BusinessException("该求助当前不在募捐中");
        }
        if (request.getUserId().equals(userId)) {
            throw new BusinessException("不能捐助自己发布的求助");
        }
        BigDecimal remaining = request.getTargetAmount().subtract(request.getRaisedAmount());
        if (dto.getAmount().compareTo(remaining) > 0) {
            throw new BusinessException("捐助金额不能超过剩余目标金额 " + remaining + " 元");
        }

        Donation donation = new Donation();
        donation.setHelpRequestId(requestId);
        donation.setDonorId(userId);
        donation.setAmount(dto.getAmount());
        donation.setMessage(dto.getMessage());
        donation.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous() == 1 ? 1 : 0);
        donation.setStatus(1);
        save(donation);

        // 原子累加已筹金额与捐助人次
        helpRequestMapper.update(null, new UpdateWrapper<HelpRequest>()
                .eq("id", requestId)
                .setSql("raised_amount = raised_amount + " + dto.getAmount())
                .setSql("donate_count = donate_count + 1"));

        BigDecimal newRaised = request.getRaisedAmount().add(dto.getAmount());
        if (newRaised.compareTo(request.getTargetAmount()) >= 0) {
            // 筹满自动完成
            helpRequestMapper.update(null, new UpdateWrapper<HelpRequest>()
                    .eq("id", requestId)
                    .eq("status", HelpStatus.RAISING)
                    .set("status", HelpStatus.COMPLETED)
                    .set("complete_time", LocalDateTime.now()));
        }

        // 消息通知双方
        User donor = userMapper.selectById(userId);
        String donorName = donation.getIsAnonymous() == 1 ? "爱心人士" : donor.getNickname();
        messageService.send(request.getUserId(), MessageType.DONATION, "收到爱心捐助",
                donorName + " 为您的求助《" + request.getTitle() + "》捐助了 " + dto.getAmount() + " 元", requestId);
        messageService.send(userId, MessageType.DONATION, "捐助成功",
                "您为《" + request.getTitle() + "》捐助了 " + dto.getAmount() + " 元，感谢您的爱心！", requestId);

        // 返回最新进度
        HelpRequest fresh = helpRequestMapper.selectById(requestId);
        return HelpRequestService.toVO(fresh, userMapper.selectById(fresh.getUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long userId, Long donationId) {
        Donation donation = getById(donationId);
        if (donation == null) {
            throw new BusinessException("捐助记录不存在");
        }
        HelpRequest request = helpRequestMapper.selectById(donation.getHelpRequestId());
        if (request == null || !request.getUserId().equals(userId)) {
            throw new BusinessException("仅求助人本人可确认");
        }
        if (donation.getStatus() == 2) {
            return;
        }
        Donation update = new Donation();
        update.setId(donationId);
        update.setStatus(2);
        update.setConfirmTime(LocalDateTime.now());
        updateById(update);
        messageService.send(donation.getDonorId(), MessageType.DONATION, "捐助已被确认",
                "您为《" + request.getTitle() + "》捐助的 " + donation.getAmount() + " 元已被受助人确认收到", request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long userId, Long requestId) {
        HelpRequest request = helpRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException("求助不存在");
        }
        if (!request.getUserId().equals(userId)) {
            throw new BusinessException("仅求助人本人可操作");
        }
        if (request.getStatus() != HelpStatus.RAISING) {
            throw new BusinessException("当前状态不可完成");
        }
        HelpRequest update = new HelpRequest();
        update.setId(requestId);
        update.setStatus(HelpStatus.COMPLETED);
        update.setCompleteTime(LocalDateTime.now());
        helpRequestMapper.updateById(update);

        // 通知所有捐助人
        List<Donation> donations = lambdaQuery().eq(Donation::getHelpRequestId, requestId).list();
        Set<Long> donorIds = donations.stream().map(Donation::getDonorId).collect(Collectors.toSet());
        for (Long donorId : donorIds) {
            messageService.send(donorId, MessageType.DONATION, "求助已完成",
                    "您捐助的《" + request.getTitle() + "》已完成筹款，感谢您的爱心捐助！", requestId);
        }
    }

    @Override
    public PageResult<DonationVO> myDonations(Long userId, PageQuery query) {
        Page<Donation> page = buildPage(query);
        lambdaQuery()
                .eq(Donation::getDonorId, userId)
                .orderByDesc(Donation::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(d -> toVOWithTitle(d, false)));
    }

    @Override
    public PageResult<DonationVO> received(Long userId, PageQuery query, Long helpRequestId) {
        List<HelpRequest> myRequests = helpRequestMapper.selectList(
                new LambdaQueryWrapper<HelpRequest>().eq(HelpRequest::getUserId, userId));
        if (myRequests.isEmpty()) {
            return new PageResult<>(0, new ArrayList<>());
        }
        List<Long> requestIds = myRequests.stream().map(HelpRequest::getId).collect(Collectors.toList());
        Page<Donation> page = buildPage(query);
        lambdaQuery()
                .in(Donation::getHelpRequestId, requestIds)
                .eq(helpRequestId != null, Donation::getHelpRequestId, helpRequestId)
                .orderByDesc(Donation::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(d -> toVOWithTitle(d, true)));
    }

    // ---------- 管理端 ----------

    @Override
    public PageResult<DonationVO> adminPage(PageQuery query, String keyword, Long helpRequestId) {
        List<Long> matchedIds = null;
        if (keyword != null && !keyword.isBlank()) {
            matchedIds = helpRequestMapper.selectList(new LambdaQueryWrapper<HelpRequest>()
                            .like(HelpRequest::getTitle, keyword))
                    .stream().map(HelpRequest::getId).collect(Collectors.toList());
            if (matchedIds.isEmpty()) {
                return new PageResult<>(0, new ArrayList<>());
            }
        }
        Page<Donation> page = buildPage(query);
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<Donation>()
                .in(matchedIds != null, Donation::getHelpRequestId, matchedIds)
                .eq(helpRequestId != null, Donation::getHelpRequestId, helpRequestId)
                .orderByDesc(Donation::getCreateTime);
        page(page, wrapper);
        return PageResult.of(page.convert(d -> toVOWithTitle(d, false)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        Donation donation = getById(id);
        if (donation == null) {
            throw new BusinessException("捐助记录不存在");
        }
        HelpRequest request = helpRequestMapper.selectById(donation.getHelpRequestId());
        if (request != null) {
            // 回滚已筹金额与人次
            helpRequestMapper.update(null, new UpdateWrapper<HelpRequest>()
                    .eq("id", donation.getHelpRequestId())
                    .setSql("raised_amount = GREATEST(raised_amount - " + donation.getAmount() + ", 0)")
                    .setSql("donate_count = GREATEST(donate_count - 1, 0)"));
            if (request.getStatus() == HelpStatus.COMPLETED) {
                helpRequestMapper.update(null, new UpdateWrapper<HelpRequest>()
                        .eq("id", donation.getHelpRequestId())
                        .set("status", HelpStatus.RAISING)
                        .set("complete_time", null));
            }
        }
        removeById(id);
        messageService.send(donation.getDonorId(), MessageType.SYSTEM, "捐助记录已撤销",
                "您在《" + (request != null ? request.getTitle() : "未知求助") + "》中的捐助记录已被管理员撤销", donation.getHelpRequestId());
    }

    // ---------- 私有方法 ----------

    private DonationVO toVOWithTitle(Donation d, boolean withDonor) {
        HelpRequest request = helpRequestMapper.selectById(d.getHelpRequestId());
        String title = request != null ? request.getTitle() : null;
        User donor = withDonor ? userMapper.selectById(d.getDonorId()) : null;
        return DonationService.toVO(d, donor, title);
    }

    private Page<Donation> buildPage(PageQuery query) {
        int num = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int size = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100);
        return new Page<>(num, size);
    }
}
