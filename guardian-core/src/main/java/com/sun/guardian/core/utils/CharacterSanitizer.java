package com.sun.guardian.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 转义符替换处理器，构造时预计算转义还原，sanitize 运行时零解析开销
 *
 * @author scj
 * @version java version 1.8
 * @since 2026-02-20 14:43
 */
public class CharacterSanitizer {

    private final List<String[]> replacementPairs;

    /**
     * 构造替换处理器，对配置的 key/value 执行转义还原并缓存
     */
    public CharacterSanitizer(Map<String, String> replacements) {
        this.replacementPairs = new ArrayList<>(replacements.size());
        replacements.forEach((key, value) -> {
            replacementPairs.add(new String[]{unescape(key), unescape(value)});
        });
    }

    /**
     * 对字符串依次执行配置的替换规则
     */
    public String sanitize(String value) {
        if (value == null) return null;
        for (String[] pair : replacementPairs) {
            value = value.replace(pair[0], pair[1]);
        }
        return value;
    }

    /**
     * 将配置中的转义表示还原为实际字符，支持 \r \n \t \0 \\ 及 \\uXXXX
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
}
