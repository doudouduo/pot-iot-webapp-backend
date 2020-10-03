package com.pot.iot.webapp.Util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@Component
public class RS256Util {
    public static RSAKey getKey() throws JOSEException {
        RSAKeyGenerator rsaKeyGenerator = new RSAKeyGenerator(2048);
        RSAKey rsaJWK = rsaKeyGenerator.generate();
        return rsaJWK;
    }

    public static long ACTIVATION_EXPIRE_TIME = 1000 * 48 * 60 * 60;//48h
    public static long LOGIN_EXPIRE_TIME = 1000 * 24 * 60 * 60;//24h
    public static long RESET_PASSWORD_EXPIRE_TIME = 1000 * 30 * 60;//30min
    private static RSAKey rsaKey;
    private static RSAKey publicRsaKey;

    static {
        /**
         * 生成公钥，公钥是提供出去，让使用者校验token的签名
         */
        try {
            rsaKey = new RSAKeyGenerator(2048).generate();
            publicRsaKey = rsaKey.toPublicJWK();

        } catch (JOSEException e) {
            e.printStackTrace();
        }
    }


    public static String buildToken(String account,String type) {
        try {
            /**
             * 1. 生成秘钥,秘钥是token的签名方持有，不可对外泄漏
             */
            RSASSASigner rsassaSigner = new RSASSASigner(rsaKey);
            /**
             * 2. 建立payload 载体
             */
            long EXPIRE_TIME=0;
            if (type.equals("ACTIVATION")){
                EXPIRE_TIME=ACTIVATION_EXPIRE_TIME;
            }
            else if (type.equals("LOGIN")){
                EXPIRE_TIME=LOGIN_EXPIRE_TIME;
            }
            else if (type.equals("RESET_PASSWORD")){
                EXPIRE_TIME=RESET_PASSWORD_EXPIRE_TIME;
            }

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("doi")
                    .issuer("http://www.doiduoyi.com")
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                    .claim("ACCOUNT",account)
                    .build();

            /**
             * 3. 建立签名
             */
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
            signedJWT.sign(rsassaSigner);

            /**
             * 4. 生成token
             */
            String token = signedJWT.serialize();
            return token;

        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            //添加私密钥匙 进行解密
            RSASSAVerifier rsassaVerifier = new RSASSAVerifier(publicRsaKey);

            //校验是否有效
            if (!jwt.verify(rsassaVerifier)) {
                return "Token 无效";
            }

            //校验超时
            if (new Date().after(jwt.getJWTClaimsSet().getExpirationTime())) {
                return "Token 已过期";
            }

            //获取载体中的数据
            Object account = jwt.getJWTClaimsSet().getClaim("ACCOUNT");


            //是否有openUid
            if (Objects.isNull(account)){
                return "账号为空";
            }
            return account.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return "";
    }
}
