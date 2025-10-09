# Spring Boot CRM 客服系统架构分析与优化方案

## 执行摘要

本文档提供了基于现有代码库发现的系统性架构分析，并提出向后兼容的优化路径。优化方案保证 REST API schema 不变，确保 PHP 客户端无缝集成。

**系统概况**:
- 技术栈：Spring Boot 3.5.4, Java 17, MyBatis Plus 3.5.8
- 架构模式：多租户客服系统（Admin/Tenant/Kefu/Mobile 四角色）
- 代码规模：187个 Java 文件，56个 REST Controller，57个带 TODO 标记的文件
- 依赖：JWT (com.auth0:java-jwt 4.4.0), WebSocket, BCrypt, MyBatis Plus

---

## 一、架构现状评估

### 1.1 安全架构

**现状发现**:
```yaml
Critical Issues:
  - JWT secret 硬编码在配置文件: "crmchat-jwt-secret-key-please-change-in-production"
  - 数据库密码明文: "root123"
  - CORS 全开放: allowedOriginPatterns("*") + allowCredentials(true)
  - Filter 认证但无统一拦截器架构
  - 无请求速率限制机制
  - 无 HTTPS 强制配置
  - 缺少安全响应头 (CSP, X-Frame-Options, HSTS)
```

**架构分析**:
- ✅ 使用了 `AuthenticationFilter` 进行 JWT 验证
- ✅ 支持 PHP 兼容的非标准请求头 `Authori-zation`
- ✅ 使用 `UserContext` ThreadLocal 存储用户信息
- ❌ **缺乏统一的安全配置层** - 没有 Spring Security 或等效框架
- ❌ **CORS 配置过于宽松** - 生产环境严重安全风险
- ❌ **密钥管理缺失** - 所有敏感信息硬编码

**风险评级**: 🔴 CRITICAL

### 1.2 性能架构

**现状发现**:
```yaml
Configuration Issues:
  MyBatis:
    cache-enabled: false  # 二级缓存完全禁用
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 性能日志到标准输出

  Redis:
    - 依赖已添加但未充分利用
    - 仅 12 处代码引用，主要用于基础功能
    - 无分布式缓存策略

  Async Processing:
    - @Async 注解使用次数: 0
    - 无异步任务配置
    - 无线程池自定义配置

  Spring Cache:
    - @Cacheable 使用次数: 0
    - @CacheEvict 使用次数: 0
    - 无缓存抽象层应用
```

**架构分析**:
- ❌ **缺少多层缓存架构** - 无 L1 (本地) + L2 (Redis) 缓存策略
- ❌ **同步阻塞模式** - 所有 I/O 操作同步执行
- ❌ **数据库压力大** - 无缓存导致重复查询
- ⚠️ **日志配置不当** - 标准输出影响性能

**风险评级**: 🟡 HIGH

### 1.3 代码质量架构

**现状发现**:
```yaml
Quality Issues:
  TODO Markers: 57 个文件

  Validation:
    - 手动参数校验替代 JSR-303
    - 缺少统一验证拦截器

  Exception Handling:
    - GlobalExceptionHandler 存在但覆盖不全
    - 异常日志缺少上下文信息

  Testing:
    - 单元测试覆盖率: 未知（未找到测试文件）
    - 集成测试: 缺失

  Logging:
    - 使用 @Slf4j 但日志到标准输出
    - 缺少结构化日志
    - 无日志级别配置
```

**架构分析**:
- ⚠️ **技术债务积累** - 57 个 TODO 标记表示未完成功能
- ❌ **测试架构缺失** - 无法保证代码质量
- ⚠️ **日志架构不完善** - 难以追踪生产问题

**风险评级**: 🟡 MEDIUM

### 1.4 API 架构

**现状发现**:
```yaml
API Design:
  Controllers: 56 个
  Roles: 4 种（Admin, Tenant, Kefu, Mobile）

  Structure:
    - 基于角色的 URL 前缀: /api/{role}/{resource}
    - 统一响应格式: ApiResult<T>
    - PHP 兼容的命名约定

  Missing:
    - 无 API 版本管理 (v1, v2)
    - 无统一限流机制
    - 无 API 文档生成（Swagger 配置但未验证）
    - 缺少请求/响应日志记录
```

