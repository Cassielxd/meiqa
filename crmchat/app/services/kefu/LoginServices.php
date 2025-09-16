<?php

namespace app\services\kefu;


use app\dao\chat\ChatServiceDao;
use app\services\chat\ChatUserServices;
use crmeb\basic\BaseServices;
use crmeb\exceptions\AuthException;
use crmeb\services\CacheService;
use crmeb\utils\ApiErrorCode;
use crmeb\utils\JwtAuth;
use Psr\SimpleCache\InvalidArgumentException;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;
use think\exception\ValidateException;
use Firebase\JWT\ExpiredException;

/**
 * Class LoginServices
 * @package app\services\kefu
 */
class LoginServices extends BaseServices
{

    /**
     * LoginServices constructor.
     * @param ChatServiceDao $dao
     */
    public function __construct(ChatServiceDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * 客服账号密码登录
     * @param string $account
     * @param string $password
     * @param int $isApp
     * @param string|null $clientId
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function authLogin(string $account, string $password = null, int $isApp = 0, string $clientId = null)
    {
        $kefuInfo = $this->dao->get(['account' => $account]);
        if (!$kefuInfo) {
            throw new ValidateException('没有此用户');
        }
        if ($password) {
            // 支持新旧密码验证，并自动升级旧密码
            $isPasswordValid = false;
            
            // 检查是否是新的 bcrypt 密码格式
            if (strpos($kefuInfo->password, '$2y$') === 0) {
                $isPasswordValid = password_verify($password, $kefuInfo->password);
            } else {
                // 兼容旧的 MD5 密码
                if (md5($password) === $kefuInfo->password) {
                    $isPasswordValid = true;
                    // 自动升级为 bcrypt 密码
                    $kefuInfo->password = password_hash($password, PASSWORD_DEFAULT);
                    $kefuInfo->save();
                }
            }
            
            if (!$isPasswordValid) {
                throw new ValidateException('账号或密码错误');
            }
        }
        if (!$kefuInfo->status) {
            throw new ValidateException('您已被禁止登录');
        }
        $token = $this->createToken($kefuInfo->id, 'kefu');
        $kefuInfo->ip = request()->ip();
        $kefuInfo->status = 1;
        if (!$kefuInfo->is_app) {
            $kefuInfo->is_app = $isApp;
        }
        //不再app端登录或者,没有登录过,自动上线
        if (!$kefuInfo->is_app || $kefuInfo->update_time == 0) {
            $kefuInfo->online = 1;
            /** @var ChatUserServices $service */
            $service = app()->make(ChatUserServices::class);
            $service->update(['id' => $kefuInfo['user_id']], ['online' => 1]);
        }
        if ($clientId) {
            $kefuInfo->client_id = $clientId;
        }
        $kefuInfo->update_time = time();
        $kefuInfo->save();
        return [
            'token' => $token['token'],
            'exp_time' => $token['params']['exp'],
            'kefuInfo' => $kefuInfo->hidden(['password', 'ip', 'update_time', 'add_time', 'status', 'mer_id', 'customer', 'notify'])->toArray()
        ];
    }

    /**
     * 解析token
     * @param string $token
     * @return array
     * @throws InvalidArgumentException
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function parseToken(string $token, int $code = 410003)
    {
        if ($token === 'undefined' || !$token) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN, $code);
        }
        /** @var JwtAuth $jwtAuth */
        $jwtAuth = app()->make(JwtAuth::class);
        //设置解析token
        [$id, $type] = $jwtAuth->parseToken($token);

        //验证token
        try {
            $jwtAuth->verifyToken();
        } catch (ExpiredException $e) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_INVALID, $code);
        } catch (\Throwable $e) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_INVALID, $code);
        }

        //获取管理员信息
        $adminInfo = $this->dao->get($id);
        if (!$adminInfo || !$adminInfo->id) {
            throw new AuthException(ApiErrorCode::ERR_LOGIN_STATUS, $code);
        }

        $adminInfo->type = $type;

        return $adminInfo->hidden(['password', 'ip', 'status']);
    }

    /**
     * 检测有没有人扫描登录
     * @param string $key
     * @return array|int[]
     * @throws InvalidArgumentException
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function scanLogin(string $key)
    {
        $hasKey = CacheService::has($key);
        if ($hasKey === false) {
            $status = 0;//不存在需要刷新二维码
        } else {
            $keyValue = CacheService::get($key);
            if ($keyValue === '0') {
                $status = 1;//正在扫描中
                $kefuInfo = $this->dao->get(['uniqid' => $key], ['account', 'uniqid']);
                if ($kefuInfo) {
                    $tokenInfo = $this->authLogin($kefuInfo->account);
                    $tokenInfo['status'] = 3;
                    $kefuInfo->uniqid = '';
                    $kefuInfo->save();
                    CacheService::delete($key);
                    return $tokenInfo;
                }
            } else {
                $status = 2;//没有扫描
            }
        }
        return ['status' => $status];
    }
}
