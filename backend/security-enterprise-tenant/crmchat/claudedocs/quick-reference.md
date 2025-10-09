# Authentication Services - Quick Reference Guide

## Common Services API

### PasswordService

**Location**: `io.renren.crmchat.service.common.PasswordService`

#### Verify Password (Most Common)
```java
// Throws exception if password is wrong
passwordService.verifyOrThrow(
    plainPassword,
    hashedPassword,
    "账号或密码错误，请重新输入"
);

// Returns boolean
boolean isValid = passwordService.verify(plainPassword, hashedPassword);
```

#### Hash Password
```java
// Default strength (workload factor = 10)
String hash = passwordService.hash("password123");

// Custom strength
String strongHash = passwordService.hash("password123", 12);
```

#### Validate Password Strength
```java
String result = passwordService.validatePasswordStrength("weak");
// Returns: "密码长度至少8位" or "OK"

boolean isStrong = passwordService.isStrongPassword("StrongPass123");
// Returns: true if meets requirements
```

#### Check if Rehash Needed
```java
if (passwordService.needsRehash(currentHash, 12)) {
    String newHash = passwordService.hash(plainPassword, 12);
    // Update database
}
```

---

### TokenService

**Location**: `io.renren.crmchat.service.common.TokenService`

#### Generate Tokens
```java
// Admin token (appid = "10000")
String token = tokenService.generateAdminToken(userId, username);

// Tenant token (custom appid)
String token = tokenService.generateTenantToken(userId, username, appid);

// Kefu token (inherited from tenant)
String token = tokenService.generateKefuToken(userId, username, tenantAppid);

// Generic token
String token = tokenService.generateToken(userId, username, appid);
```

#### Validate Token
```java
// Full validation (signature + expiration)
if (tokenService.validateToken(token)) {
    // Token is valid
}

// Check if expired
if (tokenService.isTokenExpired(token)) {
    // Token expired
}
```

#### Extract Claims
```java
Long userId = tokenService.extractUserId(token);
String username = tokenService.extractUsername(token);
String appid = tokenService.extractAppid(token);
```

#### Verify AppID
```java
// Check token belongs to specific tenant
if (tokenService.verifyAppid(token, "APP123")) {
    // Token belongs to tenant APP123
}

// Check if admin token
if (tokenService.isAdminToken(token)) {
    // This is admin token (appid = "10000")
}
```

#### Refresh Token
```java
String newToken = tokenService.refreshToken(oldToken);
// Returns null if old token is invalid
```

---

## Usage Patterns

### Pattern 1: Admin Login

```java
@Service
@AllArgsConstructor
public class SystemAdminService {
    private final SystemAdminMapper systemAdminMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public Map<String, Object> login(String account, String password) {
        // 1. Verify login
        SystemAdminEntity admin = verifyLogin(account, password);

        // 2. Generate token
        String token = tokenService.generateAdminToken(
            Long.valueOf(admin.getId()),
            admin.getAccount()
        );

        // 3. Build response
        return buildResponse(token, admin);
    }

    private SystemAdminEntity verifyLogin(String account, String password) {
        // Query admin
        SystemAdminEntity admin = systemAdminMapper.selectOne(wrapper);

        // Check status
        if (admin.getStatus() == 0) {
            throw new CrmChatException("您已被禁止登录!");
        }

        // Verify password
        passwordService.verifyOrThrow(
            password,
            admin.getPwd(),
            "账号或密码错误，请重新输入"
        );

        // Update login info
        updateLoginInfo(admin);

        return admin;
    }
}
```

### Pattern 2: Tenant Login with Expiration Check

```java
@Service
@AllArgsConstructor
public class TenantsService {
    private final TenantsMapper tenantsMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public Map<String, Object> login(String account, String password) {
        TenantsEntity tenant = verifyLogin(account, password);

        String token = tokenService.generateTenantToken(
            Long.valueOf(tenant.getId()),
            tenant.getAccount(),
            tenant.getAppid()
        );

        return buildResponse(token, tenant);
    }

    private TenantsEntity verifyLogin(String account, String password) {
        TenantsEntity tenant = tenantsMapper.selectOne(wrapper);

        // Status check
        if (tenant.getStatus() == 0) {
            throw new CrmChatException("租户已被禁用!");
        }

        // Expiration check
        if (tenant.getExpireAt() != null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.after(tenant.getExpireAt())) {
                throw new CrmChatException("租户已过期，请联系管理员!");
            }
        }

        // Password verification
        passwordService.verifyOrThrow(
            password,
            tenant.getPwd(),
            "账号或密码错误，请重新输入"
        );

        return tenant;
    }
}
```