**架构分析**:
- ✅ **角色隔离清晰** - 基于角色的控制器分离
- ✅ **统一响应格式** - ApiResult 封装
- ❌ **缺少版本管理** - API 演进困难
- ❌ **缺少限流** - 易受 DDoS 攻击

**风险评级**: 🟡 MEDIUM

---

## 二、系统性优化方案（向后兼容）

### 优化原则

1. **API Schema 不变**: 所有 REST 接口保持现有路径、参数、响应格式
2. **PHP 兼容性**: 保持 `Authori-zation` 请求头支持，JWT payload 结构不变
3. **渐进式改进**: 分阶段实施，每个阶段可独立部署
4. **零停机**: 所有改动支持滚动更新

### 2.1 安全架构优化 (Phase 1 - Critical)

**优先级**: 🔴 P0 (必须立即执行)

#### 2.1.1 密钥外部化

**问题**: JWT secret 和数据库密码硬编码

**解决方案**:
```yaml
# 迁移到环境变量或 Spring Cloud Config
crmchat:
  jwt:
    secret: ${JWT_SECRET:crmchat-jwt-secret-key-please-change-in-production}
    expire: ${JWT_EXPIRE:7200}

spring:
  datasource:
    password: ${DB_PASSWORD:root123}
```

**实施步骤**:
1. 创建 `.env.template` 示例文件
2. 修改 `application.yml` 使用环境变量
3. 更新部署文档说明环境变量配置
4. 添加启动时密钥验证逻辑（检测默认值并警告）

**风险**: 低 - 向后兼容（默认值保持不变）

**工作量**: 2 小时

#### 2.1.2 CORS 策略收紧

**问题**: 允许所有源访问并携带凭证

**解决方案**:
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    String[] allowedOrigins = corsProperties.getAllowedOrigins(); // 从配置读取

    registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)  // 替代 allowedOriginPatterns("*")
            .allowCredentials(true)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Authori-zation", "Content-Type")  // 明确指定
            .exposedHeaders("Authorization")
            .maxAge(3600);
}
```

**配置属性**:
```yaml
crmchat:
  cors:
    allowed-origins:
      - http://localhost:3000  # 开发环境
      - https://your-domain.com  # 生产环境
```

**实施步骤**:
1. 创建 `CorsProperties` 配置类
2. 修改 `WebMvcConfig.addCorsMappings()`
3. 提供环境特定配置文件

**风险**: 中 - 需要提前配置允许的源

**工作量**: 4 小时

#### 2.1.3 安全响应头

**问题**: 缺少安全响应头

**解决方案**:
```java
@Component
public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // XSS 保护
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // HTTPS 强制（生产环境）
        if (isProduction()) {
            httpResponse.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");
        }

        // CSP (根据需求调整)
        httpResponse.setHeader("Content-Security-Policy",
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");

        chain.doFilter(request, response);
    }
}
```

**实施步骤**:
1. 创建 `SecurityHeaderFilter`
2. 注册到 Filter Chain (Order = Ordered.HIGHEST_PRECEDENCE + 1)
3. 配置环境特定的 CSP 策略

**风险**: 低 - 仅添加响应头，不影响功能

**工作量**: 3 小时

#### 2.1.4 请求速率限制

**问题**: 无限流机制，易受 DDoS 攻击

**解决方案** (基于 Redis 的令牌桶算法):
```java
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = generateKey(point, rateLimit);

        // 令牌桶算法
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.time(), rateLimit.timeUnit());
        }

        if (count > rateLimit.limit()) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }

        return point.proceed();
    }

    private String generateKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        CrmChatUser user = UserContext.getUser();
        String identifier = rateLimit.key().isEmpty() ?
            user.getUserId().toString() : user.getAppid();

        String methodName = point.getSignature().getName();
        return String.format("rate_limit:%s:%s", identifier, methodName);
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {
    int limit() default 100;  // 请求次数
    int time() default 60;    // 时间窗口
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    String key() default "";  // user|appid|ip
}
```

**使用示例**:
```java
@RateLimit(limit = 10, time = 60)  // 每分钟最多 10 次
@PostMapping("/api/kefu/send-message")
public ApiResult sendMessage(@RequestBody MessageDTO message) {
    // ...
}
```

**实施步骤**:
1. 创建 `@RateLimit` 注解
2. 实现 `RateLimitAspect`
3. 在高频接口添加注解
4. 创建 `RateLimitException` 和全局异常处理

**风险**: 低 - 通过注解逐步启用

**工作量**: 6 小时

**Phase 1 总结**:
- 总工作量：15 小时（2 人日）
- 风险级别：低到中
- 优先级：P0（立即执行）
- 依赖：Redis（已有）

---

### 2.2 性能架构优化 (Phase 2 - High Priority)

**优先级**: 🟡 P1 (1-2 周内执行)

#### 2.2.1 多层缓存架构

**问题**: 无缓存导致数据库重复查询

**解决方案** (L1 Caffeine + L2 Redis):

**架构设计**:
```
Request → L1 Cache (Caffeine) → L2 Cache (Redis) → Database
          ↑                      ↑
          └─ 热点数据，100ms TTL  └─ 共享数据，1h TTL
