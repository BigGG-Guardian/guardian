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
<p align="center">防重提交、接口限流、接口幂等、参数自动Trim、慢接口检测、请求链路追踪、IP黑白名单、防重放攻击、接口开关 —— 一个 Starter 搞定 API 请求防护。</p>

<p align="center">
  <a href="https://github.com/BigGG-Guardian/guardian">GitHub</a> ·
  <a href="https://gitee.com/BigGG-Guardian/guardian">Gitee</a> ·
  <a href="https://central.sonatype.com/artifact/io.github.biggg-guardian/guardian-repeat-submit-spring-boot-starter">Maven Central</a>
</p>

---

<p align="center">
  <img src="assets/guardian-mindmap.png" alt="Guardian 功能全景图" width="700">
</p>

---

## 功能一览

| 功能       | Starter                                      | 注解 | YAML | 说明                                           |
|----------|----------------------------------------------|------|------|----------------------------------------------|
| 防重复提交    | `guardian-repeat-submit-spring-boot-starter` | `@RepeatSubmit` | ✅ | 防止用户重复提交表单/请求                                |
| 接口限流     | `guardian-rate-limit-spring-boot-starter`    | `@RateLimit` | ✅ | 滑动窗口 + 令牌桶，双算法可选                             |
| 接口幂等     | `guardian-idempotent-spring-boot-starter`    | `@Idempotent` | — | Token 机制保证接口幂等性，支持结果缓存                       |
| 参数自动Trim | `guardian-auto-trim-spring-boot-starter`     | — | ✅ | 自动去除请求参数首尾空格 + 不可见字符替换                       |
| 慢接口检测    | `guardian-slow-api-spring-boot-starter`      | `@SlowApiThreshold` | ✅ | 慢接口检测 + Top N 统计 + Actuator 端点               |
| 请求链路追踪   | `guardian-trace-spring-boot-starter`         | — | ✅ | 自动生成/透传 TraceId，MDC 日志串联，支持跨线程传递、MQ 链路追踪     |
| IP黑白名单   | `guardian-ip-filter-spring-boot-starter`     | — | ✅ | 全局黑名单 + URL 绑定白名单，支持精确/通配符/CIDR              |
| 防重放攻击    | `guardian-anti-replay-spring-boot-starter`   | — | ✅ | Timestamp + Nonce 双重校验，nonce TTL 与 timestamp 窗口解耦 |
| 接口开关     | `guardian-api-switch-spring-boot-starter`    | — | ✅ | 动态关闭/开启接口                                    |

每个功能独立模块、独立 Starter，**用哪个引哪个，互不依赖**。所有模块的 YAML 配置均支持**配置中心动态刷新**（Nacos / Apollo 等），无需重启即可生效。

---

## 快速开始

### 所有starter模块引用

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-starter-all</artifactId>
    <version>1.7.2</version>
</dependency>
```
><small>特别说明：`请求链路追踪模块`若开启RabbitMq/Kafka/RocketMq请求链路追踪，需额外引入对应的依赖(`guardian-trace-rabbitmq`/`guardian-trace-kafka`/`guardian-trace-rocketmq`)</small>

### 防重复提交

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-repeat-submit-spring-boot-starter</artifactId>
    <version>1.7.2</version>
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
    <version>1.7.2</version>
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

### 接口幂等

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-idempotent-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

**1. 获取 Token：**

```
GET /guardian/idempotent/token?key=order-submit
```

**2. 业务接口携带 Token：**

```java
@Idempotent("order-submit")
@PostMapping("/order/submit")
public Result submitOrder(@RequestBody OrderDTO order) {
    return orderService.submit(order);
}
```

请求头带上 `X-Idempotent-Token: {token}`，首次请求正常处理，重复请求直接拒绝。

### 参数自动Trim

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-auto-trim-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

零配置即可使用，所有请求参数（表单 + JSON Body）自动去除首尾空格。可选配置不可见字符替换：

```yaml
guardian:
  auto-trim:
    exclude-fields:
      - password
      - signature
    character-replacements:
      - from: "\\u200B"
        to: ""
```

### 慢接口检测

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-slow-api-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

零配置即可使用（默认阈值 3000ms），也可通过注解为单个接口自定义阈值：

```java
@SlowApiThreshold(1000)
@GetMapping("/detail")
public Result getDetail(@RequestParam Long id) {
    return detailService.query(id);
}
```

超过阈值自动打印 WARN 日志，通过 `GET /actuator/guardianSlowApi` 查看 Top N 慢接口排行。

### 请求链路追踪

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-trace-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

零配置即可使用，每个请求自动生成 TraceId 并写入 MDC。只需在 logback pattern 中加入 `%X{traceId}`：

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n</pattern>
```

上游服务通过请求头 `X-Trace-Id` 传递 TraceId，下游自动复用，实现跨服务链路串联。支持跨线程传递（`TraceUtils.wrap()` / `TraceTaskDecorator`），`@Async`、`CompletableFuture`、手动线程池场景均可保持 traceId 不丢失。支持 MQ 消息链路追踪（RabbitMQ / Kafka / RocketMQ），发送端自动注入 traceId，消费端通过 AOP 自动提取。

### IP黑白名单

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-ip-filter-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

```yaml
guardian:
  ip-filter:
    enabled: true
    # 全局黑名单（命中直接拒绝所有请求）
    black-list:
      - 192.168.100.100
      - 10.0.0.0/8
    # URL 绑定白名单（仅白名单 IP 可访问指定接口）
    urls:
      - pattern: /admin/**
        white-list:
          - 127.0.0.1
          - 192.168.1.*
```

匹配优先级：**全局黑名单 > URL 绑定白名单 > 放行**。IP 规则支持精确匹配、通配符（`192.168.1.*`）和 CIDR（`10.0.0.0/8`）。

### 防重放攻击

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-anti-replay-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

```yaml
guardian:
  anti-replay:
    enabled: true
    max-age: 60              # timestamp 有效窗口（秒）
    nonce-ttl: 86400         # nonce 存活时间（秒，默认 24h）
    urls:
      - pattern: /api/payment/**
      - pattern: /api/transfer/**
```

客户端请求头携带 `X-Timestamp`（毫秒时间戳）和 `X-Nonce`（UUID），服务端校验时间戳有效性 + Nonce 唯一性，双重防护拦截重放请求。

> **关键设计**：`nonce-ttl` 与 `max-age` 解耦（默认 24h vs 60s），防止攻击者在 Nonce 过期后篡改 Timestamp 重放。搭配 `guardian-sign` 签名模块时，`nonce-ttl` 可缩短至与 `max-age` 相同。

### 接口开关

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-api-switch-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

```yaml
guardian:
  api-switch:
    enabled: true                        # 总开关（默认 true）
    response-mode: json                  # exception / json
    message: "接口暂时关闭，请稍后再试"    # 提示信息（支持 i18n Key）
    log-enabled: true                    # 是否打印拦截日志
    interceptor-order: 500             # 拦截器排序
    disabled-urls:                       # 启动时默认关闭的接口
      - /api-switch/disabled
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
  repeatable-filter-order: -100       # 请求体缓存过滤器排序（全局共享，仅需配置一次）
  repeat-submit:
    storage: redis                    # redis / local
    key-encrypt: md5                  # none / md5
    response-mode: exception          # exception / json
    log-enabled: false
    interceptor-order: 2000           # 拦截器排序（值越小越先执行）
    exclude-urls:
      - /api/public/**
    urls:
      - pattern: /api/order/submit
        interval: 10
        time-unit: seconds
        key-scope: user
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
- **Actuator**：`GET /actuator/guardianRepeatSubmit`

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `top` | 被拦截接口排行返回条数 | 10 |

> 所有参数均为可选，不传参数与原调用方式完全一致。

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

Guardian 的核心组件均可替换，注册同类型 Bean 即可覆盖默认实现。

**自定义用户上下文（所有模块共享）：**

```java
@Bean
public UserContext userContext() {
    // 从你的登录体系中获取当前用户 ID
    return () -> SecurityUtils.getCurrentUserId();
}
```

> 不实现也能用，框架会自动以 SessionId / IP 作为用户标识。

**自定义 Key 生成策略：**

```java
public class MyKeyGenerator extends AbstractKeyGenerator {

