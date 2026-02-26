package com.sun.guardian.slow.api.core.domain.record;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 慢接口记录实体，记录单次超阈值的执行信息
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-26 18:49
 */
@AllArgsConstructor
@Data
public class SlowApiRecord {
    private final String uri;
    private final long duration;
    private final long timestamp;

}
