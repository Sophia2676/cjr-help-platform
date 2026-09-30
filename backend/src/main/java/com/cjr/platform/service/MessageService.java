package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Message;
import com.cjr.platform.vo.MessageVO;

public interface MessageService extends IService<Message> {

    PageResult<MessageVO> page(Long userId, PageQuery query, String type);

    long unreadCount(Long userId);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId, String type);

    void delete(Long userId, Long id);

    /** 统一消息发送入口 */
    void send(Long receiverId, String type, String title, String content, Long relatedId);
}
