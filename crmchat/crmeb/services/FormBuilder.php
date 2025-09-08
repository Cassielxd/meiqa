<?php

namespace crmeb\services;

use FormBuilder\Factory\Iview as Form;

/**
 * Form Builder
 * Class FormBuilder
 * @package crmeb\services
 */
class FormBuilder extends Form
{

    public static function setOptions($call)
    {
        if (is_array($call)) {
            return $call;
        } else {
            return $call();
        }

    }


}
