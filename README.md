# JPostman

<div align="center">

一个轻量级、跨平台的HTTP API测试工具

[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey.svg)](https://github.com/voghost/JPostman)

[English](#) | [简体中文](#简体中文)

</div>

---

## 📖 简介

JPostman 是一个基于 Java 11 开发的 HTTP 客户端工具，灵感来源于 Postman。它专为解决**内网环境无法安装商业软件**的痛点而设计，利用 Java 的跨平台特性，可以在 Windows、macOS 和 Linux 系统上无缝运行。

### 🎯 设计初衷

- **内网友好**：许多企业内网环境出于安全考虑，无法安装 Postman 等外部工具，JPostman 作为开源软件可以自由部署
- **跨平台**：基于 Java 11，一次编译，到处运行，无需为不同操作系统维护多个版本
- **轻量级**：单一 JAR 包，无需复杂安装过程，开箱即用
- **开源免费**：完全开源，遵循 MIT 协议，可自由使用、修改和分发

---

## ✨ 主要特性

### 核心功能

- ✅ **完整的 HTTP 方法支持**：GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS
- ✅ **请求配置**：
  - 灵活的 URL 和查询参数编辑
  - Headers 管理（支持常用 Header 自动补全）
  - 多种请求体格式：JSON、XML、Form Data、x-www-form-urlencoded、Raw Text
- ✅ **认证支持**：Basic Auth、Bearer Token、API Key
- ✅ **环境变量**：支持 `{{variable}}` 语法，可在 URL、Headers、Body 中使用
- ✅ **Collection 管理**：组织和管理多个 API 请求
- ✅ **请求历史**：自动记录最近 100 条请求历史

### 用户体验

- 🎨 **现代化 UI**：基于 FlatLaf，提供亮色/暗色主题
- 🌍 **多语言支持**：内置中文和英文界面
- 💾 **持久化存储**：请求和配置自动保存到本地 JSON 文件
- 🚀 **快速响应**：异步请求执行，不阻塞界面
- 📊 **响应查看**：支持语法高亮显示响应内容

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 11 | 核心运行环境 |
| Swing | - | GUI 框架 |
| FlatLaf | 3.2.5 | 现代化 Look and Feel |
| OkHttp | 4.12.0 | HTTP 客户端 |
| Jackson | 2.15.3 | JSON 序列化/反序列化 |
| RSyntaxTextArea | 3.3.4 | 代码编辑器与语法高亮 |
| SLF4J + Logback | - | 日志框架 |
| Lombok | 1.18.30 | 减少样板代码 |
| Maven | - | 构建工具 |

---

## 📦 安装与运行

### 前置要求

- Java 11 或更高版本

### 快速开始

#### 方式一：从源码构建

```bash
# 克隆仓库
git clone https://github.com/voghost/JPostman.git
cd JPostman

# 使用 Maven 构建
mvn clean package

# 运行应用
java -jar target/JPostman-1.0-SNAPSHOT.jar
```

#### 方式二：直接运行（推荐）

```bash
# 使用 Maven 直接运行
mvn clean compile exec:java
```

---

## 🚀 使用指南

### 1. 创建 Collection

点击左侧面板的 **"新建集合"** 按钮，输入集合名称。Collection 用于组织和管理相关的 API 请求。

### 2. 添加请求

在已创建的 Collection 中，点击 **"新建请求"**，选择 HTTP 方法并输入 URL。

### 3. 配置请求
- **请求参数**：在 Params 标签页中添加查询参数，会自动同步到 URL
- **请求头**：在 Headers 标签页中添加自定义 HTTP 头
- **请求体**：在 Body 标签页中选择格式并输入内容
- **认证**：在 Auth 标签页中配置认证信息

### 4. 发送请求

点击 **"发送"** 按钮，响应结果将显示在下方面板中，包括状态码、耗时、响应头和响应体。

### 5. 使用环境变量

在 **"工具 → 管理环境"** 中定义变量，然后在请求中使用 `{{variable_name}}` 语法引用。

---

## 📂 数据存储

所有数据存储在用户目录下：

```
~/.jpostman/
├── config/
│   └── app-settings.json       # 应用配置
├── projects/
│   └── default/
│       ├── collections/        # Collection 文件
│       ├── environments/       # 环境配置
│       ├── globals.json        # 全局变量
│       └── history.json        # 请求历史
└── logs/
    └── jpostman.log            # 日志文件
```

---

## 🗺️ 功能路线图

### ✅ 已完成

- [x] 基础 HTTP 请求功能
- [x] Collection 管理
- [x] 环境变量支持
- [x] 主题切换（亮/暗）
- [x] 多语言界面
- [x] 请求历史记录
- [x] URL 与 Params 双向同步

### 🚧 开发中

- [ ] 响应内容语法高亮和格式化
- [ ] 请求前/后脚本支持
- [ ] 代码生成（curl、Python、Java 等）

### 📋 计划中

- [ ] 导入/导出 Postman Collection 格式
- [ ] GraphQL 支持
- [ ] WebSocket 支持
- [ ] Mock Server 功能
- [ ] 团队协作与云同步

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发环境设置

1. Fork 本仓库
2. 克隆到本地：`git clone https://github.com/YOUR_USERNAME/JPostman.git`
3. 创建分支：`git checkout -b feature/your-feature`
4. 提交更改：`git commit -am 'Add some feature'`
5. 推送分支：`git push origin feature/your-feature`
6. 提交 Pull Request

### 代码规范

- 遵循 Java 编码规范
- 使用 Lombok 减少样板代码
- 添加必要的注释和文档
- 确保代码通过编译和测试

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

您可以自由地：

- ✅ 商业使用
- ✅ 修改
- ✅ 分发
- ✅ 私有使用

唯一的要求是保留原作者的版权声明。

---

## 👤 作者

**LeifLiu**

- Email: [voghost2@gmail.com](mailto:voghost2@gmail.com)
- GitHub: [@voghost](https://github.com/voghost)

---

## 🙏 致谢

感谢以下开源项目：

- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) - 现代化的 Swing Look and Feel
- [OkHttp](https://github.com/square/okhttp) - 强大的 HTTP 客户端
- [Jackson](https://github.com/FasterXML/jackson) - JSON 处理库
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) - 语法高亮编辑器

---

## ⭐ Star History

如果这个项目对您有帮助，请给它一个 Star ⭐️

[![Star History Chart](https://api.star-history.com/svg?repos=voghost/JPostman&type=Date)](https://star-history.com/#voghost/JPostman&Date)

---

<div align="center">

**[⬆ 返回顶部](#jpostman)**

Made with ❤️ by LeifLiu

</div>
