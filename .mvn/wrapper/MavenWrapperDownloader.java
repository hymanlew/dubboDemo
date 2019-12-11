/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.Properties;

/**
 * maven-wrapper：
 * 它是使用 https://start.spring.io/ 生成Spring Boot 初始项目时，自动生成的文件（包括 mvnw 文件）。
 *
 * 先来看看传统maven的使用流程：
 * 传统使用maven需要先到官网上下载。
 * 配置环境变量把mvn可执行文件路径加入到环境变量，以便之后使用直接使用mvn命令。
 * 另外项目pom.xml文件描述的依赖文件默认是下载在用户目录下的.m2文件下的repository目录下。
 * 再次，如果需要更换maven的版本，需要重新下载maven并替换环境变量path中的maven路径。
 *
 * 现在有了maven-wrapper，会获得以下特性：
 * 执行mvnw比如mvnw clean ，如果本地没有匹配的maven版本，直接会去下载maven，放在用户目录下的.m2/wrapper中。
 * 并且项目的依赖的jar包会直接放在项目目录下的repository目录，这样可以很清晰看到当前项目的依赖文件。
 * 如果需要更换maven的版本，只需要更改项目当前目录下.mvn/wrapper/maven-wrapper.properties的distributionUrl属性值，更换对应
 * 版本的maven下载地址。mvnw命令就会自动重新下载maven。
 * 可以说带有mvnw文件的项目，除了额外需要配置 java环境外，只需要使用本项目的mvnw脚本就可以完成编译，打包，发布等一系列操作。
 *
 * idea对maven-wrapper的支持：
 * idea提供了插件maven-wrapper-support 这个插件会监测项目下的.mvn/wrapper/maven-wrapper.properties中的distributionUrl属性
 * 值，且自动下载maven版本到用户目录.m2/wrapper目录中，并且改变setting->build->build Tools ->maven-> maven home directory
 * 的值。 但是这个插件并不会改变setting->build->build Tools->maven->Local repository的值；点击Navigation Bar中的maven projectjs
 * 中的命令，执行的命令是原生mvn的命令，而不是项目中下的mvnw命令。
 *
 * 自己的看法：
 * maven wrapper可以自动下载maven，但实际上我们常用的idea软件都自带了maven。
 * 且如果用上了idea，一般习惯也是直接使用Navigation Bar执行maven命令比较方便。
 * maven wrapper根据配置自动切换maven版本。这个看起来很有用，但实际上maven版本也是很稳定。很少会出现需要切换maven版本的情况。
 * 使用mvnw命令会在直接当前项目下生成repository，看起来每一个项目独立了repository，很模块化的样子。但是这样不仅浪费了磁盘空间，
 * 且实际上开发中并不关心repository，idea会自动有external librayies目录提供查看依赖的jar包。
 * 当然，如果纯命令行工作，这会是个不错的选择。
 */
public class MavenWrapperDownloader {

    private static final String WRAPPER_VERSION = "0.5.5";
    /**
     * Default URL to download the maven-wrapper.jar from, if no 'downloadUrl' is provided.
     */
    private static final String DEFAULT_DOWNLOAD_URL = "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/"
            + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

    /**
     * Path to the maven-wrapper.properties file, which might contain a downloadUrl property to
     * use instead of the default one.
     */
    private static final String MAVEN_WRAPPER_PROPERTIES_PATH =
            ".mvn/wrapper/maven-wrapper.properties";

    /**
     * Path where the maven-wrapper.jar will be saved to.
     */
    private static final String MAVEN_WRAPPER_JAR_PATH =
            ".mvn/wrapper/maven-wrapper.jar";

    /**
     * Name of the property which should be used to override the default download url for the wrapper.
     */
    private static final String PROPERTY_NAME_WRAPPER_URL = "wrapperUrl";

    public static void main(String args[]) {
        System.out.println("- Downloader started");
        File baseDirectory = new File(args[0]);
        System.out.println("- Using base directory: " + baseDirectory.getAbsolutePath());

        // If the maven-wrapper.properties exists, read it and check if it contains a custom
        // wrapperUrl parameter.
        File mavenWrapperPropertyFile = new File(baseDirectory, MAVEN_WRAPPER_PROPERTIES_PATH);
        String url = DEFAULT_DOWNLOAD_URL;
        if (mavenWrapperPropertyFile.exists()) {
            FileInputStream mavenWrapperPropertyFileInputStream = null;
            try {
                mavenWrapperPropertyFileInputStream = new FileInputStream(mavenWrapperPropertyFile);
                Properties mavenWrapperProperties = new Properties();
                mavenWrapperProperties.load(mavenWrapperPropertyFileInputStream);
                url = mavenWrapperProperties.getProperty(PROPERTY_NAME_WRAPPER_URL, url);
            } catch (IOException e) {
                System.out.println("- ERROR loading '" + MAVEN_WRAPPER_PROPERTIES_PATH + "'");
            } finally {
                try {
                    if (mavenWrapperPropertyFileInputStream != null) {
                        mavenWrapperPropertyFileInputStream.close();
                    }
                } catch (IOException e) {
                    // Ignore ...
                }
            }
        }
        System.out.println("- Downloading from: " + url);

        File outputFile = new File(baseDirectory.getAbsolutePath(), MAVEN_WRAPPER_JAR_PATH);
        if (!outputFile.getParentFile().exists()) {
            if (!outputFile.getParentFile().mkdirs()) {
                System.out.println(
                        "- ERROR creating output directory '" + outputFile.getParentFile().getAbsolutePath() + "'");
            }
        }
        System.out.println("- Downloading to: " + outputFile.getAbsolutePath());
        try {
            downloadFileFromURL(url, outputFile);
            System.out.println("Done");
            System.exit(0);
        } catch (Throwable e) {
            System.out.println("- Error downloading");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void downloadFileFromURL(String urlString, File destination) throws Exception {
        if (System.getenv("MVNW_USERNAME") != null && System.getenv("MVNW_PASSWORD") != null) {
            String username = System.getenv("MVNW_USERNAME");
            char[] password = System.getenv("MVNW_PASSWORD").toCharArray();
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        URL website = new URL(urlString);
        ReadableByteChannel rbc;
        rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(destination);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

}
