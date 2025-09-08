<?php

namespace app\dao\other;


use app\models\other\AppVersion;
use crmeb\basic\BaseDao;

class AppVersionDao extends BaseDao
{

    protected function setModel(): string
    {
        return AppVersion::class;
    }

    /**
     * @param array $where
     * @param string $field
     * @return mixed
     */
    public function max(array $where = [], string $field = 'verisons_num')
    {
        return $this->search($where)->max($field);
    }

    /**
     * @param string $version
     * @return array|\think\Model|null
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getVersion(string $version)
    {
        return $this->getModel()->whereNull('delete_time')->where('verisons_num', '>', $version)->order('verisons_num desc')->find();
    }
}