```

**配置**:
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m

crmchat:
  cache:
    redis:
      enabled: true
      default-ttl: 3600  # 1 小时
      key-prefix: "crmchat:cache:"
```

**实现**:
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // L1: Caffeine (本地缓存)
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES));

        // L2: Redis (分布式缓存)
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .transactionAware()
            .build();

        // 组合策略：优先 L1，回退 L2
        return new CompositeCacheManager(caffeineCacheManager, redisCacheManager);
    }
}
```

**使用示例**:
```java
@Service
public class AdminUserService {

    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public UserVO getUserById(Long userId) {
        // 只在缓存未命中时执行
        return userDao.selectById(userId);
    }

    @CacheEvict(value = "users", key = "#user.id")
    public void updateUser(UserDTO user) {
        userDao.updateById(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUserCache() {
        // 清除所有用户缓存
    }
}
```

**缓存策略**:
```yaml
Cache Keys:
  # 高频读取，低频变更
  - users:{userId}           TTL: 1h
  - tenants:{tenantId}       TTL: 1h
  - menus:{roleId}           TTL: 30m
  - config:{configKey}       TTL: 10m

  # 频繁变更
  - messages:{conversationId}  TTL: 5m
  - online_users:{appid}       TTL: 1m
```

**实施步骤**:
1. 启用 MyBatis Plus 二级缓存：`cache-enabled: true`
2. 配置 Spring Cache 抽象层
3. 识别高频查询方法并添加 `@Cacheable`
4. 在更新/删除方法添加 `@CacheEvict`
5. 监控缓存命中率（添加 Micrometer metrics）

**性能预期**:
- 缓存命中率：>70%
- 数据库查询减少：60-80%
- 响应时间改善：30-50%

**风险**: 低 - Spring Cache 抽象层保证透明性

**工作量**: 12 小时

#### 2.2.2 异步处理架构

**问题**: 所有 I/O 操作同步执行

**解决方案**:
```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU 核数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());

        // 最大线程数 = CPU 核数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);

        // 队列容量
        executor.setQueueCapacity(500);

        // 线程名前缀
        executor.setThreadNamePrefix("async-");

        // 拒绝策略：调用者执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            log.error("异步任务执行失败 - 方法: {}, 参数: {}", method.getName(), params, ex);
    }
}
```

**应用场景**:
```java
@Service
public class NotificationService {

    // 异步发送通知（不阻塞主流程）
    @Async
    public void sendNotification(Long userId, String message) {
        // 发送邮件、短信、推送通知等
        log.info("发送通知给用户 {} : {}", userId, message);
    }
}

@Service
public class MessageService {

    @Autowired
    private NotificationService notificationService;

