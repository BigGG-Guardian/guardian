# Guardian 消息国际化（i18n）实现方案

## 一、目标

在 `ResponseUtils.reject()` 这个唯一出口处对 message 做国际化解析，让用户可以通过 Spring 标准的 `MessageSource` 机制实现多语言错误提示。不使用国际化的用户**零感知、零改动**。

## 二、需要修改的文件

### 1. 新增文件

| 文件 | 路径 | 说明 |
|------|------|------|
| `GuardianMessageResolver` | `guardian-core/.../core/i18n/GuardianMessageResolver.java` | 消息解析工具，判断 message 是纯文本还是 i18n Key，是 Key 则走 MessageSource 解析 |

```java
package com.sun.guardian.core.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;

public class GuardianMessageResolver {

    private final MessageSource messageSource;

    /**
     * @param messageSource 可为 null（未配置国际化时直接返回原始 message）
     */
    public GuardianMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 解析消息：尝试从 MessageSource 解析，解析不到则原样返回
     */
    public String resolve(String message) {
        if (messageSource == null || message == null) {
            return message;
        }
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(message, null, message, locale);
    }
}
```

---

### 2. 修改：ResponseUtils

**文件**：`guardian-core/src/main/java/com/sun/guardian/core/utils/ResponseUtils.java`

**改动点**：构造函数新增 `GuardianMessageResolver` 参数，`reject()` 方法中对 message 做解析。

```java
// 修改前
public class ResponseUtils {
    private final BaseConfig baseConfig;
    private final GuardianResponseHandler responseHandler;
    private final Function<String, RuntimeException> exceptionFactory;

    public ResponseUtils(BaseConfig baseConfig,
                         GuardianResponseHandler responseHandler,
                         Function<String, RuntimeException> exceptionFactory) {
        this.baseConfig = baseConfig;
        this.responseHandler = responseHandler;
        this.exceptionFactory = exceptionFactory;
    }

    public boolean reject(HttpServletRequest request, HttpServletResponse response,
                          String message) throws IOException {
        if (baseConfig.getResponseMode() == ResponseMode.JSON) {
            responseHandler.handle(request, response, 500, null, message);
            return false;
        }
        throw exceptionFactory.apply(message);
    }
}

// 修改后
public class ResponseUtils {
    private final BaseConfig baseConfig;
    private final GuardianResponseHandler responseHandler;
    private final Function<String, RuntimeException> exceptionFactory;
    private final GuardianMessageResolver messageResolver;  // 新增

    public ResponseUtils(BaseConfig baseConfig,
                         GuardianResponseHandler responseHandler,
                         Function<String, RuntimeException> exceptionFactory,
                         GuardianMessageResolver messageResolver) {  // 新增参数
        this.baseConfig = baseConfig;
        this.responseHandler = responseHandler;
        this.exceptionFactory = exceptionFactory;
        this.messageResolver = messageResolver;
    }

    public boolean reject(HttpServletRequest request, HttpServletResponse response,
                          String message) throws IOException {
        String resolvedMessage = messageResolver.resolve(message);  // 新增：解析消息
        if (baseConfig.getResponseMode() == ResponseMode.JSON) {
            responseHandler.handle(request, response, 500, null, resolvedMessage);
            return false;
        }
        throw exceptionFactory.apply(resolvedMessage);
    }
}
```

---

### 3. 修改：三个拦截器的构造处（传入 messageResolver）

拦截器本身代码**不用改**，只需要在构造 `ResponseUtils` 的地方多传一个 `messageResolver`。

#### 3.1 RepeatSubmitInterceptor

**文件**：`guardian-repeat-submit-core/.../interceptor/RepeatSubmitInterceptor.java`

```java
// 修改前
this.responseUtils = new ResponseUtils(repeatSubmitConfig, repeatSubmitResponseHandler, RepeatSubmitException::new);

// 修改后
this.responseUtils = new ResponseUtils(repeatSubmitConfig, repeatSubmitResponseHandler, RepeatSubmitException::new, messageResolver);
```

