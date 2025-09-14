<?php
declare (strict_types = 1);

namespace app\services\tenant;

use app\dao\tenant\TenantDao;
use crmeb\basic\BaseServices;
use crmeb\exceptions\AuthException;
use crmeb\services\CacheService;
use crmeb\utils\ApiErrorCode;
use crmeb\utils\JwtAuth;
use Firebase\JWT\ExpiredException;

/**
 * 租户授权service
 * Class TenantAuthServices
 * @package app\services\tenant
 */
class TenantAuthServices extends BaseServices
{
    /**
     * 构造方法
     * TenantAuthServices constructor.
     * @param TenantDao $dao
     */
    public function __construct(TenantDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * 获取租户授权信息
     * @param string $token
     * @return array
     * @throws \Psr\SimpleCache\InvalidArgumentException
     */
    public function parseToken(string $token): array
    {
        if (!$token || $token === 'undefined') {
            throw new AuthException(ApiErrorCode::ERR_LOGIN);
        }
        
        /** @var JwtAuth $jwtAuth */
        $jwtAuth = app()->make(JwtAuth::class);
        
        //设置解析token
        [$id, $type] = $jwtAuth->parseToken($token);

        //验证token
        try {
            $jwtAuth->verifyToken();
        } catch (ExpiredException $e) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_INVALID);
        } catch (\Throwable $e) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_INVALID);
        }

        // 验证类型必须是tenant
        if ($type !== 'tenant') {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_INVALID);
        }

        //获取租户信息
        $tenantInfo = $this->dao->get($id);
        if (!$tenantInfo || !$tenantInfo->id) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_STATUS);
        }

        // 检查租户状态
        if ($tenantInfo->status !== 1) {
            throw new AuthException('租户状态异常，无法访问');
        }

        // 检查是否过期
        if (!empty($tenantInfo->expire_at) && strtotime($tenantInfo->expire_at) < time()) {
            throw new AuthException('租户已过期，请联系管理员');
        }

        $tenantInfo->type = $type;
        
        // 计算额外属性
        $tenantArray = $tenantInfo->toArray();
        $tenantArray['is_expired'] = !empty($tenantArray['expire_at']) && strtotime($tenantArray['expire_at']) < time();
        $tenantArray['remaining_days'] = $this->calculateRemainingDays($tenantArray['expire_at']);
        
        return $tenantArray;
    }

    /**
     * 计算剩余天数
     * @param string|null $expireAt
     * @return int
     */
    private function calculateRemainingDays($expireAt): int
    {
        if (empty($expireAt)) {
            return -1; // 永久有效
        }
        $days = (strtotime($expireAt) - time()) / 86400;
        return max(0, (int)$days);
    }
}