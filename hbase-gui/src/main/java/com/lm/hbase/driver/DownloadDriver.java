package com.lm.hbase.driver;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.lm.hbase.common.Env;
import com.lm.hbase.util.network.HttpURLConnectionFactory;

/**
 * 根据提供的版本号去中央仓库下载指定版本的hbase client驱动
 * 
 * @author limin Apr 22, 2019 11:06:03 PM
 */
public class DownloadDriver {

    private static String getPomUrl(String version) {
        StringBuilder url = new StringBuilder("http://central.maven.org/maven2/org/apache/hbase/hbase-client");
        url.append("/" + version + "/");
        url.append("hbase-client-");
        url.append(version);
        url.append(".pom");
        return url.toString();
    }

    private static String getJarUrl(String version) {
        StringBuilder url = new StringBuilder("http://central.maven.org/maven2/org/apache/hbase/hbase-client");
        url.append("/" + version + "/");
        url.append("hbase-client-");
        url.append(version);
        url.append(".jar");
        return url.toString();
    }

    public static boolean load(String version, String mavenHome) throws Throwable {

        String outputDir = Env.DRIVER_DIR + version;
        // XXX 如果驱动存在则不处理。后续完善可以添加校验功能
        File outputFileDir = new File(outputDir);
        if (outputFileDir.exists()) {
            return true;
        } else {
            // 创建版本目录
            outputFileDir.mkdir();

        }

        // 下载pom已经client jar
        System.out.println("download pom file to " + outputDir);
        HttpURLConnection con = HttpURLConnectionFactory.getConn(getPomUrl(version));
        HttpURLConnectionFactory.downloadFile(con, outputDir, "pom.xml");
        con = HttpURLConnectionFactory.getConn(getJarUrl(version));
        HttpURLConnectionFactory.downloadFile(con, outputDir, "hbase-client-" + version + ".jar");
        System.out.println("download pom file success");

        File pomFile = new File(outputDir);
        StringBuilder cmd = new StringBuilder("dependency:copy-dependencies -DoutputDirectory=");
        cmd.append(outputDir);

        // 执行maven命令下载并拷贝依赖jar
        return executeMavenCmd(cmd.toString(), pomFile, mavenHome);
    }

    public static boolean executeMavenCmd(String cmd, File pomFile, String mavenHome) {
        System.out.println("download dependency jar." + cmd);
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Collections.singletonList(cmd));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenHome));

        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() == 0) {
                System.out.println("download dependency jar success");
                return true;
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}