# 残疾人互助交流平台

基于 **SpringBoot + Vue 前后端分离**架构的残疾人互助交流平台，为残疾人朋友及家属提供互助发帖交流、康复养护经验分享、一对一爱心捐助、残联政策资讯浏览等服务，并配套后台内容审核管理。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.12 · MyBatis-Plus 3.5.12 · MySQL 8 · JWT(jjwt 0.12) · Spring AOP · BCrypt |
| 前端 | Vue 3 · Vite · Element Plus · Pinia · Vue Router · Axios · ECharts |

## 功能模块

- **用户认证**：注册 / 登录（JWT 无状态认证）/ 个人资料 / 修改密码
- **互助交流**：发帖（分类、图片、待审核）、帖子浏览 / 搜索 / 置顶 / 点赞 / 评论回复
- **康复经验**：家属康复养护经验分享（分类、审核）
- **爱心捐助**：发布求助（目标金额、进度展示）→ 爱心人士一对一捐助（可匿名）→ 受助人确认 → 筹满自动完成 / 手动完成，全流程站内消息通知
- **政策资讯**：管理员发布残联政策，用户浏览
- **站内消息**：审核结果、捐助动态、系统通知，未读提醒
- **后台管理**：数据概览（ECharts 图表）、帖子 / 经验 / 求助审核、捐助管理、用户管理、政策管理、操作日志（AOP 自动记录，密码字段脱敏）
- **系统能力**：统一响应与全局异常处理、AOP 操作日志、接口权限控制（@RequireLogin / @RequireAdmin）、图片上传、响应式适配电脑与手机

## 目录结构

```
cjr-help-platform/
├── backend/                        # Spring Boot 后端（端口 8080）
│   ├── src/main/java/com/cjr/platform/
│   │   ├── common/                 # 统一响应/异常/常量
│   │   ├── config/                 # Web/MyBatis-Plus/时间填充/种子数据
│   │   ├── security/               # JWT + 拦截器 + 权限注解
│   │   ├── aspect/                 # AOP 操作日志切面
│   │   ├── entity/ dto/ vo/ mapper/ service/ controller/   # 分层结构
│   └── src/main/resources/db/init.sql   # 建库建表脚本
├── frontend/                       # Vue 3 前端（端口 5173，代理 /api 与 /uploads）
│   └── src/
│       ├── api/ stores/ router/ utils/ layout/ components/
│       └── views/                  # 用户端 + admin 管理端页面
├── docs/测试报告.md                 # 功能/接口/兼容性测试记录
└── README.md
```

## 快速启动

### 方式一：单文件运行（推荐，交付版）

项目根目录的 **`cjr-help-platform.jar`** 已内置前端页面与后端服务，一个文件运行全站：

> **下载运行包**：<https://github.com/Sophia2676/cjr-help-platform/releases/download/v1.1.0/cjr-help-platform-v1.1.0.jar>
> （或自行构建：见下方「重新生成单文件 jar」）

```bash
# 1. 准备数据库（首次）
mysql -uroot -p < backend/src/main/resources/db/init.sql

# 2. 运行
java -Dfile.encoding=UTF-8 -jar cjr-help-platform.jar

# 3. 浏览器访问（前端页面、接口、上传文件均由此 jar 提供）
#    http://localhost:8080
```

数据库连接默认 root/123456（`backend/src/main/resources/application.yml`），首次启动自动写入种子数据（幂等）；上传图片保存在运行目录的 `uploads/` 下。前端采用 hash 路由，刷新与深链均正常。

### 方式二：开发模式（前后端分离联调）

```bash
# 后端
cd backend && mvn clean package -DskipTests && java -Dfile.encoding=UTF-8 -jar target/cjr-help-platform-backend-1.0.0.jar

# 前端（另开终端，Vite 已配置 /api、/uploads 代理到 8080）
cd frontend && npm install && npm run dev   # 访问 http://localhost:5173
```

### 重新生成单文件 jar

