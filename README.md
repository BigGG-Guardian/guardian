<p align="center">
  <h1 align="center">Guardian</h1>
  <p align="center">轻量级接口防重复提交框架，开箱即用，为 Spring Boot 应用提供优雅的重复请求拦截能力。</p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-1.8+-blue.svg" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.x-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg" alt="License">
  <img src="https://img.shields.io/badge/Version-1.0.0-orange.svg" alt="Version">
</p>

---

## 特性

- **注解驱动**：方法级 `@RepeatSubmit` 注解，一行代码即可防重
- **YAML 批量配置**：通过 `application.yml` 批量配置 URL 防重规则，支持 AntPath 通配符，优先级高于注解
- **多存储方案**：内置 Redis（推荐）和本地缓存两种存储，通过配置一键切换
- **策略可扩展**：Key 生成策略、加密策略均可自定义替换
- **请求体感知**：自动缓存 JSON 请求体，将完整请求参数纳入防重 Key（Base64 编码）
- **匿名用户支持**：未登录时自动降级为 SessionId / IP 标识，不会产生 null Key
- **context-path 兼容**：URL 规则自动适配 `server.servlet.context-path`，无需额外处理
- **异常自动回滚**：业务异常时自动释放防重锁，不影响后续正常请求
- **零侵入**：基于 Spring Boot Starter 自动装配，无需手动配置 Bean

## 模块结构

```
guardian-parent
├── guardian-core                   # 核心模块：注解、拦截器、策略接口、存储接口
├── guardian-storage-redis          # Redis 存储实现
└── guardian-spring-boot-starter    # Spring Boot 自动配置 & Starter
```

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.sun.guardian</groupId>
    <artifactId>guardian-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 实现用户上下文

Guardian 需要获取当前用户 ID 来区分不同用户的请求，需实现 `UserContextResolver` 接口：

```java
@Component
public class MyUserContextResolver implements UserContextResolver {
    @Override
    public String getUserId() {
        // 从 Token、Session 或 SecurityContext 中获取用户 ID
        // 未登录时返回 null 即可，框架会自动降级为 SessionId / IP
        return SecurityUtils.getCurrentUserId();
    }
}
```

### 3. 使用注解

在需要防重的 Controller 方法上添加 `@RepeatSubmit`：

```java
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @PostMapping("/submit")
    @RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS, message = "订单正在处理中，请勿重复提交")
    public Result submitOrder(@RequestBody OrderDTO order) {
        return orderService.submit(order);
    }
}
```

完成，启动项目即可生效。

## 配置说明

### application.yml 全量配置

```yaml
guardian:
  # 存储方式：redis（默认） / local
  storage: redis

  # Key 生成策略：default（默认）
  key-generator: default

  # Key 加密策略：none（默认，不加密） / md5
  key-encrypt: md5

  # YAML 批量配置防重 URL（优先级高于 @RepeatSubmit 注解）
  # pattern 支持 AntPath 通配符，自动兼容 context-path（带或不带均可匹配）
  urls:
    - pattern: /api/order/submit
      interval: 10
      time-unit: seconds
      message: "订单正在处理中，请勿重复提交"
      client-type: PC
    - pattern: /api/payment/**
      interval: 30
      time-unit: seconds
    - pattern: /api/user/bindPhone
      interval: 60
```

### 配置项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `guardian.storage` | `redis` | 存储方式，可选 `redis` / `local` |
| `guardian.key-generator` | `default` | Key 生成策略 |
| `guardian.key-encrypt` | `none` | Key 加密策略，可选 `none` / `md5` |
| `guardian.urls` | `[]` | YAML 批量 URL 防重规则 |

### @RepeatSubmit 注解参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `interval` | `int` | `5` | 防重时间间隔 |
| `timeUnit` | `TimeUnit` | `SECONDS` | 时间单位 |
| `message` | `String` | `"您的请求过于频繁，请稍后再试"` | 拦截时的提示信息 |
| `clientType` | `ClientType` | `PC` | 客户端类型（PC / MOBILE） |

