package org.csstudio.alarm.beast.history.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.csstudio.alarm.beast.history.views.AlarmHistoryQueryParameters.AlarmHistoryQueryBuilder;
import org.csstudio.alarm.beast.history.views.PeriodicAlarmHistoryQuery.AlarmHistoryResult;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Kunal Shroff
 *
 */
public class AlarmHistoryQueryJob extends Job {

    private final static String name = "LogQueryJob";

    private final AlarmHistoryQueryParameters query;
    private final Client client;

    AlarmHistoryQueryJob(AlarmHistoryQueryParameters query, Client client) {
    super(name);
    this.query = query;
    this.client = client;
    }

    void completedQuery(AlarmHistoryResult result) {

    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        AlarmHistoryResult result = null;
        List<Map<String, String>> alarmMessages = new ArrayList<Map<String, String>>();
    try {
        
        Client client = Client.create();
        WebResource r = client.resource("http://130.199.219.79:9999/alarms/beast/_search");
        AlarmHistoryQueryParameters parameter = AlarmHistoryQueryBuilder.buildQuery().build();

        String response = r.accept(MediaType.APPLICATION_JSON).post(String.class, parameter.getQueryString());

        try {
            JsonFactory factory = new JsonFactory();

            ObjectMapper mapper = new ObjectMapper(factory);
            JsonNode rootNode = mapper.readTree(response);

            JsonNode node = rootNode.get("hits").get("hits");
            for (JsonNode jsonNode : node) {
                alarmMessages.add(
                        mapper.readValue(jsonNode.get("_source"), new TypeReference<Map<String, String>>() {
                }));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
        result = new AlarmHistoryResult(alarmMessages, null);
    } catch (Exception e) {
        result = new AlarmHistoryResult(alarmMessages, e);
    } finally {
        if (!monitor.isCanceled()) {
                completedQuery(result);
        }
    }
    return Status.OK_STATUS;
    }

}
