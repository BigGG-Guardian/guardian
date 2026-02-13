---
name: Bug 报告
about: 提交一个 Bug 帮助 Guardian 变得更好
title: '[Bug] '
labels: bug
assignees: ''
---

## 问题描述

简要描述遇到的问题。

## 复现步骤

1. 配置 `application.yml`：
```yaml
guardian:
  repeat-submit:
    ...
```
2. 调用接口：`POST /xxx`
3. 出现异常 / 非预期行为

## 预期行为

描述你期望的正确行为。

## 实际行为

描述实际发生了什么（附日志、截图更佳）。

## 环境信息

- Guardian 版本：
- JDK 版本：
- Spring Boot 版本：
- 存储方式：Redis / Local
- 操作系统：

## 补充信息

其他有助于定位问题的信息（配置文件、堆栈日志等）。
