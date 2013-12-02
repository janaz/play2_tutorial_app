package com.neutrino.csv;

import com.neutrino.profiling.StagingSchema;

import java.util.List;


public class DBSaver implements LineReadListener {
    private final StagingSchema stg;
    private boolean tableCreated;
    private final int userId;
    private final int dataSetId;
    private final StatisticsGatherer statsGatherer;

    public DBSaver(int userId, int dataSetId) {
        this.userId = userId;
        this.dataSetId = dataSetId;
        tableCreated = false;
        statsGatherer = new StatisticsGatherer();
        stg = new StagingSchema(userId, dataSetId);
    }

    public StatisticsGatherer getStatsGatherer() {
        return statsGatherer;
    }


    private void createTable(List<DataCategory> columns){
        if (tableCreated) {
            return;
        }
        realCreateTable(columns);
        tableCreated = true;
    }

    private void realCreateTable(List<DataCategory> columns) {
        stg.createTables(columns);
    }

    @Override
    public Object lineRead(long lineNumber, String[] line, String raw, List<DataCategory> categories) {
        createTable(categories);
        List<Comparable<?>> parsedValues = (List<Comparable<?>>)statsGatherer.lineRead(lineNumber, line, raw, categories);
        if (parsedValues == null) {
            CSVError lastError = statsGatherer.getErrors().get(statsGatherer.getErrors().size() - 1);
            insertError(lastError);
        } else {
            insertRecord(parsedValues, categories);

        }
        return null;
    }

    @Override
    public boolean finished() {
        return false;
    }

    private void insertRecord(List<Comparable<?>> parsedValues, List<DataCategory> categories) {
        stg.insertIntoStagingTable(categories, parsedValues);
    }

    private void insertError(CSVError lastError) {
        stg.insertIntoRejectsTable(lastError);
    }
}
