package com.clustrino.csv;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class StatisticsGatherer implements LineReadListener {
    private final List<ColumnStatistics> stats;
    private final List<CSVError> errors;

    public StatisticsGatherer() {
        stats = new ArrayList<>();
        errors = new ArrayList<>();
    }

    public List<ColumnStatistics> getStats() {
        return stats;

    }

    public List<CSVError> getErrors() {
        return errors;
    }

    @Override
    public Object lineRead(long lineNumber, String[] line, List<DataCategory> categories) {
        while (stats.size() < categories.size()) {
            stats.add(new ColumnStatistics());
        }
        List<Comparable<?>> parsedValues = parsedLine(lineNumber, line, categories);
        if (parsedValues != null) {
            for (int idx = 0; idx < stats.size(); idx++) {
                stats.get(idx).add(parsedValues.get(idx));
            }
        }
        return parsedValues;
    }

    private List<Comparable<?>> parsedLine(long lineNumber, String[] line, List<DataCategory> categories){
        if (categories.size() != line.length) {
            errors.add(new CSVError(lineNumber, Joiner.on(',').join(line)));
            return null;
        } else {
            try {
                List<Comparable<?>> parsedValues = new ArrayList<>(categories.size());

                for (int idx = 0; idx < stats.size(); idx++) {
                    String stringValue = line[idx];
                    Comparable<?> parsedValue = categories.get(idx).parsedValue(stringValue);
                    parsedValues.add(parsedValue);
                }
                return parsedValues;
            } catch (Exception e) {
                errors.add(new CSVError(lineNumber, Joiner.on(',').join(line)));
                return null;
            }
        }

    }

    public List<Float> getPercentagePopulated() {
        return Lists.transform(getStats(), new Function<ColumnStatistics, Float>() {
            @Nullable
            @Override
            public Float apply(@Nullable ColumnStatistics columnStatistics) {
                return columnStatistics.getPopulatedPercentage();
            }
        });

    }
}
