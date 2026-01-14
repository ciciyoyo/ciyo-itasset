# Ciyo IT Asset Management (ITAM) System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5+-4fc08d.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

Ciyo ITAM is an enterprise-grade IT asset management platform built with **Spring Boot 3** and **Vue 3**.

> **RuoYi-based · Snipe-IT Alternative · Specifically for China Workflow**

Rooted in the pain points of enterprise IT asset management, Ciyo ITAM deeply aligns with Snipe-IT's features while
providing comprehensive localization for complex workflows, inventory habits, and asset classification. Say goodbye to "
ill-fitting" foreign solutions and experience a digital lifecycle management platform truly tailored for local
enterprise needs.

## ✨ Key Features

- **📦 Lifecycle Management**: Full tracking of assets from procurement and领用 to inventory, maintenance, and
  retirement.
- **📋 Flexible Asset Models**: Easily define asset categories (office equipment, software licenses, consumables) with
  custom fields.
- **🔍 Smart Inventory**: Supports mobile QR code scanning for physical inventory, automated stock comparison, and report
  generation.
- **📈 Financial Depreciation**: Built-in depreciation algorithms to calculate net book value and generate financial
  reports.
- **🛡️ Security & Permissions**: Fine-grained RBAC permissions, integrated JWT authentication, and various captcha
  supports.
- **☁️ Cloud Integration**: Native support for Aliyun, Tencent Cloud, Qiniu, and Minio for OSS and SMS services.
- **📱 Responsive UI**: Modern responsive design for Web and seamless extension to mobile browsers and WeChat Mini
  Programs.

## 🛠️ Technology Stack

### Backend

- **Core Framework**: Spring Boot 3.4
- **Persistence**: MyBatis Plus 3.5.16
- **Database**: MySQL 9.5+, H2 (for unit testing)
- **API Docs**: Knife4j + OpenAPI 3
- **Security**: JJWT, BCrypt
- **Utils**: Hutool, Lombok, MapStruct, EasyExcel
- **Job & Messaging**: Quartz, WebSocket

### Frontend

- **Framework**: Vue 3.5 (Composition API)
- **Build Tool**: Vite 7.1
- **UI Kit**: Element Plus
- **Styling**: UnoCSS (Atomic CSS)
- **State Management**: Pinia
- **Development**: TypeScript, ESLint, Prettier

## 📂 Project Structure

```text
ciyo-itasset
├── ciyo-admin        # Backend entry, API controllers & security configurations
├── ciyo-asset        # Core business logic for asset management
├── ciyo-common       # Common utils, constants, and base components
├── ciyo-file         # File storage service with multi-OSS integration
├── ciyo-generator    # Rapid development code generator
├── ciyo-job          # Distributed scheduled job processing
├── ciyo-message      # Message center (Email, SMS, Notifications)
├── ciyo-system       # System management (Users, Roles, Dicts, Logs)
└── itasset-front     # Vue 3 frontend project
```

## 🚀 Quick Start

### 1. Prerequisites

- JDK 17+
- MySQL 8.0+
- Redis (Optional, for caching)
- Node.js 20+ & pnpm 8+

### 2. Backend Setup

1. Clone the repo: `git clone https://gitee.com/your-repo/ciyo-itasset.git`
2. Create database `ciyo_itasset` and run the SQL initialization script.
3. Update database credentials in `ciyo-admin/src/main/resources/application-dev.yml`.
4. Run `com.ciyocloud.api.CiyoAssetApiApplication`.

### 3. Frontend Setup

```bash
cd itasset-front
pnpm install
pnpm dev
```

## 🤝 Contributing

1. Fork the repository
2. Create your `Feat_xxx` branch
3. Commit your changes
4. Open a Pull Request

## Acknowledgements

- [RuoYi](https://gitee.com/y_project/RuoYi): An excellent open-source management system framework.
- [Snipe-IT](https://github.com/snipe/snipe-it): A powerful open-source asset management system, the inspiration for
  this project's business logic.
- [Art Design Pro](https://github.com/Daymychen/art-design-pro): A visually stunning Vue 3 admin template, the
  inspiration for this project's frontend design.

## 📄 License

This project is licensed under the [GPL-3.0 License](LICENSE).
