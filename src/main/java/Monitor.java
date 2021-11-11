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

    // OM2M

    public static String originator;
    public static String cseProtocol;
    public static String inCseIp;
    public static int inCsePort;
    public static String targetCse;

    public static String aeMonitorName;
    public static String aeDangerReports;
    public static String aeAjataName;
    public static String subName;

    public static String csePoa;

    // Monitor and database

    public static String monitorIp;
    public static int monitorPort;
    public static String monitorContext;
    public static String databaseUri;

    public static String monitorPoa;

    private static final GetPropertyValues properties = new GetPropertyValues();

    private static void processData(JSONObject content) {
        boolean dangerous = content.getDouble("object_speed") > 5.0;
        content.put("dangerous", dangerous);
    }

    private static void deploy() {
        System.out.println("[INFO] Discover all mn-cse in " + csePoa + "/~/" + targetCse);

        HttpResponse httpResponse = RestHttpClient.get(originator, csePoa + "/~/" + targetCse + "?fu=1&ty=16");
        JSONObject result = new JSONObject(httpResponse.getBody());
        JSONArray resultArray = result.getJSONArray("m2m:uril");
        if (resultArray.length() > 0) {
            for (int i = 0; i < resultArray.length(); i++) {

                String uril = resultArray.getString(i);

                // For each mn-cse, we get the poa

                httpResponse = RestHttpClient.get(originator, csePoa + "/~" + uril);
                JSONObject result2 = new JSONObject(httpResponse.getBody());
                JSONObject result2Object = result2.getJSONObject("m2m:csr");
                JSONArray poa = result2Object.getJSONArray("poa");
                String csi = result2Object.getString("csi");
                String mnName = result2Object.getString("rn");

                String mnCsePoa = poa.getString(0) + "~" + csi + "/" + mnName + "/";
                String inCsePoa = poa.getString(0) + "~/" + targetCse;

                // Once we got the poa we deploy the necessaray elements (if the poa is accessible)

                httpResponse = RestHttpClient.get(originator, mnCsePoa);
                if (httpResponse.getStatusCode() != 200) {
                    System.out.println("[INFO] Couldn't access " + csi + "/" + mnName + " at " + poa.getString(0));
                    continue;
                }

                // DELETE old AE AjataSensor

                RestHttpClient.delete(originator, mnCsePoa + aeAjataName);

                // POST AE AjataSensor

                JSONObject obj = new JSONObject();
                obj.put("rn", aeAjataName);
                obj.put("api", targetCse.replace('/', '_') + "_ID");
                obj.put("rr", false);
                obj.put("lbl", "Type/sensor Location/bike Category/speed Category/position Category/distance");
                JSONObject ae = new JSONObject();
                ae.put("m2m:ae", obj);
                RestHttpClient.post(originator, mnCsePoa, ae.toString(), 2);

                // POST CNT DESCRIPTOR

                obj = new JSONObject();
                obj.put("rn", "DESCRIPTOR");
                ae = new JSONObject();
                ae.put("m2m:cnt", obj);
                RestHttpClient.post(originator, mnCsePoa + aeAjataName, ae.toString(), 3);

                // POST CIN for DESCRIPTOR

                obj = new JSONObject();
                obj.put("cnf", "application/json");
                JSONObject tmp = new JSONObject();
                tmp.put("Type", "Sensor");
                tmp.put("Category", "Speed Position Distance");
                tmp.put("Unit", "");
                tmp.put("Location", "Bike");
                obj.put("con", tmp.toString());
                ae = new JSONObject();
                ae.put("m2m:cin", obj);
                RestHttpClient.post(originator, mnCsePoa + aeAjataName + "/DESCRIPTOR", ae.toString(), 4);

                // POST CNT DATA

                obj = new JSONObject();
                obj.put("rn", "DATA");
                ae = new JSONObject();
                ae.put("m2m:cnt", obj);
                RestHttpClient.post(originator, mnCsePoa + aeAjataName, ae.toString(), 3);

                // POST SUB

                JSONArray array = new JSONArray();
                //array.put(mnCsePoa + aeMonitorName);
//                array.put(csi + "/" + mnName + "/" + aeMonitorName);
                array.put(inCsePoa + "/" + aeMonitorName);
                obj = new JSONObject();
                obj.put("nu", array);
                obj.put("rn", subName);
                obj.put("nct", 2);
                JSONObject sub = new JSONObject();
                sub.put("m2m:sub", obj);
                RestHttpClient.post(originator, mnCsePoa + aeAjataName + "/DATA", sub.toString(), 23);

//                // DELETE old AE MONITOR
//
//                RestHttpClient.delete(originator, mnCsePoa + aeMonitorName);
//
//                // POST AE MONITOR
//
//                array = new JSONArray();
//                array.put(monitorPoa);
//                obj = new JSONObject();
//                obj.put("rn", aeMonitorName);
//                obj.put("api", "MONITOR");
//                obj.put("rr", true);
//                obj.put("poa", array);
//                ae = new JSONObject();
//                ae.put("m2m:ae", obj);
//                RestHttpClient.post(originator, mnCsePoa, ae.toString(), 2);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        properties.getPropValues();

        // Deploy
        if (args.length > 0 && args[0].equals("-d")) {
            Monitor.deploy();
        }

//        final String argsFormat = "Argument format: (-d) monitor-ip port database-uri";
//        final String argsEgs = "Arguments egs: -d 192.168.43.129 1600 http://localhost:12345/dangerReports\n" +
//                "or: 1600 http://localhost:12345/dangerReports";
//        try {
//            if (args.length < 2) {
//                throw new Exception(argsFormat + "\n" + argsEgs);
//            }
//            if (args[0].equals("-d")) {
//                if (args.length != 4) {
//                    throw new Exception(argsFormat + "\n" + argsEgs);
//                }
//                port = args[2];
//                monitorPoa = "http://" + args[1] + ":" + port + context;
//                RestHttpClient.databaseUri = args[3];
//                Monitor.deploy();
//            } else {
//                port = args[0];
//                RestHttpClient.databaseUri = args[1];
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//            return;
//        }

//        // Testing database connection
//
//        RestHttpClient.verifyDatabaseConnection();

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(monitorPort), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert server != null;
        server.createContext(monitorContext, new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        // DELETE old AE DangerReports

        RestHttpClient.delete(originator, csePoa + "/~/" + targetCse + "/" + aeDangerReports);

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

        RestHttpClient.delete(originator, csePoa + "/~/" + targetCse + "/" + aeMonitorName);

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
        RestHttpClient.post(originator, csePoa + "/~/" + targetCse, ae.toString(), 2);

        System.out.println("Monitor running on port: " + monitorPort);
        System.out.println("Context: " + monitorContext);
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
                        processData(tmp2);
                        obj.put("con", tmp2.toString());
                        JSONObject cin = new JSONObject();
                        cin.put("m2m:cin", obj);
                        RestHttpClient.post(originator, csePoa + "/~/" + targetCse + "/" + aeDangerReports + "/DATA" , cin.toString(), 4);

                        // Send to database

                        RestHttpClient.sendToDatabase(tmp2.toString());

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