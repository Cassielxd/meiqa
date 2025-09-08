<?php

use think\migration\Migrator;

class Qrcode extends Migrator
{
    /**
     * Change Method.
     *
     * Write your reversible migrations using this method.
     *
     * More information on writing migrations is available here:
     * http://docs.phinx.org/en/latest/migrations.html#the-abstractmigration-class
     *
     * The following commands can be used in this method and Phinx will
     * automatically reverse them when rolling back:
     *
     *    createTable
     *    renameTable
     *    addColumn
     *    renameColumn
     *    addIndex
     *    addForeignKey
     *
     * Remember to call "create()" or "update()" and NOT "save()" when working
     * with the Table class.
     */
    public function change()
    {
        $table = $this->table('qrcode', ['comment' => '二维码']);
        $table->addColumn('name', 'string', ['limit' => 255, 'default' => 0, 'comment' => '二维码名称']);
        $table->addColumn('url', 'string', ['limit' => 255, 'default' => 0, 'comment' => '地址']);
        $table->addColumn('user_ids', 'string', ['limit' => 255, 'default' => 0, 'comment' => '用户ids']);
        $table->addColumn('appid', 'string', ['limit' => 35, 'default' => '', 'comment' => 'APPID']);
        $table->addColumn('sort', 'integer', ['limit' => 10, 'default' => '', 'comment' => '排序']);
        $table->addColumn('create_time', 'timestamp', ['default' => 'CURRENT_TIMESTAMP', 'comment' => '添加时间']);
        $table->create();
    }
}
