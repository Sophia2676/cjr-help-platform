package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.LoginDTO;
import com.cjr.platform.dto.PhoneLoginDTO;
import com.cjr.platform.dto.WechatLoginDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.RegisterDTO;
import com.cjr.platform.dto.UpdatePasswordDTO;
import com.cjr.platform.dto.UpdateProfileDTO;
import com.cjr.platform.entity.User;
import com.cjr.platform.vo.AuthorVO;
import com.cjr.platform.vo.LoginVO;
import com.cjr.platform.vo.UserVO;

public interface UserService extends IService<User> {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    /** 手机号登录(密码或验证码) */
    LoginVO phoneLogin(PhoneLoginDTO dto);

    /** 微信扫码登录(openid)，首次登录自动注册 */
    LoginVO wechatLogin(String code);

    /** 发送短信验证码(演示模式，返回验证码) */
    String sendSmsCode(String phone);

    /** 当前登录人完整信息 */
    UserVO getCurrentInfo();

    /** 用户公开信息(不含手机/邮箱/角色) */
    UserVO getUserById(Long id);

    void updateProfile(Long userId, UpdateProfileDTO dto);

    void updatePassword(Long userId, UpdatePasswordDTO dto);

    // ---------- 管理端 ----------
    PageResult<UserVO> adminPage(PageQuery query, String role, Integer status);

    void setStatus(Long id, Integer status);

    void setRole(Long id, String role);

    void deleteUser(Long id);

    void resetPassword(Long id, String password);

    /** 实体转作者简要信息(用户已注销时返回占位昵称) */
    static AuthorVO toAuthorVO(User u) {
        AuthorVO vo = new AuthorVO();
        if (u == null) {
            vo.setNickname("该用户已注销");
            return vo;
        }
        vo.setId(u.getId());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setDisabilityType(u.getDisabilityType());
        return vo;
    }

    static UserVO toUserVO(User u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setRealName(u.getRealName());
        vo.setAvatar(u.getAvatar());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setGender(u.getGender());
        vo.setDisabilityType(u.getDisabilityType());
        vo.setDisabilityLevel(u.getDisabilityLevel());
        vo.setRole(u.getRole());
        vo.setBio(u.getBio());
        vo.setCreateTime(u.getCreateTime());
        return vo;
    }
}
