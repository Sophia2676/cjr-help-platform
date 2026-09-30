package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.PostDTO;
import com.cjr.platform.entity.Post;
import com.cjr.platform.vo.PostDetailVO;
import com.cjr.platform.vo.PostVO;

import java.util.Map;

public interface PostService extends IService<Post> {

    /** 公开帖子分页(仅已通过, 置顶优先) */
    PageResult<PostVO> page(PageQuery query, String category, String sort);

    /** 智能推荐：按用户高频搜索词(>20次)推送相关帖子 */
    PageResult<PostVO> recommendPage(PageQuery query);

    PostDetailVO detail(Long id);

    Long create(Long userId, PostDTO dto);

    void update(Long userId, Long id, PostDTO dto);

    void delete(Long userId, Long id);

    /** 点赞切换, 返回 {liked, likeCount} */
    Map<String, Object> toggleLike(Long userId, Long postId);

    PageResult<PostVO> myPage(Long userId, PageQuery query, Integer status);

    /** 某用户已通过的帖子 */
    PageResult<PostVO> userPage(Long userId, PageQuery query);

    // ---------- 管理端 ----------
    PageResult<PostVO> adminPage(PageQuery query, String keyword, Integer status, String category);

    void audit(Long id, AuditDTO dto);

    void top(Long id, Integer isTop);

    void adminDelete(Long id);
}
