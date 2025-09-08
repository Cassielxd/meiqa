<?php
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2020 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------
namespace app\controller\admin;


use app\validate\system\SystemAdminValidata;
use crmeb\utils\Captcha;
use app\services\system\admin\SystemAdminServices;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;
use think\Request;
use think\Response;

/**
 * 后台登陆
 * Class Login
 * @package app\controller\admin
 */
class Login
{

    /**
     * @var Request
     */
    protected $request;

    /**
     * Login constructor.
     * @param SystemAdminServices $services
     */
    public function __construct(SystemAdminServices $services)
    {
        $this->services = $services;
        $this->request = app()->request;
    }

    /**
     * 验证码
     * @return $this|Response
     */
    public function captcha()
    {
        return app('json')->success(app()->make(Captcha::class)->create([], true));
    }

    /**
     * @return mixed
     */
    public function ajcaptcha()
    {
        $captchaType = $this->request->get('captchaType', 'blockPuzzle');
        return app('json')->success(aj_captcha_create($captchaType));
    }

    /**
     * 一次验证
     * @return mixed
     */
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
     * 登陆
     * @return mixed
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function login()
    {
        [$account, $password, $imgcode, $captchaVerification, $captchaType] = $this->request->postMore([
            'account',
            'pwd',
            ['imgcode', ''],
            ['captchaVerification', ''],
            ['captchaType', '']
        ], true);

        if (!app()->make(Captcha::class)->check($imgcode)) {
            return app('json')->fail('请输入正确的验证码');
        }

        try {
            aj_captcha_check_two($captchaType, $captchaVerification);
        } catch (\Throwable $e) {
            return app('json')->fail('滑块验证失败');
        }

        validate(SystemAdminValidata::class)->scene('get')->check(['account' => $account, 'pwd' => $password]);

        return app('json')->success($this->services->login($account, $password, 'admin'));
    }

    /**
     * 获取后台登录页轮播图以及LOGO
     * @return mixed
     */
    public function info()
    {
        return app('json')->success($this->services->getLoginInfo());
    }
}