### Pattern 3: Password Change

```java
public void changePassword(Long userId, String oldPassword, String newPassword) {
    // 1. Get user
    UserEntity user = userMapper.selectById(userId);

    // 2. Verify old password
    passwordService.verifyOrThrow(
        oldPassword,
        user.getPwd(),
        "原密码错误"
    );

    // 3. Validate new password strength
    String validation = passwordService.validatePasswordStrength(newPassword);
    if (!"OK".equals(validation)) {
        throw new CrmChatException(validation);
    }

    // 4. Hash and save new password
    String newHash = passwordService.hash(newPassword);
    user.setPwd(newHash);
    userMapper.updateById(user);
}
```

### Pattern 4: Token-Based Authorization

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        // Validate token
        if (!tokenService.validateToken(token)) {
            throw new CrmChatException("无效的令牌");
        }

        // Extract user info
        Long userId = tokenService.extractUserId(token);
        String appid = tokenService.extractAppid(token);

        // Store in request context
        request.setAttribute("userId", userId);
        request.setAttribute("appid", appid);

        return true;
    }
}
```

### Pattern 5: Admin-Only Endpoint

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private TokenService tokenService;

    @PostMapping("/sensitive-operation")
    public Result performAdminAction(@RequestHeader("Authorization") String token) {
        // Verify admin token
        if (!tokenService.isAdminToken(token)) {
            return Result.error("需要管理员权限");
        }

        // Perform admin operation
        return Result.ok();
    }
}
```

### Pattern 6: Tenant Isolation

```java
@Service
public class DataService {
    @Autowired
    private TokenService tokenService;

    public List<Data> getData(String token) {
        // Extract tenant's appid from token
        String appid = tokenService.extractAppid(token);

        // Query data for this tenant only
        QueryWrapper<Data> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        return dataMapper.selectList(wrapper);
    }
}
```

---

## Error Messages

### PasswordService Errors

```java
// verifyOrThrow() - Custom message
throw new CrmChatException("账号或密码错误，请重新输入");

// hash() - Invalid input
throw new IllegalArgumentException("密码不能为空");
throw new IllegalArgumentException("logRounds 必须在 4-31 之间");

// validatePasswordStrength() - Validation failures
return "密码不能为空";
return "密码长度至少8位";
return "密码必须包含大写字母";
return "密码必须包含小写字母";
return "密码必须包含数字";
return "OK";  // Success
```

### TokenService Errors

No exceptions thrown - returns null for invalid operations:
- `validateToken()` → false
- `extractUserId()` → null
- `refreshToken()` → null

---

## PHP BCrypt Compatibility

**Automatic**: No action needed. PasswordService handles conversion transparently.

```java
// PHP generated hash (starts with $2y$)
String phpHash = "$2y$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

// Works automatically - converted to $2a$ internally
passwordService.verify("password", phpHash);  // Returns true
```

**Why?**
- PHP BCrypt uses `$2y$` identifier
- Java BCrypt uses `$2a$` identifier
- Algorithms are identical
- PasswordService converts automatically

---

## Configuration

### JWT Settings (application.yml)

```yaml
crmchat:
  jwt:
    secret: your-secret-key-change-in-production
    expire: 7200  # seconds (2 hours)
```

### BCrypt Settings

Default workload factor: 10 (hardcoded in PasswordService)

To use custom strength:
```java
String hash = passwordService.hash(password, 12);  // Stronger
```

---

## Testing Examples

### Unit Test - PasswordService

```java
@SpringBootTest
class PasswordServiceTest {

    @Autowired
    private PasswordService passwordService;

    @Test
    void testVerifyPassword_withPhpHash() {
        String phpHash = "$2y$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        assertTrue(passwordService.verify("password", phpHash));
    }

    @Test
    void testVerifyOrThrow_throwsException() {
        String hash = passwordService.hash("correct");
        assertThrows(CrmChatException.class, () -> {
            passwordService.verifyOrThrow("wrong", hash, "Error");
        });
    }

    @Test
    void testHashPassword() {
        String hash = passwordService.hash("password123");
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(passwordService.verify("password123", hash));
    }
}
```

### Unit Test - TokenService

