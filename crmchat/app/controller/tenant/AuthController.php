<?php
namespace app\controller\tenant;

use crmeb\traits\Help;
use think\facade\Validate;

/**
 * Class AuthController
 * @package app\controller\admin
 */
abstract class AuthController
{
    use Help;

    /**
     * 当前登陆管理员信息
     * @var
     */
    protected $tenantInfo;

    /**
     * 当前登陆管理员ID
     * @var
     */
    protected $tenantId;



    /**
     * service
     * @var
     */
    protected $services;

    /**
     * @var
     */
    protected $request;

    /**
     * @var object|\think\App
     */
    protected $app;

    /**
     * AuthController constructor.
     */
    public function __construct()
    {
        $this->app     = app();
        $this->request = app()->request;
        $this->initialize();
    }

    /**
     * 初始化
     */
    protected function initialize()
    {
        $this->tenantId   = $this->request->tenantId();
        $this->tenantInfo = $this->request->tenantInfo();
    }

    /**
     * 数据验证
     * @param array $data
     * @param $validate
     * @param null $message
     * @param bool $batch
     * @return bool
     */
    protected function validate(array $data, $validate, $message = null, bool $batch = false)
    {
        if (is_array($validate)) {
            $v = new Validate();
            $v->rule($validate);
        } else {
            if (strpos($validate, '.')) {
                // 支持场景
                list($validate, $scene) = explode('.', $validate);
            }
            $class = false !== strpos($validate, '\\') ? $validate : $this->app->parseClass('validate', $validate);
            $v     = new $class();
            if (!empty($scene)) {
                $v->scene($scene);
            }

            if (is_string($message) && empty($scene)) {
                $v->scene($message);
            }
        }

        if (is_array($message))
            $v->message($message);


        // 是否批量验证
        if ($batch) {
            $v->batch(true);
        }

        return $v->failException(true)->check($data);
    }
}
