package com.neutrino.datamappingdiscovery;

import com.avaje.ebean.Query;
import com.avaje.ebean.QueryListener;
import com.neutrino.models.configuration.DataFormat;
import com.neutrino.models.configuration.MappingDiscoveryRule;
import com.neutrino.models.configuration.ReferenceData;
import com.neutrino.models.metadata.*;
import com.neutrino.profiling.MetadataSchema;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataMapping {
    private final DataSet dataSet;

    public DataMapping(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void process() {

        for (DataColumn dataColumn : dataSet.getColumns()) {
            processColumn(dataColumn);
        }

    }

    private class ProfilingResultValueListener implements QueryListener<ProfilingResultValue> {
        private final Map<Integer, Integer> regexComplianceCount = new HashMap<>();
        private final Map<Integer, Integer> refDataComplianceCount = new HashMap<>();
        private final Map<String, Set<String>> refValues = new HashMap<>();


        public Map<Integer, Integer> getRefDataComplianceCount() {
            return refDataComplianceCount;
        }

        public Map<Integer, Integer> getRegexComplianceCount() {
            return regexComplianceCount;
        }


        public ProfilingResultValueListener() {
            ReferenceData.find.setAutofetch(false).setListener(new QueryListener<ReferenceData>() {
                @Override
                public void process(ReferenceData referenceData) {
                    Set<String> set = refValues.get(referenceData.code);
                    if (set == null) {
                        set = new HashSet<>();
                    }
                    set.add(referenceData.value);
                }
            }).findList();
        }

        private Set<String> refValuesByCode(String code) {
            return refValues.get(code);
        }

        @Override
        public void process(ProfilingResultValue profilingResultValue) {
            String[] tokens = profilingResultValue.value.split("\\s");
            //go through the config
            for (MappingDiscoveryRule rule : MappingDiscoveryRule.find.all()) {
                Integer refCountInt = refDataComplianceCount.get(rule.id);
                int refCount = (refCountInt == null) ? 0 : refCountInt.intValue();
                boolean refCountChanged = false;
                for (String token : tokens) {
                    if (refValuesByCode(rule.refCode).contains(token.trim().toUpperCase())) {
                        refCount += profilingResultValue.cardinality;
                        refCountChanged = true;
                    }
                }
                if (refCountChanged) {
                    refDataComplianceCount.put(rule.id, refCount);
                }

                Integer regexCountInt = regexComplianceCount.get(rule.id);
                int regexCount = (regexCountInt == null) ? 0 : regexCountInt.intValue();
                if (rule.regexPatternCompiled() != null && rule.regexPatternCompiled().matcher(profilingResultValue.value.trim()).find()) {
                    regexCount += profilingResultValue.cardinality;
                    regexComplianceCount.put(rule.id, regexCount);
                }
            }
        }
    }

    private class ProfilingResultFormatListener implements QueryListener<ProfilingResultFormat> {
        private final Map<Integer, Integer> formatComplianceCount = new HashMap<>();

        private final Map<String, Set<String>> formats = new HashMap<>();

        public Map<Integer, Integer> getFormatComplianceCount() {
            return formatComplianceCount;
        }

        public ProfilingResultFormatListener() {
            DataFormat.find.setAutofetch(false).setListener(new QueryListener<DataFormat>() {
                @Override
                public void process(DataFormat dataFormat) {
                    Set<String> set = formats.get(dataFormat.code);
                    if (set == null) {
                        set = new HashSet<>();
                    }
                    set.add(dataFormat.format);
                }
            }).findList();
        }

        @Override
        public void process(ProfilingResultFormat profilingResultFormat) {
            for (MappingDiscoveryRule rule : MappingDiscoveryRule.find.all()) {
                Integer formatCountInt = formatComplianceCount.get(rule.id);
                int formatCount = (formatCountInt == null) ? 0 : formatCountInt.intValue();
                if (formats.get(rule.formatsCode).contains(profilingResultFormat.format)) {
                    formatCount += profilingResultFormat.cardinality;
                    formatComplianceCount.put(rule.id, formatCount);
                }
            }
        }
    }

    private class ProfilingResultColumnListener implements QueryListener<ProfilingResultColumn> {
        private final Map<Integer, Boolean> nameMatching = new HashMap<>();

        private final Map<String, Set<String>> formats = new HashMap<>();


        public ProfilingResultColumnListener() {
        }

        @Override
        public void process(ProfilingResultColumn profilingResultColumn) {
            for (MappingDiscoveryRule rule : MappingDiscoveryRule.find.all()) {
                nameMatching.put(rule.id, rule.isNameMatching(profilingResultColumn.columnName));
            }
        }
    }


    public void processColumn(DataColumn dataColumn) {
        ProfilingResultValueListener valueListener = new ProfilingResultValueListener();
        ProfilingResultFormatListener formatListener = new ProfilingResultFormatListener();
        MetadataSchema mtd = new MetadataSchema(dataSet.userId);
        Query<ProfilingResultValue> valueQuery = mtd.server().createQuery(ProfilingResultValue.class);
        Query<ProfilingResultFormat> formatQuery = mtd.server().createQuery(ProfilingResultFormat.class);

        valueQuery.setAutofetch(false).where().eq("dataColumn", dataColumn).setListener(valueListener).findList();
        formatQuery.setAutofetch(false).where().eq("dataColumn", dataColumn).setListener(formatListener).findList();

        ProfilingResultColumn profilingColRes = mtd.server().createQuery(ProfilingResultColumn.class).setAutofetch(false).where().eq("dataColumn", dataColumn).findUnique();
        Map<Integer,Double> results = new HashMap<>();
        for (MappingDiscoveryRule rule : MappingDiscoveryRule.find.all()) {
            double score = 0.0f;
            if (rule.isNameMatching(dataColumn.name)) {
                score += rule.nameScore;
            }

            int refMatchCount = valueListener.getRefDataComplianceCount().get(rule.id);
            if (rule.refFullScorePercThresh != null && rule.refFullScore != null && rule.refMinimumPercMatch != null && ((float)refMatchCount > profilingColRes.totalCount * rule.refMinimumPercMatch.floatValue())) {
                double multiplier;
                if (profilingColRes.totalCount == 0) {
                    multiplier = 0;
                } rule.refFullScorePercThresh.equals(new BigDecimal("0.0"))) {
                    multiplier = 1;
                } else {
                    multiplier = (refMatchCount - profilingColRes.totalCount * rule.refMinimumPercMatch.floatValue()) / (profilingColRes.totalCount * rule.refFullScorePercThresh.floatValue());
                }
                if (multiplier > 1) {
                    multiplier = 1;
                }

                score += (double)rule.refFullScore * multiplier;

            }

            if (rule.nullMaximumPerc != null && rule.nullMinimumPerc != null && rule.nullPenalty != null) {
                double nullPerc = 1.0 - profilingColRes.percentagePopulated.doubleValue();
                if (nullPerc > rule.nullMaximumPerc.doubleValue() || nullPerc < rule.nullMinimumPerc.doubleValue()) {
                    score += rule.nullPenalty;
                }
            }

            //min - max ??? - what about data parsing / data type stuff for dates ????


            //formats


            results.put(rule.id, score);
        }



        formatListener.getFormatComplianceCount();
        valueListener.getRefDataComplianceCount();
        valueListener.getRegexComplianceCount();

    }
}
