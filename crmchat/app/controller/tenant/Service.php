<?php
declare (strict_types = 1);

namespace app\controller\tenant;

use app\Request;
use app\services\chat\ChatServiceServices;
use app\services\chat\ChatServiceGroupServices;
use crmeb\traits\Help;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;

/**
 * 租户的客服管理控制器
 * Class Service
 * @package app\controller\tenant
 */
class Service
{
    use Help;
    
    /**
     * @var ChatServiceServices
     */
    protected $services;
    
    /**
     * @var Request
     */
    protected $request;

    /**
     * Service constructor.
     * @param ChatServiceServices $services
     */
    public function __construct(ChatServiceServices $services)
    {
        $this->services = $services;
        $this->request = app()->request;
    }

    /**
     * 获取客服列表
     * @return mixed
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function list()
    {
        $where = $this->request->getMore([
            ['nickname', ''],
            ['status', ''],
            ['group_id', ''],
        ]);
        
        // 获取当前租户的APP ID
        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }
        
        // 只查询当前租户APP ID下的客服
        $where['appid'] = $tenantInfo['appid'];
        
        return $this->success($this->services->getServiceList($where));
    }

    /**
     * 获取客服详情
     * @param int $id
     * @return mixed
     */
    public function read($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }
        
        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }
        
        $info = $this->services->get($id);
        // 统一返回"客服不存在"，避免ID遍历攻击
        if (!$info || $info->appid !== $tenantInfo['appid']) {
            return $this->fail('客服不存在');
        }
        
        return $this->success($info);
    }

    /**
     * 创建客服
     * @return mixed
     */
    public function save()
    {
        $data = $this->request->postMore([
            ['group_id', 0],
            ['nickname', ''],
            ['account', ''],
            ['password', ''],
            ['true_password', ''],
            ['phone', ''],
            ['avatar', ''],
            ['welcome_words', ''],
            ['auto_reply', 0],
            ['status', 0],
        ]);

        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }

        // 验证密码
        if ($data['password'] !== $data['true_password']) {
            return $this->fail('两次密码输入不一致');
        }
        
        // 验证必填字段
        if (!$data['nickname'] || !$data['account'] || !$data['password']) {
            return $this->fail('请填写完整信息');
        }

        // 检查账号是否已存在（在当前租户APP ID下）
        if ($this->services->count(['account' => $data['account'], 'appid' => $tenantInfo['appid']])) {
            return $this->fail('账号已存在');
        }

        // 检查当前租户的客服数量是否超限
        $currentCount = $this->services->count(['appid' => $tenantInfo['appid']]);
        if ($currentCount >= $tenantInfo['max_services']) {
            return $this->fail('客服数量已达上限');
        }

        try {
            // 设置APP ID
            $data['appid'] = $tenantInfo['appid'];
            $data['password'] = md5($data['password']);
            unset($data['true_password']);
            
            $service = $this->services->save($data);
            return $this->success('创建成功', ['id' => $service->id]);
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 更新客服
     * @param int $id
     * @return mixed
     */
    public function update($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }

        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }

        $serviceInfo = $this->services->get($id);
        // 统一返回"客服不存在"，避免ID遍历攻击
        if (!$serviceInfo || $serviceInfo->appid !== $tenantInfo['appid']) {
            return $this->fail('客服不存在');
        }

        $data = $this->request->postMore([
            ['group_id', ''],
            ['nickname', ''],
            ['password', ''],
            ['true_password', ''],
            ['phone', ''],
            ['avatar', ''],
            ['welcome_words', ''],
            ['auto_reply', ''],
            ['status', ''],
        ]);

        // 如果修改密码，验证密码一致性
        if ($data['password']) {
            if ($data['password'] !== $data['true_password']) {
                return $this->fail('两次密码输入不一致');
            }
            $data['password'] = md5($data['password']);
        }
        unset($data['true_password']);

        // 过滤空值
        $data = array_filter($data, function($value) {
            return $value !== '';
        });

        try {
            $this->services->update($id, $data);
            return $this->success('更新成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 删除客服
     * @param int $id
     * @return mixed
     */
    public function delete($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }

        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }

        $serviceInfo = $this->services->get($id);
        // 统一返回"客服不存在"，避免ID遍历攻击
        if (!$serviceInfo || $serviceInfo->appid !== $tenantInfo['appid']) {
            return $this->fail('客服不存在');
        }

        try {
            $this->services->delete($id);
            return $this->success('删除成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 更新客服状态
     * @param int $id
     * @return mixed
     */
    public function updateStatus($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }

        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }

        $serviceInfo = $this->services->get($id);
        // 统一返回"客服不存在"，避免ID遍历攻击
        if (!$serviceInfo || $serviceInfo->appid !== $tenantInfo['appid']) {
            return $this->fail('客服不存在');
        }

        $status = $this->request->post('status', 0);
        
        try {
            $this->services->update($id, ['status' => $status]);
            return $this->success('状态更新成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 获取客服分组列表
     * @return mixed
     */
    public function groups()
    {
        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('请先登录');
        }

        /** @var ChatServiceGroupServices $groupServices */
        $groupServices = app()->make(ChatServiceGroupServices::class);
        
        // 只获取当前租户的客服分组
        $list = $groupServices->getGroupList(['appid' => $tenantInfo['appid']]);
        
        return $this->success($list);
    }
}