<?php

namespace app\dao\chat;


use app\dao\other\AuxiliaryDao;

/**
 * 辅助
 * Class ChatServiceAuxiliaryDao
 * @package app\dao\chat
 */
class ChatServiceAuxiliaryDao extends AuxiliaryDao
{
    /**
     * 搜索
     * @param array $where
     * @return \crmeb\basic\BaseModel|mixed|\think\Model
     */
    protected function search(array $where = [])
    {
        return parent::search($where)->where('type', 0);
    }
}
