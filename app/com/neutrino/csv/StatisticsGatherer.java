package com.neutrino.csv;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class StatisticsGatherer implements LineReadListener {
    private final List<ColumnStatistics> stats;
    private final List<CSVError> errors;
    private final long limit;
    private long linesRead;

    public StatisticsGatherer() {
        this(-1L);
    }

    public StatisticsGatherer(long limit) {
        stats = new ArrayList<>();
        errors = new ArrayList<>();
        this.limit=limit;
        linesRead = 0L;
    }

    public List<ColumnStatistics> getStats() {
        return stats;
    }

    public List<CSVError> getErrors() {
        return errors;
    }

    @Override
    public Object lineRead(CSVLine line, List<CSVDataHeader> headers, boolean last) {
        initStats(headers);
        List<Comparable<?>> parsedValues = parsedLine(line, headers);
        if (parsedValues != null) {
            linesRead++;
            for (int idx = 0; idx < stats.size(); idx++) {
                stats.get(idx).add(parsedValues.get(idx));
            }
        }
        return parsedValues;
    }

    private void initStats(List<CSVDataHeader> headers) {
        if(stats.isEmpty()) {
            for (CSVDataHeader header : headers) {
                stats.add(new ColumnStatistics(header));
            }
        }
    }

    @Override
    public boolean finished() {
        if (limit < 0) return false;
        return linesRead >= limit;
    }

    private List<Comparable<?>> parsedLine(CSVLine line, List<CSVDataHeader> headers){
        if (line == null || headers.size() != line.getValues().length) {
            System.out.println("Error "+ headers +" " +line);
            errors.clear();
            errors.add(new CSVError(line));
            return null;
        } else {
            try {
                List<Comparable<?>> parsedValues = new ArrayList<>(headers.size());

                for (int idx = 0; idx < stats.size(); idx++) {
                    String stringValue = line.getValues()[idx];
                    Comparable<?> parsedValue = headers.get(idx).parsedValue(stringValue);
                    parsedValues.add(parsedValue);
                }
                return parsedValues;
            } catch (Exception e) {
                System.out.println(e);
                System.out.println(e.getStackTrace());
                errors.clear();
                errors.add(new CSVError(line));
                return null;
            }
        }
    }

    public List<Float> getPercentagePopulated() {
        return Lists.newArrayList(Lists.transform(getStats(), new Function<ColumnStatistics, Float>() {
            @Nullable
            @Override
            public Float apply(@Nullable ColumnStatistics columnStatistics) {
                return columnStatistics.getPopulatedPercentage();
            }
        }));

    }
}