### URL 规则配置参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `pattern` | `String` | - | URL 匹配模式，支持 AntPath 通配符（`*`、`**`、`?`），自动兼容 context-path |
| `interval` | `int` | `5` | 防重时间间隔 |
| `time-unit` | `TimeUnit` | `SECONDS` | 时间单位 |
| `message` | `String` | `"您的请求过于频繁，请稍后再试"` | 拦截时的提示信息 |
| `client-type` | `ClientType` | `PC` | 客户端类型 |

> **优先级**：YAML `urls` 配置 > `@RepeatSubmit` 注解。同一接口如果两者都配置了，以 YAML 为准。

## 工作原理

```
请求进入
  │
  ▼
RepeatableRequestFilter（缓存 JSON 请求体）
  │
  ▼
RepeatSubmitInterceptor
  │
  ├── 1. 匹配 YAML URL 规则（AntPathMatcher，自动兼容 context-path）
  │       ↓ 未命中
  ├── 2. 检查 @RepeatSubmit 注解
  │       ↓ 均未命中 → 放行
  │
  ▼ 命中规则
KeyGenerator 生成防重 Key
  │  Key = 用户标识 + 客户端类型 + IP + 请求方法 + URI + 参数(Base64)
  │  用户标识：已登录 → userId / 未登录 → sessionId / 无 session → IP
  │
  ▼
KeyEncrypt 加密 Key（可选 MD5）
  │
  ▼
Storage.tryAcquire()
  ├── 成功 → 放行请求，Key 写入存储并设置过期时间
  └── 失败 → 抛出 RepeatSubmitException
  │
  ▼
业务执行
  ├── 正常完成 → Key 自然过期
  └── 异常 → afterCompletion 自动释放 Key
```

## 异常处理

Guardian 抛出 `RepeatSubmitException`（继承 `RuntimeException`），框架本身**不拦截异常**，交由业务方自行处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepeatSubmitException.class)
    public Result handleRepeatSubmit(RepeatSubmitException e) {
        return Result.fail(e.getMessage());
    }
}
```

## 扩展指南

### 自定义 Key 生成策略

1. 继承 `AbstractKeyGenerator` 并实现 `buildKey` 方法：

```java
public class MyKeyGenerator extends AbstractKeyGenerator {

    public MyKeyGenerator(UserContextResolver resolver, KeyEncryptManager encryptManager) {
        super(resolver, encryptManager);
    }

    @Override
    protected String buildKey(RepeatSubmitKey key) {
        return key.getServletUri() + ":" + key.getUserId();
    }
}
```

2. 注册为 Bean（会自动覆盖默认策略）：

```java
@Bean
public MyKeyGenerator myKeyGenerator(UserContextResolver resolver, KeyEncryptManager manager) {
    return new MyKeyGenerator(resolver, manager);
}
```

### 自定义加密策略

继承 `AbstractKeyEncrypt` 并注册为 Bean：

```java
@Bean
public AbstractKeyEncrypt mySha256Encrypt() {
    return new AbstractKeyEncrypt() {
        @Override
        public String encrypt(String key) {
            return DigestUtil.sha256Hex(key);
        }
    };
}
```

### 自定义存储方案

实现 `RepeatSubmitStorage` 接口并注册为 Bean：

```java
@Bean
public RepeatSubmitStorage myStorage() {
    return new RepeatSubmitStorage() {
        @Override
        public boolean tryAcquire(RepeatSubmitToken token) { /* ... */ }
        @Override
        public void release(RepeatSubmitToken token) { /* ... */ }
    };
}
```

## 存储方案对比

| 特性 | Redis | Local |
|------|-------|-------|
| 分布式支持 | 支持 | 不支持（单机） |
| 数据持久性 | Redis 持久化 | 进程重启丢失 |
| 适用场景 | 生产环境 | 开发 / 单体应用 |
| 依赖 | 需要 Redis | 无额外依赖 |
| 过期机制 | Redis TTL 原子过期 | 惰性过期检查 |

## 环境要求

- **JDK**：1.8+
- **Spring Boot**：2.7.x
- **Redis**：5.0+（使用 Redis 存储时）

## License

[Apache License 2.0](LICENSE)