    public MyKeyGenerator(UserContext userContext, AbstractKeyEncrypt keyEncrypt) {
        super(userContext, keyEncrypt);
    }

    @Override
    protected String buildKey(RepeatSubmitKey key) {
        return key.getServletUri() + ":" + key.getUserId();
    }
}

@Bean
public MyKeyGenerator myKeyGenerator(UserContext userContext, AbstractKeyEncrypt keyEncrypt) {
    return new MyKeyGenerator(userContext, keyEncrypt);
}
```

**自定义 Key 加密策略：**

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

**自定义存储：**

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

**自定义响应处理器（仅 `response-mode: json` 时生效）：**

```java
@Bean
public RepeatSubmitResponseHandler repeatSubmitResponseHandler() {
    return (request, response, code, data, message) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message)));
    };
}
```

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
    response-mode: exception          # exception / json
    log-enabled: false
    interceptor-order: 1000           # 拦截器排序（值越小越先执行）
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
- **Actuator**：`GET /actuator/guardianRateLimit`

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `blockedTop` | 被拦截接口排行返回条数 | 10 |
| `requestTop` | 高频请求接口排行返回条数 | 10 |
| `detailsTop` | 接口维度明细返回条数 | 10 |

> 所有参数均为可选，不传参数与原调用方式完全一致。

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

注册同类型 Bean 即可覆盖默认实现。

**自定义用户上下文（与防重模块共享）：**

```java
@Bean
public UserContext userContext() {
    return () -> SecurityUtils.getCurrentUserId();
}
```

**自定义限流 Key 生成策略：**

```java
public class MyRateLimitKeyGenerator extends AbstractRateLimitKeyGenerator {

    public MyRateLimitKeyGenerator(UserContext userContext) {
        super(userContext);
    }

    @Override
    protected String buildKey(RateLimitKey key) {
        return key.getServletUri() + ":" + key.getUserId();
    }
}

@Bean
public MyRateLimitKeyGenerator myRateLimitKeyGenerator(UserContext userContext) {
    return new MyRateLimitKeyGenerator(userContext);
}
```

**自定义存储：**

```java
@Bean
public RateLimitStorage myRateLimitStorage() {
    return new RateLimitStorage() {
        @Override
        public boolean tryAcquire(RateLimitToken token) { /* ... */ }
    };
}
```

**自定义响应处理器（仅 `response-mode: json` 时生效）：**

```java
@Bean
public RateLimitResponseHandler rateLimitResponseHandler() {
    return (request, response, code, data, message) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message)));
    };
}
```

</details>

---

## 接口幂等

<details>
<summary><b>展开查看完整文档</b></summary>

### 工作流程

1. 客户端调用 `GET /guardian/idempotent/token?key=order-submit` 获取一次性 Token
2. 客户端携带 Token 发起业务请求（Header 或 Param 方式）
3. 拦截器校验 Token：存在则消费放行，不存在或已消费则拒绝
4. （可选）开启结果缓存后，重复请求返回首次执行结果而非拒绝

### 注解参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `value` | **必填** | 接口唯一标识，用于隔离不同接口的 Token |
| `from` | `HEADER` | Token 来源：`HEADER` / `PARAM`（PARAM 模式依次查找 URL 参数、表单字段、JSON Body） |
| `tokenName` | `X-Idempotent-Token` | Header 名 / URL 参数名 / JSON Body 字段名 |
| `message` | `幂等Token无效或已消费` | 拒绝时的提示信息 |

### 全量配置

```yaml
guardian:
  repeatable-filter-order: -100       # 请求体缓存过滤器排序（全局共享，仅需配置一次）
  idempotent:
    enabled: true                     # 总开关
    storage: redis                    # redis / local
    timeout: 300                      # Token 有效期（默认 300）
    time-unit: seconds                # 有效期单位
    response-mode: exception          # exception / json
    log-enabled: false
    interceptor-order: 3000           # 拦截器排序（值越小越先执行）
    token-endpoint: true              # 是否注册内置 Token 获取接口
    result-cache: false               # 是否启用结果缓存
    missing-token-message: "请求缺少幂等Token"  # 缺少 Token 时的提示（支持 i18n Key）
```

### 结果缓存

开启 `result-cache: true` 后，首次请求的返回值会被缓存，重复请求直接返回缓存结果（而非拒绝）。

### 可观测性

- **拦截日志**：`log-enabled: true`，前缀 `[Guardian-Idempotent]`
- **Actuator**：`GET /actuator/guardianIdempotent`

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `top` | 被拦截接口排行返回条数 | 10 |

> 所有参数均为可选，不传参数与原调用方式完全一致。

```json
{
  "totalRequestCount": 1200,
  "totalPassCount": 1100,
  "totalBlockCount": 100,
  "blockRate": "8.33%",
  "topBlockedApis": {
    "/order/submit": 60,
    "/pay/confirm": 40
  }
}
```

### 扩展点

**自定义 Token 生成器：**

```java
@Bean
public IdempotentTokenGenerator idempotentTokenGenerator() {
    return () -> "custom-" + UUID.randomUUID().toString();
}
```

**自定义存储：**

```java
@Bean
public IdempotentStorage myIdempotentStorage() {
    return new IdempotentStorage() {
        @Override
        public void save(IdempotentToken token) { /* ... */ }
        @Override
        public boolean tryConsume(String tokenKey) { /* ... */ }
    };
}
```

**自定义响应处理器（仅 `response-mode: json` 时生效）：**

```java
@Bean
public IdempotentResponseHandler idempotentResponseHandler() {
    return (request, response, code, data, message) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message)));
    };
}
```

</details>

---

## 参数自动Trim

<details>
<summary><b>展开查看完整文档</b></summary>

### 功能说明

自动去除所有请求参数的首尾空格，同时支持不可见字符替换（如零宽空格、BOM、回车符等从复制粘贴混入的脏字符）。同时作用于表单参数（Query / Form）和 JSON Body。

### 全量配置

```yaml
guardian:
  auto-trim:
    enabled: true                      # 总开关（默认 true）
    filter-order: -10000               # Filter 排序（值越小越先执行）
    exclude-fields:                    # 排除字段（表单 + JSON Body 统一生效）
      - password
      - signature
    character-replacements:            # 字符替换规则（先替换后 trim）
      - from: "\\r"                    # 回车符
        to: ""
      - from: "\\u200B"               # 零宽空格
        to: ""
      - from: "\\uFEFF"               # BOM
        to: ""
```

### 字符替换规则

`character-replacements` 支持以下转义格式：

| 转义写法 | 实际字符 | 说明 |
|---------|---------|------|
| `\\r` | `\r` | 回车符 |
| `\\n` | `\n` | 换行符 |
| `\\t` | `\t` | 制表符 |
| `\\0` | `\0` | 空字符 |
| `\\uXXXX` | Unicode 字符 | 如 `\\u200B` = 零宽空格 |

执行顺序：**先执行字符替换，再执行 trim**。

### 排除字段

密码、签名等不应被 trim 的字段可加入 `exclude-fields`，同时作用于表单参数名和 JSON Body 字段名：

```yaml
guardian:
  auto-trim:
    exclude-fields:
      - password
      - signature
