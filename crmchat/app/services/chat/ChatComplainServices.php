<?php

namespace app\services\chat;


use app\dao\chat\ChatComplainDao;
use crmeb\basic\BaseServices;

/**
 * Class ChatComplainServices
 * @package app\services\chat
 */
class ChatComplainServices extends BaseServices
{

    /**
     * ChatComplainServices constructor.
     * @param ChatComplainDao $dao
     */
    public function __construct(ChatComplainDao $dao)
    {
        $this->dao = $dao;
    }

}
