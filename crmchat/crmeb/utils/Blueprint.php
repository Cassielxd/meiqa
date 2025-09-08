<?php

namespace crmeb\utils;

use think\migration\db\Table;

class Blueprint
{

    /**
     * @var Table
     */
    protected $table;

    /**
     * 引擎
     * @var string
     */
    public $engine = 'InnoDB';

    /**
     * 字符集
     * @var string
     */
    public $charset = 'utf8';

    /**
     * 数据库字符集
     * @var string
     */
    public $collation = 'utf8_unicode_ci';

    /**
     * Blueprint constructor.
     * @param Table $table
     */
    public function __construct(Table $table)
    {
        $this->table = $table;
    }

    public function string()
    {

    }

    public function tinyInteger()
    {

    }

    public function integer(string $field, int $length = 10)
    {

    }

    public function timestamps($name = null)
    {

    }

    public function index($field, string $name = null)
    {
    }

    public function unique($field, string $name = null)
    {

    }

    public function enum($field)
    {

    }


}
