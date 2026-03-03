package com.sun.guardian.sign.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import com.sun.guardian.sign.core.annotation.SignVerify;
import com.sun.guardian.sign.core.enums.algorithm.SignAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 参数校验规则（注解和 yml 配置的统一抽象）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-02 20:56
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SignRule extends BaseRule {

    /**
     * 签名算法：base64 /md5 / sha256 / hmac-sha256
     */
    private SignAlgorithm algorithm = SignAlgorithm.BASE64;

    /**
     * 签名校验失败提示信息（支持 i18n Key）
     */
    private String signVerifyMessage = "签名校验失败";

    /**
     * 从 @SignVerify 注解创建规则
     */
    public static SignRule fromAnnotation(SignVerify annotation) {
        return new SignRule()
                .setAlgorithm(annotation.algorithm())
                .setSignVerifyMessage(annotation.signVerifyMessage());
    }
}
