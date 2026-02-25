package com.sun.guardian.core.utils.string;

import com.sun.guardian.core.service.base.BaseCharacterReplacement;
import com.sun.guardian.core.service.base.BaseConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 转义符替换处理器，基于配置规则对字符串执行替换；内置哈希缓存，配置未变更时零解析开销，配置动态刷新后自动重建
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 14:43
 */
public class CharacterSanitizer {

    private final BaseConfig baseConfig;
    private volatile List<String[]> cachedPairs;
    private volatile int lastHash;

    /**
     * 构造替换处理器
     */
    public CharacterSanitizer(BaseConfig baseConfig) {
        this.baseConfig = baseConfig;
    }

    /**
     * 对字符串依次执行配置的替换规则
     *
     * @param value 待处理字符串，为 {@code null} 时直接返回
     * @return 替换后的字符串
     */
    public String sanitize(String value) {
        if (value == null) return null;
        for (String[] pair : getPlacementPairs()) {
            value = value.replace(pair[0], pair[1]);
        }
        return value;
    }

    /**
     * 将配置中的转义表示还原为实际字符，支持 {@code \r \n \t \0 \\} 及 {@code \\uXXXX}
     *
     * @param str 原始转义字符串
     * @return 还原后的字符串，{@code null} 时返回空串
     */
    private static String unescape(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'r':
                        sb.append('\r');
                        i++;
                        break;
                    case 'n':
                        sb.append('\n');
                        i++;
                        break;
                    case 't':
                        sb.append('\t');
                        i++;
                        break;
                    case '0':
                        sb.append('\0');
                        i++;
                        break;
                    case '\\':
                        sb.append('\\');
                        i++;
                        break;
                    case 'u':
                        if (i + 5 < str.length()) {
                            String hex = str.substring(i + 2, i + 6);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 获取转义还原后的替换对列表，基于配置哈希值缓存，配置变更时自动重建
     *
     * @return 替换对列表，每个元素为 {@code [from, to]}
     */
    private List<String[]> getPlacementPairs() {
        List<? extends BaseCharacterReplacement> rules = baseConfig.getCharacterReplacements();
        int currentHash = rules.hashCode();
        if (cachedPairs != null && currentHash == lastHash) {
            return cachedPairs;
        }
        Map<String, String> replacementMap = new LinkedHashMap<>();
        rules.forEach(rule -> replacementMap.put(rule.getFrom(), rule.getTo()));
        List<String[]> pairs = replacementMap.entrySet().stream()
                .map(e -> new String[]{unescape(e.getKey()), unescape(e.getValue())})
                .collect(Collectors.toList());
        cachedPairs = pairs;
        lastHash = currentHash;
        return pairs;
    }
}
