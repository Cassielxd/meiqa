<?php

namespace crmeb\command;


use think\console\Command;
use think\console\Input;
use think\console\Output;

class Update extends Command
{

    protected function configure()
    {
        $this->setName('chat:update')->setDescription('命令行更新');
    }

    protected function execute(Input $input, Output $output)
    {

    }
}
