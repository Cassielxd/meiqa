<?php

return [
    //默认上传模式
    'default'  => 'local',
    //上传文件大小
    'filesize' => 20971520,
    //上传文件后缀类型
    'fileExt'  => ['jpg', 'wgt', 'jpeg', 'png', 'gif', 'pem', 'mp3', 'wma', 'wav', 'amr', 'mp4', 'key', 'xlsx', 'xls'],
    //上传文件类型
    'fileMime' => [
        'image/jpeg',
        'image/gif',
        'image/png',
        'text/plain',
        'audio/mpeg',
        'application/octet-stream',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-works',
        'application/vnd.ms-excel',
        'application/zip',
        'application/vnd.ms-excel',
        'application/vnd.ms-excel',
        'text/xml',
    ],
    //驱动模式
    'stores'   => [
        //本地上传配置
        'local' => [],
        //七牛云上传配置
        'qiniu' => [],
        //oss上传配置
        'oss'   => [],
        //cos上传配置
        'cos'   => [],
    ]
];
