package com.sun.guardian.anti.replay.core.domain.rule;

import com.sun.guardian.core.domain.BaseRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 防重放攻击规则
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-27 15:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AntiReplayRule extends BaseRule {
}
