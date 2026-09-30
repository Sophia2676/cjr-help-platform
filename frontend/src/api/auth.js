import request from './request'

export const register = (data) => request.post('/auth/register', data)
export const login = (data) => request.post('/auth/login', data)
export const getInfo = () => request.get('/auth/info')
export const logout = () => request.post('/auth/logout')

/** 发送短信验证码（演示模式：验证码直接返回） */
export const sendSmsCode = (phone) => request.post('/auth/sms-code', { phone })

/** 手机号登录（密码或验证码） */
export const phoneLogin = (data) => request.post('/auth/phone-login', data)

/** 微信扫码登录（开放平台回调 code） */
export const wechatLogin = (code) => request.post('/auth/wechat-login', { code })
