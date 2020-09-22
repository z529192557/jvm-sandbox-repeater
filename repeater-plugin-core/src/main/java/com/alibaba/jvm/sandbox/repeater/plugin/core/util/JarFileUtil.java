package com.alibaba.jvm.sandbox.repeater.plugin.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuangpeng
 * @since 2020/9/21
 */
public class JarFileUtil {

    private final static Logger log = LoggerFactory.getLogger(JarFileUtil.class);

    private final static String JAR_FILE_SUFFIX = ".jar";
    /**
     * 获取jar的urls
     *
     * @param jarFilePath 插件路径
     * @return 插件URL列表
     */
    public static URL[] getURLs(String jarFilePath,String tempFilePrefix) {
        File file = new File(jarFilePath);
        List<URL> jarPaths = Lists.newArrayList();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {return jarPaths.toArray(new URL[0]);}
            for (File jarFile : files) {
                if (isJar(jarFile)) {
                    try {
                        File tempFile = File.createTempFile(tempFilePrefix, JAR_FILE_SUFFIX);
                        tempFile.deleteOnExit();
                        FileUtils.copyFile(jarFile, tempFile);
                        jarPaths.add(new URL("file:" + tempFile.getPath()));
                    } catch (IOException e) {
                        log.error("error occurred when get jar file", e);
                    }
                } else {
                    jarPaths.addAll(Arrays.asList(getURLs(jarFile.getAbsolutePath(),tempFilePrefix)));
                }
            }
        } else if (isJar(file)) {
            try {
                File tempFile = File.createTempFile("repeater_plugin", JAR_FILE_SUFFIX);
                FileUtils.copyFile(file, tempFile);
                jarPaths.add(new URL("file:" + tempFile.getPath()));
            } catch (IOException e) {
                log.error("error occurred when get jar file", e);
            }
            return jarPaths.toArray(new URL[0]);
        } else {
            log.error("plugins jar path has no available jar, use empty url, path={}", jarFilePath);
        }
        return jarPaths.toArray(new URL[0]);
    }

    /**
     * @param file
     * @return
     */
    private static boolean isJar(File file) {
        return file.isFile() && file.getName().endsWith(JAR_FILE_SUFFIX);
    }
}
