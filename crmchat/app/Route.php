<?php
namespace app;


use think\route\Url as UrlBuild;

class Route extends \think\Route
{
    /**
     * URL生成 支持路由反射
     * @access public
     * @param string $url 路由地址
     * @param array $vars 参数 ['a'=>'val1', 'b'=>'val2']
     * @return UrlBuild
     */
    public function buildUrl(string $url = '', array $vars = []): UrlBuild
    {
        $str = substr($url, 0, 1);
        if ($str != '/') $url = '/' . $url;
        return parent::buildUrl($url, $vars);
    }
}
