/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.archive.common.engine.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.csstudio.archive.common.engine.service.IServiceProvider;
import org.csstudio.archive.common.service.ArchiveServiceException;
import org.csstudio.archive.common.service.IArchiveEngineFacade;
import org.csstudio.archive.common.service.channel.IArchiveChannel;
import org.csstudio.archive.common.service.channelgroup.IArchiveChannelGroup;
import org.csstudio.archive.common.service.channelstatus.IArchiveChannelStatus;
import org.csstudio.archive.common.service.engine.IArchiveEngine;
import org.csstudio.archive.common.service.enginestatus.ArchiveEngineStatus;
import org.csstudio.archive.common.service.enginestatus.EngineMonitorStatus;
import org.csstudio.archive.common.service.enginestatus.IArchiveEngineStatus;
import org.csstudio.domain.desy.service.osgi.OsgiServiceUnavailableException;
import org.csstudio.domain.desy.system.ISystemVariable;
import org.csstudio.domain.desy.time.TimeInstant;
import org.csstudio.domain.desy.time.TimeInstant.TimeInstantBuilder;
import org.csstudio.domain.desy.typesupport.BaseTypeConversionSupport;
import org.csstudio.domain.desy.typesupport.TypeSupportException;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

/** Data model of the archive engine.
 *  @author Kay Kasemir
 *  @author Bastian Knerr
 */
public final class EngineModel {
    private static final Logger LOG = LoggerFactory.getLogger(EngineModel.class);

    private static final String[] ADDITIONAL_TYPE_PACKAGES =
        new String[]{
                     "org.csstudio.domain.desy.epics.types",
                     };

    /** Name of this model */
    private final String _name;

    /** Thread that writes to the <code>archive</code> */
    private WriteExecutor _writeExecutor;

    /**  All channels */
    private final ConcurrentMap<String, ArchiveChannel<?, ?>> _channelMap;

    /** Groups of archived channels */
    private final ConcurrentMap<String, ArchiveGroup> _groupMap;

    /** Engine start state */
    private volatile EngineState _state = EngineState.IDLE;

    /** Start time of the model */
    private TimeInstant _startTime;

    private final long _writePeriodInMS;
    private final long _heartBeatPeriodInMS;

    private final IServiceProvider _provider;
    private IArchiveEngine _engine;

    /**
     * Construct model that writes to archive
     * @param engineName
     * @param provider provider for services
     * @throws EngineModelException
     * @throws UnknownHostException
     */
    public EngineModel(@Nonnull final String engineName,
                       @Nonnull final IServiceProvider provider) throws EngineModelException {
        _name = engineName;
        _provider = provider;

        _groupMap = new MapMaker().concurrencyLevel(2).makeMap();
        _channelMap = new MapMaker().concurrencyLevel(2).makeMap();

        _writePeriodInMS = 1000*provider.getPreferencesService().getWritePeriodInS();
        _heartBeatPeriodInMS = 1000*provider.getPreferencesService().getHeartBeatPeriodInS();

        _engine = findEngineConfByName(_name, _provider);
        logHttpHostAndPort();
    }

