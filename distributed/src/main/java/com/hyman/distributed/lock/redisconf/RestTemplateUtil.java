package com.hyman.distributed.lock.redisconf;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * RestTemplate是Spring的模板类，在客户端上可以使用该类调用Web服务器端的服务，它支持REST风格的URL。在Spring中有许多类似功能的类，
 * 如JdbcTemplate, JmsTemplate等。
 * RestTemplate可以用GET方法来获取资源，或者用POST方法来创建资源。
 *
 * RestTemplate 是 Spring 提供的用于访问Rest服务的客户端，它提供了多种便捷访问远程Http服务的方法,能够大大提高客户端的编写效率。
 * 调用 RestTemplate 的默认构造函数，RestTemplate 对象在底层通过使用 java.net 包下的实现创建 HTTP 请求，
 * 可以通过使用ClientHttpRequestFactory指定不同的HTTP请求方式。
 *
 * ClientHttpRequestFactory接口主要提供了两种实现方式：
 * 1、一种是SimpleClientHttpRequestFactory，使用J2SE提供的方式（既java.net包提供的方式）创建底层的Http请求连接。
 * 2、一种方式是使用HttpComponentsClientHttpRequestFactory方式，底层使用HttpClient访问远程的Http服务，使用HttpClient可以配置连接池和证书等信息。
 */
public class RestTemplateUtil {

    public static RestTemplate getInstance(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }
}
