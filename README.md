# 西柚（若依）资产设备管理系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5+-4fc08d.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

西柚资产管理系统是一款基于 **Spring Boot 3** + **Vue 3** 开发的企业级 IT 资产管理平台。

> **基于若依 · 深度对标 Snipe-IT · 让资产管理更加简单**

本系统立足于企业 IT 资产管理痛点，深度对标 Snipe-IT 的功能逻辑，并针对国内复杂的审批流、盘点习惯及资产分类进行了全方位改良。告别“水土不服”，打造更懂国人操作习惯、更符合国内企业流程的
IT 资产全生命周期数字化管理方案。

## 🔗 在线演示

- **地址**: [http://101.35.25.237/](http://101.35.25.237/)
- **账号**: `test`
- **密码**: `123456`

## ✨ 核心功能

- **📦 资产全生命周期管理**：从入库、领用、借用、调拨到盘点、维修、报废的全过程追踪。
- **📋 资产分类与模型**：灵活定义资产类别（办公设备、软件授权、耗材等），自定义资产字段。
- **🔍 智能盘点**：支持手机端扫码盘点，自动比对库存，生成盘点差异报告。
- **📈 财务折旧**：内置多种折旧算法，自动计算资产净值，输出折旧报表。
- **🛡️ 权限与安全**：基于 RBAC 的精细化权限控制，集成 JWT 鉴权与图形/滑动验证码。
- **☁️ 云服务集成**：原生支持阿里云、腾讯云、七牛云、Minio 等主流 OSS 及 SMS 服务。

## 🛠️ 技术架构

### 后端 (Backend)

- **核心框架**: Spring Boot 3.4
- **持久层**: MyBatis Plus 3.5.16
- **数据库**: MySQL 9.5+, H2 (用于单元测试)
- **文档方案**: Knife4j + OpenAPI 3
- **加密鉴权**: JJWT, BCrypt
- **工具链**: Hutool, Lombok, MapStruct, EasyExcel
- **消息与任务**: Quartz (Job), WebSocket

### 前端 (Frontend)

- **核心框架**: Vue 3.5 (Composition API)
- **构建工具**: Vite 7.1
- **UI 组件库**: Element Plus
- **样式引擎**: UnoCSS (Atomic CSS)
- **状态管理**: Pinia
- **协作开发**: TypeScript, ESLint, Prettier

## 📂 项目模块

```text
ciyo-itasset
├── ciyo-admin        # 管理后台入口，包含 API 控制层与安全配置
├── ciyo-asset        # 资产管理业务逻辑核心
├── ciyo-common       # 通用工具类、常量、基础组件
├── ciyo-file         # 文件存储服务，集成多种 OSS
├── ciyo-generator    # 快速开发代码生成器
├── ciyo-job          # 分布式定时任务处理
├── ciyo-message      # 消息中心，支持邮件、短信、站内信
├── ciyo-system       # 系统基础管理（用户、角色、字典、日志）
└── itasset-front     # Vue3 前端工程
```

## 🚀 快速开始

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- Redis (可选，用于缓存)
- Node.js 20+ & pnpm 8+

### 2. 后端部署

1. 克隆项目：`git clone https://gitee.com/your-repo/ciyo-itasset.git`
2. 创建数据库 `ciyo_itasset` 并执行 SQL 初始化脚本。
3. 修改 `ciyo-admin/src/main/resources/application-dev.yml` 中的数据库配置。
4. 运行 `com.ciyocloud.api.CiyoAssetApiApplication` 即可。

### 3. 前端部署

```bash
cd itasset-front
pnpm install
pnpm dev
```

## 📅 后期计划

- **🔄 更灵活的自定义审批流**：支持可视化拖拽配置审批节点，实现多级审批、会签、条件分支等复杂场景，满足不同企业的个性化审批需求。
- **📝 自定义字段扩展**：允许用户根据业务需求自由添加资产扩展字段，支持多种字段类型（文本、数字、日期、下拉选择、附件等），无需开发即可适配各类资产管理场景。

## 鸣谢

- [RuoYi](https://gitee.com/y_project/RuoYi)：优秀的开源后台管理系统框架。
- [Snipe-IT](https://github.com/snipe/snipe-it)：功能强大的开源资产管理系统，本项目业务逻辑参考对象。
- [Art Design Pro](https://github.com/Daymychen/art-design-pro)：极具视觉美感的 Vue3 管理后台模板，本项目前端脚手架。

## 交流群
![输入图片说明](readmeimg/6f22c1634883fe5b389d5ac9c2ce4a40.jpg)

## 📄 许可证

本项目遵循 [GPL-3.0 License](LICENSE) 协议。