    private void logHttpHostAndPort() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            hostName = "localhost";
        }
        LOG.info("Http server on host {} and port: {}", hostName, _engine.getUrl().getPort());
    }

    /** @return Engine name (descriptive) */
    @Nonnull
    public String getName() {
        return _name;
    }

    /** @return Write period in milliseconds */
    public long getWritePeriodInMS() {
        return _writePeriodInMS;
    }

    /** @return Current model state */
    @Nonnull
    public EngineState getState() {
        return _state;
    }

    /** @return Start time of the engine or <code>null</code> if not running */
    @CheckForNull
    public TimeInstant getStartTime() {
        return _startTime;
    }

    /**
     *  Add new group if not already exists.
     *
     *  @param _name Name of the group to find or add.
     *  @return ArchiveGroup the already existing or, if not, newly added group
     */
    @Nonnull
    private ArchiveGroup addGroup(@Nonnull final IArchiveChannelGroup groupCfg) {
        final String groupName = groupCfg.getName();
        return _groupMap.putIfAbsent(groupName, new ArchiveGroup(groupName));
    }

    /** @return Group by that name or <code>null</code> if not found */
    @CheckForNull
    public ArchiveGroup getGroup(@Nonnull final String name) {
        return _groupMap.get(name);
    }

    @Nonnull
    public Collection<ArchiveGroup> getGroups() {
        return _groupMap.values();
    }

    /** @return Channel by that name or <code>null</code> if not found */
    @CheckForNull
    public ArchiveChannel<?, ?> getChannel(@Nonnull final String name) {
        return _channelMap.get(name);
    }

    /** @return All channels */
    @Nonnull
    public Collection<ArchiveChannel<?, ?>> getChannels() {
        return _channelMap.values();
    }

    /**
     * Start processing all channels and writing to archive.
     * @throws EngineModelException
     */
    public void start() throws EngineModelException {
        if (_state != EngineState.CONFIGURED) {
            throw new IllegalStateException("Engine has not been configured before start.", null);
        }
        if (_engine == null || _writeExecutor == null) {
            throw new IllegalStateException("Engine or executor are null although engine is configured.", null);
        }

        checkAndUpdateLastShutdownStatus(_provider, _engine, _channelMap.values());
        _startTime = TimeInstantBuilder.fromNow();
        _state = EngineState.RUNNING;
        _writeExecutor.start(_heartBeatPeriodInMS, _writePeriodInMS);
        startChannelGroups(_groupMap.values());
    }


    /**
     * Retrieves the last archiver status from the archive.<br/>
     * If it was a graceful shutdown, anything's fine.<br/>
     * Otherwise:
     * <ol>
     * <li> update the engine_status table by an 'engine OFF' info with the timestamp of the
     * last engine.alive value. </li>
     * <li> update the channel_status table for all channels of this engine that have status
     * 'connected' with a new row disconnected and the timestamp of the last engine.alive value.</li>
     * </ol>
     * @param collection
     *
     * @throws EngineModelException
     */
    private void checkAndUpdateLastShutdownStatus(@Nonnull final IServiceProvider provider,
                                                  @Nonnull final IArchiveEngine engine,
                                                  @Nonnull final Collection<ArchiveChannel<?, ?>> channels)
                                                  throws EngineModelException {
        try {
            final IArchiveEngineFacade facade = provider.getEngineFacade();

            final IArchiveEngineStatus engineStatus =
                facade.getLatestEngineStatusInformation(engine.getId(),
                                                        TimeInstantBuilder.fromNow());

            if (isNotFirstStart(engineStatus) && wasNotGracefullyShutdown(engineStatus)) {
                facade.writeEngineStatusInformation(engine.getId(),
                                                    EngineMonitorStatus.OFF,
                                                    engine.getLastAliveTime(),
                                                    "Ungraceful shutdown");

                checkAndUpdateChannelsStatus(facade, engine, channels);
            }
            facade.writeEngineStatusInformation(engine.getId(),
                                                EngineMonitorStatus.ON,
                                                TimeInstantBuilder.fromNow(),
                                                "Engine Startup");

        } catch (@Nonnull final Exception e) {
            handleExceptions(e);
        }
    }

    private boolean wasNotGracefullyShutdown(@Nonnull final IArchiveEngineStatus engineStatus) {
        return !EngineMonitorStatus.OFF.equals(engineStatus.getStatus());
    }

    private boolean isNotFirstStart(@CheckForNull final IArchiveEngineStatus engineStatus) {
        return engineStatus != null;
    }

    private void checkAndUpdateChannelsStatus(@Nonnull final IArchiveEngineFacade facade,
                                              @Nonnull final IArchiveEngine engine,
                                              @Nonnull final Collection<ArchiveChannel<?, ?>> channels)
                                              throws ArchiveServiceException {
        for (final ArchiveChannel<?, ?> channel : channels) {
            final IArchiveChannelStatus status =
                facade.getChannelStatusByChannelName(channel.getName());

            if (status!= null && status.isConnected()) { // still connected?
                facade.writeChannelStatusInfo(status.getChannelId(),
                                              false,
                                              "Ungraceful engine shutdown",
                                              engine.getLastAliveTime());
            }
        }
    }

    private void startChannelGroups(@Nonnull final Collection<ArchiveGroup> groups) throws EngineModelException {
        for (final ArchiveGroup group : groups) {
            group.start(ArchiveEngineStatus.ENGINE_START);
            if (getState() == EngineState.SHUTDOWN_REQUESTED) {
                break;
            }
        }
    }

    /** @return Timestamp of end of last write run or <code>null</code> if not started before */
    @CheckForNull
    public TimeInstant getLastWriteTime() {
        return _writeExecutor.getLastWriteTime();
    }

    /** @return Average number of values per write run */
    @CheckForNull
    public Double getAvgWriteCount() {
        return _writeExecutor.getAvgWriteCount();
    }

    /** @return  Average duration of write run in milliseconds */
    @CheckForNull
    public Duration getAvgWriteDuration() {
        return _writeExecutor.getAvgWriteDurationInMS();
    }

    /** Setting the model state to shutdown.
     *  @see #getState()
     */
    public void requestStop() {
        _state = EngineState.SHUTDOWN_REQUESTED;
    }

    /** Setting the model state to restart.
     *  @see #getState()
     */
    public void requestRestart() {
        _state = EngineState.RESTART_REQUESTED;
    }

    /** Reset engine statistics */
    public void resetStats() {
        _writeExecutor.reset();
        synchronized (this) {
            for (final ArchiveChannel<?, ?> channel : _channelMap.values()) {
                channel.reset();
            }
        }
    }

    /**
     * Stop monitoring the channels, flush the write buffers.
     * @throws EngineModelException
     */
    @SuppressWarnings("nls")
    public void stop() throws EngineModelException {
        if (_state == EngineState.STOPPING || _state == EngineState.IDLE) {
            return;
        }
        _state = EngineState.STOPPING;

        // Disconnect from network
        LOG.info("Stopping archive groups");
        for (final ArchiveGroup group : _groupMap.values()) {
            group.stop(ArchiveEngineStatus.ENGINE_STOP);
        }

        try {
            _provider.getEngineFacade().writeEngineStatusInformation(_engine.getId(),
                                                                     EngineMonitorStatus.OFF,
                                                                     TimeInstantBuilder.fromNow(),
                                                                     "Graceful Shutdown");
        } catch (final Exception e) {
            handleExceptions(e);
        }

        LOG.info("Shutting down writer");
        _writeExecutor.shutdown();

        _state = EngineState.CONFIGURED;
    }


    /** Read configuration of model from RDB.
     *  @param port Current HTTPD port
     * @throws EngineModelException
     */
    @SuppressWarnings("nls")
    public void readConfig() throws EngineModelException {
        try {
            if (_state != EngineState.IDLE) {
                LOG.error("Read configuration while state " + _state + ". Should be " + EngineState.IDLE);
                return;
            }
            if (_engine == null) { // to be reconfigured
                _engine = findEngineConfByName(_name, _provider);
            }

            _writeExecutor = new WriteExecutor(_provider, _engine.getId());

            final IArchiveEngineFacade service = _provider.getEngineFacade();

            final Collection<IArchiveChannelGroup> groups =
                service.getGroupsForEngine(_engine.getId());

            for (final IArchiveChannelGroup groupCfg : groups) {
                configureGroup(_provider, groupCfg, _writeExecutor, _channelMap);
            }
        } catch (final Exception e) {
            handleExceptions(e);
        }
        _state = EngineState.CONFIGURED;
    }

    @Nonnull
    private IArchiveEngine findEngineConfByName(@Nonnull final String name,
                                                @Nonnull final IServiceProvider provider)
                                                throws EngineModelException {
        IArchiveEngine engine = null;
        try {
            engine = provider.getEngineFacade().findEngine(name);
        } catch (final OsgiServiceUnavailableException e) {
            throw new EngineModelException("Engine could not be retrieved. OSGi service unavailable.", e);
        } catch (final ArchiveServiceException e) {
            throw new EngineModelException("Engine could not be retrieved. Internal archive service exception.", e);
        }
        if (engine == null) {
            throw new EngineModelException("Unknown engine '" + name + "'.", null);
        }
        return engine;
    }

    private void configureGroup(@Nonnull final IServiceProvider provider,
                                @Nonnull final IArchiveChannelGroup groupCfg,
                                @Nonnull final WriteExecutor writeExecutor,
                                @Nonnull final ConcurrentMap<String, ArchiveChannel<?, ?>> channelMap)
                                throws ArchiveServiceException,
                                       OsgiServiceUnavailableException,
                                       EngineModelException {
        final ArchiveGroup group = addGroup(groupCfg);

        final Collection<IArchiveChannel> channelCfgs =
            provider.getEngineFacade().getChannelsByGroupId(groupCfg.getId());

        for (final IArchiveChannel channelCfg : channelCfgs) {
            final ArchiveChannel<Serializable, ISystemVariable<Serializable>> channel =
                createArchiveChannel(channelCfg, provider);

            @SuppressWarnings("unchecked")
            final ArchiveChannel<Serializable, ISystemVariable<Serializable>> presentChannel =
                (ArchiveChannel<Serializable, ISystemVariable<Serializable>>) channelMap.putIfAbsent(channel.getName(), channel);

            if (presentChannel != null) {
                writeExecutor.addChannel(presentChannel);
                group.add(presentChannel);
            } else {
                writeExecutor.addChannel(channel);
                group.add(channel);
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Nonnull
    private ArchiveChannel<Serializable, ISystemVariable<Serializable>>
    createArchiveChannel(@Nonnull final IArchiveChannel cfg,
                         @Nonnull final IServiceProvider provider) throws EngineModelException {
        final String dataType = cfg.getDataType();
        try {
            final Class<?> typeClass =
                BaseTypeConversionSupport.createBaseTypeClassFromString(dataType,
                                                                        ADDITIONAL_TYPE_PACKAGES);

            if (!Collection.class.isAssignableFrom(typeClass)) {
                return new ArchiveChannel(cfg.getName(), cfg.getId(), typeClass, provider);
            }
            final String elemType =
                BaseTypeConversionSupport.parseForFirstNestedGenericType(dataType);
            final Class<?> elemClass = BaseTypeConversionSupport.createBaseTypeClassFromString(elemType,
                                                                                               ADDITIONAL_TYPE_PACKAGES);
            return new ArchiveChannel(cfg.getName(), cfg.getId(), typeClass, elemClass,  provider);

        } catch (final TypeSupportException e) {
            throw new EngineModelException("Datatype " + dataType + " of channel " + cfg.getName() +
                                           " could not be transformed into Class object", e);
        }
    }

    private void handleExceptions(@Nonnull final Exception inE) throws EngineModelException {
        final String msg = "Failure during archive engine configuration retrieval: ";
        try {
            throw inE;
        } catch (final OsgiServiceUnavailableException e) {
            throw new EngineModelException(msg + "Service unavailable.", e);
        } catch (final ArchiveServiceException e) {
            throw new EngineModelException(msg + "Internal service exception.", e);
        } catch (final MalformedURLException e) {
            throw new EngineModelException(msg + "Engine url malformed.", e);
        } catch (final TypeSupportException e) {
            throw new EngineModelException(msg + "Channel type not supported.", e);
        } catch (final EngineModelException eme) {
            throw eme;
        } catch (final Exception re) {
            throw new EngineModelException("Unknown exception: ", re);
        }
    }

    public void clearConfiguration() {
        if (_state != EngineState.IDLE && _state != EngineState.CONFIGURED) {
            throw new IllegalStateException("Clearing configuration only allowed in " +
                                            EngineState.IDLE.name() + " or " + EngineState.CONFIGURED.name() + " state.");
        }
        _engine = null;
        _groupMap.clear();
        _channelMap.clear();
        _startTime = null;

        _state = EngineState.IDLE;
    }

    @CheckForNull
    public Integer getHttpPort() {
        if (_engine != null) {
            return _engine.getUrl().getPort();
        }
        return null;
    }
}