```

</details>

---

## 慢接口检测

<details>
<summary><b>展开查看完整文档</b></summary>

### 功能说明

自动检测响应时间超过阈值的接口，打印 WARN 日志并记录统计数据。支持全局阈值 + 注解覆盖，通过 Actuator 端点查看 Top N 排行。

### 使用方式

**零配置**：引入 Starter 即可使用，默认阈值 3000ms。

**注解自定义阈值**：

```java
@SlowApiThreshold(1000)
@GetMapping("/detail")
public Result getDetail(@RequestParam Long id) {
    return detailService.query(id);
}
```

注解优先级高于全局配置。没有注解的接口使用全局阈值。

### 全量配置

```yaml
guardian:
  slow-api:
    enabled: true                      # 总开关（默认 true）
    threshold: 3000                    # 全局阈值（毫秒，默认 3000）
    interceptor-order: -1000           # 拦截器排序（值越小越先执行）
    exclude-urls:                      # 排除规则（白名单，命中直接放行）
      - /api/health
      - /api/public/**
```

### 可观测性

**日志输出**：超过阈值自动打印 WARN 日志，前缀 `[Guardian-Slow-Api]`：

```
WARN [Guardian-Slow-Api] @SlowApiThreshold 慢接口检测 | Method=GET | URI=/api/detail | 耗时=3521ms | 阈值=3000ms
```

**Actuator 端点**：`GET /actuator/guardianSlowApi`

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `top` | 慢接口排行返回条数 | 10 |
| `recordTop` | 每个接口的耗时明细记录条数 | 5（最大 100） |

> 所有参数均为可选，不传参数与原调用方式完全一致。

```json
{
  "totalSlowCount": 15,
  "topSlowApis": {
    "/api/detail": {
      "count": 8,
      "maxDuration": 5230,
      "recentRecords": [
        { "duration": 5230, "time": "2026-02-09 15:32:10" },
        { "duration": 4100, "time": "2026-02-09 14:20:45" }
      ]
    },
    "/api/export": {
      "count": 7,
      "maxDuration": 12500,
      "recentRecords": [
        { "duration": 12500, "time": "2026-02-09 11:05:22" }
      ]
    }
  }
}
```

### 扩展点

**自定义慢接口记录器（SPI）：**

默认使用内存环形缓冲记录慢接口数据，可通过注入自定义 Bean 替换为数据库 / ES 等持久化方案：

```java
@Bean
public SlowApiRecorder slowApiRecorder() {
    return new SlowApiRecorder() {
        @Override
        public void record(SlowApiRecord record) { /* 写入数据库 / ES */ }
        @Override
        public long getTotalSlowCount() { /* ... */ }
        @Override
        public LinkedHashMap<String, Map<String, Object>> getTopSlowApis(int n) { /* ... */ }
        @Override
        public List<SlowApiRecord> getRecords(String uri, int limit) { /* ... */ }
    };
}
```

</details>

---

## 请求链路追踪

<details>
<summary><b>展开查看完整文档</b></summary>

### 功能说明

自动为每个请求生成唯一的 TraceId，写入 MDC 和响应头。上游服务通过请求头传递 TraceId，下游自动复用，实现跨服务日志串联。内置跨线程传递工具类，`@Async`、`CompletableFuture`、手动线程池等场景均可保持 traceId 不丢失。

### 使用方式

**零配置**：引入 Starter 即可使用。

在 Logback 配置中加入 `%X{traceId}` 即可在日志中打印 TraceId：

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n</pattern>
```

### 工作流程

```
请求进入
  │
  ▼
TraceIdFilter（OncePerRequestFilter）
  ├─ 请求头有 X-Trace-Id → 复用（上游透传）
  └─ 请求头没有 → 自动生成（时分秒 + 10位随机字符串）
  │
  ▼
MDC.put("traceId", traceId)     ← 写入 MDC，日志自动携带
response.setHeader(headerName)  ← 写入响应头，客户端可获取
  │
  ▼
业务执行（同一线程内所有日志都带 traceId）
  │
  ▼
MDC.remove("traceId")           ← 请求结束清理
```

### 跨服务链路串联

上游服务调用下游时，把响应头中的 TraceId 传递到下游请求头即可：

```
服务 A 收到请求 → 生成 traceId=abc123 → 调用服务 B 时带上 X-Trace-Id: abc123
服务 B 收到请求 → 从请求头取出 abc123 → 复用同一个 traceId
```

同一条链路上所有服务的日志都带 `abc123`，排查问题时按 TraceId 搜索即可。

### 跨线程 TraceId 传递

MDC 基于 ThreadLocal，新开子线程时 traceId 会丢失。Guardian 提供以下工具解决：

**1. 手动包装 Runnable / Callable**

```java
executor.submit(TraceUtils.wrap(() -> {
    // 子线程中 traceId 与父线程一致
    log.info("异步任务执行");
}));

Future<String> future = executor.submit(TraceUtils.wrap(() -> {
    log.info("带返回值的异步任务");
    return "result";
}));
```

**2. 包装 Executor（适用于 CompletableFuture）**

```java
Executor tracedExecutor = TraceUtils.wrap(myExecutor);
CompletableFuture.runAsync(() -> {
    log.info("CompletableFuture 中也有 traceId");
}, tracedExecutor);
```

**3. @Async 线程池自动传递**

```java
@Bean
public ThreadPoolTaskExecutor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setTaskDecorator(new TraceTaskDecorator());
    executor.initialize();
    return executor;
}
```

配置 `TraceTaskDecorator` 后，所有 `@Async` 任务自动携带父线程的 traceId，无需手动包装。

| 类名 | 场景 | 用法 |
|------|------|------|
| `TraceRunnable` | 手动提交 Runnable | `new TraceRunnable(task)` 或 `TraceUtils.wrap(task)` |
| `TraceCallable` | 手动提交 Callable | `new TraceCallable<>(task)` 或 `TraceUtils.wrap(task)` |
| `TraceTaskDecorator` | @Async 线程池 | 设置到 `ThreadPoolTaskExecutor.setTaskDecorator()` |
| `TraceUtils` | 工具类 | `wrap(Runnable)` / `wrap(Callable)` / `wrap(Executor)` / `getTraceId()` / `switchTraceId()` |

### MQ 消息链路追踪

Guardian 支持 RabbitMQ、Kafka、RocketMQ 三种消息队列的 TraceId 自动传递，发送端自动注入 traceId，消费端通过 AOP 切面自动提取并写入 MDC。

**核心设计**：消费端采用 AOP 切面拦截 Listener 方法，不占用框架原生拦截器/Hook 位置，接入方可自由注册自己的拦截器。

> **注意**：AOP 切面从方法参数中提取 traceId，因此 Listener 方法的参数类型必须使用消息原始类型：
> - RabbitMQ：`Message`（`org.springframework.amqp.core.Message`），不能用 `String`
> - Kafka：`ConsumerRecord<?, ?>`，不能用 `String`
> - RocketMQ：`MessageExt`（`org.apache.rocketmq.common.message.MessageExt`）

#### RabbitMQ

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-trace-rabbitmq</artifactId>
    <version>1.7.2</version>
</dependency>
```

- **发送端**：通过 `MessagePostProcessor` 将 traceId 写入消息 Header（additive，不冲突）
- **消费端**：AOP 切面拦截 `@RabbitListener` / `@RabbitHandler` 方法，自动从消息 Header 提取 traceId

#### Kafka

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-trace-kafka</artifactId>
    <version>1.7.2</version>
</dependency>
```

发送端需在 Kafka Producer 配置中注册拦截器：

```yaml
spring:
  kafka:
    producer:
      properties:
        interceptor.classes: com.sun.guardian.trace.kafka.interceptor.TraceKafkaProducerInterceptor
```

- **发送端**：通过 `ProducerInterceptor` 将 traceId 写入消息 Header（不占用 RecordInterceptor）
- **消费端**：AOP 切面拦截 `@KafkaListener` / `@KafkaHandler` 方法，自动从消息 Header 提取 traceId

#### RocketMQ

