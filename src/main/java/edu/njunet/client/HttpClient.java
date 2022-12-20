package edu.njunet.client;

import edu.njunet.utils.JsonReader.ClientJsonReader;
import edu.njunet.protocol.Request;
import edu.njunet.protocol.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpClient {
    private final String hostname;
    private final int port;

    private Socket client;

    private final ClientJsonReader clientJsonReader;

    public HttpClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        clientJsonReader = new ClientJsonReader();
        try {
            client = new Socket(hostname, port);
            System.out.println("连接到主机：" + hostname + " ,端口号：" + port);
            System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 关闭服务器的socket资源
     */
    public void close() {
        try {
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /***
     * 发送get请求
     * @param url 待请求的url
     */
    public void get(String url) {
        try {
            Request request = Request.buildRequest(url);
            reRequest(request);
            System.out.println(request);
            OutputStream outToServer = client.getOutputStream();
            request.send(outToServer);

            InputStream inFromServer = client.getInputStream();
            while (inFromServer.available() == 0) { //服务器给响应了才继续
            }
            Response response = Response.parseResponse(inFromServer);
            handleResponse(response, url);
            System.out.println(response);

        } catch (IOException ex) {
            try {
                client = new Socket(hostname, port);
                System.out.println("超时，重新建立连接！");
                System.out.println("连接到主机：" + hostname + " ,端口号：" + port);
                System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
                get(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 发送post请求，主要用于login，register
     * @param url
     * @param data 向服务器发送的数据
     */

    public void post(String url, byte[] data) {
        try {
            Request request = Request.buildRequest(url);
            switchToPost(request, data);
            System.out.println(request);
            System.out.println();
            OutputStream outToServer = client.getOutputStream();
            request.send(outToServer);

            InputStream inFromServer = client.getInputStream();
            while (inFromServer.available() == 0) { //服务器给响应了才继续
            }
            Response response = Response.parseResponse(inFromServer);
            System.out.println(response);

        } catch (IOException ex) {
            try {
                client = new Socket(hostname, port);
                System.out.println("超时，重新建立连接！");
                System.out.println("连接到主机：" + hostname + " ,端口号：" + port);
                System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
                post(url, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void register(String user_name, String password) {
        String content = "userName:" + user_name + "\r\n" + "password:" + password;
        post("/register", content.getBytes());
    }

    public void login(String user_name, String password) {
        String content = "userName:" + user_name + "\r\n" + "password:" + password;
        post("/login", content.getBytes());
    }

    /***
     * 将默认请求报文设为post请求
     * @param request 生成的默认请求报文，为get请求
     * @param data 向服务器发送的数据
     */
    private void switchToPost(Request request, byte[] data) {
        request.setMethod("POST");
        request.setMessage(data);
        request.getHeader().put("Content-Length", data == null ? "0" : String.valueOf(data.length));
    }

    /***
     * 检查url是否已缓存，若true，则在request中加上条件请求if_modified_since 或 if_no_match
     * @param request 由 Message.Request 生成的默认请求报文
     */
    private void reRequest(Request request) {
        clientJsonReader.searchUrl(request);
    }

    /***
     * 对响应报文状态码的处理
     * @param response 从server接受的响应报文
     */
    private void handleResponse(Response response, String url) throws IOException {
        clientJsonReader.updateResource(response, url);
    }

}

