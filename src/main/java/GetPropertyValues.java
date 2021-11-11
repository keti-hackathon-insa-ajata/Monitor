import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class GetPropertyValues {

    public void getPropValues() throws Exception {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("config", new Locale("en", "US"));
            // get the properties' values
            Monitor.originator = bundle.getString("originator");
            Monitor.cseProtocol = bundle.getString("cseProtocol");
            Monitor.inCseIp = bundle.getString("inCseIp");
            Monitor.inCsePort = Integer.parseInt(bundle.getString("inCsePort"));
            Monitor.targetCse = bundle.getString("targetCse");

            Monitor.csePoa = Monitor.cseProtocol + "://" + Monitor.inCseIp + ":" + Monitor.inCsePort;

            Monitor.aeMonitorName = bundle.getString("aeMonitorName");
            Monitor.aeDangerReports = bundle.getString("aeDangerReports");
            Monitor.aeAjataName = bundle.getString("aeAjataName");
            Monitor.subName = bundle.getString("subName");

            Monitor.monitorIp = bundle.getString("monitorIp");
            Monitor.monitorPort = Integer.parseInt(bundle.getString("monitorPort"));
            Monitor.monitorContext = bundle.getString("monitorContext");
            Monitor.monitorPoa = "http://" + Monitor.monitorIp + ":" + Monitor.monitorPort + Monitor.monitorContext;
            Monitor.databaseUri = bundle.getString("databaseUri");

        } catch (MissingResourceException e) {
            throw new Exception("config/config.properties was not found");
        }
    }
}