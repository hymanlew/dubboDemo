package com.hyman.distributed.transaction.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
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
 * 参考官网：https://jwt.io/
 *
 * JWT的数据结构为：A.B.C三部分数据，由字符点"."分割成三部分数据。
 *
 * A-header，头信息
 * B-payload，有效负荷，claims是关于实体（常用的是用户信息）和其他数据的声明，是JWT主体中的编码信息。
 *  一般包括：已注册信息（registered claims），公开数据(public claims)，私有数据(private claims)
 * C-signature，签名信息，是将 header 和 payload 进行加密生成的。
 *
 * 什么情况下使用JWT比较适合？
 * 1，授权：这是最常见的使用场景，解决单点登录 SSO 问题或跨域请求验证 oauth2。因为JWT使用起来轻便，开销小，服务端不用记录用户状态信
 * 息（无状态），所以使用比较广泛；
 * 2，信息交换：JWT是在各个服务之间安全传输信息的好方法。因为JWT可以签名，例如使用公钥/私钥对儿，可以确定请求方是合法的。此外由于使
 * 用标头和有效负载计算签名，还可以验证内容是否未被篡改。
 *
 * 总结：
 * 优点：在非跨域环境下使用JWT机制是一个非常不错的选择，实现方式简单，操作方便，能够快速实现。由于服务端不存储用户状态信息，因此大用
 * 户量，对后台服务也不会造成压力。
 * 缺点：跨域实现相对比较麻烦，安全性也有待探讨。因为JWT令牌返回到页面中，可以使用js获取到，如果遇到XSS攻击令牌可能会被盗取，在JWT还
 * 没超时的情况下，就会被获取到敏感数据信息。
 *
 * JJWT 是在 JVM 上创建和跨域身份验证的库。是基于JWT、JWS、JWE、JWK和JWA RFC规范的Java实现。
 *
 * @author hyman
 * @date 2019/6/6 9:13 下午
 */
@Component
@Slf4j
public class JWTUtil {

    /**
     * 加密SECRET，签名密钥
     */
    @Value("${spring.jwt.secret}")
    private String secret;

    /**
     * 过期时间，超时秒数
     */
    @Value("${spring.jwt.expire-second}")
    private Long expireSecond;

    /**
     * JWT加密的密匙
     */
    @Value("${spring.jwt.datakey}")
    private String datakey;

    /**
     * 创建token
     * @param map
     * @return
     */
    public String createJWT(Map<String, Object> map) {

        // 签名算法，选择 SHA-256
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 将签名密钥常量字符串使用 base64 解码成字节数组
        byte[] secretBytes = DatatypeConverter.parseBase64Binary(secret);

        // 使用 HmacSHA256 签名算法生成一个 HS256的签名秘钥 Key。或使用 TextCodec 加密一个字符串
        String encode = TextCodec.BASE64.encode(secret);
        Key signingKey = new SecretKeySpec(secretBytes, signatureAlgorithm.getJcaName());

        // 添加构成 JWT 的参数
        Map<String, Object> header = new HashMap<>(2);
        header.put("alg", SignatureAlgorithm.HS256.getValue());
        header.put("typ", "JWT");
        Instant now = Instant.now();

        // 对主识别码进行单独加密
        String id = map.get("id").toString();
        id = AESSecretUtil.encryptToStr(id, datakey);
        map.put("id", id);

        String jwt = Jwts.builder()
                .setClaims(map)
                .setHeader(header)
                .setExpiration(Date.from(now.plus(expireSecond, ChronoUnit.SECONDS)))
                .setIssuedAt(Date.from(now))
                .signWith(signatureAlgorithm, signingKey)
                .compact();

        return Base64.getEncoder().encodeToString(jwt.getBytes());
    }


    /**
     * 校验token
     * @param jwtToken token
     * @return boolean
     */
    public boolean verify(String jwtToken) {

        try {
            Map map = getJWTData(jwtToken);
            //解密客户编号
            String decryptUserId = AESSecretUtil.decryptToStr((String)map.get("id"), datakey);
            //retMap = new HashMap<>();
            ////加密后的客户编号
            //retMap.put("userId", decryptUserId);
            ////客户名称
            //retMap.put("userName", claims.get("userName"));
            ////客户端浏览器信息
            //retMap.put("userAgent", claims.get("userAgent"));
            ////刷新JWT
            //retMap.put("freshToken", generateJWT(decryptUserId, (String)claims.get("userName"), (String)claims.get("userAgent"), (String)claims.get("domainName")));

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
     * @param jwtToken token
     * @return map
     */
    public Map getJWTData(String jwtToken) {

        try {
            if (StringUtils.isNotBlank(jwtToken)) {
                byte[] b = Base64.getDecoder().decode(jwtToken);
                String base64jwt = new String(b);
                Map map = (Map) Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secret)).parse(base64jwt).getBody();
                return map;
            }else {
                log.warn("json web token 为空");
            }
        } catch (ExpiredJwtException var4) {
            log.error(var4.getMessage(), var4);
        } catch (MalformedJwtException var5) {
            log.error(var5.getMessage(), var5);
        } catch (SignatureException var6) {
            log.error(var6.getMessage(), var6);
        } catch (Exception e) {
            log.error("JWT解析异常：可能因为token已经超时或非法token");
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
