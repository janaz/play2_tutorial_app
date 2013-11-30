package com.clustrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.clustrino.profiling.metadata.*;

import java.util.Arrays;
import java.util.List;

public class MetadataSchema {
    private static final List<Class<?>> CLASSES = Arrays.asList(new Class<?>[]{
            ProfilingResultsColumn.class,
            ProfilingResultsFormat.class,
            ProfilingResultsValue.class,
            DataSet.class,
            File.class,
            DataColumn.class});

    private final Integer userId;

    public MetadataSchema(Integer userId) {
        this.userId = userId;
    }

    private String databaseName() {
        return String.format("Metadata%03d", this.userId);
    }

    public boolean isCreated() {
        return EbeanServerManager.getManager().isCreated(databaseName());
    }

    public EbeanServer server() {
        return EbeanServerManager.getManager().getServer(databaseName(), CLASSES, false);
    }

    public void createTables() {
        EbeanServer srv = EbeanServerManager.getManager().getServer(databaseName(), CLASSES, true);
        srv.beginTransaction().end();
    }

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());

    }

}
