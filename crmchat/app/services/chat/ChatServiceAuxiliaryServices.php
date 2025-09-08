<?php

namespace app\services\chat;


use app\dao\chat\ChatServiceAuxiliaryDao;
use crmeb\basic\BaseServices;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;

/**
 * 辅助
 * Class ChatServiceAuxiliaryServices
 * @package app\services\chat
 */
class ChatServiceAuxiliaryServices extends BaseServices
{

    /**
     * ChatServiceAuxiliaryServices constructor.
     * @param ChatServiceAuxiliaryDao $dao
     */
    public function __construct(ChatServiceAuxiliaryDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * 保存转接信息
     * @param array $data
     * @return bool|mixed
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function saveAuxliary(array $data)
    {
        $auxliaryInfo = $this->dao->get(['type' => 0, 'appid' => $data['appid'], 'binding_id' => $data['binding_id'], 'relation_id' => $data['relation_id']]);
        if ($auxliaryInfo) {
            $auxliaryInfo->relation_id = $data['relation_id'];
            $auxliaryInfo->update_time = time();
            return $auxliaryInfo->save();
        } else {
            return $this->dao->save([
                'type' => 0,
                'appid' => $data['appid'],
                'binding_id' => $data['binding_id'],
                'relation_id' => $data['relation_id'],
                'update_time' => time(),
                'add_time' => time(),
            ]);
        }
    }
}
