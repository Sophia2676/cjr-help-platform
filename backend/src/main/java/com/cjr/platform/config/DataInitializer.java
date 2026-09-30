package com.cjr.platform.config;

import com.cjr.platform.common.constants.HelpStatus;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.common.constants.PostStatus;
import com.cjr.platform.entity.*;
import com.cjr.platform.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 种子数据初始化(仅当用户表为空时执行, 幂等)
 * 演示账号: admin/123456(管理员), zhang/li/wang/123456(普通用户)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;
    private final ExperienceMapper experienceMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final DonationMapper donationMapper;
    private final PolicyMapper policyMapper;
    private final MessageMapper messageMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            log.info("数据库已有数据, 跳过种子数据初始化");
            return;
        }
        log.info("开始初始化种子数据...");

        // 1. 用户
        User admin = user("admin", "平台管理员", "ADMIN");
        User zhang = user("zhang", "张阿姨", "USER");
        zhang.setPhone("13912340001");
        zhang.setDisabilityType("肢体残疾");
        zhang.setDisabilityLevel(3);
        zhang.setBio("轮椅出行多年，希望和大家交流出行经验。");
        User li = user("li", "小李", "USER");
        li.setPhone("13912340002");
        li.setDisabilityType("听力残疾");
        li.setDisabilityLevel(2);
        li.setBio("听障青年，求职中，欢迎交流。");
        User wang = user("wang", "王先生", "USER");
        wang.setPhone("13912340003");
        wang.setBio("家属身份，照顾孩子康复训练五年，分享一些护理心得。");
        User worker = user("worker", "王社工", "WORKER");
        worker.setPhone("13800000001");
        worker.setBio("社区工作者，负责本辖区残疾人帮扶对接。");
        userMapper.insert(admin);
        userMapper.insert(zhang);
        userMapper.insert(li);
        userMapper.insert(wang);
        userMapper.insert(worker);
        for (User u : new User[]{admin, zhang, li, wang}) {
            messageMapper.insert(msg(u.getId(), MessageType.SYSTEM, "欢迎加入互助大家庭",
                    "欢迎您注册残疾人互助交流平台，在这里您可以发帖交流、分享康复养护经验、参与爱心互助。", null));
        }

        // 2. 政策资讯
        policyMapper.insert(policy("困难残疾人生活补贴申请指南",
                "符合条件的一级、二级残疾人可按规定申请生活补贴，本文介绍申请条件、材料与办理流程。",
                "一、申请条件：持有《中华人民共和国残疾人证》，家庭人均收入低于当地低保标准的残疾人。\n"
                        + "二、申请材料：残疾人证、户口本、身份证、低保证明等。\n"
                        + "三、办理流程：向户籍所在地村(社区)提出申请，街道(乡镇)初审，县级残联审核发放。\n"
                        + "四、补贴标准：以当地政策为准，按月发放。"));
        policyMapper.insert(policy("重度残疾人护理补贴标准",
                "重度残疾人护理补贴主要补助因残疾产生的额外长期照护支出。",
                "补贴对象为残疾等级评定为一、二级的持证残疾人。\n补贴标准由各地根据经济社会发展水平确定，"
                        + "一般按月发放，可向当地残联咨询具体标准与发放方式。"));
        policyMapper.insert(policy("残疾人精准康复服务政策",
                "为有康复需求的残疾儿童和持证残疾人提供基本康复服务，实现精准康复。",
                "服务内容：辅助器具适配、康复训练、心理支持、家庭医生签约等服务。\n"
                        + "申请方式：持残疾人证向当地残联提出申请，由康复机构评估后提供服务。"));
        policyMapper.insert(policy("残疾人就业创业扶持办法",
                "鼓励用人单位吸纳残疾人就业，支持残疾人自主创业。",
                "一、按比例安排残疾人就业制度。\n二、残疾人自主创业可享受创业补贴、税费减免、贷款贴息等扶持。\n"
                        + "三、残联提供职业技能培训与就业推荐服务。"));
        policyMapper.insert(policy("困难重度残疾人家庭无障碍改造",
                "对困难重度残疾人家庭实施无障碍改造，改善居家生活环境。",
                "改造内容：地面防滑、低位灶台、扶手安装、卫生间改造等。\n"
                        + "申请方式：向户籍所在地残联提出申请，审核通过后由专业机构实施改造。"));
        policyMapper.insert(policy("残疾证办理与换领须知",
                "《中华人民共和国残疾人证》办理、补办与到期换领流程说明。",
                "首次办理：本人持身份证、户口本及医疗证明到指定医院评定，向县级残联申请办理。\n"
                        + "到期换领：残疾人证有效期十年，到期前及时换领，以免影响补贴领取。"));

        // 3. 帖子(含1条置顶、1条待审核)
        Post p1 = post(zhang.getId(), "轮椅出行的无障碍攻略：地铁公交经验分享", "出行交流",
                "坐了五年轮椅，总结一些实用经验：\n1. 地铁出行建议走无障碍电梯出入口，提前一站查看电梯位置；"
                        + "\n2. 公交建议等低地板车辆，上车后停在轮椅专区；\n3. 出门前用地图软件查看无障碍设施评价。"
                        + "\n希望帮到刚使用轮椅的朋友，也欢迎大家补充！", PostStatus.APPROVED, 1, LocalDateTime.now().minusDays(1));
        p1.setLikeCount(3);
        p1.setCommentCount(2);
        p1.setViewCount(128);
        Post p2 = post(wang.getId(), "孩子肢体康复训练的三年心得：贵在坚持", "心理互助",
                "孩子五岁开始系统康复训练，前半年几乎看不到进步，我们一度想放弃。\n后来坚持每天一小时的家庭训练配合机构康复，"
                        + "一年后孩子能独立站立，两年后借助助行器行走。\n想对家长朋友们说：康复是马拉松，请给自己和孩子多一点耐心。",
                PostStatus.APPROVED, 0, LocalDateTime.now().minusDays(3));
        p2.setLikeCount(5);
        p2.setCommentCount(1);
        p2.setViewCount(96);
        Post p3 = post(li.getId(), "听障求职经验：简历与面试沟通技巧", "求职就业",
                "作为听障者求职两年，分享几点心得：\n1. 简历中如实说明听力情况，同时突出技能与成果；\n2. 面试可申请文字沟通或手语翻译；"
                        + "\n3. 优先考虑残保金达标、有包容文化的企业。\n目前已入职一家科技公司做测试工程师，大家加油！",
                PostStatus.APPROVED, 0, LocalDateTime.now().minusDays(5));
        p3.setLikeCount(8);
        p3.setCommentCount(3);
        p3.setViewCount(210);
        Post p4 = post(zhang.getId(), "求助：哪里可以修理轮椅？", "生活求助",
                "我的电动轮椅左轮有异响，问了几家维修点都不修轮椅，坐标城南，请问大家有没有靠谱的维修点推荐？谢谢！",
                PostStatus.APPROVED, 0, LocalDateTime.now().minusDays(2));
        p4.setLikeCount(1);
        p4.setCommentCount(4);
        p4.setViewCount(67);
        Post p5 = post(wang.getId(), "新手指南：家庭无障碍改造从这几点入手", "其他",
                "分享我家的无障碍改造清单：卫生间加装扶手和防滑垫、门槛改坡道、门宽扩大方便轮椅进出、灯光改感应。"
                        + "费用不高但生活便利度提升巨大，大家可以参考。", PostStatus.APPROVED, 0, LocalDateTime.now().minusDays(6));
        p5.setLikeCount(2);
        p5.setViewCount(45);
        Post p6 = post(li.getId(), "申请残疾人就业补贴需要哪些材料？(待审核演示)", "求职就业",
                "想问一下大家，申请残疾人就业创业补贴具体需要准备哪些材料？流程大概多久？", PostStatus.PENDING, 0, LocalDateTime.now());
        postMapper.insert(p1);
        postMapper.insert(p2);
        postMapper.insert(p3);
        postMapper.insert(p4);
        postMapper.insert(p5);
        postMapper.insert(p6);

        // 4. 评论与点赞
        commentMapper.insert(comment(p1.getId(), li.getId(), "收藏了！地铁的无障碍电梯位置真的很重要，感谢分享。", 0L, null));
        commentMapper.insert(comment(p1.getId(), wang.getId(), "补充一点：可以拨打地铁服务热线预约爱心接力服务。", 0L, null));
        commentMapper.insert(comment(p2.getId(), zhang.getId(), "家长的心态真的很重要，向您学习！", 0L, null));
        commentMapper.insert(comment(p3.getId(), zhang.getId(), "祝贺入职！你的经验很有参考价值。", 0L, null));
        commentMapper.insert(comment(p3.getId(), wang.getId(), "请问残保金达标是什么意思？", 0L, null));
        commentMapper.insert(comment(p4.getId(), wang.getId(), "城南XX康复中心有轮椅维修服务，可以电话问问。", 0L, null));
        for (Post p : new Post[]{p1, p2, p3, p4}) {
            for (User u : new User[]{zhang, li, wang}) {
                if (!p.getUserId().equals(u.getId())) {
                    postLikeMapper.insert(postLike(p.getId(), u.getId()));
                }
            }
        }

        // 5. 康复经验(含1条待审核)
        Experience e1 = experience(wang.getId(), "脑瘫儿童家庭康复训练全记录", "康复训练",
                "从坐姿训练到站姿训练，我们分三个阶段：\n第一阶段(0-6个月)：以按摩和被动训练为主，每天两次；"
                        + "\n第二阶段(6-18个月)：加入坐位平衡、爬行训练；\n第三阶段(18个月后)：站立、行走辅助训练。"
                        + "\n每个阶段都拍照记录，定期与康复师复盘调整方案。", PostStatus.APPROVED, LocalDateTime.now().minusDays(4));
        e1.setViewCount(87);
        Experience e2 = experience(zhang.getId(), "轮椅使用者压疮预防的日常护理", "日常护理",
                "长期坐轮椅最怕压疮，我的护理习惯：\n1. 每30分钟撑起减压一次；\n2. 使用防压疮坐垫；"
                        + "\n3. 每天睡前检查皮肤；\n4. 保持皮肤清洁干燥。\n这些小习惯帮我避免了多次风险。",
                PostStatus.APPROVED, LocalDateTime.now().minusDays(7));
        e2.setViewCount(156);
        Experience e3 = experience(li.getId(), "听障人士的辅助器具使用心得(待审核演示)", "辅助器具",
                "分享助听器与手机字幕转写软件搭配使用的体验……", PostStatus.PENDING, LocalDateTime.now());
        experienceMapper.insert(e1);
        experienceMapper.insert(e2);
        experienceMapper.insert(e3);

        // 6. 求助与捐助
        HelpRequest help = new HelpRequest();
        help.setUserId(zhang.getId());
        help.setTitle("电动轮椅电池更换求助");
        help.setDescription("我的电动轮椅使用四年，电池续航严重下降，出行经常半路没电。\n咨询后更换电池需要1800元，"
                + "本人收入有限，希望能得到大家的帮助，万分感谢！");
        help.setTargetAmount(new BigDecimal("1800.00"));
        help.setRaisedAmount(new BigDecimal("800.00"));
        help.setDonateCount(2);
        help.setContactPhone("13912340001");
        help.setStatus(HelpStatus.RAISING);
        help.setCreateTime(LocalDateTime.now().minusDays(3));
        helpRequestMapper.insert(help);

        Donation d1 = donation(help.getId(), wang.getId(), new BigDecimal("500.00"), "加油！希望早日换上新电池", 0, 1,
                LocalDateTime.now().minusDays(2));
        Donation d2 = donation(help.getId(), li.getId(), new BigDecimal("300.00"), "一点心意", 1, 1,
                LocalDateTime.now().minusDays(1));
        donationMapper.insert(d1);
        donationMapper.insert(d2);

        log.info("种子数据初始化完成: 4个用户/6条帖子/5条经验/6条政策/1条求助/2条捐助");
    }

    // ---------- 构造辅助 ----------

    private User user(String username, String nickname, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode("123456"));
        u.setNickname(nickname);
        u.setGender(0);
        u.setRole(role);
        u.setStatus(1);
        return u;
    }

    private Policy policy(String title, String summary, String content) {
        Policy p = new Policy();
        p.setTitle(title);
        p.setSummary(summary);
        p.setContent(content);
        p.setSource("市残疾人联合会");
        p.setPublishTime(LocalDateTime.now().minusDays(1));
        p.setViewCount(0);
        p.setStatus(1);
        return p;
    }

    private Post post(Long userId, String title, String category, String content, int status, int isTop, LocalDateTime time) {
        Post p = new Post();
        p.setUserId(userId);
        p.setTitle(title);
        p.setCategory(category);
        p.setContent(content);
        p.setViewCount(0);
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setStatus(status);
        p.setIsTop(isTop);
        p.setCreateTime(time);
        return p;
    }

    private Comment comment(Long postId, Long userId, String content, Long parentId, Long replyUserId) {
        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setContent(content);
        c.setParentId(parentId);
        c.setReplyUserId(replyUserId);
        c.setStatus(1);
        return c;
    }

    private PostLike postLike(Long postId, Long userId) {
        PostLike l = new PostLike();
        l.setPostId(postId);
        l.setUserId(userId);
        return l;
    }

    private Experience experience(Long userId, String title, String category, String content, int status, LocalDateTime time) {
        Experience e = new Experience();
        e.setUserId(userId);
        e.setTitle(title);
        e.setCategory(category);
        e.setContent(content);
        e.setViewCount(0);
        e.setStatus(status);
        e.setCreateTime(time);
        return e;
    }

    private Donation donation(Long requestId, Long donorId, BigDecimal amount, String message, int isAnonymous, int status, LocalDateTime time) {
        Donation d = new Donation();
        d.setHelpRequestId(requestId);
        d.setDonorId(donorId);
        d.setAmount(amount);
        d.setMessage(message);
        d.setIsAnonymous(isAnonymous);
        d.setStatus(status);
        d.setCreateTime(time);
        return d;
    }

    private Message msg(Long receiverId, String type, String title, String content, Long relatedId) {
        Message m = new Message();
        m.setReceiverId(receiverId);
        m.setType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setRelatedId(relatedId);
        m.setIsRead(0);
        return m;
    }
}
