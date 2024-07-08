package com.nhnacademy.aiot;

import java.util.HashMap;
import java.util.Map;

public class Response {
    static final String CRLF = "\r\n";
    static final String FIELD_CONTENT_LENGTH = "content-length";
    String version;
    int status;
    String reason;
    Map<String, String> fieldMap;
    byte[] body;

    public Response(String version, int status) {
        this.version = version;
        this.status = status;
        fieldMap = new HashMap<>();
    }

    public Response(String version, int status, String reason) {
        this.version = version;
        this.status = status;
        this.reason = reason;
        fieldMap = new HashMap<>();
    }

    public String getStatusLine() {
        return String.format("%s %s %s%s", version, status, reason, CRLF);
    }

    public void setBody(byte[] body) {
        this.body = body;
        fieldMap.put(FIELD_CONTENT_LENGTH, String.valueOf(this.body.length));
    }

    public void addField(String key, String value) {
        fieldMap.put(key, value);
    }

    public byte[] getBytes() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s %d %s%s", version, status, reason, CRLF));

        fieldMap.forEach((k, v) -> builder.append(String.format("%s: %s%s", k, v, CRLF)));
        builder.append(CRLF);

        String header = builder.toString();

        byte[] payload = new byte[header.getBytes().length + body.length];

        System.arraycopy(header.getBytes(), 0, payload, 0, header.getBytes().length);
        System.arraycopy(body, 0, payload, header.getBytes().length, body.length);

        return payload;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%s %d %s%s", version, status, reason, CRLF));

        fieldMap.forEach((k, v) -> builder.append(String.format("%s: %s%s", k, v, CRLF)));
        builder.append(CRLF);

        if (body != null) {
            builder.append(body);
        }
        return builder.toString();
    }
}
