/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.controller;

import cn.hutool.core.io.IoUtil;
import io.renren.modules.devtools.entity.ProjectEntity;
import io.renren.modules.devtools.utils.ProjectUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 项目名修改
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("devtools/project")
public class ProjectController {

    @GetMapping
    public void project(ProjectEntity project, HttpServletResponse response) throws Exception {
        byte[] data = ProjectUtils.download(project);

        response.setHeader("Content-Disposition", "attachment; filename=\"" + project.getNewProjectName() + ".zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IoUtil.write(response.getOutputStream(), false, data);
    }
}