```xml
<dependency>
    <groupId>io.github.biggg-guardian</groupId>
    <artifactId>guardian-trace-rocketmq</artifactId>
    <version>1.7.2</version>
</dependency>
```

- **发送端**：通过 `SendMessageHook` 将 traceId 写入消息 UserProperty（支持注册多个 Hook，不冲突）
- **消费端**：AOP 切面拦截 `@RocketMQMessageListener` 注解的类，自动从消息 UserProperty 提取 traceId

#### 批量消费场景

AOP 切面自动设置**第一条**消息的 traceId。如需在批量消费中逐条切换 traceId，各模块提供了专用工具类，一行代码即可完成切换：

| 模块 | 工具类 | 用法 |
|------|--------|------|
| RabbitMQ | `TraceRabbitUtils` | `TraceRabbitUtils.switchTraceId(message)` |
| Kafka | `TraceKafkaUtils` | `TraceKafkaUtils.switchTraceId(record)` |
| RocketMQ | `TraceRocketMQUtils` | `TraceRocketMQUtils.switchTraceId(messageExt)` |

工具类内部自动从消息 Header / UserProperty 中提取 traceId 并切换 MDC，headerName 通过 `TraceConfig` 动态获取，支持配置中心热更新。

**RabbitMQ 批量消费示例**：

```java
@RabbitListener(queues = "myQueue", containerFactory = "batchContainerFactory")
public void handleBatch(List<Message> messages) {
    for (Message msg : messages) {
        TraceRabbitUtils.switchTraceId(msg);
        // 业务逻辑...
    }
}
```

**Kafka 批量消费示例**：

```java
@KafkaListener(topics = "myTopic", batch = "true")
public void handleBatch(List<ConsumerRecord<String, String>> records) {
    for (ConsumerRecord<String, String> record : records) {
        TraceKafkaUtils.switchTraceId(record);
        // 业务逻辑...
    }
}
```

> **RocketMQ 说明**：RocketMQ Spring Boot Starter 的 `RocketMQListener` 本身是逐条回调，AOP 切面会自动为每次调用注入/清理 traceId，通常无需手动切换。如有特殊场景，也可使用 `TraceRocketMQUtils.switchTraceId(messageExt)`。

### 全量配置

```yaml
guardian:
  trace:
    enabled: true                      # 总开关（默认 true）
    filter-order: -30000               # Filter 排序（值越小越先执行，确保最先执行）
    header-name: X-Trace-Id            # 请求头/响应头名称（默认 X-Trace-Id）
```

</details>

---

## IP黑白名单

<details>
<summary><b>展开查看完整文档</b></summary>

### 功能说明

基于 IP 的访问控制，支持两种模式：

- **全局黑名单**：匹配的 IP 拒绝访问所有接口
- **URL 绑定白名单**：指定接口仅允许白名单 IP 访问，其余 IP 拒绝

匹配优先级：**全局黑名单 > URL 绑定白名单 > 放行**

IP 规则支持三种格式：

| 格式 | 示例 | 说明 |
|------|------|------|
| 精确匹配 | `192.168.1.100` | 精确匹配单个 IP |
| 通配符 | `192.168.1.*` | 匹配整个网段 |
| CIDR | `10.0.0.0/8` | CIDR 网段匹配 |

### 全量配置

```yaml
guardian:
  ip-filter:
    enabled: true                       # 总开关（默认 false，需显式开启）
    response-mode: json                 # exception / json
    log-enabled: true                   # 是否打印拦截日志（默认 false）
    message: "IP 访问被拒绝"              # 拒绝提示信息（支持 i18n Key）
    filter-order: -20000                # Filter 排序（值越小越先执行）
    black-list:                         # 全局 IP 黑名单
      - 192.168.100.100
      - 10.0.0.0/8
    urls:                               # URL 绑定白名单
      - pattern: /admin/**
        white-list:
          - 127.0.0.1
          - 192.168.1.*
      - pattern: /internal/api/**
        white-list:
          - 10.0.0.0/8
```

### 可观测性

- **拦截日志**：`log-enabled: true`，前缀 `[Guardian-Ip-Filter]`
- **Actuator**：`GET /actuator/guardianIpFilter`

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `blockTop` | 黑名单拦截 IP 排行返回条数 | 10 |
| `whiteTop` | 白名单拦截请求排行返回条数 | 10 |

> 所有参数均为可选，不传参数与原调用方式完全一致。

```json
{
  "totalBlackListBlockCount": 56,
  "totalWhiteListBlockCount": 23,
  "topBlackListBlocked": {
    "192.168.100.100": 42,
    "10.1.2.3": 14
  },
  "topWhiteListBlocked": {
    "/admin/dashboard | 172.16.0.5": 15,
    "/internal/api/config | 192.168.2.1": 8
  }
}
```

### 扩展点

**自定义响应处理器（仅 `response-mode: json` 时生效）：**

```java
@Bean
public IpFilterResponseHandler ipFilterResponseHandler() {
    return (request, response, code, data, message) -> {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message)));
    };
}
```

</details>

---

## 防重放攻击

<details>
<summary><b>展开查看完整文档</b></summary>

### 功能说明

防止攻击者截获合法请求后重复发送（Replay Attack）。客户端在请求头中携带时间戳和一次性随机标识（Nonce），服务端进行双重校验：

1. **Timestamp 校验**：请求时间戳与服务器时间差超过 `max-age` 则拒绝（请求过期）
2. **Nonce 校验**：Nonce 已存在于存储中则拒绝（重放攻击），不存在则记录并放行

> **安全设计**：Nonce 的存活时间（`nonce-ttl`，默认 24h）远大于 Timestamp 有效窗口（`max-age`，默认 60s）。二者解耦可防止攻击者在 Nonce 过期后篡改 Timestamp 重放请求。搭配 `guardian-sign` 签名模块使用时，签名防止 Timestamp 被篡改，`nonce-ttl` 可缩短至与 `max-age` 相同。

### 校验流程

```
请求进入 AntiReplayFilter
  │
  ├─ 1. 排除规则匹配（exclude-urls）→ 命中直接放行
  │
  ├─ 2. URL 规则匹配（urls）→ 未命中则放行
  │
  ├─ 3. 取 X-Timestamp，判断与服务器时间差
  │      ↓ 超过 maxAge → 拒绝（请求过期）
  │
  ├─ 4. 取 X-Nonce，判断是否已使用
  │      ↓ Storage 中已存在 → 拒绝（重放攻击）
  │
  ├─ 5. 校验通过
  │      将 Nonce 存入 Storage（TTL = nonceTtl）
  │      放行
  ▼
```

### 客户端请求示例

```
POST /api/payment/submit HTTP/1.1
X-Timestamp: 1708000000000
X-Nonce: a1b2c3d4e5f6g7h8
Content-Type: application/json

{"orderId": "ORD001", "amount": 99.99}
```

### 全量配置

```yaml
guardian:
  anti-replay:
    enabled: true                        # 总开关（默认 true）
    storage: redis                       # redis / local
    max-age: 60                          # timestamp 有效窗口（默认 60）
    max-age-unit: seconds                # timestamp 有效窗口单位（默认秒）
    nonce-ttl: 86400                     # nonce 存活时间（默认 86400 = 24h，必须 >= max-age）
    nonce-ttl-unit: seconds              # nonce 存活时间单位（默认秒）
    timestamp-header: X-Timestamp        # 时间戳请求头名称
    nonce-header: X-Nonce                # Nonce 请求头名称
    response-mode: json                  # exception / json
    log-enabled: true                    # 是否打印拦截日志
    filter-order: -14000                 # Filter 排序
    missing-timestamp-message: "缺少时间戳"   # 提示信息（支持 i18n Key）
    missing-nonce-message: "缺少请求标识"
    expired-message: "请求已过期"
    replay-message: "重复请求"
    urls:                                # 需要防重放的 URL（AntPath）
      - pattern: /api/payment/**
      - pattern: /api/transfer/**
    exclude-urls:                        # 排除规则（白名单，优先级最高）
      - /api/public/**
```

