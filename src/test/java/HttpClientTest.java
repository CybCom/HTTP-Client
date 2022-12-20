import edu.njunet.client.HttpClient;

public class HttpClientTest {
    public static void main(String[] args) {
        HttpClient client1 = new HttpClient("localhost", 8080);


        //在测试之前请清理缓存，避免相互影响

        //200
//        client1.get("/test.html");

        //404
//        client1.get("/bilibili.html");

        //301
//        client1.get("/Devil_Kid.flac");

        //302
//        client1.get("/tomb_raider.png");

        //304
//        client1.get("/test.html");
//        client1.get("/test.html");

        //405
//        client1.post("/test.html", null);

        //login
//        client1.login("张三", "123456789");

        //register
//        client1.register("ant", "2233");
//        client1.login("ant", "2233");


        client1.close();
    }
}
