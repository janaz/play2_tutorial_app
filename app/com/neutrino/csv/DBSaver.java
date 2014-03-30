package com.neutrino.csv;

import com.neutrino.profiling.StagingSchema;

import javax.persistence.PersistenceException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DBSaver implements LineReadListener {
    private final StagingSchema stg;
    private boolean tableCreated;
    private String query;
    private List<CSVLine> lines;

    private static final int BATCH_COUNT = 10000;

    public DBSaver(int userId, int dataSetId) {
        tableCreated = false;
        stg = new StagingSchema(userId, dataSetId);
        lines = new ArrayList<>(BATCH_COUNT);
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
    public Object lineRead(CSVLine line, List<CSVDataHeader> headers, boolean last) {
        createTable(headers);
        if (!last && (line.getValues() == null || headers.size() != line.getValues().length)) {
            System.out.println("Inserting CSV error for line " + line.getLineNumber());
            insertError(new CSVError(line));
        } else {
            if (!last) {
                lines.add(line);
            }
            if (lines.size() >= BATCH_COUNT || last) {
                boolean errors;
                do {
                    errors = false;
                    int[] results = insertRecords(lines, headers);
                    for (int i = results.length - 1; i >= 0; i--) {
                        if (results[i] == Statement.EXECUTE_FAILED) {
                            errors = true;
                            System.out.println("Inserting CSV error for line due to SQL error " + lines.get(i).getLineNumber());
                            insertError(new CSVError(lines.get(i)));
                            lines.remove(i);
                        }
                    }
                } while (errors);
                lines.clear();
            }
        }
        return null;
    }

    @Override
    public boolean finished() {
        return false;
    }

    private int[] insertRecords(List<CSVLine> valuesList, List<CSVDataHeader> headers) {
        return stg.insertIntoStagingTableInBulk(getQuery(headers), headers, valuesList);
    }

    private void insertError(CSVError lastError) {
        try {
            stg.insertIntoRejectsTable(lastError);
        } catch (PersistenceException ex) {
            System.out.println("Can't write into filerejects: " + lastError.getText());
            ex.printStackTrace();
        }
    }
}
