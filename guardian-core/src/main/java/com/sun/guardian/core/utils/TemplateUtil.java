package com.sun.guardian.core.utils;

import cn.hutool.core.bean.BeanUtil;

import java.util.Map;

/**
 * 命名占位符模板工具类（{fieldName} → Bean 字段值）
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12
 */
public class TemplateUtil {

    private TemplateUtil() {}

    /** 将 {fieldName} 占位符替换为 Bean 字段值 */
    public static String formatByBean(String template, Object bean) {
        Map<String, Object> params = BeanUtil.beanToMap(bean, false, true);
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}",
                    entry.getValue() != null ? String.valueOf(entry.getValue()) : "");
        }
        return result;
    }
}