### 可观测性

- **拦截日志**：`log-enabled: true`，前缀 `[Guardian-Anti-Replay]`
- **Actuator**：`GET /actuator/guardianAntiReplay`

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `top` | 被拦截接口排行返回条数 | 10 |

> 所有参数均为可选，不传参数与原调用方式完全一致。

```json
{
  "totalRequestCount": 5000,
  "totalPassCount": 4980,
  "totalBlockCount": 20,
  "blockRate": "0.40%",
  "topBlockedApis": {
    "/api/payment/submit": 12,
    "/api/transfer/confirm": 8
  }
}
```

### 扩展点

**自定义 Nonce 存储：**

```java
@Bean
public NonceStorage nonceStorage() {
    return new NonceStorage() {
        @Override
        public boolean tryAcquire(String nonce, long nonceTtl, TimeUnit nonceTtlUnit) {
            // 自定义存储逻辑（如 Memcached、数据库等）
        }
    };
}
```

**自定义响应处理器（仅 `response-mode: json` 时生效）：**

```java
@Bean
public AntiReplayResponseHandler antiReplayResponseHandler() {
    return (request, response, code, data, message) -> {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message)));
    };
}
```

</details>

---

## 接口开关

<details>
<summary><b>展开查看完整文档</b></summary>

### 功能说明

基于yml/actuator实现接口的关闭/开启控制，接口链接支持AntPath

### 全量配置

```yaml
guardian:
  api-switch:
    enabled: true                        # 总开关（默认 true）
    response-mode: json                  # exception / json
    message: "接口暂时关闭，请稍后再试"    # 提示信息（支持 i18n Key）
    log-enabled: true                    # 是否打印拦截日志
    interceptor-order: 500               # 拦截器排序
    disabled-urls:                       # 启动时默认关闭的接口
      - /api-switch/disabled
```

### 可观测性

- **拦截日志**：`log-enabled: true`，前缀 `[Guardian-Api-Switch]`
- **Actuator**：`GET /actuator/guardianApiSwitch`

```json
{
  "disabledUrls": [
    "/api-switch/disabled"
  ]
}
```

### 关闭接口

- **Actuator**：`POST /actuator/guardianApiSwitch/{urlPattern}`
><small>`关闭接口`需将接口内`{urlPattern}`替换为实际接口地址，例如：/actuator/guardianApiSwitch/api-switch/disabled </small>

```json
{
  "action": "disabled",
  "urlPattern": "/api-switch/disabled",
  "disabledUrls": [
    "/123",
    "/api-switch/disabled"
  ]
}
```

### 开启接口

- **Actuator**：`DELETE /actuator/guardianApiSwitch/{urlPattern}`
><small>`开启接口`需将接口内`{urlPattern}`替换为实际接口地址，例如：/actuator/guardianApiSwitch/api-switch/disabled </small>

```json
{
  "action": "enabled",
  "urlPattern": "/api-switch/disabled",
  "disabledUrls": [
    "/123"
  ]
}
```

### 扩展点

**自定义响应处理器（仅 `response-mode: json` 时生效）：**

```java
@Bean
public ApiSwitchResponseHandler apiSwitchResponseHandler() {
    return (request, response, code, data, message) -> {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(CommonResult.result(code, data, message)));
    };
}
```

</details>

---

## 规则优先级

Guardian 各模块（防重复提交、接口限流、慢接口检测）的规则匹配遵循以下优先级：

```
exclude-urls（白名单）> YAML 规则 > 注解 > 放行
```

| 场景 | 行为 |
|------|------|
| URL 命中 `exclude-urls` | **直接放行**，跳过所有检测（包括注解） |
| URL 命中 YAML `urls` 规则 | YAML 规则生效 |
| 方法有注解 `@RateLimit` / `@RepeatSubmit` / `@SlowApiThreshold` | 注解规则生效 |
| 以上均未命中 | 放行 |

> **设计理念**：`exclude-urls` 作为白名单拥有最高优先级，可在紧急情况下通过配置中心动态添加 URL 实现"一键放行"，无需改代码重启。注解适合"长期固定"的保护策略，YAML 规则适合"动态可调"的批量策略。

---

## 动态配置

Guardian 所有模块的 YAML 配置均支持通过配置中心（Nacos、Apollo 等）动态刷新，**无需重启应用**即可生效。

### 支持动态刷新的配置项

以下列出所有模块可在配置中心动态修改的完整参数（`enabled`、`storage` 等启动期参数修改后需重启生效）。

**防重复提交**（prefix: `guardian.repeat-submit`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `response-mode` | `exception` / `json` | `exception` | 响应模式 |
| `log-enabled` | `boolean` | `false` | 是否打印拦截日志 |
| `exclude-urls` | `List<String>` | `[]` | 排除规则（白名单，优先级最高，AntPath） |
| `urls` | `List` | `[]` | 防重规则列表，每项参数如下 |
| `urls[].pattern` | `String` | — | 接口路径（AntPath） |
| `urls[].interval` | `int` | `5` | 防重间隔 |
| `urls[].time-unit` | `TimeUnit` | `seconds` | 间隔时间单位 |
| `urls[].key-scope` | `user` / `ip` / `global` | `user` | 防重维度 |
| `urls[].message` | `String` | `您的请求过于频繁，请稍后再试` | 拦截提示信息 |
| `urls[].client-type` | `pc` / `app` | `pc` | 客户端类型 |

**接口限流**（prefix: `guardian.rate-limit`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `response-mode` | `exception` / `json` | `exception` | 响应模式 |
| `log-enabled` | `boolean` | `false` | 是否打印拦截日志 |
| `exclude-urls` | `List<String>` | `[]` | 排除规则（白名单，优先级最高，AntPath） |
| `urls` | `List` | `[]` | 限流规则列表，每项参数如下 |
| `urls[].pattern` | `String` | — | 接口路径（AntPath） |
| `urls[].qps` | `int` | `10` | 滑动窗口=QPS，令牌桶=每 window 补充令牌数 |
| `urls[].window` | `int` | `1` | 滑动窗口=窗口跨度，令牌桶=补充周期 |
| `urls[].window-unit` | `TimeUnit` | `seconds` | 时间单位 |
| `urls[].algorithm` | `sliding_window` / `token_bucket` | `sliding_window` | 限流算法 |
| `urls[].capacity` | `int` | `-1` | 令牌桶容量（≤0 时取 qps 值） |
| `urls[].rate-limit-scope` | `global` / `ip` / `user` | `global` | 限流维度 |
| `urls[].message` | `String` | `请求过于频繁，请稍后再试` | 拦截提示信息 |

**接口幂等**（prefix: `guardian.idempotent`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `timeout` | `long` | `300` | Token 有效期 |
| `time-unit` | `TimeUnit` | `seconds` | 有效期单位 |
| `response-mode` | `exception` / `json` | `exception` | 响应模式 |
| `log-enabled` | `boolean` | `false` | 是否打印拦截日志 |

**参数自动Trim**（prefix: `guardian.auto-trim`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `exclude-fields` | `Set<String>` | `[]` | 排除字段（不做 trim 的字段名） |
| `character-replacements` | `List` | `[]` | 字符替换规则列表，每项参数如下 |
| `character-replacements[].from` | `String` | — | 待替换字符（支持 `\\r` `\\n` `\\t` `\\uXXXX` 转义） |
| `character-replacements[].to` | `String` | `""` | 替换为 |

**慢接口检测**（prefix: `guardian.slow-api`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `threshold` | `long` | `3000` | 全局慢接口阈值（毫秒） |
| `exclude-urls` | `List<String>` | `[]` | 排除规则（白名单，优先级最高，AntPath） |

