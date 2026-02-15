<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter?label=Maven%20Central&color=orange" alt="Maven Central"></a>
  <img src="https://img.shields.io/badge/Java-1.8+-blue?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.x-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <a href="https://github.com/BigGG-Guardian/guardian/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
  <a href="https://github.com/BigGG-Guardian/guardian/releases"><img src="https://img.shields.io/github/v/release/BigGG-Guardian/guardian?label=Release&color=green" alt="Release"></a>
  <a href="https://github.com/BigGG-Guardian/guardian/stargazers"><img src="https://img.shields.io/github/stars/BigGG-Guardian/guardian?style=flat&logo=github" alt="Stars"></a>
</p>

<h1 align="center">Guardian</h1>
<p align="center"><b>轻量级 Spring Boot API 请求层防护框架</b></p>
<p align="center">防重提交、接口限流 —— 一个 Starter 搞定 API 请求防护。</p>

<p align="center">
  <a href="https://github.com/BigGG-Guardian/guardian">GitHub</a> ·
  <a href="https://gitee.com/BigGG-Guardian/guardian">Gitee</a> ·
  <a href="https://central.sonatype.com/artifact/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter">Maven Central</a>
</p>

---

## 功能一览

| 功能 | Starter | 注解 | YAML | 说明 |
|------|---------|------|------|------|
| 防重复提交 | `guardian-repeat-submit-spring-boot-starter` | `@RepeatSubmit` | ✅ | 防止用户重复提交表单/请求 |
| 接口限流 | `guardian-rate-limit-spring-boot-starter` | `@RateLimit` | ✅ | 滑动窗口 + 令牌桶，双算法可选 |

每个功能独立模块、独立 Starter，**用哪个引哪个，互不依赖**。

---

## 快速开始

### 防重复提交

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-repeat-submit-spring-boot-starter</artifactId>
    <version>1.3.0</version>
</dependency>
```

```java
@PostMapping("/submit")
@RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS, message = "订单正在处理，请勿重复提交")
public Result submitOrder(@RequestBody OrderDTO order) {
    return orderService.submit(order);
}
```

### 接口限流

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-rate-limit-spring-boot-starter</artifactId>
    <version>1.3.0</version>
</dependency>
```

**滑动窗口（默认）：**

```java
@RateLimit(qps = 1, window = 60, windowUnit = TimeUnit.SECONDS, rateLimitScope = RateLimitKeyScope.IP)
```

**令牌桶：**

```java
@RateLimit(qps = 5, capacity = 20, algorithm = RateLimitAlgorithm.TOKEN_BUCKET)
```

或 YAML 批量配置：

```yaml
guardian:
  rate-limit:
    urls:
      # 滑动窗口
      - pattern: /api/sms/send
        qps: 1
        window: 60
        window-unit: seconds
        rate-limit-scope: ip
      # 令牌桶
      - pattern: /api/seckill/**
        qps: 10
        capacity: 50
        algorithm: token_bucket
        rate-limit-scope: global
```

---

## 防重复提交

<details>
<summary><b>展开查看完整文档</b></summary>

### 使用方式

**注解（推荐单接口）：**

```java
@RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS, message = "请勿重复提交")
```

**YAML（推荐批量）：**

```yaml
guardian:
  repeat-submit:
    urls:
      - pattern: /api/order/**
        interval: 10
        message: "订单正在处理，请勿重复提交"
```

### 全量配置

```yaml
guardian:
  repeat-submit:
    storage: redis                    # redis / local
    key-generator: default
    key-encrypt: md5                  # none / md5
    response-mode: exception          # exception / json
    log-enabled: false
    exclude-urls:
      - /api/public/**
    urls:
      - pattern: /api/order/submit
        interval: 10
        time-unit: seconds
        key-scope: user               # user / ip / global
        message: "请勿重复提交"
```

### 防重维度

| 维度 | YAML 值 | 注解值 | 效果 |
|------|---------|--------|------|
| 用户级 | `user` | `KeyScope.USER` | 同一用户 + 同一接口 + 同一参数（默认） |
| IP 级 | `ip` | `KeyScope.IP` | 同一 IP + 同一接口 + 同一参数 |
| 全局级 | `global` | `KeyScope.GLOBAL` | 同一接口 + 同一参数 |