```bash
cd frontend && npm run build
# 将 dist 内容复制到 backend/src/main/resources/static/ 后：
cd ../backend && mvn clean package -DskipTests
# 产物 target/cjr-help-platform-backend-1.0.0.jar 即为单文件运行包
```

## 演示账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 管理员（访问 /admin 后台） |
| zhang | 123456 | 普通用户（肢体残疾三级） |
| li | 123456 | 普通用户（听力残疾二级） |
| wang | 123456 | 普通用户（残疾人家属） |

## 主要接口一览

- 认证：`POST /api/auth/register|login`，`GET /api/auth/info`
- 帖子：`GET/POST/PUT/DELETE /api/post[/{id}]`，`POST /api/post/{id}/like`，`GET /api/post/my`
- 评论：`GET /api/comment/post/{postId}`，`POST /api/comment`，`DELETE /api/comment/{id}`
- 经验：`/api/experience/**`（同帖子模式）
- 捐助：`GET /api/help/page`，`POST /api/help/{id}/donate`，`POST /api/help/{id}/complete`，`POST /api/donation/{id}/confirm`
- 政策：`GET /api/policy/page`，`GET /api/policy/{id}`
- 消息：`GET /api/message/page`，`GET /api/message/unread/count`
- 上传：`POST /api/file/upload`
- 管理端：`/api/admin/**`（审核、用户、政策、捐助、统计、日志，需管理员角色）

统一响应：`{code, msg, data}`；`code` 200 成功 / 400 业务失败 / 401 未登录 / 403 无权限 / 500 系统异常。

## 安全与约束

- 密码 BCrypt 加密存储；JWT 72 小时过期，拦截器每请求校验账号状态（禁用立即生效）
- 帖子 / 经验 / 求助内容需管理员审核后公开；驳回必须填写原因并消息通知作者
- 捐助金额使用 DECIMAL 精确计算，事务保证"扣减剩余额度 + 累加已筹金额 + 消息通知"原子性；禁止捐助自己的求助、超额捐助
- 图片上传限制 10MB、扩展名与 Content-Type 双白名单、UUID 重命名防穿越
- 操作日志自动记录请求参数并对密码字段脱敏（`***`）

## 2026-09-30 新增功能

### 智能推荐
- 用户搜索帖子时自动记录关键词次数；某关键词搜索次数 **> 20 次** 时，首页出现「为你推荐」区块，推送该关键词相关帖子（按热度排序）
- 接口：`GET /api/post/recommend`（登录态）

### 智能语音播报
- 页面右下角悬浮按钮「一键智能语音播报」：点击后朗读当前页面文字（过滤按钮/表单/导航），**切换页面自动朗读新页面，直到再次点击关闭**
- 纯前端实现（浏览器 Web Speech 语音合成，建议 Chrome/Edge）；无障碍辅助

### 登录方式
- **账号登录**（原有）/ **手机号登录**（密码或短信验证码，新增）/ **微信扫码登录**（需微信开放平台账号，配置 `cjr.wechat.appid/secret` 与前端 `VITE_WECHAT_APPID` 后可用；首次微信登录自动注册）
- 短信验证码为**演示模式**：验证码直接返回并打印在后端日志，生产环境接入短信服务商后替换 `UserServiceImpl.sendSmsCode`
- 注册时可填写手机号；个人资料页可绑定/修改手机号

### 社区工作者（新角色 WORKER）
- 新增角色「社区工作者」：管理员可在后台用户管理里将用户设为工作者
- 登录后主导航出现「工作者面板」（`/help/worker`）：查看全部求助列表，**直接显示求助人账号手机号与联系电话**（一键拨号），仅 WORKER/ADMIN 角色可访问
- 接口：`GET /api/help/worker-page`（@RequireWorker）
- 演示账号：**worker / 123456**（社区工作者，手机 13800000001）

### 数据库升级
- 已部署数据库执行：`mysql -uroot -p < docs/upgrade-20260930.sql`（新增 search_log 表、user.openid 字段）