**请求链路追踪**（prefix: `guardian.trace`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `header-name` | `String` | `X-Trace-Id` | 请求头/响应头名称（同时影响 MQ 链路追踪的 Header Key） |

**防重放攻击**（prefix: `guardian.anti-replay`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `max-age` | `long` | `60` | timestamp 有效窗口 |
| `max-age-unit` | `TimeUnit` | `seconds` | timestamp 有效窗口单位 |
| `nonce-ttl` | `long` | `86400` | nonce 存活时间（必须 >= max-age） |
| `nonce-ttl-unit` | `TimeUnit` | `seconds` | nonce 存活时间单位 |
| `response-mode` | `exception` / `json` | `exception` | 响应模式 |
| `log-enabled` | `boolean` | `false` | 是否打印拦截日志 |
| `missing-timestamp-message` | `String` | `缺少时间戳` | 缺少时间戳提示（支持 i18n Key） |
| `missing-nonce-message` | `String` | `缺少请求标识` | 缺少 Nonce 提示（支持 i18n Key） |
| `expired-message` | `String` | `请求已过期` | 请求过期提示（支持 i18n Key） |
| `replay-message` | `String` | `重复请求` | 重放攻击提示（支持 i18n Key） |
| `urls` | `List` | `[]` | 防重放 URL 规则列表（AntPath） |
| `exclude-urls` | `List<String>` | `[]` | 排除规则（白名单，优先级最高） |

**IP黑白名单**（prefix: `guardian.ip-filter`）

| YAML Key | 类型 | 默认值 | 说明 |
|----------|------|--------|------|
| `response-mode` | `exception` / `json` | `exception` | 响应模式 |
| `log-enabled` | `boolean` | `false` | 是否打印拦截日志 |
| `message` | `String` | `IP 访问被拒绝` | 拒绝提示信息（支持 i18n Key） |
| `black-list` | `List<String>` | `[]` | 全局 IP 黑名单（精确 / 通配符 / CIDR） |
| `urls` | `List` | `[]` | URL 绑定白名单规则列表，每项参数如下 |
| `urls[].pattern` | `String` | — | 接口路径（AntPath） |
| `urls[].white-list` | `List<String>` | `[]` | 允许访问的 IP 列表（精确 / 通配符 / CIDR） |

**接口开关**（prefix: `guardian.api-switch`）

| YAML Key | 类型 | 默认值 | 说明                 |
|----------|------|--------|--------------------|
| `response-mode` | `exception` / `json` | `exception` | 响应模式               |
| `log-enabled` | `boolean` | `false` | 是否打印拦截日志           |
| `message` | `String` | `IP 访问被拒绝` | 提示信息（支持 i18n Key）  |
| `disabled-urls` | `List<String>` | `[]` | 默认关闭的接口路径（AntPath） |

### 使用方式

以 Nacos 为例，引入 Spring Cloud Alibaba Nacos Config 依赖后，在 Nacos 控制台修改 Guardian 相关配置并发布，应用会自动感知变更并即时生效。

**1. 添加依赖（以 Spring Boot 2.7.x 为例）：**

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

**2. 配置 `bootstrap.yml`：**

```yaml
spring:
  application:
    name: your-app
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
```

**3. 在 Nacos 控制台修改配置：**

在对应的 Data ID（如 `your-app.yml`）中修改 Guardian 配置并发布，发布后即时生效，无需重启。以下是覆盖全模块的 Nacos 配置示例：

```yaml
guardian:
  repeat-submit:
    response-mode: exception
    log-enabled: false
    exclude-urls:
      - /api/public/**
    urls:
      - pattern: /api/order/**
        interval: 10
        time-unit: seconds
        key-scope: user
        message: "请勿重复提交"

  rate-limit:
    response-mode: exception
    log-enabled: false
    exclude-urls:
      - /api/public/**
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

  idempotent:
    timeout: 300
    time-unit: seconds
    response-mode: exception
    log-enabled: false
    missing-token-message: guardian.idempotent.missing-token

  auto-trim:
    exclude-fields:
      - password
      - signature
    character-replacements:
      - from: "\\u200B"
        to: ""
      - from: "\\uFEFF"
        to: ""

  slow-api:
    threshold: 3000
    exclude-urls:
      - /api/health

  trace:
    header-name: X-Trace-Id

  ip-filter:
    enabled: true
    response-mode: json
    log-enabled: true
    message: "IP 访问被拒绝"
    black-list:
      - 192.168.100.100
    urls:
      - pattern: /admin/**
        white-list:
          - 127.0.0.1
          - 192.168.1.*

  anti-replay:
    max-age: 60
    nonce-ttl: 86400
    response-mode: json
    log-enabled: true
    missing-timestamp-message: "缺少时间戳"
    missing-nonce-message: "缺少请求标识"
    expired-message: "请求已过期"
    replay-message: "重复请求"
    urls:
      - pattern: /api/payment/**
      - pattern: /api/transfer/**
    exclude-urls:
      - /api/public/**

  api-switch:
    response-mode: json
    message: "接口暂时关闭，请稍后再试"
    log-enabled: true
    disabled-urls:
      - /api-switch/disabled
```

> 只需配置你用到的模块，没用到的模块无需配置。修改任意参数后点击发布，下一次请求即可读取到最新值。

### 实现原理

Guardian 的 `@ConfigurationProperties` 属性类实现了模块配置接口（如 `RepeatSubmitConfig`、`RateLimitConfig` 等），拦截器/过滤器通过接口引用动态读取配置值。当配置中心推送变更时，Spring Cloud 的 `ConfigurationPropertiesRebinder` 自动重新绑定属性，所有引用该配置的组件在下次请求时即可读取到最新值。

---

## 消息国际化

Guardian 的拒绝响应消息（防重、限流、幂等）支持 Spring 标准的 `MessageSource` 国际化机制。**不使用国际化的用户无需任何改动**，message 配置中文纯文本即可，行为与之前完全一致。

### 工作原理

message 字段同时支持**纯文本**和 **i18n Key** 两种写法：

```yaml
# 纯文本（默认，不走国际化）
message: "请求过于频繁，请稍后再试"

# i18n Key（自动走 MessageSource 解析）
message: guardian.rate-limit.rejected
```

Guardian 通过 `GuardianMessageResolver` 统一解析：尝试从 `MessageSource` 查找，找到则返回对应语言的翻译，找不到则原样返回。语言由请求头 `Accept-Language` 自动决定。

### 使用方式

**第一步**，把 message 改成 i18n Key。

**YAML 规则示例：**

```yaml
guardian:
  rate-limit:
    urls:
      - pattern: /api/sms/send
        qps: 1
        message: guardian.rate-limit.rejected
  repeat-submit:
    urls:
      - pattern: /api/order/submit
        interval: 10
        message: guardian.repeat-submit.rejected
  idempotent:
    missing-token-message: guardian.idempotent.missing-token
```

**注解示例：**

```java
@RateLimit(qps = 5, message = "guardian.rate-limit.rejected")
@RepeatSubmit(interval = 10, message = "guardian.repeat-submit.rejected")
@Idempotent(value = "createOrder", message = "guardian.idempotent.rejected")
```

**第二步**，在项目中添加多语言消息文件。

> **重要**：必须创建基础文件 `messages.properties`，Spring Boot 的 `MessageSourceAutoConfiguration` 需要检测到该文件才会激活 `ResourceBundleMessageSource`，否则国际化不生效。

```properties
# src/main/resources/messages.properties（必须，作为默认回退）
guardian.rate-limit.rejected=请求过于频繁，请稍后再试
guardian.repeat-submit.rejected=您的请求过于频繁，请稍后再试
guardian.idempotent.rejected=幂等Token无效或已消费
guardian.idempotent.missing-token=请求缺少幂等Token
```

