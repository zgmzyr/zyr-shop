package com.zyr.shop.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * MD5 加密工具类
 *
 * <p>系统用户密码不允许明文存储，统一使用 MD5 摘要后保存</p>
 *
 * @author zyr
 */
public final class Md5Util {

    /** 私有构造，禁止实例化 */
    private Md5Util() {
    }

    /**
     * 对明文进行 MD5 加密
     *
     * @param raw 明文字符串
     * @return 32 位十六进制 MD5 摘要
     */
    public static String md5(String raw) {
        if (raw == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
