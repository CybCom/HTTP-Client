package edu.njunet.protocol;


import edu.njunet.utils.DefaultRequestHead;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private String method;
    private String url;
    private String version;
    private Map<String, String> header;
    private byte[] message;

    /***
     * 解析inputSteam流，返回一个request对象，用于server接受来自client的请求
     * @param reqStream 请求报文的字节流
     * @return 解析好的request报文对象
     * @throws IOException
     */
    public static Request parseRequest(InputStream reqStream) throws IOException {
        Request request = new Request();
        decodeRequestLineAndHeader(reqStream, request);
        decodeRequestMessage(reqStream, request);
        return request;
    }

    /***
     * 用于在client端创建request
     * @param url URL
     * @return 建立好的request报文
     */
    public static Request buildRequest(String url) {
        Request request = new Request();
        request.setMethod("GET");
        request.setUrl(url);
        request.setVersion("HTTP/1.1");
        request.setHeader(DefaultRequestHead.DEFAULT_HEADER);
        request.getHeader().put("Content-Type", MIME.getMimeList().getMimeType(request.getUrl()));
        return request;
    }

    /***
     * 设置request的行和头
     */
    private static void decodeRequestLineAndHeader(InputStream reqStream, Request request) throws IOException {
        List<String> lines = getLines(reqStream);
        String[] line = lines.get(0).replace("\r\n", "").split(" ", 3);
        request.setVersion(line[2]);
        request.setUrl(line[1]);
        request.setMethod(line[0]);
        Map<String, String> header = new HashMap<>();
        for (int i = 1; i < lines.size()-1; i++) {
            String[] entry = lines.get(i).replace("\r\n", "").split(":", 2);
            header.put(entry[0], entry[1]);
        }
        request.setHeader(header);
    }

    /***
     * @return line和 header组成的List
     */
    private static List<String> getLines(InputStream reqStream) throws IOException {
        int i = -1;
        byte[] buffer = new byte[1024];
        List<String> lines = new ArrayList<>();
        while (reqStream.available() > 0) {
            int b = reqStream.read();
            buffer[++i] = (byte) b;
            if (i >= 1 && (buffer[i-1] == '\r') && (buffer[i] == '\n')) {
                byte[] line = Arrays.copyOfRange(buffer, 0, i+1);
                lines.add(new String(line, StandardCharsets.UTF_8));
                if (i == 1) {
                    break;
                } else {
                    i = -1;
                }
            }
        }
        return lines;
    }


    private static void decodeRequestMessage(InputStream reqStream, Request request) throws IOException {
        int messageLen = Integer.parseInt(request.getHeader().getOrDefault("Content-Length", "0").trim()); //请求体有多少字节，可能为0
        if (messageLen != 0) {
            int remainingByte = messageLen;
            byte[] buffer = new byte[messageLen];
            int i = 0;
            while (remainingByte > 0) {
                int alreadyRead = reqStream.read(buffer, i, remainingByte);
                remainingByte -= alreadyRead;
                i += alreadyRead;
            }
            request.setMessage(buffer);
        }
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String s) {
        method = s;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String s) {
        url = s;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String s) {
        version = s;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> map) {
        header = map;
    }

    public String text() {return message == null ? null : new String(message, StandardCharsets.UTF_8); }

    public byte[] content() {return message; }

    public void setMessage(byte[] s) {
        message = s;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String requestLine = method + " " + url + " " + version + "\r\n";
        sb.append(requestLine);

        for (Map.Entry<String, String> entry : header.entrySet()) {
            String tmp = entry.getKey() + ":" + entry.getValue() + "\r\n";
            sb.append(tmp);
        }
        sb.append("\r\n");
        if (message != null) {
            sb.append(text());
        }

        return sb.toString();
    }

    public void send(OutputStream out) throws IOException {
        String requestLine = method + " " + url + " " + version + "\r\n";
        out.write(requestLine.getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, String> entry : header.entrySet()) {
            String tmp = entry.getKey() + ":" + entry.getValue() + "\r\n";
            out.write(tmp.getBytes(StandardCharsets.UTF_8));
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        if (message != null) {
            out.write(message);
        }
    }
}