    public ApiResult sendMessage(MessageDTO message) {
        // 同步：保存消息到数据库
        messageDao.insert(message);

        // 异步：发送通知（不影响响应时间）
        notificationService.sendNotification(message.getReceiverId(), message.getContent());

        return ApiResult.success();
    }
}
```

**典型异步场景**:
- 消息推送通知
- 日志记录（操作日志）
- 文件上传/下载
- 报表生成
- 数据统计计算
- 第三方 API 调用

**实施步骤**:
1. 创建 `AsyncConfig` 配置类
2. 识别可异步化的操作（I/O 密集型）
3. 添加 `@Async` 注解
4. 监控线程池指标（活跃线程、队列大小）

**性能预期**:
- 接口响应时间减少：20-40%
- 系统吞吐量提升：30-50%

**风险**: 中 - 需要处理异步异常和事务一致性

**工作量**: 8 小时

#### 2.2.3 数据库连接池优化

**问题**: 使用默认连接池配置

**解决方案** (HikariCP 调优):
```yaml
spring:
  datasource:
    hikari:
      # 最小空闲连接数
      minimum-idle: 10

      # 最大连接数 (公式：((core_count * 2) + effective_spindle_count))
      maximum-pool-size: 20

      # 连接超时时间
      connection-timeout: 30000

      # 空闲连接最大存活时间
      idle-timeout: 600000

      # 连接最大生命周期
      max-lifetime: 1800000

      # 连接测试查询
      connection-test-query: SELECT 1

      # 池名称
      pool-name: CRMChatHikariPool

      # 自动提交
      auto-commit: true
```

**实施步骤**:
1. 修改 `application.yml` 添加 HikariCP 配置
2. 根据数据库服务器规格调整 `maximum-pool-size`
3. 启用连接池监控指标

**工作量**: 2 小时

#### 2.2.4 日志优化

**问题**: MyBatis 日志输出到标准输出

**解决方案**:
```yaml
# application.yml
mybatis-plus:
  configuration:
    # 生产环境禁用 SQL 日志
    log-impl: ${MYBATIS_LOG_IMPL:org.apache.ibatis.logging.nologging.NoLoggingImpl}

# application-dev.yml (开发环境)
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

# logback-spring.xml
<configuration>
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/crmchat.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/crmchat.%d{yyyy-MM-dd}.log</fileNamePattern>
                <maxHistory>30</maxHistory>
                <totalSizeCap>10GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="FILE" />
        </root>
    </springProfile>

    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE" />
        </root>
    </springProfile>
</configuration>
```

**实施步骤**:
1. 创建 `logback-spring.xml`
2. 配置环境特定日志级别
3. 移除 `StdOutImpl` 配置

**工作量**: 3 小时

**Phase 2 总结**:
- 总工作量：25 小时（3 人日）
- 风险级别：低到中
- 优先级：P1（1-2 周内）
- 依赖：Redis（已有）、Caffeine（新增）

---

### 2.3 代码质量优化 (Phase 3 - Medium Priority)

**优先级**: 🟢 P2 (2-4 周内执行)

#### 2.3.1 统一参数验证

**问题**: 手动验证替代 JSR-303

**解决方案**:
```java
// 启用验证
@RestController
@Validated  // 类级别启用
public class AdminUserController {

    @PostMapping("/api/admin/users")
    public ApiResult createUser(@Valid @RequestBody UserDTO user) {
        // @Valid 自动触发验证
        return adminUserService.createUser(user);
    }
}

// DTO 定义验证规则
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
             message = "密码必须包含大小写字母和数字，至少8位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色不能为空")
    @Min(value = 1, message = "角色ID必须大于0")
    private Long roleId;
}

// 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        return ApiResult.fail(errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));

        return ApiResult.fail(errorMessage);
    }
}
```

**自定义验证器**:
```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AppidValidator.class)
public @interface ValidAppid {
    String message() default "无效的租户APPID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class AppidValidator implements ConstraintValidator<ValidAppid, String> {

    @Autowired
    private TenantService tenantService;

