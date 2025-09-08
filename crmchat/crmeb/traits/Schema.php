<?php

namespace crmeb\utils;


use think\migration\Migrator;

/**
 * Trait Schema
 * @package crmeb\utils
 * @mixin Migrator
 */
trait Schema
{

    public function create(string $table, \Closure $callback)
    {
        $table = $this->table($table);

        $blueprint = new Blueprint($table);

        $callback($blueprint);

        $table->setEngine($blueprint->engine);
        $table->setCollation($blueprint->collation);
        $table->setComment($blueprint->comment);

        $table->create();
    }
}