```properties
# src/main/resources/messages_zh_CN.properties
guardian.rate-limit.rejected=请求过于频繁，请稍后再试
guardian.repeat-submit.rejected=您的请求过于频繁，请稍后再试
guardian.idempotent.rejected=幂等Token无效或已消费
guardian.idempotent.missing-token=请求缺少幂等Token
```

```properties
# src/main/resources/messages_en.properties
guardian.rate-limit.rejected=Rate limit exceeded, please try again later
guardian.repeat-submit.rejected=Too many requests, please try again later
guardian.idempotent.rejected=Idempotent token is invalid or already consumed
guardian.idempotent.missing-token=Missing idempotent token
```

**第三步**，通过请求头 `Accept-Language` 切换语言：

| 请求头 | 匹配文件 | 效果 |
|-------|---------|------|
| `Accept-Language: zh-CN` | `messages_zh_CN.properties` | 中文 |
| `Accept-Language: en` | `messages_en.properties` | 英文 |
| 不传 | `messages.properties` | 默认回退 |

Spring Boot 自动根据 `Accept-Language` 匹配语言，无需额外配置。

> 如果项目使用自定义消息文件路径（如 `i18n/messages`），只需配置 `spring.messages.basename=i18n/messages`，Guardian 自动适配。同样需要确保基础文件 `i18n/messages.properties` 存在。

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
├── guardian-idempotent/                   # 接口幂等
│   ├── guardian-idempotent-core/
│   └── guardian-idempotent-spring-boot-starter/
├── guardian-auto-trim/                    # 参数自动Trim
│   ├── guardian-auto-trim-core/
│   └── guardian-auto-trim-spring-boot-starter/
├── guardian-slow-api/                     # 慢接口检测
│   ├── guardian-slow-api-core/
│   └── guardian-slow-api-spring-boot-starter/
├── guardian-trace/                        # 请求链路追踪
│   ├── guardian-trace-core/
│   ├── guardian-trace-spring-boot-starter/
│   ├── guardian-trace-rabbitmq/           # RabbitMQ 链路追踪
│   ├── guardian-trace-kafka/              # Kafka 链路追踪
│   └── guardian-trace-rocketmq/           # RocketMQ 链路追踪
├── guardian-ip-filter/                    # IP黑白名单
│   ├── guardian-ip-filter-core/
│   └── guardian-ip-filter-spring-boot-starter/
├── guardian-anti-replay/                  # 防重放攻击
│   ├── guardian-anti-replay-core/
│   └── guardian-anti-replay-spring-boot-starter/
├── guardian-api-switch/                  # 接口开关
│   ├── guardian-api-switch-core/
│   └── guardian-api-switch-spring-boot-starter/
├── guardian-starter-all/                  # 整合引用所有Starter模块
├── guardian-storage-redis/                # Redis 存储（多模块共享）
└── guardian-example/                      # 示例工程
```

### guardian-core 共享类

| 类 | 作用 |
|----|------|
| `GuardianCoreProperties` | 全局共享配置（repeatableFilterOrder），prefix = `guardian` |
| `BaseGuardianProperties` | 模块配置基类（storage / responseMode / logEnabled / interceptorOrder） |
| `UserContext` | 用户上下文接口，实现一次所有模块共享 |
| `GuardianResponseHandler` | 统一响应处理器接口 |
| `DefaultGuardianResponseHandler` | 默认 JSON 响应实现 |
| `BaseStatistics` | 拦截统计基类 |
| `GuardianLogUtils` | 参数化日志工具 |
| `IpMatcher` | IP 匹配工具（精确 / 通配符 / CIDR） |
| `RepeatableRequestFilter` | 请求体缓存过滤器 |

### 执行顺序

Guardian 的 Filter 和 Interceptor 通过 `order` 值控制执行优先级，**值越小越先执行**。

#### Filter 执行顺序

Filter 在 Servlet 层执行，先于所有 Interceptor：

| 顺序 | 模块 | 配置项 | 默认 order | 说明 |
|------|------|--------|-----------|------|
| 1 | 请求链路追踪 | `guardian.trace.filter-order` | **-30000** | 最先执行，为后续所有操作提供 TraceId |
| 2 | IP 黑白名单 | `guardian.ip-filter.filter-order` | **-20000** | 拦截恶意 IP，尽早阻断 |
| 3 | 防重放攻击 | `guardian.anti-replay.filter-order` | **-14000** | 校验 Timestamp + Nonce，拦截重放请求 |
| 4 | 参数自动 Trim | `guardian.auto-trim.filter-order` | **-10000** | 参数预处理，在业务逻辑前清洗数据 |
| 5 | 请求体缓存 | `guardian.repeatable-filter-order` | **-100** | 缓存请求体供防重 / 幂等模块重复读取 |

#### Interceptor 执行顺序

Interceptor 在 Spring MVC 层执行，Filter 之后：

| 顺序 | 模块    | 配置项 | 默认 order  | 说明                          |
|----|-------|--------|-----------|-----------------------------|
| 1  | 慢接口检测 | `guardian.slow-api.interceptor-order` | **-1000** | 最先进入最后退出，精确计算整体耗时           |
| 2  | 接口开关  | `guardian.api-switch.interceptor-order` | **500**   | 接口关闭直接拒绝，避免后续无意义计算          |
| 3  | 接口限流  | `guardian.rate-limit.interceptor-order` | **1000**  | 流量超限直接拒绝，避免后续无意义计算          |
| 4  | 防重复提交 | `guardian.repeat-submit.interceptor-order` | **2000**  | 通过限流后，判断是否短时间重复请求           |
| 5  | 接口幂等  | `guardian.idempotent.interceptor-order` | **3000**  | 最后执行，消费 Token 不可逆，确保前面校验都通过 |

每个模块的 order 均可通过 YAML 自定义，方便与项目中其他拦截器协调：

```yaml
guardian:
  # Filter 排序
  repeatable-filter-order: -100          # 请求体缓存（全局共享）
  trace:
    filter-order: -30000                 # 链路追踪
  ip-filter:
    filter-order: -20000                 # IP 黑白名单
  anti-replay:
    filter-order: -14000                 # 防重放攻击
  auto-trim:
    filter-order: -10000                 # 参数自动 Trim

  # Interceptor 排序
  slow-api:
    interceptor-order: -1000             # 慢接口检测
  api-switch:
    interceptor-order: 500               # 接口开关
  rate-limit:
    interceptor-order: 1000              # 接口限流
  repeat-submit:
    interceptor-order: 2000              # 防重复提交
  idempotent:
    interceptor-order: 3000              # 接口幂等