    @Override
    public boolean isValid(String appid, ConstraintValidatorContext context) {
        if (appid == null || appid.isEmpty()) {
            return false;
        }
        return tenantService.existsByAppid(appid);
    }
}
```

**实施步骤**:
1. 在所有 DTO 添加 JSR-303 注解
2. Controller 方法添加 `@Valid`
3. 扩展 `GlobalExceptionHandler` 处理验证异常
4. 创建自定义验证器（如租户验证、枚举验证）
5. 移除手动验证代码

**工作量**: 16 小时

#### 2.3.2 TODO 清理

**问题**: 57 个文件含 TODO 标记

**策略**:
1. **分类 TODO**:
   - 功能缺失：需要实现的业务逻辑
   - 性能优化：可后续优化的代码
   - 技术债务：需要重构的代码
   - 文档缺失：需要补充的注释

2. **优先级排序**:
   - P0：影响核心功能（如认证、支付）
   - P1：影响用户体验（如消息发送）
   - P2：优化类（如缓存、日志）
   - P3：文档类

3. **执行计划**:
   - 创建 Jira/GitHub Issues 追踪每个 TODO
   - 分配到各迭代 Sprint
   - 设置完成时间表

**实施步骤**:
1. 扫描所有 TODO 并分类
2. 创建跟踪 Issue
3. 每周解决 5-10 个 TODO
4. 在 PR 审查中禁止新增 TODO（改用 Issue）

**工作量**: 持续进行（每周 2-4 小时）

#### 2.3.3 测试架构建立

**问题**: 无单元测试和集成测试

**解决方案**:
```java
// 单元测试
@SpringBootTest
@AutoConfigureMockMvc
class AdminUserServiceTest {

    @Autowired
    private AdminUserService adminUserService;

    @MockBean
    private AdminUserDao adminUserDao;

    @Test
    void testGetUserById_Success() {
        // Given
        Long userId = 1L;
        AdminUserEntity mockUser = new AdminUserEntity();
        mockUser.setId(userId);
        mockUser.setUsername("test");

        when(adminUserDao.selectById(userId)).thenReturn(mockUser);

        // When
        UserVO result = adminUserService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals("test", result.getUsername());
        verify(adminUserDao, times(1)).selectById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(adminUserDao.selectById(userId)).thenReturn(null);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            adminUserService.getUserById(userId);
        });
    }
}

// 集成测试
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AdminAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void testProtectedEndpoint_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpoint_WithToken() throws Exception {
        String token = "valid-jwt-token";

        mockMvc.perform(get("/api/admin/users")
                .header("Authori-zation", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
```

**测试覆盖目标**:
- 核心业务逻辑：80%
- Controller 层：60%
- Service 层：70%
- 工具类：90%

**实施步骤**:
1. 配置 JUnit 5 + Mockito
2. 为核心 Service 编写单元测试
3. 为关键 API 编写集成测试
4. 配置 Jacoco 生成覆盖率报告
5. 在 CI/CD 中强制最低覆盖率

**工作量**: 40 小时（持续进行）

**Phase 3 总结**:
- 总工作量：56+ 小时（7 人日）
- 风险级别：低
- 优先级：P2（2-4 周）
- 依赖：无

---

### 2.4 架构增强 (Phase 4 - Future Enhancement)

**优先级**: 🟢 P3 (1-3 月内执行)

#### 2.4.1 API 版本管理

**目标**: 支持 API 平滑演进

**实现方案**:
```java
// 版本控制策略 1: URL 路径
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserControllerV1 { }

@RestController
@RequestMapping("/api/v2/admin/users")
public class AdminUserControllerV2 { }

// 版本控制策略 2: 请求头
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @GetMapping
    @ApiVersion("1.0")
    public ApiResult getUsersV1() { }

    @GetMapping
    @ApiVersion("2.0")
    public ApiResult getUsersV2() { }
}

// 版本拦截器
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String version = request.getHeader("API-Version");
        if (version == null) {
            version = "1.0";  // 默认版本
        }
        request.setAttribute("api.version", version);
        return true;
    }
}
```

**迁移策略**:
- v1: 保持现有 API 不变
- v2: 新功能和优化
- 维护期：同时支持 v1 和 v2（6-12 个月）
- 废弃通知：提前 3 个月通知客户端

**工作量**: 12 小时

#### 2.4.2 分布式追踪

**目标**: 请求链路追踪和性能分析

**技术选型**: Spring Cloud Sleuth + Zipkin

**配置**:
```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0  # 采样率 100%（生产环境降低到 0.1）
  zipkin:
    base-url: http://localhost:9411
    sender:
      type: web
