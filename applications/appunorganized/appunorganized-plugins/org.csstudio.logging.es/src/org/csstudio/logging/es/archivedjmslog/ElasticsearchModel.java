package org.csstudio.logging.es.archivedjmslog;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.csstudio.utility.esclient.ElasticsearchClient;
import org.csstudio.utility.esclient.ScrollSettings;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 *
 * Default filtering is by time stamp. It is assumed that the used index
 * includes a field of type date that represents this time stamp.
 *
 * @author Michael Ritzert <michael.ritzert@ziti.uni-heidelberg.de>
 */
public class ElasticsearchModel<T extends LogMessage> extends ArchiveModel<T>
{
    /** Number of hits per page when scrolling is used. */
    protected static final int PAGE_SIZE = 10000;

    protected final String dateField;
    protected String mapping;
    protected final String server;
    protected Function<JsonObject, T> parser;

    protected Job queryJob;
    protected Class<T> parameterType;

    protected List<T> messages;

    @SuppressWarnings("unchecked")
    public ElasticsearchModel(String server, String mapping, String dateField,
            Function<JsonObject, T> parser)
    {
        Activator.checkParameterString(dateField, "dateField"); //$NON-NLS-1$
        Activator.checkParameterString(server, "server"); //$NON-NLS-1$
        Activator.checkParameterString(mapping, "mapping"); //$NON-NLS-1$
        Activator.checkParameter(parser, "parser"); //$NON-NLS-1$
        this.dateField = dateField;
        this.server = server;
        this.mapping = mapping;
        this.parser = parser;
        this.parameterType = ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    /**
     * Build the query to be sent to the Elasticsearch server.
     *
     * As a default, only filtering by the time stamp and limiting the number of
     * results is implemented. Override to change the query.
     */
    @SuppressWarnings("nls")
    protected JsonObject buildQuery(Instant from, Instant to, long maxResults)
    {
        var request = Json.createObjectBuilder();
        var query = Json.createObjectBuilder();

        var bool = Json.createObjectBuilder();
        var conditions = Json.createArrayBuilder();
        var not_conditions = Json.createArrayBuilder();
        boolean not_conditions_empty = true;
        conditions.add(getTimeQuery(from, to));
        synchronized (this)
        {
            if (null != this.filters)
            {
                for (PropertyFilter filter : this.filters)
                {
                    if (filter.isInverted())
                    {
                        not_conditions.add(getFilter(filter));
                        not_conditions_empty = false;
                    }
                    else
                    {
                        conditions.add(getFilter(filter));
                    }
                }
            }
        }
        bool.add("filter", conditions);
        if (!not_conditions_empty)
        {
            bool.add("must_not", not_conditions);
        }
        query.add("bool", bool);

        if (0 >= maxResults)
        {
            // if we want all results, we can use the fastest possible sort in
            // the database.
            request.add("sort", "_doc");
        }
        else
        {
            // however, if we need only the latest n results, we have to sort by
            // date.
            request.add("sort",
                    Json.createObjectBuilder().add(this.dateField, "desc"));
            request.add("size", maxResults);
        }
        request.add("query", query);
        return request.build();
    }

    protected JsonObjectBuilder getFilter(PropertyFilter filter)
    {
        if (filter instanceof StringPropertyFilter)
        {
            return getFilterQuery((StringPropertyFilter) filter);
        }
        else if (filter instanceof StringPropertyMultiFilter)
        {
            return getFilterQuery((StringPropertyMultiFilter) filter);
        }
        throw new IllegalArgumentException("Filter type not supported.");
    }

    @SuppressWarnings("nls")
    protected JsonObjectBuilder getFilterQuery(StringPropertyFilter filter)
    {
        return Json.createObjectBuilder().add("match",
                Json.createObjectBuilder().add(filter.getProperty(),
                        filter.getPattern()));
    }

    @SuppressWarnings("nls")
    protected JsonObjectBuilder getFilterQuery(StringPropertyMultiFilter filter)
    {
        var terms = Json.createArrayBuilder();
        for (var f : filter.getPatterns())
        {
            terms.add(f);
        }
        return Json.createObjectBuilder().add("terms",
                Json.createObjectBuilder().add(filter.getProperty(), terms));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] getMessages()
    {
        synchronized (this)
        {
            if (null != this.messages)
            {
                return this.messages.toArray((T[]) Array
                        .newInstance(this.parameterType, this.messages.size()));
            }
            else
            {
                return (T[]) Array.newInstance(this.parameterType, 0);
            }
        }
    }

    /**
     * Override if your time stamp format cannot be correctly converted to
     * millis since the epoch. E.g. if the time zone information is missing…
     */
    @SuppressWarnings("nls")
    protected JsonObjectBuilder getTimeQuery(Instant from, Instant to)
    {
        var timematch = Json.createObjectBuilder();
        timematch.add("gte", from.toEpochMilli());
        timematch.add("lte", to.toEpochMilli());
        timematch.add("format", "epoch_millis");
        return Json.createObjectBuilder().add("range",
                Json.createObjectBuilder().add(this.dateField, timematch));
    }

    @Override
    public void refresh(Instant from, Instant to, long maxResults)
    {
        Activator.checkParameter(from, "from"); //$NON-NLS-1$
        Activator.checkParameter(to, "to"); //$NON-NLS-1$
        synchronized (this)
        {
            // Cancel a job that might already be running
            if (null != this.queryJob) this.queryJob.cancel();
            this.queryJob = new Job("ES query") //$NON-NLS-1$
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
                {

                    List<T> result = new LinkedList<>();
                    ScrollSettings ss = null;
                    if (0 >= maxResults)
                    {
                        // unlimited number of results requested ⇒ use
                        // scrolling.
                        ss = new ScrollSettings("1m", //$NON-NLS-1$
                                ElasticsearchModel.PAGE_SIZE);
                    }
                    // try
                    // {
                    ElasticsearchClient client = new ElasticsearchClient(
                            ElasticsearchModel.this.server, hit -> {
                                Optional.ofNullable(
                                        ElasticsearchModel.this.parser
                                                .apply(hit))
                                        .ifPresent(result::add);
                                return !monitor.isCanceled();
                            });
                    client.setScrollSettings(ss);
                    JsonObject query = buildQuery(from, to, maxResults);
                    client.executeQuery(ElasticsearchModel.this.mapping, query);
                    if (0 >= maxResults)
                    {
                        Collections.sort(result);
                    }
                    // }
                    // catch (JSONException e)
                    // {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    synchronized (ElasticsearchModel.this)
                    {
                        ElasticsearchModel.this.messages = result;
                        ElasticsearchModel.this.queryJob = null;
                    }
                    ElasticsearchModel.this.sendCompletionNotification();
                    return Status.OK_STATUS;
                }
            };
            this.queryJob.schedule();
        }
    }
}
