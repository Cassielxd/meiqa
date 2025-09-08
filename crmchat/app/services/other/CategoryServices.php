<?php

namespace app\services\other;


use app\dao\other\CategoryDao;
use crmeb\basic\BaseServices;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;

/**
 * 分类
 * Class CategoryServices
 * @package app\services\other
 */
class CategoryServices extends BaseServices
{

    /**
     * CategoryServices constructor.
     * @param CategoryDao $dao
     */
    public function __construct(CategoryDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * 获取分类列表
     * @param array $where
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getCateList(array $where = [], array $field = ['*'])
    {
        [$page, $limit] = $this->getPageValue();
        $data = $this->dao->getCateList($where, $page, $limit, $field);
        $count = $this->dao->count($where);
        return compact('data', 'count');
    }

    /**
     * 投诉
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getComplainList()
    {
        $list = $this->dao->getCateList(['type' => 2], 0, 0);
        return get_tree_children($list, 'children');
    }
}
