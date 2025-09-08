<?php

namespace app\dao\chat\user;


use app\models\chat\user\ChatUserGroup;
use crmeb\basic\BaseDao;

/**
 * Class ChatUserGroupDao
 * @package app\dao\chat\user
 */
class ChatUserGroupDao extends BaseDao
{

    /**
     * @return string
     */
    protected function setModel(): string
    {
        return ChatUserGroup::class;
    }
}
