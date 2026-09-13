package com.aporia.knowledge.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimalist, zero-dependency JSON parser.
 * Converts JSON strings into generic Maps, Lists, Strings, Doubles, and Booleans.
 */
public class MiniJson {
    private final String json;
    private int index;

    private MiniJson(String json) {
        this.json = json;
        this.index = 0;
    }

    public static Object parse(String json) {
        if (json == null || json.isBlank()) return null;
        return new MiniJson(json).parseValue();
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }

    private Object parseValue() {
        skipWhitespace();
        if (index >= json.length()) return null;
        char c = json.charAt(index);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        throw new RuntimeException("Unexpected character at " + index + ": " + c);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        index++; // skip '{'
        skipWhitespace();
        if (json.charAt(index) == '}') {
            index++;
            return map;
        }
        while (index < json.length()) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            if (json.charAt(index) != ':') throw new RuntimeException("Expected ':'");
            index++; // skip ':'
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char c = json.charAt(index);
            if (c == '}') {
                index++;
                return map;
            }
            if (c == ',') {
                index++;
            } else {
                throw new RuntimeException("Expected ',' or '}'");
            }
        }
        throw new RuntimeException("Unterminated object");
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        index++; // skip '['
        skipWhitespace();
        if (json.charAt(index) == ']') {
            index++;
            return list;
        }
        while (index < json.length()) {
            list.add(parseValue());
            skipWhitespace();
            char c = json.charAt(index);
            if (c == ']') {
                index++;
                return list;
            }
            if (c == ',') {
                index++;
            } else {
                throw new RuntimeException("Expected ',' or ']'");
            }
        }
        throw new RuntimeException("Unterminated array");
    }

    private String parseString() {
        index++; // skip '"'
        StringBuilder sb = new StringBuilder();
        while (index < json.length()) {
            char c = json.charAt(index++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                char esc = json.charAt(index++);
                if (esc == '"' || esc == '\\' || esc == '/') sb.append(esc);
                else if (esc == 'b') sb.append('\b');
                else if (esc == 'f') sb.append('\f');
                else if (esc == 'n') sb.append('\n');
                else if (esc == 'r') sb.append('\r');
                else if (esc == 't') sb.append('\t');
                else if (esc == 'u') {
                    String hex = json.substring(index, index + 4);
                    sb.append((char) Integer.parseInt(hex, 16));
                    index += 4;
                }
            } else {
                sb.append(c);
            }
        }
        throw new RuntimeException("Unterminated string");
    }

    private Boolean parseBoolean() {
        if (json.startsWith("true", index)) { index += 4; return true; }
        if (json.startsWith("false", index)) { index += 5; return false; }
        throw new RuntimeException("Expected boolean");
    }

    private Object parseNull() {
        if (json.startsWith("null", index)) { index += 4; return null; }
        throw new RuntimeException("Expected null");
    }

    private Double parseNumber() {
        int start = index;
        while (index < json.length()) {
            char c = json.charAt(index);
            if (Character.isDigit(c) || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                index++;
            } else {
                break;
            }
        }
        return Double.parseDouble(json.substring(start, index));
    }
}