```

**使用**:
```java
@Service
public class MessageService {

    @Autowired
    private Tracer tracer;

    public void sendMessage(MessageDTO message) {
        Span span = tracer.nextSpan().name("send-message");
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("user.id", message.getUserId().toString());
            span.tag("message.type", message.getType());

            // 业务逻辑
            messageDao.insert(message);
            notificationService.notify(message);
        } finally {
            span.finish();
        }
    }
}
```

**工作量**: 8 小时

#### 2.4.3 WebSocket 优化

**目标**: 提升实时消息性能

**优化点**:
1. **连接管理**: 使用 Redis 存储 WebSocket Session
2. **消息队列**: RabbitMQ 或 Kafka 解耦消息发送
3. **负载均衡**: 支持多实例部署
4. **心跳机制**: 检测和清理僵尸连接

**架构**:
```
Client → Nginx (WebSocket) → Spring Boot Instance 1 ─┐
                            → Spring Boot Instance 2 ─┤→ Redis (Session Store)
                            → Spring Boot Instance 3 ─┘      ↓
                                                         RabbitMQ (Message Queue)
```

**工作量**: 24 小时

#### 2.4.4 监控和告警

**目标**: 生产环境可观测性

**技术栈**: Spring Boot Actuator + Prometheus + Grafana

**配置**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: crmchat
```

**关键指标**:
- HTTP 请求计数和延迟
- JVM 内存和 GC
- 数据库连接池
- 缓存命中率
- WebSocket 连接数
- 业务指标（消息发送量、在线客服数）

**告警规则**:
- 响应时间 > 1s
- 错误率 > 5%
- CPU 使用率 > 80%
- 内存使用率 > 85%
- 数据库连接池耗尽

**工作量**: 16 小时

**Phase 4 总结**:
- 总工作量：60 小时（7.5 人日）
- 风险级别：中
- 优先级：P3（1-3 月）
- 依赖：Zipkin, Prometheus, Grafana

---

## 三、实施路线图

### 3.1 时间表

```
Week 1-2: Phase 1 (安全架构)
├─ W1: 密钥外部化 + CORS 收紧
└─ W2: 安全响应头 + 速率限制

Week 3-5: Phase 2 (性能架构)
├─ W3: 多层缓存架构
├─ W4: 异步处理 + 连接池优化
└─ W5: 日志优化 + 性能测试

Week 6-9: Phase 3 (代码质量)
├─ W6-7: 统一参数验证
├─ W8: TODO 清理（首批）
└─ W9: 测试架构建立

Month 3-5: Phase 4 (架构增强)
├─ M3: API 版本管理 + 分布式追踪
├─ M4: WebSocket 优化
└─ M5: 监控和告警
```

### 3.2 人力资源

**团队配置**:
- 后端开发：2 人
- 测试工程师：1 人（Phase 3 开始）
- DevOps：0.5 人（Phase 4）

**总工作量**: 156 小时 ≈ 19.5 人日

### 3.3 风险评估

| 阶段 | 主要风险 | 缓解措施 | 影响级别 |
|------|---------|----------|---------|
| Phase 1 | CORS 配置导致客户端无法访问 | 提前收集所有合法域名，灰度发布 | 中 |
| Phase 2 | 缓存策略不当导致数据不一致 | 使用短 TTL，业务变更立即失效 | 中 |
| Phase 3 | 参数验证过严导致现有请求失败 | 先记录后验证，逐步启用 | 低 |
| Phase 4 | 分布式组件引入复杂度 | 充分测试，提供回退方案 | 高 |

### 3.4 验收标准

**Phase 1**:
- ✅ 所有密钥从环境变量读取
- ✅ CORS 仅允许配置的域名
- ✅ 响应头包含所有安全头
- ✅ 速率限制在高频接口生效

**Phase 2**:
- ✅ 缓存命中率 > 70%
- ✅ 数据库查询减少 60%
- ✅ API 响应时间降低 30%
- ✅ 异步任务不阻塞主流程

**Phase 3**:
- ✅ 所有 DTO 使用 JSR-303 验证
- ✅ TODO 减少 50%
- ✅ 核心业务测试覆盖率 > 70%

