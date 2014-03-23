package com.neutrino.csv;

import com.neutrino.profiling.StagingSchema;

import javax.persistence.PersistenceException;
import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DBSaver implements LineReadListener {
    private final StagingSchema stg;
    private boolean tableCreated;
    private String query;
    private List<String[]> queryBuffer;
    private List<String> rawBuffer;
    private List<Long> lineNumbers;

    private static final int BATCH_COUNT = 1000;

    public DBSaver(int userId, int dataSetId) {
        tableCreated = false;
        stg = new StagingSchema(userId, dataSetId);
        queryBuffer = new ArrayList<>(BATCH_COUNT);
        rawBuffer = new ArrayList<>(BATCH_COUNT);
        lineNumbers = new ArrayList<>(BATCH_COUNT);
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
    public Object lineRead(long lineNumber, String[] line, String raw, List<CSVDataHeader> headers, boolean last) {
        createTable(headers);
        if (!last && (line == null || headers.size() != line.length)) {
            System.out.println("Inserting CSV error for line " + lineNumber);
            insertError(new CSVError(lineNumber, raw));
        } else {
            if (!last) {
                queryBuffer.add(line);
                rawBuffer.add(raw);
                lineNumbers.add(lineNumber);
            }
            if (queryBuffer.size() >= BATCH_COUNT || last) {
                try {
                    insertRecords(queryBuffer, headers);
                } catch (PersistenceException e) {
                    if (e.getCause() instanceof BatchUpdateException) {
                        BatchUpdateException bue = (BatchUpdateException) e.getCause();
                        int[] counts = bue.getUpdateCounts();
                        for (int i = 0; i < counts.length; i++) {
                            if (counts[i] == Statement.EXECUTE_FAILED) {
                                System.out.println("Inserting CSV error for line due to SQL error " + lineNumbers.get(i));
                                insertError(new CSVError(lineNumbers.get(i), rawBuffer.get(i)));
                            }
                        }
                    } else {
                        throw e;
                    }
                }
                queryBuffer.clear();
                lineNumbers.clear();
                rawBuffer.clear();
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

    private void insertRecords(List<String[]> valuesList, List<CSVDataHeader> headers) {
        stg.insertIntoStagingTableInBulk(getQuery(headers), headers, valuesList);
    }

    private void insertError(CSVError lastError) {
        stg.insertIntoRejectsTable(lastError);
    }
}