### 响应模式

| 模式 | 配置值 | 行为 |
|------|--------|------|
| 异常模式 | `exception`（默认） | 抛出 `RepeatSubmitException`，由全局异常处理器捕获 |
| JSON 模式 | `json` | 拦截器直接写入 JSON 响应 |

### 可观测性

- **拦截日志**：`log-enabled: true`，前缀 `[Guardian-Repeat-Submit]`
- **Actuator**：`GET /actuator/guardian-repeat-submit`

```json
{
  "totalBlockCount": 128,
  "totalPassCount": 5432,
  "topBlockedApis": {
    "/api/order/submit": 56,
    "/api/sms/send": 42
  }
}
```

### 扩展点

| 组件 | 接口 | 说明 |
|------|------|------|
| 用户上下文 | `UserContext` | 返回当前用户 ID（所有模块共享） |
| 键生成策略 | `KeyGenerator` | 自定义防重 Key 拼接逻辑 |
| 键加密策略 | `AbstractKeyEncrypt` | 自定义 Key 加密方式 |
| 存储 | `RepeatSubmitStorage` | 自定义存储后端 |
| 响应处理 | `RepeatSubmitResponseHandler` | 自定义 JSON 响应格式 |

</details>

---

## 接口限流

<details>
<summary><b>展开查看完整文档</b></summary>

### 两种算法

| | 滑动窗口（默认） | 令牌桶 |
|--|-----------------|--------|
| 算法 | 统计窗口内请求数，超过阈值拒绝 | 按速率补充令牌，有令牌放行，无令牌拒绝 |
| 突发流量 | 不允许，窗口内严格限制 | 允许，桶满时可瞬间消耗所有令牌 |
| 参数 | `maxCount = qps × window(秒)` | `补充速率 = qps / window(秒)`, `capacity = 桶容量` |
| 适合场景 | 精确控速的接口 | 允许突发的场景（秒杀、验证码） |
| 数据结构 | Local: Deque / Redis: ZSET | Local: double + synchronized / Redis: HASH |

### 使用方式

**注解：**

```java
// 滑动窗口：每秒 10 次，全局
@RateLimit(qps = 10)

// 令牌桶：每秒补 5 个，桶容量 20，允许突发 20 次
@RateLimit(qps = 5, capacity = 20, algorithm = RateLimitAlgorithm.TOKEN_BUCKET)

// 令牌桶 + 分钟级：每分钟补 10 个，桶容量 10
@RateLimit(qps = 10, window = 1, windowUnit = TimeUnit.MINUTES, capacity = 10, algorithm = RateLimitAlgorithm.TOKEN_BUCKET)
```

**YAML：**

```yaml
guardian:
  rate-limit:
    urls:
      - pattern: /api/sms/send
        qps: 1
        window: 60
        window-unit: seconds
        rate-limit-scope: ip
      - pattern: /api/seckill/**
        qps: 10
        capacity: 50
        algorithm: token_bucket
        rate-limit-scope: global
        message: "抢购太火爆，请稍后重试"
```

### 全量配置

```yaml
guardian:
  rate-limit:
    enabled: true                     # 总开关
    storage: redis                    # redis / local
    key-generator: default
    response-mode: exception          # exception / json
    log-enabled: false
    exclude-urls:
      - /api/public/**
    urls:
      - pattern: /api/sms/send
        qps: 1
        window: 60
        window-unit: seconds
        rate-limit-scope: ip
      - pattern: /api/order/**
        qps: 10
        rate-limit-scope: user
      - pattern: /api/seckill/**
        qps: 10
        capacity: 50
        algorithm: token_bucket
        rate-limit-scope: global
```

### 注解参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `qps` | `10` | 滑动窗口=QPS，令牌桶=每 window 补充的令牌数 |
| `window` | `1` | 滑动窗口=窗口跨度，令牌桶=补充周期 |
| `windowUnit` | `SECONDS` | 时间单位 |
| `algorithm` | `SLIDING_WINDOW` | 限流算法：`SLIDING_WINDOW` / `TOKEN_BUCKET` |
| `capacity` | `-1` | 令牌桶容量，≤0 时取 qps 值 |
| `rateLimitScope` | `GLOBAL` | 限流维度 |
| `message` | `请求过于频繁，请稍后再试` | 提示信息 |

