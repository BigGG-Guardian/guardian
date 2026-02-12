package com.sun.guardian.repeat.submit.core.utils;

import cn.hutool.core.bean.BeanUtil;

import java.util.Map;

/**
 * 命名占位符模板工具类
 * <p>
 * 支持将 {@code {fieldName}} 形式的占位符替换为 Bean 对应字段的值。
 * <pre>
 * 模板: "{servletUri}:{method}:{clientIp}"
 * Bean: servletUri="/order", method="POST", clientIp="127.0.0.1"
 * 结果: "/order:POST:127.0.0.1"
 * </pre>
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-12
 */
public class TemplateUtil {

    private TemplateUtil() {}

    /**
     * 将模板中的 {fieldName} 占位符替换为 Bean 对应字段值
     *
     * @param template 含命名占位符的模板字符串
     * @param bean     数据源对象，字段名需与占位符名称一致
     * @return 替换后的字符串，未匹配的占位符保持原样
     */
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
