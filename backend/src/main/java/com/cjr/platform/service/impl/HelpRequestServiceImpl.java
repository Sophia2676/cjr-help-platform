package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.constants.HelpStatus;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.HelpRequestDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Donation;
import com.cjr.platform.entity.HelpRequest;
import com.cjr.platform.entity.User;
import com.cjr.platform.mapper.DonationMapper;
import com.cjr.platform.mapper.HelpRequestMapper;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.service.DonationService;
import com.cjr.platform.service.HelpRequestService;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.vo.HelpRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HelpRequestServiceImpl extends ServiceImpl<HelpRequestMapper, HelpRequest> implements HelpRequestService {

    private final DonationMapper donationMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;

    @Override
    public PageResult<HelpRequestVO> page(PageQuery query) {
        Page<HelpRequest> page = buildPage(query);
        lambdaQuery()
                .eq(HelpRequest::getStatus, HelpStatus.RAISING)
                .and(query.getKeyword() != null && !query.getKeyword().isBlank(),
                        w -> w.like(HelpRequest::getTitle, query.getKeyword())
                                .or().like(HelpRequest::getDescription, query.getKeyword()))
                .orderByDesc(HelpRequest::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(this::toVOWithUser));
    }

    @Override
    public HelpRequestVO detail(Long id) {
        HelpRequest request = getById(id);
        if (request == null) {
            throw new BusinessException(404, "求助不存在");
        }
        Long currentId = UserContext.getUserId();
        boolean isAuthor = request.getUserId().equals(currentId);
        boolean isAdmin = isAdmin();
        if (request.getStatus() != HelpStatus.RAISING && request.getStatus() != HelpStatus.COMPLETED
                && !isAuthor && !isAdmin) {
            throw new BusinessException(404, "求助不存在或未通过审核");
        }
        HelpRequestVO vo = toVOWithUser(request);
        // 联系电话仅登录用户可见
        if (currentId == null) {
            vo.setContactPhone(null);
        }
        // 最近捐助记录(匿名显示"爱心人士")
        List<Donation> donations = donationMapper.selectList(new LambdaQueryWrapper<Donation>()
                .eq(Donation::getHelpRequestId, id)
                .orderByDesc(Donation::getCreateTime)
                .last("LIMIT 10"));
        if (!donations.isEmpty()) {
            Map<Long, User> donors = userMapper.selectBatchIds(
                            donations.stream().map(Donation::getDonorId).collect(Collectors.toSet()))
                    .stream().collect(Collectors.toMap(User::getId, Function.identity()));
            vo.setRecentDonations(donations.stream()
                    .map(d -> DonationService.toVO(d, donors.get(d.getDonorId()), null))
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    public Long create(Long userId, HelpRequestDTO dto) {
        HelpRequest request = new HelpRequest();
        request.setUserId(userId);
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setTargetAmount(dto.getTargetAmount());
        request.setRaisedAmount(java.math.BigDecimal.ZERO);
        request.setDonateCount(0);
        request.setImages(joinImages(dto.getImages()));
        request.setContactPhone(dto.getContactPhone());
        request.setStatus(HelpStatus.PENDING);
        save(request);
        return request.getId();
    }

    @Override
    public void update(Long userId, Long id, HelpRequestDTO dto) {
        HelpRequest request = requireOwned(userId, id);
        if (request.getStatus() != HelpStatus.PENDING && request.getStatus() != HelpStatus.REJECTED) {
            throw new BusinessException("该求助已通过审核或已完成，不可编辑");
        }
        HelpRequest update = new HelpRequest();
        update.setId(id);
        update.setTitle(dto.getTitle());
        update.setDescription(dto.getDescription());
        update.setTargetAmount(dto.getTargetAmount());
        update.setImages(joinImages(dto.getImages()));
        update.setContactPhone(dto.getContactPhone());
        update.setStatus(HelpStatus.PENDING);
        update.setRejectReason(null);
        updateById(update);
    }

    @Override
    public void delete(Long userId, Long id) {
        HelpRequest request = requireOwned(userId, id);
        if (!request.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException("无权删除该求助");
        }
        if (request.getUserId().equals(userId)
                && request.getStatus() != HelpStatus.PENDING && request.getStatus() != HelpStatus.REJECTED) {
            throw new BusinessException("该求助已开始募捐，请联系管理员处理");
        }
        removeById(id);
    }

    @Override
    public PageResult<HelpRequestVO> workerPage(PageQuery query) {
        Page<HelpRequest> page = buildPage(query);
        // 待审核/募捐中优先，其次按时间倒序
        lambdaQuery()
                .orderByAsc(HelpRequest::getStatus)
                .orderByDesc(HelpRequest::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(r -> {
            HelpRequestVO vo = toVOWithUser(r);
            User u = userMapper.selectById(r.getUserId());
            if (u != null) vo.setUserPhone(u.getPhone()); // 工作者可直接获取手机号
            return vo;
        }));
    }

    @Override
    public PageResult<HelpRequestVO> myPage(Long userId, PageQuery query) {
        Page<HelpRequest> page = buildPage(query);
        lambdaQuery()
                .eq(HelpRequest::getUserId, userId)
                .orderByDesc(HelpRequest::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(this::toVOWithUser));
    }

    // ---------- 管理端 ----------

    @Override
    public PageResult<HelpRequestVO> adminPage(PageQuery query, String keyword, Integer status) {
        Page<HelpRequest> page = buildPage(query);
        lambdaQuery()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(HelpRequest::getTitle, keyword).or().like(HelpRequest::getDescription, keyword))
                .eq(status != null, HelpRequest::getStatus, status)
                .orderByDesc(HelpRequest::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(this::toVOWithUser));
    }

    @Override
    public void audit(Long id, AuditDTO dto) {
        HelpRequest request = getById(id);
        if (request == null) {
            throw new BusinessException("求助不存在");
        }
        if (dto.getStatus() != HelpStatus.RAISING && dto.getStatus() != HelpStatus.REJECTED) {
            throw new BusinessException("审核状态不合法");
        }
        if (dto.getStatus() == HelpStatus.REJECTED
                && (dto.getRejectReason() == null || dto.getRejectReason().isBlank())) {
            throw new BusinessException("驳回必须填写原因");
        }
        HelpRequest update = new HelpRequest();
        update.setId(id);
        update.setStatus(dto.getStatus());
        update.setRejectReason(dto.getStatus() == HelpStatus.REJECTED ? dto.getRejectReason() : null);
        updateById(update);
        messageService.send(request.getUserId(), MessageType.AUDIT, "求助审核结果",
                dto.getStatus() == HelpStatus.RAISING
                        ? "您的求助《" + request.getTitle() + "》已审核通过，开始募捐"
                        : "您的求助《" + request.getTitle() + "》被驳回，原因：" + dto.getRejectReason(),
                id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("求助不存在");
        }
        // 级联删除捐助记录
        donationMapper.delete(new LambdaQueryWrapper<Donation>().eq(Donation::getHelpRequestId, id));
        removeById(id);
    }

    // ---------- 私有方法 ----------

    private HelpRequestVO toVOWithUser(HelpRequest r) {
        return HelpRequestService.toVO(r, userMapper.selectById(r.getUserId()));
    }

    private HelpRequest requireOwned(Long userId, Long id) {
        HelpRequest request = getById(id);
        if (request == null) {
            throw new BusinessException("求助不存在");
        }
        return request;
    }

    private boolean isAdmin() {
        return UserContext.get() != null && UserContext.get().isAdmin();
    }

    private Page<HelpRequest> buildPage(PageQuery query) {
        int num = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int size = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100);
        return new Page<>(num, size);
    }

    private String joinImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream().filter(i -> i != null && !i.isBlank()).collect(Collectors.joining(","));
    }
}
