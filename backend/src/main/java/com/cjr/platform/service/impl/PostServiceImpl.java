package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.common.constants.PostStatus;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.PostDTO;
import com.cjr.platform.entity.Post;
import com.cjr.platform.entity.PostLike;
import com.cjr.platform.entity.SearchLog;
import com.cjr.platform.entity.User;
import com.cjr.platform.mapper.CommentMapper;
import com.cjr.platform.mapper.PostLikeMapper;
import com.cjr.platform.mapper.PostMapper;
import com.cjr.platform.mapper.SearchLogMapper;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.service.PostService;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.PostDetailVO;
import com.cjr.platform.vo.PostVO;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostLikeMapper postLikeMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final SearchLogMapper searchLogMapper;
    private final MessageService messageService;

    @Override
    public PageResult<PostVO> page(PageQuery query, String category, String sort) {
        recordSearch(query);
        Page<Post> page = buildPage(query);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, PostStatus.APPROVED)
                .and(query.getKeyword() != null && !query.getKeyword().isBlank(),
                        w -> w.like(Post::getTitle, query.getKeyword()).or().like(Post::getContent, query.getKeyword()))
                .eq(category != null && !category.isBlank(), Post::getCategory, category)
                .orderByDesc(Post::getIsTop);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Post::getLikeCount);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        page(page, wrapper);
        return PageResult.of(page.convert(toVO()));
    }

    /** 记录用户搜索关键词次数(供智能推荐) */
    private void recordSearch(PageQuery query) {
        String kw = query.getKeyword();
        Long userId = UserContext.getUserId();
        if (kw == null || kw.isBlank() || userId == null) return;
        SearchLog log = searchLogMapper.selectOne(new LambdaQueryWrapper<SearchLog>()
                .eq(SearchLog::getUserId, userId)
                .eq(SearchLog::getKeyword, kw));
        if (log == null) {
            log = new SearchLog();
            log.setUserId(userId);
            log.setKeyword(kw);
            log.setCount(1);
            log.setLastTime(LocalDateTime.now());
            searchLogMapper.insert(log);
        } else {
            SearchLog upd = new SearchLog();
            upd.setId(log.getId());
            upd.setCount(log.getCount() + 1);
            upd.setLastTime(LocalDateTime.now());
            searchLogMapper.updateById(upd);
        }
    }

    /** 搜索次数超过该阈值的关键词参与推荐 */
    private static final int RECOMMEND_THRESHOLD = 20;

    @Override
    public PageResult<PostVO> recommendPage(PageQuery query) {
        Long userId = UserContext.getUserId();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        if (userId == null) {
            return page(query, null, "latest");
        }
        // 取该用户搜索次数 > 20 的关键词(按次数降序, 最多 3 个)
        List<SearchLog> logs = searchLogMapper.selectList(new LambdaQueryWrapper<SearchLog>()
                .eq(SearchLog::getUserId, userId)
                .gt(SearchLog::getCount, RECOMMEND_THRESHOLD)
                .orderByDesc(SearchLog::getCount)
                .last("LIMIT 3"));
        if (logs.isEmpty()) {
            return page(query, null, "latest");
        }
        // 每个关键词取匹配的已通过帖子(按热度)，合并去重
        List<Post> recs = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (SearchLog log : logs) {
            List<Post> list = lambdaQuery()
                    .eq(Post::getStatus, PostStatus.APPROVED)
                    .and(w -> w.like(Post::getTitle, log.getKeyword()).or().like(Post::getContent, log.getKeyword()))
                    .orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCreateTime)
                    .last("LIMIT 20")
                    .list();
            for (Post p : list) {
                if (seen.add(p.getId())) recs.add(p);
            }
        }
        // 不足一页时用最新帖子补齐
        if (recs.size() < pageSize) {
            int need = pageSize - recs.size();
            List<Post> fill = lambdaQuery()
                    .eq(Post::getStatus, PostStatus.APPROVED)
                    .notIn(!seen.isEmpty(), Post::getId, seen)
                    .orderByDesc(Post::getCreateTime)
                    .last("LIMIT " + need)
                    .list();
            recs.addAll(fill);
        }
        // 内存分页
        int from = Math.min((pageNum - 1) * pageSize, recs.size());
        int to = Math.min(from + pageSize, recs.size());
        List<PostVO> vos = recs.subList(from, to).stream().map(toVO()).toList();
        return new PageResult<>((long) recs.size(), vos);
    }

    @Override
    public PostDetailVO detail(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        Long currentId = UserContext.getUserId();
        boolean isAuthor = post.getUserId().equals(currentId);
        boolean isAdmin = isAdmin();
        if (post.getStatus() != PostStatus.APPROVED && !isAuthor && !isAdmin) {
            throw new BusinessException(404, "帖子不存在或未通过审核");
        }
        // 作者本人浏览不计浏览量
        if (!isAuthor) {
            update(new UpdateWrapper<Post>().eq("id", id).setSql("view_count = view_count + 1"));
        }
        PostDetailVO vo = new PostDetailVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setCategory(post.getCategory());
        vo.setImages(splitImages(post.getImages()));
        vo.setViewCount(post.getViewCount() + (isAuthor ? 0 : 1));
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setIsTop(post.getIsTop());
        vo.setCreateTime(post.getCreateTime());
        vo.setIsLiked(currentId != null
                && postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, id).eq(PostLike::getUserId, currentId)) > 0);
        vo.setAuthor(UserService.toAuthorVO(userMapper.selectById(post.getUserId())));
        return vo;
    }

    @Override
    public Long create(Long userId, PostDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategory(dto.getCategory());
        post.setImages(joinImages(dto.getImages()));
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(PostStatus.PENDING);
        post.setIsTop(0);
        save(post);
        return post.getId();
    }

    @Override
    public void update(Long userId, Long id, PostDTO dto) {
        Post post = requireOwned(userId, id);
        if (post.getStatus() != PostStatus.PENDING && post.getStatus() != PostStatus.REJECTED) {
            throw new BusinessException("帖子已审核通过，不可编辑");
        }
        Post update = new Post();
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!post.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException("无权删除该帖子");
        }
        // 级联物理删除评论与点赞
        commentMapper.delete(new LambdaQueryWrapper<com.cjr.platform.entity.Comment>()
                .eq(com.cjr.platform.entity.Comment::getPostId, id));
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, id));
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        Post post = getById(postId);
        if (post == null || post.getStatus() != PostStatus.APPROVED) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId).eq(PostLike::getUserId, userId));
        boolean liked;
        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            update(new UpdateWrapper<Post>().eq("id", postId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            try {
                postLikeMapper.insert(like);
                update(new UpdateWrapper<Post>().eq("id", postId).setSql("like_count = like_count + 1"));
            } catch (DuplicateKeyException e) {
                // 唯一索引兜底: 并发重复点赞按已点赞处理
            }
            liked = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", getById(postId).getLikeCount());
        return result;
    }

    @Override
    public PageResult<PostVO> myPage(Long userId, PageQuery query, Integer status) {
        Page<Post> page = buildPage(query);
        lambdaQuery()
                .eq(Post::getUserId, userId)
                .eq(status != null, Post::getStatus, status)
                .orderByDesc(Post::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    @Override
    public PageResult<PostVO> userPage(Long userId, PageQuery query) {
        Page<Post> page = buildPage(query);
        lambdaQuery()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, PostStatus.APPROVED)
                .orderByDesc(Post::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    // ---------- 管理端 ----------

    @Override
    public PageResult<PostVO> adminPage(PageQuery query, String keyword, Integer status, String category) {
        Page<Post> page = buildPage(query);
        lambdaQuery()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword))
                .eq(status != null, Post::getStatus, status)
                .eq(category != null && !category.isBlank(), Post::getCategory, category)
                .orderByDesc(Post::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(toVO()));
    }

    @Override
    public void audit(Long id, AuditDTO dto) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (dto.getStatus() != PostStatus.APPROVED && dto.getStatus() != PostStatus.REJECTED) {
            throw new BusinessException("审核状态不合法");
        }
        if (dto.getStatus() == PostStatus.REJECTED
                && (dto.getRejectReason() == null || dto.getRejectReason().isBlank())) {
            throw new BusinessException("驳回必须填写原因");
        }
        Post update = new Post();
        update.setId(id);
        update.setStatus(dto.getStatus());
        update.setRejectReason(dto.getStatus() == PostStatus.REJECTED ? dto.getRejectReason() : null);
        updateById(update);
        messageService.send(post.getUserId(), MessageType.AUDIT, "帖子审核结果",
                dto.getStatus() == PostStatus.APPROVED
                        ? "您的帖子《" + post.getTitle() + "》已审核通过"
                        : "您的帖子《" + post.getTitle() + "》被驳回，原因：" + dto.getRejectReason(),
                id);
    }

    @Override
    public void top(Long id, Integer isTop) {
        if (getById(id) == null) {
            throw new BusinessException("帖子不存在");
        }
        Post update = new Post();
        update.setId(id);
        update.setIsTop(isTop != null && isTop == 1 ? 1 : 0);
        updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("帖子不存在");
        }
        commentMapper.delete(new LambdaQueryWrapper<com.cjr.platform.entity.Comment>()
                .eq(com.cjr.platform.entity.Comment::getPostId, id));
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, id));
        removeById(id);
    }

    // ---------- 私有方法 ----------

    private Post requireOwned(Long userId, Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该帖子");
        }
        return post;
    }

    private boolean isAdmin() {
        return UserContext.get() != null && UserContext.get().isAdmin();
    }

    private Function<Post, PostVO> toVO() {
        return post -> {
            PostVO vo = new PostVO();
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            String content = post.getContent();
            vo.setSummary(content != null && content.length() > 120 ? content.substring(0, 120) : content);
            vo.setCategory(post.getCategory());
            vo.setCover(firstImage(post.getImages()));
            vo.setViewCount(post.getViewCount());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setIsTop(post.getIsTop());
            vo.setCreateTime(post.getCreateTime());
            vo.setStatus(post.getStatus());
            vo.setRejectReason(post.getRejectReason());
            vo.setAuthor(UserService.toAuthorVO(userMapper.selectById(post.getUserId())));
            return vo;
        };
    }

    private Page<Post> buildPage(PageQuery query) {
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