```

## 存储对比

| | Redis | Local |
|--|-------|-------|
| 分布式 | 支持 | 仅单机 |
| 持久性 | Redis 持久化 | 重启丢失 |
| 推荐场景 | 生产环境 | 开发/单体应用 |
| 额外依赖 | 需要 Redis | 无 |

## 更新日志

### v1.7.2

- **新增**：接口开关模块（`guardian-api-switch`），动态关闭/开启接口

### v1.7.1

- **新增**：整合所有starter模块（`guardian-starter-all`），以便于引用所有功能时只需引用此模块
- **优化**：共享类重新分类分包

### v1.7.0

- **新增**：防重放攻击模块（`guardian-anti-replay-spring-boot-starter`），通过 Timestamp + Nonce 双重校验拦截重放请求
- **新增**：`NonceStorage` 存储接口，支持 Redis（分布式）和 Local（单机）两种实现
- **新增**：`nonce-ttl` 与 `max-age` 解耦设计，Nonce 存活时间（默认 24h）远大于 Timestamp 有效窗口（默认 60s），防止攻击者在 Nonce 过期后篡改 Timestamp 重放
- **新增**：`nonce-ttl-unit` / `max-age-unit` 支持自定义时间单位，`validate()` 方法统一换算后比较
- **新增**：防重放攻击 Actuator 监控端点（`GET /actuator/guardianAntiReplay`），展示拦截统计和 Top N 被拦截接口
- **新增**：防重放攻击拦截日志（`log-enabled: true`，前缀 `[Guardian-Anti-Replay]`）
- **新增**：防重放攻击 URL 规则 + 排除规则（`urls` + `exclude-urls`），支持 AntPath 匹配
- **新增**：`AntiReplayResponseHandler` SPI 扩展点，支持自定义拒绝响应格式
- **新增**：全部拒绝提示信息支持 i18n Key（`missing-timestamp-message` / `missing-nonce-message` / `expired-message` / `replay-message`）

### v1.6.2

- **新增**：MQ 消息链路追踪，支持 RabbitMQ、Kafka、RocketMQ 三种消息队列的 TraceId 自动传递
- **新增**：`guardian-trace-rabbitmq` 模块，发送端通过 MessagePostProcessor 注入 traceId，消费端通过 AOP 切面拦截 `@RabbitListener` 自动提取
- **新增**：`guardian-trace-kafka` 模块，发送端通过 ProducerInterceptor 注入 traceId，消费端通过 AOP 切面拦截 `@KafkaListener` 自动提取
- **新增**：`guardian-trace-rocketmq` 模块，发送端通过 SendMessageHook 注入 traceId，消费端通过 AOP 切面拦截 `@RocketMQMessageListener` 自动提取
- **新增**：`TraceUtils.switchTraceId()` 方法，支持 MQ 批量消费场景逐条切换 traceId
- **新增**：`TraceRabbitUtils` / `TraceKafkaUtils` / `TraceRocketMQUtils` 批量消费专用工具类，一行代码从消息中提取并切换 traceId
- **新增**：慢接口记录器 SPI 扩展点（`SlowApiRecorder`），支持自定义持久化方案（数据库 / ES 等）替换默认内存实现
- **优化**：MQ 消费端统一采用 AOP 切面拦截，不占用框架原生拦截器/Hook 位置，与用户自定义拦截器互不冲突
- **优化**：Kafka ProducerInterceptor 通过持有 TraceConfig 引用动态获取 headerName，支持配置中心热更新
- **修复**：MQ 自动配置类添加 `@AutoConfigureAfter`，确保在 `TraceConfig` 和 MQ Template Bean 创建之后加载，解决因加载顺序导致的链路追踪不生效问题

### v1.6.1

- **重构**：移除 `hutool-all` 依赖，全面切换 Spring/JDK/Jackson 原生工具，项目零外部工具包依赖
- **新增**：`GuardianJsonUtils`（基于 Jackson）、`IpUtils`、`Md5Utils` 三个核心工具类
- **修复**：`jackson-databind` 作用域由 `provided` 改为 `compile`，解决下游模块编译失败

### v1.6.0

- **新增**：IP 黑白名单模块（`guardian-ip-filter-spring-boot-starter`），支持全局 IP 黑名单 + URL 绑定白名单
- **新增**：IP 规则匹配支持精确匹配、通配符（`192.168.1.*`）和 CIDR（`10.0.0.0/8`）三种格式
- **新增**：IP 黑白名单拦截统计 + Actuator 端点（`GET /actuator/guardianIpFilter`）
- **新增**：`IpMatcher` 工具类，统一处理 IP 规则匹配逻辑
- **新增**：IP 黑白名单拦截日志（`log-enabled: true`，前缀 `[Guardian-Ip-Filter]`）

### v1.5.3

- **新增**：消息国际化支持，拒绝响应消息（防重、限流、幂等）支持 Spring `MessageSource` 国际化，message 字段可配置 i18n Key，根据 `Accept-Language` 自动匹配语言
- **新增**：`GuardianMessageResolver` 消息解析工具，MessageSource 能解析则返回翻译，否则原样返回，不使用国际化的用户零感知
- **新增**：幂等模块 `missing-token-message` 配置项，缺少 Token 时的提示信息支持自定义及国际化

### v1.5.2

- **优化**：`GuardianRateLimitProperties` / `GuardianRepeatSubmitProperties` 删除多余无用参数

### v1.5.1

- **新增**：全模块配置动态刷新支持，配合 Nacos / Apollo 等配置中心可在不重启应用的情况下实时更新 Guardian 配置
- **新增**：`BaseConfig` / `BaseCharacterReplacement` 配置接口，拦截器/过滤器通过接口引用动态读取配置，与 Spring Cloud `ConfigurationPropertiesRebinder` 无缝集成
- **优化**：`CharacterSanitizer` 引入基于哈希值的缓存机制，配置未变更时零解析开销，配置动态刷新后自动重建
- **修复**：`DefaultIdempotentTokenService` 改为持有 `IdempotentConfig` 接口引用，修复 Token 过期时间无法动态更新的问题

### v1.5.0

- **新增**：参数自动Trim模块（`guardian-auto-trim-spring-boot-starter`），自动去除请求参数首尾空格，支持表单参数 + JSON Body
- **新增**：不可见字符替换功能（`character-replacements`），可清除零宽空格、BOM、回车符等从复制粘贴混入的不可见字符
- **新增**：排除字段配置（`exclude-fields`），密码、签名等敏感字段可跳过 Trim
- **新增**：慢接口检测模块（`guardian-slow-api-spring-boot-starter`），超过阈值自动记录并打印 WARN 日志
- **新增**：`@SlowApiThreshold` 注解，支持为单个接口自定义慢接口阈值
- **新增**：慢接口 Actuator 端点（`GET /actuator/guardianSlowApi`），展示触发次数 + Top N 排行 + 最大耗时
- **新增**：请求链路追踪模块（`guardian-trace-spring-boot-starter`），自动生成/透传 TraceId
- **新增**：TraceId 写入 MDC + 响应头，Logback pattern 加 `%X{traceId}` 即可串联全链路日志
- **新增**：上游服务通过 `X-Trace-Id` 请求头透传 TraceId，支持跨服务链路追踪

### v1.4.3

- **新增**：接口幂等模块（`guardian-idempotent-spring-boot-starter`），Token 机制保证接口幂等性
- **新增**：幂等结果缓存，开启后重复请求返回首次执行结果
- **新增**：幂等 Actuator 端点、拦截统计、Token 生成器可插拔
- **新增**：幂等 PARAM 模式支持从 JSON Body 解析 Token（兼容 form-data / x-www-form-urlencoded / JSON 三种 POST 传参）
- **优化**：`RepeatableRequestFilter` 排序提升到全局配置 `GuardianCoreProperties`（`guardian.repeatable-filter-order`），仅需配置一次
- **优化**：拦截器执行顺序可配置（`interceptor-order`），默认：限流 1000 → 防重 2000 → 幂等 3000
- **优化**：三模块 Properties 提取公共基类 `BaseGuardianProperties`，统一 storage / responseMode / logEnabled / interceptorOrder
- **优化**：移除 Manager 中间层（`KeyGeneratorManager`、`RateLimitKeyGeneratorManager`、`KeyEncryptManager`、`IdempotentTokenGeneratorManager`），改为构造函数直接注入
- **优化**：删除未使用的异常类（`TokenGeneratorNotFoundException`、`KeyGeneratorNotFoundException`、`KeyEncryptNotFoundException`）
- **修复**：幂等 null 返回值处理，与 Spring 原生行为保持一致
- **修复**：`BaseResult.error()` 状态码错误（200 → 500）
- **修复**：Actuator Endpoint ID 改为驼峰命名，消除 Spring Boot 启动 WARN
- **修复**：三模块参数校验（`qps`、`window`、`interval`、`timeout` ≤ 0 时抛出明确异常）
- **修复**：令牌桶算法时间回拨防护（Redis Lua + 本地存储均加 `max(0, elapsed)`）

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
