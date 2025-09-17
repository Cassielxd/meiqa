<?php
declare (strict_types = 1);

namespace app\controller\tenant;

use app\Request;
use app\services\tenant\TenantLoginServices;
use app\services\tenant\TenantServices;
use app\validates\tenant\TenantLoginValidate;
use app\validates\tenant\TenantRegisterValidate;
use crmeb\utils\Captcha;
use think\Response;

/**
 * 租户登录
 * Class Login
 * @package app\controller\tenant
 */
class Login
{
    /**
     * @var Request
     */
    protected $request;

    /**
     * @var TenantLoginServices
     */
    protected $services;

    /**
     * @var TenantServices
     */
    protected $tenantServices;

    /**
     * Login constructor.
     * @param TenantLoginServices $services
     * @param TenantServices $tenantServices
     */
    public function __construct(TenantLoginServices $services, TenantServices $tenantServices)
    {
        $this->services = $services;
        $this->tenantServices = $tenantServices;
        $this->request = app()->request;
    }

    /**
     * 租户登录
     * @return mixed
     */
    public function login()
    {
        [$account, $password] = $this->request->postMore([
            'account',
            'pwd',
        ], true);

        validate(TenantLoginValidate::class)->scene('login')->check(['account' => $account, 'pwd' => $password]);

        return app('json')->success($this->services->login($account, $password));
    }
    public function captcha()
    {
        return app('json')->success(app()->make(Captcha::class)->create([], true));
    }

    public function ajcaptcha()
    {
        $captchaType = $this->request->get('captchaType', 'blockPuzzle');
        return app('json')->success(aj_captcha_create($captchaType));
    }

    public function ajcheck()
    {
        [$token, $pointJson, $captchaType] = $this->request->postMore([
            ['token', ''],
            ['pointJson', ''],
            ['captchaType', ''],
        ], true);
        try {
            aj_captcha_check_one($captchaType, $token, $pointJson);
            return app('json')->success();
        } catch (\Throwable $e) {
            return app('json')->fail('滑块验证失败');
        }
    }

    /**
     * 获取租户信息
     * @return mixed
     */
    public function info()
    {
        return app('json')->success($this->services->getLoginInfo());
    }

    /**
     * 退出登录
     * @return mixed
     */
    public function logout()
    {
        return app('json')->success('退出成功');
    }

    /**
     * 修改密码
     * @return mixed
     */
    public function changePassword()
    {
        [$oldPassword, $newPassword, $confirmPassword] = $this->request->postMore([
            'old_password',
            'new_password',
            'confirm_password',
        ], true);

        if ($newPassword !== $confirmPassword) {
            return app('json')->fail('两次密码输入不一致');
        }

        validate(TenantLoginValidate::class)->scene('change_password')->check([
            'old_password' => $oldPassword,
            'new_password' => $newPassword,
        ]);

        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return app('json')->fail('请先登录');
        }

        try {
            $this->services->changePassword((int)$tenantInfo['id'], $oldPassword, $newPassword);
            return app('json')->success('密码修改成功，请重新登录');
        } catch (\Exception $e) {
            return app('json')->fail($e->getMessage());
        }
    }

    /**
     * 发送注册验证码
     * @return mixed
     */
    public function sendCaptcha()
    {
        $email = $this->request->post('email', '');
        
        if (!$email) {
            return app('json')->fail('请输入邮箱地址');
        }
        
        try {
            $result = $this->tenantServices->sendRegisterCaptcha($email);
            
            // 如果是开发环境，返回验证码
            if (is_array($result) && isset($result['captcha'])) {
                return app('json')->success($result['message']);
            }
            
            return app('json')->success('验证码发送成功');
        } catch (\Exception $e) {
            return app('json')->fail($e->getMessage());
        }
    }

    /**
     * 租户注册
     * @return mixed
     */
    public function register()
    {
        $data = $this->request->post();

        // 验证数据
        try {
            validate(TenantRegisterValidate::class)->scene('register')->check($data);
        } catch (\Exception $e) {
            return app('json')->fail($e->getMessage());
        }
        $data["contact_email"]=$data["account"];
        try {
            $result = $this->tenantServices->register($data);
            return app('json')->success($result['message'], [
                'tenant_id' => $result['tenant_id'],
                'tenant_code' => $result['tenant_code'],
                'status' => $result['status']
            ]);
        } catch (\Exception $e) {
            return app('json')->fail($e->getMessage());
        }
    }

}