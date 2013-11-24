package com.clustrino.csv;

import com.clustrino.profiling.StagingSchema;

import java.util.List;


public class DBSaver implements LineReadListener {
    private final StagingSchema stg;
    private boolean tableCreated;
    private final long userId;
    private final long dataSetId;
    private final StatisticsGatherer statsGatherer;

    public DBSaver(long userId, long dataSetId) {
        this.userId = userId;
        this.dataSetId = dataSetId;
        tableCreated = false;
        statsGatherer = new StatisticsGatherer();
        stg = new  StagingSchema(userId, dataSetId);
    }

    private void createTable(List<DataCategory> columns){
        if (tableCreated) {
            return;
        }
        realCreateTable(columns);
        tableCreated = true;
    }

    private void realCreateTable(List<DataCategory> columns) {
        stg.createRejectsTable();
        stg.createStagingTable(columns);
    }

    @Override
    public Object lineRead(long lineNumber, String[] line, List<DataCategory> categories) {
        createTable(categories);
        List<Comparable<?>> parsedValues = (List<Comparable<?>>)statsGatherer.lineRead(lineNumber, line, categories);
        if (parsedValues == null) {
            CSVError lastError = statsGatherer.getErrors().get(statsGatherer.getErrors().size() - 1);
            insertError(lastError);
        } else {
            insertRecord(parsedValues, categories);
        }
        return null;
    }

    private void insertRecord(List<Comparable<?>> parsedValues, List<DataCategory> categories) {
        stg.insertIntoStagingTable(categories, parsedValues);
    }

    private void insertError(CSVError lastError) {
        stg.insertIntoRejectsTable(lastError);
        //To change body of created methods use File | Settings | File Templates.
    }


}
