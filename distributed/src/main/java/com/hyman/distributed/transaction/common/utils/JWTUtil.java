package com.hyman.distributed.transaction.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt 工具类
 * JJWT 是一个提供端到端的 JWT 创建和验证的 Java库，是在JVM上创建和验证JSON Web Token(JWTs)的库。
 * JJWT是基于JWT、JWS、JWE、JWK和JWA RFC规范的Java实现。
 *
 * JSON Web Token（JWT）是目前最流行的跨域身份验证解决方案。JWT的原则是在服务器身份验证之后，将生成一个JSON对象并将其发送回用户。
 * 它定义了一种紧凑且独立的方式，可以在各方之间作为 JSON 对象安全地传输信息。此信息可以通过数字签名进行验证和信任。JWT可以使用秘密
 * （使用HMAC算法）或使用RSA或ECDSA的公钥/私钥对进行签名。
 * 虽然JWT可以加密以在各方之间提供保密，但只将专注于签名令牌。签名令牌可以验证其中包含的声明的完整性，而加密令牌则隐藏其他方的声明。
 * 当使用公钥/私钥对签署令牌时，签名还证明只有持有私钥的一方是签署私钥的一方。
 * 通俗来讲，JWT是一个含签名并携带用户相关信息的加密串，页面请求校验登录接口时，请求头中携带JWT串到后端服务，后端通过签名加密串匹配
 * 校验，保证信息未被篡改。校验通过则认为是可靠的请求，将正常返回数据。
 *
 * @author hyman
 * @date 2019/6/6 9:13 下午
 */
@Component
@Slf4j
public class JWTUtil {

    /**
     * 加密SECRET
     */
    @Value("${spring-boot-api.jwt.secret}")
    private String secret;
    /**
     * 过期时间
     */
    @Value("${spring-boot-api.jwt.expire-second}")
    private Long expireSecond;

    /**
     * 创建token
     * @param map
     * @return
     */
    public String createJWT(Map map) {
        Map<String, Object> header = new HashMap<>(2);
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .setClaims(map)
                .setHeader(header)
                .setExpiration(Date.from(now.plus(expireSecond, ChronoUnit.SECONDS)))
                .setIssuedAt(Date.from(now))
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode(secret))
                .compact();

        return Base64.getEncoder().encodeToString(jwt.getBytes());
    }


    /**
     * 校验token
     * @param jwtToken
     * @return
     */
    public boolean verify(String jwtToken) {
        try {
            byte[] b = Base64.getDecoder().decode(jwtToken);
            String base64jwt = new String(b);
            Jwts.parser().setSigningKey(TextCodec.BASE64.encode(secret)).parse(base64jwt);
            return true;
        } catch (ExpiredJwtException var3) {
            log.error(var3.getMessage(), var3);
        } catch (MalformedJwtException var4) {
            log.error(var4.getMessage(), var4);
        } catch (SignatureException var5) {
            log.error(var5.getMessage(), var5);
        } catch (Exception var6) {
            log.error(var6.getMessage(), var6);
        }

        return false;
    }

    /**
     * Token解密
     * @param jwtToken
     * @return
     */
    public Map getJWTData(String jwtToken) {
        try {
            byte[] b = Base64.getDecoder().decode(jwtToken);
            String base64jwt = new String(b);
            Jwt jwt = Jwts.parser().setSigningKey(TextCodec.BASE64.encode(secret)).parse(base64jwt);
            return (Map)jwt.getBody();
        } catch (ExpiredJwtException var4) {
            log.error(var4.getMessage(), var4);
        } catch (MalformedJwtException var5) {
            log.error(var5.getMessage(), var5);
        } catch (SignatureException var6) {
            log.error(var6.getMessage(), var6);
        } catch (Exception var7) {
            log.error(var7.getMessage(), var7);
        }

        return null;
    }

    /**
     * 刷新token
     * @param token
     * @return
     */
    public String refreshToken(String token) {
        return verify(token) ? createJWT(getJWTData(token)) : null;
    }
}
