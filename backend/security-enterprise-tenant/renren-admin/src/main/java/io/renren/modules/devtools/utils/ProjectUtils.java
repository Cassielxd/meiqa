package io.renren.modules.devtools.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import io.renren.modules.devtools.entity.ProjectEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目名修改 工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class ProjectUtils {
    /**
     * 需要修改的文件后缀
     */
    public final static String MODIFY_SUFFIX = "java,xml,yml,factories,md,txt";
    /**
     * 排除的文件
     */
    public final static String EXCLUSIONS = ".git,.idea,target,logs";
    /**
     * 分隔符
     */
    public final static String SPLIT = ",";
    /**
     * 项目包名
     */
    public final static String PACKAGE_NAME = "io.renren";
    /**
     * 项目标识
     */
    public final static String PROJECT_CODE = "renren";

    /**
     * 拷贝目录文件
     *
     * @param srcRoot    原文件
     * @param destRoot   目标文件
     * @param exclusions 排除文件
     * @param replaceMap 替换规则
     */
    public static void copyDirectory(File srcRoot, File destRoot, List<String> exclusions, Map<String, String> replaceMap) throws IOException {
        String destPath = destRoot.getPath().replaceAll("\\\\", "/");
        destRoot = new File(replaceData(destPath, replaceMap));

        // 获取排除后的源文件
        File[] srcFiles = CollectionUtil.isEmpty(exclusions) ?
                srcRoot.listFiles() : srcRoot.listFiles(file -> !exclusions.contains(file.getName()));

        if (srcFiles == null) {
            throw new IOException("No files to copy: " + srcRoot);
        }

        for (File srcFile : srcFiles) {
            String fileName = srcFile.getName();
            if (srcFile.isFile()) {
                fileName = replaceData(fileName, replaceMap);
            }
            File destFile = new File(destRoot, fileName);
            if (srcFile.isDirectory()) {
                copyDirectory(srcFile, destFile, exclusions, replaceMap);
            } else {
                FileUtil.copyFile(srcFile, destFile);
            }
        }
    }

    /**
     * 内容格式化
     *
     * @param rootFile   文件根目录
     * @param suffixList 需要格式化的文件后缀
     * @param replaceMap 替换规则
     */
    public static void contentFormat(File rootFile, List<String> suffixList, Map<String, String> replaceMap) {
        List<File> destList = FileUtil.loopFiles(rootFile, file -> suffixList.contains(FileNameUtil.getSuffix(file)));

        for (File dest : destList) {
            List<String> lines = FileUtil.readUtf8Lines(dest);
            List<String> newList = new ArrayList<>();

            for (String line : lines) {
                newList.add(replaceData(line, replaceMap));
            }

            FileUtil.writeUtf8Lines(newList, dest);
        }
    }

    /**
     * 生成临时路径
     */
    public static String getTmpDirPath(String... names) {
        StringBuilder tmpPath = new StringBuilder(FileUtil.getTmpDirPath());
        tmpPath.append("generator");
        tmpPath.append(File.separator);
        tmpPath.append(System.currentTimeMillis());

        for (String name : names) {
            tmpPath.append(File.separator).append(name);
        }

        return tmpPath.toString();
    }

    /**
     * 替换数据
     *
     * @param str 待替换的字符串
     * @param map 替换的kv集合
     * @return 返回替换后的数据
     */
    private static String replaceData(String str, Map<String, String> map) {
        for (String key : map.keySet()) {
            str = str.replaceAll(key, map.get(key));
        }
        return str;
    }

    public static byte[] download(ProjectEntity project) throws IOException {
        // 原项目根路径
        File srcRoot = new File(project.getProjectPath());

        // 临时项目根路径
        File destRoot = new File(ProjectUtils.getTmpDirPath(project.getNewProjectName()));

        // 排除的文件
        List<String> exclusions = StrUtil.split(EXCLUSIONS, ProjectUtils.SPLIT);

        // 获取替换规则
        Map<String, String> replaceMap = getReplaceMap(project);

        // 拷贝项目到新路径，并替换路径和文件名
        ProjectUtils.copyDirectory(srcRoot, destRoot, exclusions, replaceMap);

        // 需要替换的文件后缀
        List<String> suffixList = StrUtil.split(MODIFY_SUFFIX, ProjectUtils.SPLIT);

        // 替换文件内容数据
        ProjectUtils.contentFormat(destRoot, suffixList, replaceMap);

        // 生成zip文件
        File zipFile = ZipUtil.zip(destRoot);

        byte[] data = FileUtil.readBytes(zipFile);

        // 清空文件
        FileUtil.clean(destRoot.getParentFile().getParentFile());

        return data;
    }

    /**
     * 获取替换规则
     */
    private static Map<String, String> getReplaceMap(ProjectEntity project) {
        Map<String, String> map = new LinkedHashMap<>();

        // 项目路径替换
        String srcPath = "src/main/java/" + PACKAGE_NAME.replaceAll("\\.", "/");
        String destPath = "src/main/java/" + project.getNewProjectPackage().replaceAll("\\.", "/");
        map.put(srcPath, destPath);

        // 项目包名替换
        map.put(PACKAGE_NAME, project.getNewProjectPackage());

        // 项目名称替换
        map.put(project.getProjectName(), project.getNewProjectName());

        // 项目标识替换
        map.put(PROJECT_CODE, project.getNewProjectCode());
        map.put(StrUtil.upperFirst(PROJECT_CODE), StrUtil.upperFirst(project.getNewProjectCode()));

        return map;
    }
}
