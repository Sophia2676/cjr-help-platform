package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.common.constants.PostStatus;
import com.cjr.platform.dto.CommentDTO;
import com.cjr.platform.entity.Comment;
import com.cjr.platform.entity.Post;
import com.cjr.platform.entity.User;
import com.cjr.platform.mapper.CommentMapper;
import com.cjr.platform.mapper.PostMapper;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.service.CommentService;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;

    @Override
    public List<CommentVO> listByPost(Long postId) {
        List<Comment> all = lambdaQuery()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime)
                .list();
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, User> users = userMapper.selectBatchIds(
                        all.stream().map(Comment::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<CommentVO> parents = new ArrayList<>();
        Map<Long, List<CommentVO>> childrenMap = new java.util.HashMap<>();
        for (Comment c : all) {
            CommentVO vo = toVO(c, users);
            if (c.getParentId() == null || c.getParentId() == 0L) {
                parents.add(vo);
            } else {
                childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(vo);
            }
        }
        for (CommentVO parent : parents) {
            parent.setReplies(childrenMap.getOrDefault(parent.getId(), new ArrayList<>()));
        }
        return parents;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CommentDTO dto) {
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null || post.getStatus() != PostStatus.APPROVED) {
            throw new BusinessException("帖子不存在或未通过审核");
        }
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        if (parentId != 0L) {
            Comment parent = getById(parentId);
            if (parent == null || parent.getStatus() != 1 || !parent.getPostId().equals(dto.getPostId())) {
                throw new BusinessException("被回复的评论不存在");
            }
        }
        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setContent(dto.getContent());
        comment.setStatus(1);
        save(comment);

        postMapper.update(null, new UpdateWrapper<Post>()
                .eq("id", dto.getPostId()).setSql("comment_count = comment_count + 1"));

        // 通知帖子作者(自己评论自己不发)
        if (!post.getUserId().equals(userId)) {
            messageService.send(post.getUserId(), MessageType.SYSTEM, "评论通知",
                    "您的帖子《" + post.getTitle() + "》收到了新评论", post.getId());
        }
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        Comment comment = getById(id);
        if (comment == null || comment.getStatus() != 1) {
            throw new BusinessException("评论不存在");
        }
        boolean isAdmin = UserContext.get() != null && UserContext.get().isAdmin();
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException("无权删除该评论");
        }
        Long childCount = lambdaQuery().eq(Comment::getParentId, id).eq(Comment::getStatus, 1).count();
        // 物理删除评论及其回复
        removeById(id);
        remove(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id));
        postMapper.update(null, new UpdateWrapper<Post>()
                .eq("id", comment.getPostId())
                .setSql("comment_count = GREATEST(comment_count - " + (childCount + 1) + ", 0)"));
    }

    private CommentVO toVO(Comment c, Map<Long, User> users) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setCreateTime(c.getCreateTime());
        vo.setUser(UserService.toAuthorVO(users.get(c.getUserId())));
        if (c.getReplyUserId() != null) {
            User replyUser = users.get(c.getReplyUserId());
            vo.setReplyTo(replyUser != null ? replyUser.getNickname() : "该用户已注销");
        }
        return vo;
    }
}