构造函数新增 `GuardianMessageResolver messageResolver` 参数。

#### 3.2 RateLimitInterceptor

**文件**：`guardian-rate-limit-core/.../interceptor/RateLimitInterceptor.java`

```java
// 修改前
this.responseUtils = new ResponseUtils(rateLimitConfig, rateLimitResponseHandler, RateLimitException::new);

// 修改后
this.responseUtils = new ResponseUtils(rateLimitConfig, rateLimitResponseHandler, RateLimitException::new, messageResolver);
```

构造函数新增 `GuardianMessageResolver messageResolver` 参数。

#### 3.3 IdempotentInterceptor

**文件**：`guardian-idempotent-core/.../interceptor/IdempotentInterceptor.java`

```java
// 修改前
this.responseUtils = new ResponseUtils(idempotentConfig, idempotentResponseHandler, IdempotentException::new);

// 修改后
this.responseUtils = new ResponseUtils(idempotentConfig, idempotentResponseHandler, IdempotentException::new, messageResolver);
```

构造函数新增 `GuardianMessageResolver messageResolver` 参数。

**额外注意**：IdempotentInterceptor 中有一处硬编码消息也要走解析：

```java
// 修改前（第 81 行）
return responseUtils.reject(request, response, "请求缺少幂等Token");

// 修改后（使用常量 Key，同时保持中文兜底）
return responseUtils.reject(request, response, idempotentConfig.getMissingTokenMessage());
```

在 `IdempotentConfig` 接口中新增：

```java
default String getMissingTokenMessage() { return "请求缺少幂等Token"; }
```

在 `GuardianIdempotentProperties` 中新增对应字段：

```java
private String missingTokenMessage = "请求缺少幂等Token";
```

这样用户可以通过 YAML 配置 `guardian.idempotent.missing-token-message: guardian.idempotent.missing-token` 来启用国际化。

---

### 4. 修改：三个 AutoConfiguration（注册 messageResolver Bean 并注入拦截器）

#### 4.1 guardian-core 新增公共 Bean 注册

在 `guardian-core` 中新增一个自动配置类，注册 `GuardianMessageResolver` Bean：

**新增文件**：`guardian-core/src/main/java/com/sun/guardian/core/config/GuardianCoreAutoConfiguration.java`

```java
@Configuration
public class GuardianCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GuardianMessageResolver.class)
    public GuardianMessageResolver guardianMessageResolver(
            @Autowired(required = false) MessageSource messageSource) {
        return new GuardianMessageResolver(messageSource);
    }
}
```

> 注意：需要在 `guardian-core` 的 `META-INF/spring.factories` 中注册此配置类。

#### 4.2 三个模块 AutoConfiguration 修改

在创建拦截器 Bean 时注入 `GuardianMessageResolver`：

**RepeatSubmitAutoConfiguration**：
```java
// 修改前
public RepeatSubmitInterceptor repeatSubmitInterceptor(KeyGenerator keyGenerator,
                                                       RepeatSubmitStorage repeatSubmitStorage,
                                                       RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                                       GuardianRepeatSubmitProperties guardianProperties,
                                                       RepeatSubmitStatistics statistics) {
    return new RepeatSubmitInterceptor(keyGenerator, repeatSubmitStorage, repeatSubmitResponseHandler, guardianProperties, statistics);
}

// 修改后
public RepeatSubmitInterceptor repeatSubmitInterceptor(KeyGenerator keyGenerator,
                                                       RepeatSubmitStorage repeatSubmitStorage,
                                                       RepeatSubmitResponseHandler repeatSubmitResponseHandler,
                                                       GuardianRepeatSubmitProperties guardianProperties,
                                                       RepeatSubmitStatistics statistics,
                                                       GuardianMessageResolver messageResolver) {  // 新增
    return new RepeatSubmitInterceptor(keyGenerator, repeatSubmitStorage, repeatSubmitResponseHandler, guardianProperties, statistics, messageResolver);
}
```

