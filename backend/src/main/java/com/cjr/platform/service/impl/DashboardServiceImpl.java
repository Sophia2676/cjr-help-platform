package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjr.platform.common.constants.HelpStatus;
import com.cjr.platform.common.constants.PostStatus;
import com.cjr.platform.entity.*;
import com.cjr.platform.mapper.*;
import com.cjr.platform.service.DashboardService;
import com.cjr.platform.vo.DashboardStatsVO;
import com.cjr.platform.vo.NameValueVO;
import com.cjr.platform.vo.PendingItemVO;
import com.cjr.platform.vo.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final ExperienceMapper experienceMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final DonationMapper donationMapper;
    private final StatsMapper statsMapper;

    @Override
    public DashboardStatsVO stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setPostCount(postMapper.selectCount(new QueryWrapper<Post>().eq("status", PostStatus.APPROVED)));
        vo.setExperienceCount(experienceMapper.selectCount(new QueryWrapper<Experience>().eq("status", PostStatus.APPROVED)));
        vo.setHelpCount(helpRequestMapper.selectCount(new QueryWrapper<HelpRequest>().eq("status", HelpStatus.RAISING)));
        vo.setDonationTotal(sumDonation(new QueryWrapper<Donation>()));
        vo.setDonateCount(donationMapper.selectCount(null));
        vo.setPendingPostCount(postMapper.selectCount(new QueryWrapper<Post>().eq("status", PostStatus.PENDING)));
        vo.setPendingExperienceCount(experienceMapper.selectCount(new QueryWrapper<Experience>().eq("status", PostStatus.PENDING)));
        vo.setPendingHelpCount(helpRequestMapper.selectCount(new QueryWrapper<HelpRequest>().eq("status", HelpStatus.PENDING)));
        vo.setTodayNewUser(userMapper.selectCount(new QueryWrapper<User>().ge("create_time", todayStart)));
        vo.setTodayNewPost(postMapper.selectCount(new QueryWrapper<Post>()
                .eq("status", PostStatus.APPROVED).ge("create_time", todayStart)));
        vo.setTodayDonationAmount(sumDonation(new QueryWrapper<Donation>().ge("create_time", todayStart)));
        return vo;
    }

    @Override
    public TrendVO trend(int days) {
        int n = Math.max(1, Math.min(days, 60));
        LocalDate start = LocalDate.now().minusDays(n - 1L);
        List<String> dates = new ArrayList<>();
        Map<String, Long> userMap = toMap(statsMapper.dailyNewUsers(start));
        Map<String, Long> postMap = toMap(statsMapper.dailyNewPosts(start));
        Map<String, BigDecimal> amountMap = toAmountMap(statsMapper.dailyDonationAmount(start));
        List<Long> userCounts = new ArrayList<>();
        List<Long> postCounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String d = start.plusDays(i).format(DATE_FMT);
            dates.add(d);
            userCounts.add(userMap.getOrDefault(d, 0L));
            postCounts.add(postMap.getOrDefault(d, 0L));
            amounts.add(amountMap.getOrDefault(d, BigDecimal.ZERO));
        }
        TrendVO vo = new TrendVO();
        vo.setDates(dates);
        vo.setUserCounts(userCounts);
        vo.setPostCounts(postCounts);
        vo.setDonationAmounts(amounts);
        return vo;
    }

    @Override
    public List<NameValueVO> category() {
        return statsMapper.postCategoryStats().stream()
                .map(m -> new NameValueVO(String.valueOf(m.get("name")), ((Number) m.get("value")).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingItemVO> latest(int limit) {
        int n = Math.max(1, Math.min(limit, 20));
        List<Object[]> raw = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        for (Post p : postMapper.selectList(new QueryWrapper<Post>().eq("status", PostStatus.PENDING)
                .orderByDesc("create_time").last("LIMIT " + n))) {
            raw.add(new Object[]{toItem("POST", p.getId(), p.getTitle(), p.getCreateTime()), p.getUserId()});
            userIds.add(p.getUserId());
        }
        for (Experience e : experienceMapper.selectList(new QueryWrapper<Experience>().eq("status", PostStatus.PENDING)
                .orderByDesc("create_time").last("LIMIT " + n))) {
            raw.add(new Object[]{toItem("EXPERIENCE", e.getId(), e.getTitle(), e.getCreateTime()), e.getUserId()});
            userIds.add(e.getUserId());
        }
        for (HelpRequest h : helpRequestMapper.selectList(new QueryWrapper<HelpRequest>().eq("status", HelpStatus.PENDING)
                .orderByDesc("create_time").last("LIMIT " + n))) {
            raw.add(new Object[]{toItem("HELP", h.getId(), h.getTitle(), h.getCreateTime()), h.getUserId()});
            userIds.add(h.getUserId());
        }
        Map<Long, User> users = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        List<PendingItemVO> items = raw.stream().map(arr -> {
            PendingItemVO vo = (PendingItemVO) arr[0];
            Long uid = (Long) arr[1];
            User u = users.get(uid);
            vo.setNickname(u != null ? u.getNickname() : "该用户已注销");
            return vo;
        }).collect(Collectors.toList());
        items.sort(Comparator.comparing(PendingItemVO::getCreateTime).reversed());
        return items.size() > n ? items.subList(0, n) : items;
    }

    private PendingItemVO toItem(String type, Long id, String title, LocalDateTime createTime) {
        PendingItemVO vo = new PendingItemVO();
        vo.setType(type);
        vo.setId(id);
        vo.setTitle(title);
        vo.setCreateTime(createTime);
        return vo;
    }

    private Map<String, Long> toMap(List<Map<String, Object>> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("date")), ((Number) row.get("cnt")).longValue());
        }
        return map;
    }

    private Map<String, BigDecimal> toAmountMap(List<Map<String, Object>> rows) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("date")), new BigDecimal(String.valueOf(row.get("amount"))));
        }
        return map;
    }

    private BigDecimal sumDonation(QueryWrapper<Donation> wrapper) {
        List<Map<String, Object>> rows = donationMapper.selectMaps(
                wrapper.select("COALESCE(SUM(amount), 0) AS total"));
        if (rows.isEmpty() || rows.get(0).get("total") == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(rows.get(0).get("total")));
    }
}
