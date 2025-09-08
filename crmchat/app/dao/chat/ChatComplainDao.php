<?php

namespace app\dao\chat;


use app\models\chat\ChatComplain;
use crmeb\basic\BaseDao;

class ChatComplainDao extends BaseDao
{

    protected function setModel(): string
    {
        return ChatComplain::class;
    }
}
