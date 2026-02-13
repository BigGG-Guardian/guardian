<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter?label=Maven%20Central&color=orange" alt="Maven Central"></a>
  <img src="https://img.shields.io/badge/Java-1.8+-blue?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.x-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <a href="https://github.com/BigGG-Guardian/guardian/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
  <a href="https://github.com/BigGG-Guardian/guardian/releases"><img src="https://img.shields.io/github/v/release/BigGG-Guardian/guardian?label=Release&color=green" alt="Release"></a>
  <a href="https://github.com/BigGG-Guardian/guardian/stargazers"><img src="https://img.shields.io/github/stars/BigGG-Guardian/guardian?style=flat&logo=github" alt="Stars"></a>
</p>

<h1 align="center">Guardian</h1>
<p align="center"><b>轻量级 Spring Boot 接口防重复提交框架</b></p>
<p align="center">一个注解、一行配置，为你的 API 提供优雅的重复请求拦截能力。</p>

---

## 为什么选 Guardian？

| 痛点 | Guardian 的解决方案 |
|------|-------------------|
| 每个项目都要手写防重逻辑 | 引入 Starter，**零代码**开箱即用 |
| 只能用注解，批量配置麻烦 | 注解 + YAML **双模式**，YAML 支持 AntPath 通配符 |
| 只支持 Redis，没有 Redis 的项目用不了 | Redis / 本地缓存**一键切换** |
| 防重 Key 策略写死，不够灵活 | Key 生成、加密策略**可插拔替换** |
| 防重粒度固定，无法区分场景 | 三种维度可选：**用户级 / IP 级 / 全局级** |
| 某些接口不需要防重，但被全局拦截了 | 支持**排除规则（白名单）**，优先级最高 |
| 拦截后只能抛异常，不够灵活 | **双响应模式**：抛异常 / 直接返回 JSON，响应处理器可自定义 |
| 未登录用户防重 Key 出现 null | 自动降级为 **SessionId / IP**，永不为 null |
| 有 context-path 的项目 URL 匹配失败 | 自动兼容 **context-path**，无需额外处理 |
| 业务异常后锁未释放，后续请求一直被拦截 | 异常时**自动释放锁**，不影响正常使用 |

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-repeat-submit-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. 实现用户上下文（可选）

> 不实现也能用，框架会自动以 SessionId / IP 作为用户标识。

```java
@Bean
public UserContextResolver userContextResolver() {
    // 从你的登录体系中获取当前用户 ID
    return () -> SecurityUtils.getCurrentUserId();
}
```

### 3. 使用

**方式一：注解（推荐单个接口使用）**

```java
@PostMapping("/submit")
@RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS, message = "订单正在处理，请勿重复提交")
public Result submitOrder(@RequestBody OrderDTO order) {
    return orderService.submit(order);
}
```

**方式二：YAML（推荐批量配置）**

```yaml
guardian:
  repeat-submit:
    urls:
      - pattern: /api/order/**
        interval: 10
        message: "订单正在处理，请勿重复提交"
      - pattern: /api/payment/**
        interval: 30
```

完成。启动项目即可生效。

---

## 全量配置

```yaml
guardian:
  repeat-submit:
    # 存储方式：redis（默认）/ local
    storage: redis

    # Key 生成策略：default（默认）
    key-generator: default

    # Key 加密策略：none（默认）/ md5
    key-encrypt: md5

    # 响应模式：exception（默认）/ json
    response-mode: exception

    # 排除规则（白名单，优先级最高，命中直接跳过防重检查）
    exclude-urls:
      - /api/public/**
      - /api/health

    # YAML 批量 URL 防重规则（优先级高于注解）
    urls:
      - pattern: /api/order/submit
        interval: 10
        time-unit: seconds
        message: "订单正在处理，请勿重复提交"
        key-scope: user
        client-type: PC
      - pattern: /api/sms/send
        interval: 60
        key-scope: ip
      - pattern: /api/init/data
        interval: 30
        key-scope: global
```

<details>
<summary><b>配置项速查表</b></summary>

### guardian.repeat-submit.* 全局配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `storage` | `redis` | 存储方式：`redis` / `local` |
| `key-generator` | `default` | Key 生成策略 |
| `key-encrypt` | `none` | Key 加密策略：`none` / `md5` |
| `response-mode` | `exception` | 响应模式：`exception` / `json` |
| `exclude-urls` | `[]` | 排除规则（白名单），支持 AntPath 通配符 |
| `urls` | `[]` | YAML 批量 URL 防重规则 |

### @RepeatSubmit 注解参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `interval` | `5` | 防重间隔时长 |
| `timeUnit` | `SECONDS` | 时间单位 |
| `message` | `您的请求过于频繁，请稍后再试` | 拦截提示信息 |
| `keyScope` | `USER` | 防重维度：`USER` / `IP` / `GLOBAL` |
| `clientType` | `PC` | 客户端类型：`PC` / `MOBILE` |

### YAML URL 规则参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `pattern` | - | URL 匹配模式，支持 `*`、`**`、`?` 通配符 |
| `interval` | `5` | 防重间隔时长 |
| `time-unit` | `SECONDS` | 时间单位 |
| `message` | `您的请求过于频繁，请稍后再试` | 拦截提示信息 |
| `key-scope` | `user` | 防重维度：`user` / `ip` / `global` |
| `client-type` | `PC` | 客户端类型 |

> **优先级**：排除规则 > YAML `urls` > `@RepeatSubmit` 注解

</details>

---

## 响应模式

Guardian 支持两种重复提交时的响应方式，通过 `response-mode` 切换：