**Phase 4**:
- ✅ 支持 API 版本切换
- ✅ 请求链路可追踪
- ✅ Grafana 仪表盘展示关键指标

---

## 四、向后兼容性保证

### 4.1 API 契约

**保持不变**:
- URL 路径：`/api/{role}/{resource}`
- 请求方法：GET, POST, PUT, DELETE
- 请求体格式：JSON
- 响应格式：`ApiResult<T>`
- HTTP 状态码：200, 401, 500

**允许扩展**:
- 响应体新增字段（不破坏现有字段）
- 请求头新增可选参数
- 查询参数新增可选项

### 4.2 JWT 兼容性

**保持不变**:
- Payload 结构：`{ user_id, username, appid, iat, exp }`
- 签名算法：HMAC256
- 请求头支持：`Authori-zation` (非标准) 和 `Authorization` (标准)

**迁移路径**:
1. 阶段 1：同时支持两种请求头（现状）
2. 阶段 2：引导客户端使用标准 `Authorization` 头
3. 阶段 3（6 个月后）：废弃 `Authori-zation` 支持

### 4.3 数据库兼容性

**Schema 变更策略**:
- 新增字段：默认值或允许 NULL
- 删除字段：先标记废弃，6 个月后物理删除
- 重命名字段：创建视图或触发器兼容旧名称

**迁移脚本**:
```sql
-- 示例：添加新字段
ALTER TABLE chat_user
ADD COLUMN avatar_url VARCHAR(255) DEFAULT NULL
COMMENT '用户头像URL';

-- 示例：废弃字段（不立即删除）
ALTER TABLE chat_message
MODIFY COLUMN old_field VARCHAR(100) DEFAULT NULL
COMMENT 'DEPRECATED: 使用 new_field 替代';
```

### 4.4 配置兼容性

**application.yml 变更**:
- 新增配置：提供默认值
- 废弃配置：保留兼容逻辑，打印警告日志
- 重命名配置：同时读取新旧配置名

**示例**:
```java
@ConfigurationProperties(prefix = "crmchat")
public class CrmChatProperties {

    // 新配置
    private JwtProperties jwt = new JwtProperties();

    // 兼容旧配置（已废弃）
    @Deprecated
    @Value("${crmchat.jwt-secret:}")
    private String legacyJwtSecret;

    @PostConstruct
    public void init() {
        // 兼容逻辑
        if (!legacyJwtSecret.isEmpty() && jwt.getSecret().equals("default")) {
            log.warn("使用废弃的配置 crmchat.jwt-secret，请迁移到 crmchat.jwt.secret");
            jwt.setSecret(legacyJwtSecret);
        }
    }
}
```

---

## 五、技术决策记录 (ADR)

### ADR-001: 选择 Caffeine + Redis 多层缓存

**上下文**: 需要缓存架构减少数据库压力

**决策**: 使用 Caffeine (L1) + Redis (L2) 多层缓存

**理由**:
- Caffeine 提供堆内缓存，延迟 < 1ms
- Redis 提供分布式缓存，支持多实例共享
- Spring Cache 抽象层统一管理

**后果**:
- 需要处理缓存一致性
- 需要监控缓存命中率
- 增加 Redis 依赖（已有）

### ADR-002: 使用注解驱动的速率限制

**上下文**: 需要防止 API 滥用

**决策**: 使用 `@RateLimit` 注解 + Redis 令牌桶

**理由**:
- 注解方式灵活，按需启用
- Redis 支持分布式计数
- 令牌桶算法平滑限流

**替代方案**:
- Nginx 限流：无法基于用户/租户
- Guava RateLimiter：不支持分布式

### ADR-003: 保持 Filter 而非迁移到 Spring Security

**上下文**: 现有认证基于自定义 Filter

**决策**: 保持 `AuthenticationFilter`，不迁移到 Spring Security

**理由**:
- PHP 兼容性：自定义 Filter 更灵活
- 迁移成本高：Spring Security 学习曲线陡峭
- 功能足够：当前认证需求简单

**风险**:
- 缺少 Spring Security 生态（OAuth2、SAML）
- 需要手动实现安全功能