### 限流维度

| 维度 | YAML 值 | 注解值 | 效果 |
|------|---------|--------|------|
| 全局 | `global` | `GLOBAL` | 接口维度，不区分用户和 IP（默认） |
| IP | `ip` | `IP` | 同一 IP 独立计数 |
| 用户 | `user` | `USER` | 同一用户独立计数 |

### 可观测性

- **拦截日志**：`log-enabled: true`，前缀 `[Guardian-Rate-Limit]`
- **Actuator**：`GET /actuator/guardian-rate-limit`

```json
{
  "totalRequestCount": 5560,
  "totalPassCount": 5432,
  "totalBlockCount": 128,
  "blockRate": "2.30%",
  "topBlockedApis": { "/api/sms/send": 56 },
  "topRequestApis": { "/api/search": 3200 },
  "apiDetails": {
    "/api/sms/send": { "requests": 200, "passes": 144, "blocks": 56, "blockRate": "28.00%" }
  }
}
```

### 扩展点

| 组件 | 接口 | 说明 |
|------|------|------|
| 用户上下文 | `UserContext` | 返回当前用户 ID（所有模块共享） |
| 键生成策略 | `RateLimitKeyGenerator` | 自定义限流 Key 拼接逻辑 |
| 存储 | `RateLimitStorage` | 自定义存储后端 |
| 响应处理 | `RateLimitResponseHandler` | 自定义 JSON 响应格式 |

</details>

---

## 架构

### 模块结构

```
guardian-parent
├── guardian-core                          # 公共基础（共享类）
├── guardian-repeat-submit/                # 防重复提交
│   ├── guardian-repeat-submit-core/
│   └── guardian-repeat-submit-spring-boot-starter/
├── guardian-rate-limit/                   # 接口限流
│   ├── guardian-rate-limit-core/
│   └── guardian-rate-limit-spring-boot-starter/
├── guardian-storage-redis/                # Redis 存储（多模块共享）
└── guardian-example/                      # 示例工程
```

### guardian-core 共享类

| 类 | 作用 |
|----|------|
| `UserContext` | 用户上下文接口，实现一次所有模块共享 |
| `GuardianResponseHandler` | 统一响应处理器接口 |
| `DefaultGuardianResponseHandler` | 默认 JSON 响应实现 |
| `BaseStatistics` | 拦截统计基类 |
| `GuardianLogUtils` | 参数化日志工具 |
| `RepeatableRequestFilter` | 请求体缓存过滤器 |

## 存储对比

| | Redis | Local |
|--|-------|-------|
| 分布式 | 支持 | 仅单机 |
| 持久性 | Redis 持久化 | 重启丢失 |
| 推荐场景 | 生产环境 | 开发/单体应用 |
| 额外依赖 | 需要 Redis | 无 |

## 更新日志

### v1.3.0

- **新增**：接口限流令牌桶算法（`TOKEN_BUCKET`），支持突发流量
- **修复**：本地滑动窗口并发竞态条件（check-then-act），加 synchronized 保证原子性
- **修复**：本地缓存内存泄漏，增加守护线程定期清理过期 Key
- **修复**：rate-limit 模块 POM parent 版本写死，统一改为 `${revision}`

### v1.2.0

- **新增**：接口限流模块（滑动窗口算法），支持注解 + YAML 双模式
- **新增**：限流维度（GLOBAL / IP / USER）
- **新增**：限流 Actuator 监控端点

### v1.1.0

- 防重复提交初始版本

## 环境要求

- **JDK** 1.8+
- **Spring Boot** 2.7.x
- **Redis** 5.0+（使用 Redis 存储时）

## 仓库地址

| 平台 | 地址 |
|------|------|
| GitHub（主仓） | https://github.com/BigGG-Guardian/guardian |
| Gitee（镜像同步） | https://gitee.com/BigGG-Guardian/guardian |
| Maven Central | https://central.sonatype.com/artifact/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter |

> Gitee 从 GitHub 自动同步，Issues 和 PR 请提交到 GitHub。

## 开源协议

[Apache License 2.0](LICENSE)
