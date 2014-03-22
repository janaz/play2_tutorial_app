package com.neutrino.csv;

import com.neutrino.profiling.StagingSchema;

import javax.persistence.PersistenceException;
import java.util.List;


public class DBSaver implements LineReadListener {
    private final StagingSchema stg;
    private boolean tableCreated;
    private String query;

    public DBSaver(int userId, int dataSetId) {
        tableCreated = false;
        stg = new StagingSchema(userId, dataSetId);
    }

    private void createTable(List<CSVDataHeader> headers) {
        if (tableCreated) {
            return;
        }
        realCreateTable(headers);
        tableCreated = true;
    }

    private String getQuery(List<CSVDataHeader> headers) {
        if (query == null) {
            query = stg.getInsertIntoStagingQuery(headers);
        }
        return query;
    }

    private void realCreateTable(List<CSVDataHeader> headers) {
        stg.createTables(headers);
    }

    @Override
    public Object lineRead(long lineNumber, String[] line, String raw, List<CSVDataHeader> headers) {
        createTable(headers);
        if (line == null || headers.size() != line.length) {
            System.out.println("Inserting CSV error for line " + lineNumber);
            insertError(new CSVError(lineNumber, raw));

        } else {
            try {
                insertRecord(line, headers);
            } catch (PersistenceException e) {
                System.out.println("Inserting error for line " + lineNumber + "["+raw+"]");
                insertError(new CSVError(lineNumber, raw));
            }
        }
        return null;
    }

    @Override
    public boolean finished() {
        return false;
    }

    private void insertRecord(String[] values, List<CSVDataHeader> headers) {
        stg.insertIntoStagingTable(getQuery(headers), headers, values);
    }

    private void insertError(CSVError lastError) {
        stg.insertIntoRejectsTable(lastError);
    }
}
