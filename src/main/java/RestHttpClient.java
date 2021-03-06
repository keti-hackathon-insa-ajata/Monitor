import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestHttpClient {

    public static String databaseUri;
//    private static boolean isDatabaseConnected;

    public static HttpResponse get(String originator, String uri) {
        System.out.println("HTTP GET " + uri);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);

        httpGet.addHeader("X-M2M-Origin", originator);
        httpGet.addHeader("Accept", "application/json");

        HttpResponse httpResponse = new HttpResponse();

        try {
            CloseableHttpResponse closeableHttpResponse = httpclient.execute(httpGet);
            try {
                httpResponse.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(closeableHttpResponse.getEntity()));
            } finally {
                closeableHttpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("HTTP Response " + httpResponse.getStatusCode() + "\n" + httpResponse.getBody());
        return httpResponse;
    }

    public static HttpResponse post(String originator, String uri, String body, int ty) {
        System.out.println("HTTP POST " + uri + "\n" + body);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uri);

        httpPost.addHeader("X-M2M-Origin", originator);
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json;ty=" + ty);

        HttpResponse httpResponse = new HttpResponse();
        try {
            CloseableHttpResponse closeableHttpResponse = null;
            try {
                httpPost.setEntity(new StringEntity(body));
                closeableHttpResponse = httpclient.execute(httpPost);
                httpResponse.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(closeableHttpResponse.getEntity()));

            } finally {
                closeableHttpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("HTTP Response " + httpResponse.getStatusCode() + "\n" + httpResponse.getBody());
        return httpResponse;
    }

    public static HttpResponse delete(String originator, String uri) {
        System.out.println("HTTP DELETE " + uri + "\n");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(uri);

        httpDelete.addHeader("X-M2M-Origin", originator);

        HttpResponse httpResponse = new HttpResponse();
        try {
            CloseableHttpResponse closeableHttpResponse = null;
            try {
                closeableHttpResponse = httpclient.execute(httpDelete);
                httpResponse.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(closeableHttpResponse.getEntity()));

            } finally {
                closeableHttpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("HTTP Response " + httpResponse.getStatusCode() + "\n" + httpResponse.getBody());
        return httpResponse;
    }

    public static HttpResponse sendToDatabase(String body) {
//        if (!isDatabaseConnected) {
//            System.out.println("[WARNING] Database is not connected, we do not send to " + databaseUri);
//            return null;
//        }
        System.out.println("HTTP POST " + databaseUri + "\n" + body);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(databaseUri);

        httpPost.addHeader("Content-Type", "application/json");

        HttpResponse httpResponse = new HttpResponse();
        try {
            CloseableHttpResponse closeableHttpResponse = null;
            try {
                httpPost.setEntity(new StringEntity(body));
                closeableHttpResponse = httpclient.execute(httpPost);
                httpResponse.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(closeableHttpResponse.getEntity()));

            } finally {
                closeableHttpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("HTTP Response " + httpResponse.getStatusCode() + "\n" + httpResponse.getBody());
        return httpResponse;
    }

    public static void verifyDatabaseConnection() {

        System.out.println("HTTP GET " + databaseUri);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(databaseUri);

        HttpResponse httpResponse = new HttpResponse();

        try {
            CloseableHttpResponse closeableHttpResponse = httpclient.execute(httpGet);
            try {
                httpResponse.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
                httpResponse.setBody(EntityUtils.toString(closeableHttpResponse.getEntity()));
            } finally {
                closeableHttpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (httpResponse.getStatusCode() != 200) {
//            System.out.println("[WARNING] Couldn't access database at " + databaseUri);
//            RestHttpClient.isDatabaseConnected = false;
//        } else {
//            RestHttpClient.isDatabaseConnected = true;
//        }
    }
}