| 模式 | 配置值 | 行为 | 适用场景 |
|------|--------|------|----------|
| 异常模式 | `exception`（默认） | 抛出 `RepeatSubmitException`，由业务全局异常处理器捕获 | 项目已有统一异常处理 |
| JSON 模式 | `json` | 拦截器直接写入 JSON 响应，不抛出异常 | 想开箱即用 / 不想写异常处理器 |

**异常模式**（默认）需要业务端配合全局异常处理器：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepeatSubmitException.class)
    public Result handleRepeatSubmit(RepeatSubmitException e) {
        return Result.fail(e.getMessage());
    }
}
```

**JSON 模式**默认返回格式：

```json
{"code": 500, "msg": "您的请求过于频繁，请稍后再试", "timestamp": 1234567890}
```

如需适配项目统一返回格式，注册自定义 `RepeatSubmitResponseHandler` Bean 即可覆盖默认实现：

```java
@Bean
public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
    return (request, response, message) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.error(message)));
    };
}
```

---

## 防重维度

| 维度 | YAML 值 | 注解值 | 效果 |
|------|---------|--------|------|
| 用户级 | `user` | `KeyScope.USER` | 同一用户 + 同一接口 + 同一参数视为重复（默认） |
| IP 级 | `ip` | `KeyScope.IP` | 同一 IP + 同一接口 + 同一参数视为重复，不区分用户 |
| 全局级 | `global` | `KeyScope.GLOBAL` | 同一接口 + 同一参数视为重复，不区分用户和 IP |

```
user 级 Key:   {servletUri}:{method}:{clientIp}:{client}:{userId}:{args}
ip 级 Key:     {servletUri}:{method}:{clientIp}:{args}
global 级 Key: {servletUri}:{method}:{args}
```

---

## 工作原理

```
请求进入
  │
  ▼
RepeatableRequestFilter          ← 缓存 JSON 请求体，支持重复读取
  │
  ▼
RepeatSubmitInterceptor
  ├─ 1. 匹配排除规则（白名单）
  │      ↓ 命中 → 直接放行
  ├─ 2. 匹配 YAML URL 规则（AntPathMatcher，自动兼容 context-path）
  │      ↓ 未命中
  ├─ 3. 检查 @RepeatSubmit 注解
  │      ↓ 均未命中 → 直接放行
  ▼ 命中规则
KeyGenerator                     ← 根据 keyScope 选取模板，生成防重 Key
  │  用户标识：已登录→userId / 未登录→sessionId / 无session→IP
  ▼
KeyEncrypt                       ← 加密 Key（可选 MD5）
  │
  ▼
Storage.tryAcquire()
  ├─ 成功 → 放行，Key 写入存储并设置 TTL
  └─ 失败 → 根据 response-mode 决定响应方式
  │    ├─ exception → 抛出 RepeatSubmitException
  │    └─ json → RepeatSubmitResponseHandler 写入 JSON 响应
  ▼
业务执行
  ├─ 正常完成 → Key 自然过期
  └─ 异常 → afterCompletion 自动释放 Key
```

---

## 扩展

Guardian 的核心组件均可替换，只需注册自定义 Bean 即可覆盖默认实现。

<details>
<summary><b>自定义 Key 生成策略</b></summary>

继承 `AbstractKeyGenerator`，注册为 Bean：

```java
public class MyKeyGenerator extends AbstractKeyGenerator {

    public MyKeyGenerator(UserContextResolver resolver, KeyEncryptManager encryptManager) {
        super(resolver, encryptManager);
    }

    @Override
    protected String buildKey(RepeatSubmitKey key) {
        // 自定义 Key 拼接逻辑
        return key.getServletUri() + ":" + key.getUserId();
    }
}
```

```java
@Bean
public MyKeyGenerator myKeyGenerator(UserContextResolver resolver, KeyEncryptManager manager) {
    return new MyKeyGenerator(resolver, manager);
}
```

</details>

<details>
<summary><b>自定义加密策略</b></summary>

继承 `AbstractKeyEncrypt`，注册为 Bean：

```java
@Bean
public AbstractKeyEncrypt sha256Encrypt() {
    return new AbstractKeyEncrypt() {
        @Override
        public String encrypt(String key) {
            return DigestUtil.sha256Hex(key);
        }
    };
}
```

</details>

<details>
<summary><b>自定义存储方案</b></summary>

实现 `RepeatSubmitStorage` 接口，注册为 Bean：

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

</details>

<details>
<summary><b>自定义响应处理器</b></summary>

实现 `RepeatSubmitResponseHandler` 接口，注册为 Bean（仅 `response-mode: json` 时生效）：

```java
@Bean
public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
    return (request, response, message) -> {
        response.setContentType("application/json;charset=UTF-8");
        // 适配你的项目统一返回格式
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.error(message)));
    };
}
```

</details>

---

## 模块结构

```
guardian-parent
├── guardian-core                                       # 公共基础模块
├── guardian-example                                    # 示例工程
└── guardian-repeat-submit/                             # 防重复提交功能
    ├── guardian-repeat-submit-core/                    # 防重核心：注解、拦截器、策略、存储接口
    ├── guardian-storage-redis/                         # Redis 存储实现
    └── guardian-repeat-submit-spring-boot-starter/     # Spring Boot 自动配置
```

## 存储方案对比

| | Redis | Local |
|--|-------|-------|
| 分布式 | 支持 | 仅单机 |
| 持久性 | Redis 持久化 | 重启丢失 |
| 推荐场景 | 生产环境 | 开发/单体应用 |
| 额外依赖 | 需要 Redis | 无 |
| 过期机制 | TTL 原子过期 | 惰性检查 |

## 环境要求

- **JDK** 1.8+
- **Spring Boot** 2.7.x
- **Redis** 5.0+（使用 Redis 存储时）

## 开源协议

[Apache License 2.0](LICENSE)