**缓解**:
- 文档化认证流程
- 未来需要复杂认证时重新评估

---

## 六、监控指标定义

### 6.1 业务指标

```yaml
业务健康度:
  - 在线客服数量
  - 活跃会话数量
  - 消息发送成功率
  - 平均响应时间（客服回复用户）
  - 用户满意度评分

系统性能:
  - API 平均响应时间
  - API 95 分位延迟
  - 吞吐量 (QPS)
  - 错误率
  - 缓存命中率
```

### 6.2 告警规则

```yaml
Critical (P0):
  - API 错误率 > 10% (持续 5 分钟)
  - 数据库连接池耗尽
  - 内存使用率 > 90%

High (P1):
  - API 95 分位延迟 > 2s
  - 缓存命中率 < 50%
  - CPU 使用率 > 80% (持续 10 分钟)

Medium (P2):
  - 磁盘使用率 > 80%
  - JVM GC 时间 > 1s
  - WebSocket 连接数异常波动
```

---

## 七、总结

### 7.1 关键收益

| 优化领域 | 预期改善 |
|---------|---------|
| 安全性 | 从 C 级提升到 A 级 |
| 性能 | 响应时间降低 30-50%，吞吐量提升 30-50% |
| 可维护性 | TODO 减少 50%，测试覆盖率 > 70% |
| 可观测性 | 从无到有，完整的监控和追踪体系 |

### 7.2 投资回报

- **总投入**: 156 小时 ≈ 20 人日
- **风险降低**: 消除 P0 安全风险
- **性能提升**: 减少 60% 数据库负载
- **技术债务**: 清理 50% TODO，建立测试体系

### 7.3 下一步行动

1. **立即执行** (本周):
   - Phase 1.1: 密钥外部化
   - Phase 1.2: CORS 策略收紧

2. **近期规划** (2 周内):
   - Phase 1.3-1.4: 安全响应头 + 速率限制
   - Phase 2.1: 多层缓存架构设计

3. **持续改进**:
   - 每周代码审查关注安全和性能
   - 每月评估缓存命中率和 TODO 清理进度
   - 每季度架构回顾和调整

---

## 附录

### A. 配置检查清单

**生产环境部署前**:
- [ ] JWT secret 已更改为强密码
- [ ] 数据库密码已更改
- [ ] CORS 允许源已配置
- [ ] HTTPS 已启用
- [ ] 日志级别设置为 INFO
- [ ] MyBatis SQL 日志已禁用
- [ ] 缓存已启用
- [ ] 速率限制已配置
- [ ] 监控告警已配置
- [ ] 备份策略已建立

### B. 性能基准测试

**测试场景**:
1. 用户登录：100 并发用户，持续 1 分钟
2. 消息发送：50 并发用户，每秒发送 10 条消息
3. 历史记录查询：100 并发用户，随机查询

**测试工具**: JMeter 或 Gatling

**基准指标** (优化前):
```yaml
登录:
  平均响应时间: 500ms
  95分位延迟: 1200ms
  错误率: 0%

消息发送:
  平均响应时间: 300ms
  95分位延迟: 800ms
  吞吐量: 200 msg/s

历史记录查询:
  平均响应时间: 1000ms
  95分位延迟: 2500ms
  数据库查询: 5 次/请求
```

**目标指标** (优化后):
```yaml
登录:
  平均响应时间: < 300ms (-40%)
  95分位延迟: < 600ms (-50%)

消息发送:
  平均响应时间: < 150ms (-50%)
  吞吐量: > 300 msg/s (+50%)

历史记录查询:
  平均响应时间: < 500ms (-50%)
  缓存命中率: > 70%
  数据库查询: < 2 次/请求 (-60%)
```

### C. 参考资料

- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MyBatis Plus Documentation](https://baomidou.com/)
- [Redis Caching Strategies](https://redis.io/docs/manual/patterns/)
- [JWT Security Best Practices](https://tools.ietf.org/html/rfc8725)
- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)

---

**文档版本**: 1.0
**最后更新**: 2025-10-03
**作者**: Architecture Team
**审阅者**: Security Team, Performance Team
