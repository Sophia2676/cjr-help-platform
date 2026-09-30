package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.common.constants.PostStatus;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.ExperienceDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Experience;
import com.cjr.platform.mapper.ExperienceMapper;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.service.ExperienceService;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.ExperienceDetailVO;
import com.cjr.platform.vo.ExperienceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl extends ServiceImpl<ExperienceMapper, Experience> implements ExperienceService {

    private final UserMapper userMapper;
    private final MessageService messageService;

    @Override
    public PageResult<ExperienceVO> page(PageQuery query, String category) {
        Page<Experience> page = buildPage(query);
        lambdaQuery()
                .eq(Experience::getStatus, PostStatus.APPROVED)
                .and(query.getKeyword() != null && !query.getKeyword().isBlank(),
                        w -> w.like(Experience::getTitle, query.getKeyword()).or().like(Experience::getContent, query.getKeyword()))
                .eq(category != null && !category.isBlank(), Experience::getCategory, category)
                .orderByDesc(Experience::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    @Override
    public ExperienceDetailVO detail(Long id) {
        Experience exp = getById(id);
        if (exp == null) {
            throw new BusinessException(404, "经验不存在");
        }
        Long currentId = UserContext.getUserId();
        boolean isAuthor = exp.getUserId().equals(currentId);
        boolean isAdmin = isAdmin();
        if (exp.getStatus() != PostStatus.APPROVED && !isAuthor && !isAdmin) {
            throw new BusinessException(404, "经验不存在或未通过审核");
        }
        if (!isAuthor) {
            update(new UpdateWrapper<Experience>().eq("id", id).setSql("view_count = view_count + 1"));
        }
        ExperienceDetailVO vo = new ExperienceDetailVO();
        vo.setId(exp.getId());
        vo.setTitle(exp.getTitle());
        vo.setContent(exp.getContent());
        vo.setCategory(exp.getCategory());
        vo.setImages(splitImages(exp.getImages()));
        vo.setViewCount(exp.getViewCount() + (isAuthor ? 0 : 1));
        vo.setCreateTime(exp.getCreateTime());
        vo.setAuthor(UserService.toAuthorVO(userMapper.selectById(exp.getUserId())));
        return vo;
    }

    @Override
    public Long create(Long userId, ExperienceDTO dto) {
        Experience exp = new Experience();
        exp.setUserId(userId);
        exp.setTitle(dto.getTitle());
        exp.setContent(dto.getContent());
        exp.setCategory(dto.getCategory());
        exp.setImages(joinImages(dto.getImages()));
        exp.setViewCount(0);
        exp.setStatus(PostStatus.PENDING);
        save(exp);
        return exp.getId();
    }

    @Override
    public void update(Long userId, Long id, ExperienceDTO dto) {
        Experience exp = requireOwned(userId, id);
        if (exp.getStatus() != PostStatus.PENDING && exp.getStatus() != PostStatus.REJECTED) {
            throw new BusinessException("经验已审核通过，不可编辑");
        }
        Experience update = new Experience();
        update.setId(id);
        update.setTitle(dto.getTitle());
        update.setContent(dto.getContent());
        update.setCategory(dto.getCategory());
        update.setImages(joinImages(dto.getImages()));
        update.setStatus(PostStatus.PENDING);
        update.setRejectReason(null);
        updateById(update);
    }

    @Override
    public void delete(Long userId, Long id) {
        Experience exp = requireOwned(userId, id);
        removeById(exp.getId());
    }

    @Override
    public PageResult<ExperienceVO> myPage(Long userId, PageQuery query, Integer status) {
        Page<Experience> page = buildPage(query);
        lambdaQuery()
                .eq(Experience::getUserId, userId)
                .eq(status != null, Experience::getStatus, status)
                .orderByDesc(Experience::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    // ---------- 管理端 ----------

    @Override
    public PageResult<ExperienceVO> adminPage(PageQuery query, String keyword, Integer status) {
        Page<Experience> page = buildPage(query);
        lambdaQuery()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(Experience::getTitle, keyword).or().like(Experience::getContent, keyword))
                .eq(status != null, Experience::getStatus, status)
                .orderByDesc(Experience::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    @Override
    public void audit(Long id, AuditDTO dto) {
        Experience exp = getById(id);
        if (exp == null) {
            throw new BusinessException("经验不存在");
        }
        if (dto.getStatus() != PostStatus.APPROVED && dto.getStatus() != PostStatus.REJECTED) {
            throw new BusinessException("审核状态不合法");
        }
        if (dto.getStatus() == PostStatus.REJECTED
                && (dto.getRejectReason() == null || dto.getRejectReason().isBlank())) {
            throw new BusinessException("驳回必须填写原因");
        }
        Experience update = new Experience();
        update.setId(id);
        update.setStatus(dto.getStatus());
        update.setRejectReason(dto.getStatus() == PostStatus.REJECTED ? dto.getRejectReason() : null);
        updateById(update);
        messageService.send(exp.getUserId(), MessageType.AUDIT, "经验审核结果",
                dto.getStatus() == PostStatus.APPROVED
                        ? "您的经验分享《" + exp.getTitle() + "》已审核通过"
                        : "您的经验分享《" + exp.getTitle() + "》被驳回，原因：" + dto.getRejectReason(),
                exp.getId());
    }

    @Override
    public void adminDelete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("经验不存在");
        }
        removeById(id);
    }

    // ---------- 私有方法 ----------

    private Experience requireOwned(Long userId, Long id) {
        Experience exp = getById(id);
        if (exp == null) {
            throw new BusinessException("经验不存在");
        }
        if (!exp.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException("无权操作该经验");
        }
        return exp;
    }

    private boolean isAdmin() {
        return UserContext.get() != null && UserContext.get().isAdmin();
    }

    private Function<Experience, ExperienceVO> toVO() {
        return exp -> {
            ExperienceVO vo = new ExperienceVO();
            vo.setId(exp.getId());
            vo.setTitle(exp.getTitle());
            String content = exp.getContent();
            vo.setSummary(content != null && content.length() > 120 ? content.substring(0, 120) : content);
            vo.setCategory(exp.getCategory());
            vo.setCover(firstImage(exp.getImages()));
            vo.setViewCount(exp.getViewCount());
            vo.setCreateTime(exp.getCreateTime());
            vo.setStatus(exp.getStatus());
            vo.setRejectReason(exp.getRejectReason());
            vo.setAuthor(UserService.toAuthorVO(userMapper.selectById(exp.getUserId())));
            return vo;
        };
    }

    private Page<Experience> buildPage(PageQuery query) {
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

    private List<String> splitImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(images.split(",")).filter(s -> !s.isBlank()).collect(Collectors.toList());
    }

    private String firstImage(String images) {
        List<String> list = splitImages(images);
        return list.isEmpty() ? null : list.get(0);
    }
}
