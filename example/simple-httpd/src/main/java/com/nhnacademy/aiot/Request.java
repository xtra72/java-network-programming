package com.nhnacademy.aiot;

import java.util.HashMap;
import java.util.Map;

public class Request {
    public static final String FIELD_CONTENT_LENGTH = "content-length";
    public static final String CRLF = "\r\n";
    String method;
    String path;
    String version;
    char[] body;
    Map<String, String> fieldMap;

    public Request(String method, String path, String version) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.fieldMap = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public void setBody(char[] body) {
        this.body = body;
    }

    public void addField(String line) {
        String[] fields = line.split(":", 2);
        if (fields.length != 2) {
            throw new InvalidHttpRequestException();
        }

        addField(fields[0].trim(), fields[1].trim());
    }

    public void addField(String key, String value) {
        if (key.equalsIgnoreCase(FIELD_CONTENT_LENGTH)) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException ignore) {
                throw new InvalidHttpRequestException();
            }
        }
        fieldMap.put(key.toLowerCase(), value);
    }

    public String getField(String key) {
        return fieldMap.get(key);
    }

    public boolean hasField(String key) {
        return fieldMap.containsKey(key);
    }

    public int getContentLength() {
        return Integer.parseInt(getField(FIELD_CONTENT_LENGTH));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s %s %s%s", getMethod(), getPath(), getVersion(), CRLF));
        fieldMap.forEach((k, v) -> builder.append(String.format("%s: %s%s", k, v, CRLF)));

        return builder.toString();
    }
}
