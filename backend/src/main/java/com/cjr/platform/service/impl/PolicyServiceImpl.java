package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.PolicyDTO;
import com.cjr.platform.entity.Policy;
import com.cjr.platform.mapper.PolicyMapper;
import com.cjr.platform.service.PolicyService;
import com.cjr.platform.vo.PolicyDetailVO;
import com.cjr.platform.vo.PolicyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Function;

@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy> implements PolicyService {

    @Override
    public PageResult<PolicyVO> page(PageQuery query) {
        Page<Policy> page = buildPage(query);
        lambdaQuery()
                .eq(Policy::getStatus, 1)
                .like(query.getKeyword() != null && !query.getKeyword().isBlank(), Policy::getTitle, query.getKeyword())
                .orderByDesc(Policy::getPublishTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    @Override
    public PolicyDetailVO detail(Long id) {
        Policy policy = getById(id);
        if (policy == null || policy.getStatus() != 1) {
            throw new BusinessException(404, "政策资讯不存在或已下架");
        }
        update(new UpdateWrapper<Policy>().eq("id", id).setSql("view_count = view_count + 1"));
        PolicyDetailVO vo = new PolicyDetailVO();
        vo.setId(policy.getId());
        vo.setTitle(policy.getTitle());
        vo.setContent(policy.getContent());
        vo.setCoverImage(policy.getCoverImage());
        vo.setSource(policy.getSource());
        vo.setPublishTime(policy.getPublishTime());
        vo.setViewCount(policy.getViewCount() + 1);
        return vo;
    }

    // ---------- 管理端 ----------

    @Override
    public PageResult<Policy> adminPage(PageQuery query, String keyword) {
        Page<Policy> page = buildPage(query);
        lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), Policy::getTitle, keyword)
                .orderByDesc(Policy::getCreateTime)
                .page(page);
        return PageResult.of(page);
    }

    @Override
    public Long create(Long adminId, PolicyDTO dto) {
        Policy policy = new Policy();
        policy.setTitle(dto.getTitle());
        policy.setSummary(dto.getSummary());
        policy.setContent(dto.getContent());
        policy.setCoverImage(dto.getCoverImage());
        policy.setSource(dto.getSource());
        policy.setPublishTime(LocalDateTime.now());
        policy.setViewCount(0);
        policy.setStatus(1);
        policy.setCreateBy(adminId);
        save(policy);
        return policy.getId();
    }

    @Override
    public void update(Long id, PolicyDTO dto) {
        if (getById(id) == null) {
            throw new BusinessException("政策资讯不存在");
        }
        Policy update = new Policy();
        update.setId(id);
        update.setTitle(dto.getTitle());
        update.setSummary(dto.getSummary());
        update.setContent(dto.getContent());
        update.setCoverImage(dto.getCoverImage());
        update.setSource(dto.getSource());
        updateById(update);
    }

    @Override
    public void status(Long id, Integer status) {
        if (getById(id) == null) {
            throw new BusinessException("政策资讯不存在");
        }
        Policy update = new Policy();
        update.setId(id);
        update.setStatus(status != null && status == 0 ? 0 : 1);
        updateById(update);
    }

    @Override
    public void adminDelete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("政策资讯不存在");
        }
        removeById(id);
    }

    // ---------- 私有方法 ----------

    private Function<Policy, PolicyVO> toVO() {
        return p -> {
            PolicyVO vo = new PolicyVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        };
    }

    private Page<Policy> buildPage(PageQuery query) {
        int num = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int size = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100);
        return new Page<>(num, size);
    }
}
