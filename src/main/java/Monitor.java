import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Monitor {

    private static final String originator = "admin:admin";
    private static final String cseProtocol = "http";
    private static final String cseIp = "127.0.0.1";
    private static final int csePort = 8080;
    private static final String cseId = "in-cse";
    private static final String cseName = "in-name";

    private static final String aeMonitorName = "Monitor";
    private static final String aeDangerReports = "DangerReports";
    private static final String targetCse = "in-cse/in-name";

    private static final String csePoa = cseProtocol + "://" + cseIp + ":" + csePort;

    public static void main(String[] args) {


        if (args.length < 2) {
            System.out.println("Arguments egs: 192.168.43.129 1600");
            return;
        }
        final String port = args[1];
        final String monitorPoa = "http://" + args[0] + ":" + port;

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert server != null;
        server.createContext("/", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        // POST AE DangerReports

        JSONObject obj = new JSONObject();
        obj.put("rn", aeDangerReports);
        obj.put("api", "Danger_Reports_ID");
        obj.put("rr", false);
        obj.put("lbl", "Type/sensor Location/bike Category/speed Category/position Category/distance Category/danger");
        JSONObject ae = new JSONObject();
        ae.put("m2m:ae", obj);
        RestHttpClient.post(originator, csePoa + "/~/" + targetCse , ae.toString(), 2);

        // POST CNT DESCRIPTOR

        obj = new JSONObject();
        obj.put("rn", "DESCRIPTOR");
        JSONObject cnt = new JSONObject();
        cnt.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa + "/~/" + targetCse + "/" + aeDangerReports , cnt.toString(), 3);

        // POST CIN for DESCRIPTOR

        obj = new JSONObject();
        obj.put("cnf","application/json");
        JSONObject tmp = new JSONObject();
        tmp.put("Type", "DataContainer");
        tmp.put("Category", "Speed Position Distance Danger");
        tmp.put("Unit", "");
        tmp.put("Location", "Server");
        obj.put("con", tmp.toString());
        JSONObject cin = new JSONObject();
        cin.put("m2m:cin", obj);
        RestHttpClient.post(originator, csePoa + "/~/" + targetCse + "/" + aeDangerReports + "/DESCRIPTOR" , cin.toString(), 4);

        // POST CNT DATA

        obj = new JSONObject();
        obj.put("rn", "DATA");
        cnt = new JSONObject();
        cnt.put("m2m:cnt", obj);
        RestHttpClient.post(originator, csePoa + "/~/" + targetCse + "/" + aeDangerReports , cnt.toString(), 3);

        // DELETE old AE MONITOR

        RestHttpClient.delete(originator, csePoa + "/~/" + cseId + "/" + cseName + "/" + aeMonitorName);

        // POST AE MONITOR

        JSONArray array = new JSONArray();
        array.put(monitorPoa);
        obj = new JSONObject();
        obj.put("rn", aeMonitorName);
        obj.put("api", "MONITOR");
        obj.put("rr", true);
        obj.put("poa", array);
        ae = new JSONObject();
        ae.put("m2m:ae", obj);
        RestHttpClient.post(originator, csePoa + "/~/" + cseId + "/" + cseName, ae.toString(), 2);

//        array = new JSONArray();
//        array.put("/" + cseId + "/" + cseName + "/" + aeMonitorName);
//        obj = new JSONObject();
//        obj.put("nu", array);
//        obj.put("rn", subName);
//        obj.put("nct", 2);
//        sub = new JSONObject();
//        sub.put("m2m:sub", obj);
//        RestHttpClient.post(originator, csePoa + "/~/" + targetCse, sub.toString(), 23);
//
//        System.out.println("[INFO] Discover all containers in " + csePoa);
//
//        HttpResponse httpResponse = RestHttpClient.get(originator, csePoa + "/~/" + targetCse + "?fu=1&ty=3");
//        JSONObject result = new JSONObject(httpResponse.getBody());
//        JSONArray resultArray = result.getJSONArray("m2m:uril");
//        if (resultArray.length() > 0) {
//            String[] uril = resultArray.toString().split(" ");
//            for (int i = 0; i < uril.length; i++) {
//                RestHttpClient.post(originator, csePoa + "/~" + uril[i], sub.toString(), 23);
//            }
//        }
    }

    static class MyHandler implements HttpHandler {

        public void handle(HttpExchange httpExchange) {
            System.out.println("Event Recieved!");

            try {
                InputStream in = httpExchange.getRequestBody();

                String requestBody = "";
                int i;
                char c;
                while ((i = in.read()) != -1) {
                    c = (char) i;
                    requestBody = (String) (requestBody + c);
                }

                System.out.println(requestBody);

                JSONObject json = new JSONObject(requestBody);
                if (json.getJSONObject("m2m:sgn").has("m2m:vrq")) {
                    System.out.println("Confirm subscription");
                } else {
                    System.out.println(json.toString());
                    JSONObject rep = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev")
                            .getJSONObject("m2m:rep");
                    JSONObject tmp = (JSONObject) rep.get(rep.keySet().iterator().next());
                    int ty = tmp.getInt("ty");
                    System.out.println("Resource type: " + ty);

                    if (ty == 4) {
                        String ciName = tmp.getString("rn");
                        String content = tmp.getString("con");

                        System.out.println("[INFO] New Content Instance " + ciName + " has been created");
                        System.out.println("[INFO] Content: " + content);
                        System.out.println("[INFO] Copying data in in-cse:");

                        // POST CIN DATA

                        JSONObject obj = new JSONObject();
                        obj.put("cnf","application/json");
                        JSONObject tmp2 = new JSONObject(content);
                        boolean dangerous = tmp2.getInt("Data") > 100;
                        tmp2.put("Dangerous", dangerous);
                        obj.put("con", tmp2.toString());
                        JSONObject cin = new JSONObject();
                        cin.put("m2m:cin", obj);
                        RestHttpClient.post(originator, csePoa + "/~/" + targetCse + "/" + aeDangerReports + "/DATA" , cin.toString(), 4);

                    }
                }

                String responseBudy = "";
                byte[] out = responseBudy.getBytes("UTF-8");
                httpExchange.sendResponseHeaders(200, out.length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(out);
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}