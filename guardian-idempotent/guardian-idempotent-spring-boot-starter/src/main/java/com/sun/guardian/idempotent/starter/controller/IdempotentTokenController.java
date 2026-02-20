package com.sun.guardian.idempotent.starter.controller;

import com.sun.guardian.core.domain.BaseResult;
import com.sun.guardian.idempotent.core.service.token.IdempotentTokenService;
import com.sun.guardian.idempotent.starter.domain.IdempotentTokenControllerResp;
import com.sun.guardian.idempotent.starter.properties.GuardianIdempotentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口幂等Token获取接口
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-19 15:40
 */
@RestController
@RequestMapping("/guardian/idempotent")
@RequiredArgsConstructor
public class IdempotentTokenController {
    private final IdempotentTokenService tokenService;
    private final GuardianIdempotentProperties properties;

    /**
     * 获取幂等Token
     */
    @GetMapping("/token")
    public BaseResult getToken(@RequestParam String key) {
        String token = tokenService.createToken(key);

        return BaseResult.success(new IdempotentTokenControllerResp()
                .setToken(token)
                .setExpireIn(properties.getTimeout())
                .setExpireUnit(properties.getTimeUnit().name()));
    }
}
