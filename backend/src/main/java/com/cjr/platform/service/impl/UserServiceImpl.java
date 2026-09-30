package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.constants.MessageType;
import com.cjr.platform.dto.*;
import com.cjr.platform.entity.User;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.security.JwtUtil;
import com.cjr.platform.security.LoginUser;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.LoginVO;
import com.cjr.platform.vo.UserVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    /** 演示用短信验证码存储 phone -> [code, 过期时间戳] */
    private final Map<String, String[]> smsCodes = new ConcurrentHashMap<>();

    @Value("${cjr.wechat.appid:}")
    private String wechatAppid;
    @Value("${cjr.wechat.secret:}")
    private String wechatSecret;

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long count = lambdaQuery().eq(User::getUsername, dto.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已被注册");
        }
        User user = new User();
        if (dto.getPhone() != null && !dto.getPhone().isBlank()
                && lambdaQuery().eq(User::getPhone, dto.getPhone()).count() > 0) {
            throw new BusinessException("该手机号已被绑定");
        }
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setGender(0);
        user.setRole("USER");
        user.setStatus(1);
        save(user);
        messageService.send(user.getId(), MessageType.SYSTEM, "欢迎加入互助大家庭",
                "欢迎您注册残疾人互助交流平台，在这里您可以发帖交流、分享康复养护经验、参与爱心互助。", null);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = lambdaQuery().eq(User::getUsername, dto.getUsername()).one();
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        LoginUser lu = LoginUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
        String token = jwtUtil.generateToken(lu);
        return LoginVO.builder().token(token).user(UserService.toUserVO(user)).build();
    }

    // ---------- 手机号/微信 登录 ----------

    @Override
    public String sendSmsCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        smsCodes.put(phone, new String[]{code, String.valueOf(System.currentTimeMillis() + 5 * 60 * 1000)});
        log.warn("【演示短信】手机号 {} 验证码：{}（5分钟有效，生产环境请接入短信服务商）", phone, code);
        return code;
    }

    private void verifySmsCode(String phone, String code) {
        String[] item = smsCodes.get(phone);
        if (item == null || Long.parseLong(item[1]) < System.currentTimeMillis() || !item[0].equals(code)) {
            throw new BusinessException("验证码错误或已过期");
        }
        smsCodes.remove(phone);
    }

    @Override
    public LoginVO phoneLogin(PhoneLoginDTO dto) {
        User user = lambdaQuery().eq(User::getPhone, dto.getPhone()).one();
        if (user == null) {
            throw new BusinessException("该手机号未注册，请先注册并绑定手机号");
        }
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            verifySmsCode(dto.getPhone(), dto.getCode());
        } else {
            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                throw new BusinessException("请输入密码或验证码");
            }
            if (user.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new BusinessException("手机号或密码错误");
            }
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        return buildLoginVO(user);
    }

    @Override
    public LoginVO wechatLogin(String code) {
        if (wechatAppid == null || wechatAppid.isBlank() || wechatSecret == null || wechatSecret.isBlank()) {
            throw new BusinessException("微信登录未配置：请在 application.yml 填写 cjr.wechat.appid/secret（需微信开放平台账号）");
        }
        String openid;
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + wechatAppid
                    + "&secret=" + wechatSecret + "&code=" + code + "&grant_type=authorization_code";
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(resp.body());
            if (node.has("errcode")) {
                throw new BusinessException("微信登录失败：" + node.path("errmsg").asText());
            }
            openid = node.path("openid").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("微信登录请求失败，请重试");
        }
        if (openid == null || openid.isBlank()) {
            throw new BusinessException("微信登录失败：未获取到openid");
        }
        User user = lambdaQuery().eq(User::getOpenid, openid).one();
        if (user == null) {
            user = new User();
            user.setUsername("wx_" + openid.substring(Math.max(0, openid.length() - 8)));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setNickname("微信用户" + openid.substring(Math.max(0, openid.length() - 4)));
            user.setGender(0);
            user.setRole("USER");
            user.setStatus(1);
            user.setOpenid(openid);
            save(user);
            messageService.send(user.getId(), MessageType.SYSTEM, "欢迎加入互助大家庭",
                    "欢迎您通过微信登录注册残疾人互助交流平台。", null);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(User user) {
        LoginUser lu = LoginUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
        String token = jwtUtil.generateToken(lu);
        return LoginVO.builder().token(token).user(UserService.toUserVO(user)).build();
    }

    @Override
    public UserVO getCurrentInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return UserService.toUserVO(getById(userId));
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserVO vo = UserService.toUserVO(user);
        // 公开信息隐藏敏感字段
        vo.setPhone(null);
        vo.setEmail(null);
        vo.setRole(null);
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setDisabilityType(dto.getDisabilityType());
        user.setDisabilityLevel(dto.getDisabilityLevel());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setBio(dto.getBio());
        updateById(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = getById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(update);
    }

    // ---------- 管理端 ----------

    @Override
    public PageResult<UserVO> adminPage(PageQuery query, String role, Integer status) {
        Page<User> page = buildPage(query);
        lambdaQuery()
                .and(query.getKeyword() != null && !query.getKeyword().isBlank(),
                        w -> w.like(User::getUsername, query.getKeyword()).or().like(User::getNickname, query.getKeyword()))
                .eq(role != null && !role.isBlank(), User::getRole, role)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(UserService::toUserVO));
    }

    @Override
    public void setStatus(Long id, Integer status) {
        User target = requireOperable(id);
        User update = new User();
        update.setId(id);
        update.setStatus(status == 1 ? 1 : 0);
        updateById(update);
    }

    @Override
    public void setRole(Long id, String role) {
        User target = requireOperable(id);
        if (!"USER".equals(role) && !"ADMIN".equals(role) && !"WORKER".equals(role)) {
            throw new BusinessException("角色不合法");
        }
        User update = new User();
        update.setId(id);
        update.setRole(role);
        updateById(update);
    }

    @Override
    public void deleteUser(Long id) {
        User target = requireOperable(id);
        // 先改写用户名释放唯一索引, 再逻辑删除
        User rename = new User();
        rename.setId(id);
        rename.setUsername(target.getUsername() + "_del_" + id);
        updateById(rename);
        removeById(id);
    }

    @Override
    public void resetPassword(Long id, String password) {
        requireOperable(id);
        User update = new User();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(password));
        updateById(update);
    }

    private User requireOperable(Long id) {
        User target = getById(id);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        if ("ADMIN".equals(target.getRole())) {
            throw new BusinessException("不能操作管理员账号");
        }
        Long currentId = UserContext.getUserId();
        if (id.equals(currentId)) {
            throw new BusinessException("不能操作自己的账号");
        }
        return target;
    }

    private <T> Page<T> buildPage(PageQuery query) {
        int num = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int size = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100);
        return new Page<>(num, size);
    }
}
