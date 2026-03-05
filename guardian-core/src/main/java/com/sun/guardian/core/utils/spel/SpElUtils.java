package com.sun.guardian.core.utils.spel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.guardian.core.utils.json.GuardianJsonUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * SpEl 表达式工具类
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-03-04 21:54
 */
public class SpElUtils {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private static final MapPropertyAccessor MAP_PROPERTY_ACCESSOR = new MapPropertyAccessor();

    private SpElUtils() {
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param expression SpEL 表达式，如 "#orderId" 或 "#userId + ':' + #body.orderId"
     * @param params     请求参数（来自 ArgsUtils.toSorted）
     * @return 解析结果
     */
    public static String evaluate(String expression, Map<String, String> params) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(MAP_PROPERTY_ACCESSOR);

        if (params != null && !params.isEmpty()) {
            boolean hasBody = params.containsKey("body");
            boolean hasParams = params.size() > (hasBody ? 1 : 0);

            if (hasBody) {
                String bodyStr = params.get("body");
                if (StringUtils.hasText(bodyStr)) {
                    Map<String, Object> bodyMap = GuardianJsonUtils.toBean(bodyStr, new TypeReference<Map<String, Object>>() {});

                    if (hasParams) {
                        Map<String, Object> bodyWrapper = new HashMap<>(bodyMap);
                        context.setVariable("body", bodyWrapper);
                        params.forEach((key, value) -> {
                            if (!"body".equals(key)) {
                                context.setVariable(key, value);
                            }
                        });
                    } else {
                        bodyMap.forEach(context::setVariable);
                    }
                }
            } else {
                params.forEach(context::setVariable);
            }
        }

        Expression exp = PARSER.parseExpression(expression);
        Object result = exp.getValue(context);
        return result != null ? result.toString() : null;
    }

    /**
     * 自定义 PropertyAccessor，支持用 . 访问 Map
     */
    private static class MapPropertyAccessor implements PropertyAccessor {

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class[]{Map.class};
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) {
            if (target instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) target;
                Object value = map.get(name);
                return new TypedValue(value);
            }
            return null;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object value) {
            throw new UnsupportedOperationException("不支持写操作");
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) {
            return false;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            return target instanceof Map && ((Map<?, ?>) target).containsKey(name);
        }
    }
}