**RateLimitAutoConfiguration** 和 **IdempotentAutoConfiguration** 同理。

---

## 三、不需要修改的文件

| 模块 | 原因 |
|------|------|
| SlowApiInterceptor | 不调用 `reject()`，只记录日志，无用户响应消息 |
| AutoTrimFilter | 不涉及错误消息 |
| TraceIdFilter | 不涉及错误消息 |
| GuardianLogUtils | 日志面向开发者，不需要国际化 |
| 各模块 validate() | 参数校验异常面向开发者，不需要国际化 |

---

## 四、用户使用方式

### 不需要国际化（默认，零改动）

message 配置中文纯文本，和之前完全一样：

```yaml
guardian:
  rate-limit:
    message: "请求过于频繁，请稍后再试"
```

或注解：

```java
@RateLimit(message = "请求过于频繁，请稍后再试")
```

`GuardianMessageResolver` 识别到不是 Key，原样返回，行为不变。

### 需要国际化

**第一步**，把 message 改成 i18n Key：

```yaml
guardian:
  rate-limit:
    message: guardian.rate-limit.rejected
  repeat-submit:
    message: guardian.repeat-submit.rejected
  idempotent:
    missing-token-message: guardian.idempotent.missing-token
```

或注解：

```java
@RateLimit(message = "guardian.rate-limit.rejected")
@RepeatSubmit(message = "guardian.repeat-submit.rejected")
@Idempotent(value = "createOrder", message = "guardian.idempotent.rejected")
```

**第二步**，在项目中添加多语言消息文件：

```properties
# messages_zh_CN.properties
guardian.rate-limit.rejected=请求过于频繁，请稍后再试
guardian.repeat-submit.rejected=您的请求过于频繁，请稍后再试
guardian.idempotent.rejected=幂等Token无效或已消费
guardian.idempotent.missing-token=请求缺少幂等Token

# messages_en.properties
guardian.rate-limit.rejected=Rate limit exceeded, please try again later
guardian.repeat-submit.rejected=Too many requests, please try again later
guardian.idempotent.rejected=Idempotent token is invalid or already consumed
guardian.idempotent.missing-token=Missing idempotent token
```

Spring 根据请求头 `Accept-Language` 自动选择语言，无需额外配置。

---

## 五、修改清单总览

| 序号 | 操作 | 文件 | 模块 |
|------|------|------|------|
| 1 | **新增** | `core/i18n/GuardianMessageResolver.java` | guardian-core |
| 2 | **新增** | `core/config/GuardianCoreAutoConfiguration.java` | guardian-core |
| 3 | **新增** | `META-INF/spring.factories`（或追加） | guardian-core |
| 4 | **修改** | `ResponseUtils.java` — 构造函数 + reject() | guardian-core |
| 5 | **修改** | `RepeatSubmitInterceptor.java` — 构造函数 | repeat-submit-core |
| 6 | **修改** | `RateLimitInterceptor.java` — 构造函数 | rate-limit-core |
| 7 | **修改** | `IdempotentInterceptor.java` — 构造函数 + 硬编码消息提取 | idempotent-core |
| 8 | **修改** | `IdempotentConfig.java` — 新增 getMissingTokenMessage() | idempotent-core |
| 9 | **修改** | `GuardianIdempotentProperties.java` — 新增 missingTokenMessage 字段 | idempotent-starter |
| 10 | **修改** | `GuardianRepeatSubmitAutoConfiguration.java` — 注入 messageResolver | repeat-submit-starter |
| 11 | **修改** | `GuardianRateLimitAutoConfiguration.java` — 注入 messageResolver | rate-limit-starter |
| 12 | **修改** | `GuardianIdempotentAutoConfiguration.java` — 注入 messageResolver | idempotent-starter |