```java
@SpringBootTest
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Test
    void testGenerateAndValidateAdminToken() {
        String token = tokenService.generateAdminToken(1L, "admin");

        assertTrue(tokenService.validateToken(token));
        assertEquals(1L, tokenService.extractUserId(token));
        assertEquals("admin", tokenService.extractUsername(token));
        assertEquals("10000", tokenService.extractAppid(token));
        assertTrue(tokenService.isAdminToken(token));
    }

    @Test
    void testVerifyAppid() {
        String token = tokenService.generateTenantToken(1L, "tenant", "APP123");

        assertTrue(tokenService.verifyAppid(token, "APP123"));
        assertFalse(tokenService.verifyAppid(token, "APP999"));
        assertFalse(tokenService.isAdminToken(token));
    }
}
```

---

## Common Mistakes

### Mistake 1: Manual BCrypt Conversion

```java
// ❌ WRONG - Don't do manual conversion
String dbPassword = user.getPwd();
if (dbPassword.startsWith("$2y$")) {
    dbPassword = "$2a$" + dbPassword.substring(4);
}
if (!BCrypt.checkpw(password, dbPassword)) {
    throw new CrmChatException("密码错误");
}

// ✓ RIGHT - Use PasswordService
passwordService.verifyOrThrow(password, user.getPwd(), "密码错误");
```

### Mistake 2: Direct JwtUtils Usage

```java
// ❌ WRONG - Don't use JwtUtils directly
@Autowired
private JwtUtils jwtUtils;

String token = jwtUtils.generateToken(userId, username, "10000");

// ✓ RIGHT - Use TokenService
@Autowired
private TokenService tokenService;

String token = tokenService.generateAdminToken(userId, username);
```

### Mistake 3: Hardcoding AppID

```java
// ❌ WRONG - Hardcoded appid
String token = tokenService.generateToken(userId, username, "10000");

// ✓ RIGHT - Use type-specific methods
String adminToken = tokenService.generateAdminToken(userId, username);
String tenantToken = tokenService.generateTenantToken(userId, username, tenant.getAppid());
```

### Mistake 4: Ignoring Null Returns

```java
// ❌ WRONG - No null check
Long userId = tokenService.extractUserId(token);
User user = userMapper.selectById(userId);  // NPE if token invalid!

// ✓ RIGHT - Check validity first
if (tokenService.validateToken(token)) {
    Long userId = tokenService.extractUserId(token);
    User user = userMapper.selectById(userId);
}
```

---

## Migration Checklist

When refactoring existing service to use common services:

1. **Update imports**
   - [ ] Remove: `import org.mindrot.jbcrypt.BCrypt;`
   - [ ] Remove: `import io.renren.crmchat.security.JwtUtils;`
   - [ ] Add: `import io.renren.crmchat.service.common.PasswordService;`
   - [ ] Add: `import io.renren.crmchat.service.common.TokenService;`

2. **Update dependencies**
   - [ ] Remove: `private final JwtUtils jwtUtils;`
   - [ ] Add: `private final PasswordService passwordService;`
   - [ ] Add: `private final TokenService tokenService;`

3. **Replace password verification**
   - [ ] Replace BCrypt.checkpw() with passwordService.verifyOrThrow()
   - [ ] Remove manual $2y$ → $2a$ conversion

4. **Replace token generation**
   - [ ] Replace jwtUtils.generateToken() with tokenService.generateXxxToken()
   - [ ] Use appropriate method: Admin/Tenant/Kefu

5. **Test**
   - [ ] Unit tests pass
   - [ ] Integration tests pass
   - [ ] Login works in development
   - [ ] Token validation works

---

## Performance Notes

**PasswordService**:
- `verify()`: ~100ms (BCrypt workload factor 10)
- `hash()`: ~100ms (BCrypt workload factor 10)
- Thread-safe, stateless

**TokenService**:
- `generateToken()`: <1ms (HMAC256)
- `validateToken()`: <1ms (signature verification)
- Thread-safe, stateless

**Caching**:
- Services are Spring singletons (shared instances)
- No internal caching (delegated to Spring)
- Database queries still need caching as before

---

## Support

**Documentation**:
- Architecture: `/claudedocs/authentication-architecture.md`
- Diagram: `/claudedocs/architecture-diagram.txt`
- This guide: `/claudedocs/quick-reference.md`

**Code Location**:
- PasswordService: `src/main/java/io/renren/crmchat/service/common/PasswordService.java`
- TokenService: `src/main/java/io/renren/crmchat/service/common/TokenService.java`
