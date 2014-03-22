package com.neutrino.datamappingdiscovery;

import com.avaje.ebean.Query;
import com.avaje.ebean.QueryListener;
import com.neutrino.models.configuration.DataFormat;
import com.neutrino.models.configuration.MappingDiscoveryRule;
import com.neutrino.models.configuration.ReferenceData;
import com.neutrino.models.metadata.*;
import com.neutrino.profiling.MetadataSchema;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class DataMapping {
    private final DataSet dataSet;
    private final List<MappingDiscoveryRule> rules;
    private final Map<String, Set<String>> refValues;
    private final Map<String, Set<String>> formats;
    private final Map<Integer, Pattern> regexPatterns;


    public DataMapping(DataSet dataSet) {
        this.dataSet = dataSet;
        this.rules = MappingDiscoveryRule.find.all();
        this.refValues = new HashMap<>();
        this.formats = new HashMap<>();
        this.regexPatterns = new HashMap<>();

        for (MappingDiscoveryRule rule : rules) {
            regexPatterns.put(rule.id, rule.regexPatternCompiled());
        }
        ReferenceData.find.setAutofetch(false).setListener(new QueryListener<ReferenceData>() {
            @Override
            public void process(ReferenceData referenceData) {
                Set<String> set = refValues.get(referenceData.code);
                if (set == null) {
                    set = new HashSet<>();
                    refValues.put(referenceData.code, set);
                }
                set.add(referenceData.value.trim().toUpperCase());
            }
        }).findList();


        DataFormat.find.setAutofetch(false).setListener(new QueryListener<DataFormat>() {
            @Override
            public void process(DataFormat dataFormat) {
                Set<String> set = formats.get(dataFormat.code);
                if (set == null) {
                    set = new HashSet<>();
                    formats.put(dataFormat.code, set);
                }
                set.add(dataFormat.format.trim());
            }
        }).findList();

    }

    public void process() {

        for (DataColumn dataColumn : dataSet.getColumns()) {
            processColumn(dataColumn);
        }

    }

    private class ProfilingResultValueListener implements QueryListener<ProfilingResultValue> {
        private final Map<Integer, Integer> regexComplianceCount = new HashMap<>();
        private final Map<Integer, Integer> refDataComplianceCount = new HashMap<>();
        private final Map<Integer, Integer> minMaxComplianceCount = new HashMap<>();


        public Map<Integer, Integer> getRefDataComplianceCount() {
            return refDataComplianceCount;
        }

        public Map<Integer, Integer> getRegexComplianceCount() {
            return regexComplianceCount;
        }

        public Map<Integer, Integer> getMinMaxComplianceCount() {
            return minMaxComplianceCount;
        }

        private Set<String> refValuesByCode(String code) {
            return refValues.get(code);
        }

        @Override
        public void process(ProfilingResultValue profilingResultValue) {
            String val = profilingResultValue.value;
            if (val == null || val.isEmpty()) {
                return;
            }
            String[] tokens = val.split("\\s");
            //go through the config
            for (MappingDiscoveryRule rule : rules) {
                Integer ruleId = rule.id;
                Integer refCountInt = refDataComplianceCount.get(ruleId);
                int refCount = (refCountInt == null) ? 0 : refCountInt.intValue();
                boolean refCountChanged = false;
                Set<String> refValByCode = refValuesByCode(rule.refCode);
                if (refValByCode != null) {
                    for (String token : tokens) {
                        if (refValByCode.contains(token.trim().toUpperCase())) {
                            refCount += profilingResultValue.cardinality;
                            refCountChanged = true;
                        }
                    }
                }
                if (refCountChanged) {
                    refDataComplianceCount.put(ruleId, refCount);
                }
                Pattern regexPattern = regexPatterns.get(ruleId);
                if (regexPattern != null && regexPattern.matcher(val.trim()).find()) {
                    Integer regexCountInt = regexComplianceCount.get(ruleId);
                    int regexCount = (regexCountInt == null) ? 0 : regexCountInt.intValue();
                    regexCount += profilingResultValue.cardinality;
                    regexComplianceCount.put(ruleId, regexCount);
                }

                if (rule.maximumValue != null && rule.minimumValue != null) {
                    if (val.compareTo(rule.minimumValue) >= 0 &&
                            val.compareTo(rule.maximumValue) <= 0) {
                        Integer minMaxCountInt = minMaxComplianceCount.get(ruleId);
                        int minMaxCount = (minMaxCountInt == null) ? 0 : minMaxCountInt.intValue();
                        minMaxCount += profilingResultValue.cardinality;
                        minMaxComplianceCount.put(ruleId, minMaxCount);
                    }
                }
            }
        }
    }

    private class ProfilingResultFormatListener implements QueryListener<ProfilingResultFormat> {
        private final Map<Integer, Integer> formatComplianceCount = new HashMap<>();

        public Map<Integer, Integer> getFormatComplianceCount() {
            return formatComplianceCount;
        }

        @Override
        public void process(ProfilingResultFormat profilingResultFormat) {
            if (profilingResultFormat.format == null) {
                return;
            }
            for (MappingDiscoveryRule rule : rules) {
                Integer formatCountInt = formatComplianceCount.get(rule.id);
                int formatCount = (formatCountInt == null) ? 0 : formatCountInt.intValue();
                if (formats.get(rule.formatsCode) != null && formats.get(rule.formatsCode).contains(profilingResultFormat.format.trim())) {
                    formatCount += profilingResultFormat.cardinality;
                    formatComplianceCount.put(rule.id, formatCount);
                }
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
        ColumnMapping cm = new ColumnMapping();
        cm.setDataColumn(dataColumn);
        cm.setDataSet(dataSet);
        cm.confidenceFlag = false;
        cm.maybeFlag = false;
        cm.manualOverrideFlag = false;

        for (MappingDiscoveryRule rule : rules) {
            double score = 0.0f;

            //column name match
            if (rule.isNameMatching(dataColumn.name)) {
                System.out.println("Adding " + rule.nameScore + " points for column name");
                score += rule.nameScore;
            }

            //reference data match
            Integer refMatchCount = valueListener.getRefDataComplianceCount().get(rule.id);
            int refMatchCountInt = (refMatchCount == null) ? 0 : refMatchCount.intValue();
            double totalPopulated = profilingColRes.percentagePopulated.doubleValue() * 0.01 * profilingColRes.totalCount;
            if (rule.refFullScorePercThresh != null && rule.refFullScore != null && rule.refMinimumPercMatch != null && ((float) refMatchCountInt >= totalPopulated * rule.refMinimumPercMatch.floatValue())) {
                double multiplier;
                if (totalPopulated < 0.1) {
                    multiplier = 0;
                } else if (rule.refFullScorePercThresh.equals(new BigDecimal("0.0"))) {
                    multiplier = 1;
                } else {
                    multiplier = ((float) refMatchCountInt - totalPopulated * rule.refMinimumPercMatch.floatValue()) / (totalPopulated * rule.refFullScorePercThresh.floatValue());
                }
                if (multiplier > 1) {
                    multiplier = 1;
                }
                System.out.println("totalPopulated: " + totalPopulated);
                System.out.println("percentagePopulated: " + profilingColRes.percentagePopulated.doubleValue());
                System.out.println("refMatchCountInt: " + refMatchCountInt);
                System.out.println("refMinimumPercMatch: " + rule.refMinimumPercMatch.floatValue());
                System.out.println("refFullScorePercThresh: " + rule.refFullScorePercThresh.floatValue());

                System.out.println("Adding " + ((double) rule.refFullScore * multiplier) + " points for ref data (multiplier:" + multiplier + ")");

                score += (double) rule.refFullScore * multiplier;

            }

            //unique match
            if (rule.uniqueMaximumPerc != null && rule.uniqueMinimumPerc != null && rule.uniqueScore != null) {
                double uniqPerc = profilingColRes.percentageUnique.doubleValue() / 100.0;
                System.out.println("Uniq percentage: " + uniqPerc + " for " + dataColumn.name);
                if (uniqPerc <= rule.uniqueMaximumPerc.doubleValue() && uniqPerc >= rule.uniqueMinimumPerc.doubleValue()) {
                    System.out.println("Adding " + rule.uniqueScore + " points for uniqueness");
                    score += rule.uniqueScore;
                }
            }

            //null match
            if (rule.nullMaximumPerc != null && rule.nullMinimumPerc != null && rule.nullPenalty != null) {
                double nullPerc = 1.0 - profilingColRes.percentagePopulated.doubleValue() / 100.0;
                System.out.println("Null percentage: " + nullPerc + " for " + dataColumn.name);
                if (nullPerc > rule.nullMaximumPerc.doubleValue() || nullPerc < rule.nullMinimumPerc.doubleValue()) {
                    System.out.println("Adding " + rule.nullPenalty + " points for null penalty");
                    score += rule.nullPenalty;
                }
            }

            //min - max ??? - what about data parsing / data type stuff for dates ????
            if (rule.maximumValue != null && rule.minimumValue != null) {
                Integer minMaxCount = valueListener.getMinMaxComplianceCount().get(rule.id);
                System.out.println("MinMaxCount: " + minMaxCount);
                if (minMaxCount == null) {
                    System.out.println("Adding " + rule.valPenalty + " points for value penalty minmaxcnt = null");

                    score += rule.valPenalty;

                } else if (minMaxCount >= profilingColRes.percentagePopulated.doubleValue() * 0.01 * profilingColRes.totalCount * rule.allowedExcPerc.doubleValue()) {
                    System.out.println("Adding " + rule.valScore + " points for value");
                    score += rule.valScore;
                } else {
                    System.out.println("Adding " + rule.valPenalty + " points for value penalty");

                    score += rule.valPenalty;
                }
            }

            //regex match
            if (rule.regexPattern != null && rule.regexMetScore != null && rule.regexMinimumPercThresh != null) {
                Integer regexCount = valueListener.getRegexComplianceCount().get(rule.id);
                if (regexCount != null && regexCount >= profilingColRes.percentagePopulated.doubleValue() * 0.01 * profilingColRes.totalCount * rule.regexMinimumPercThresh.doubleValue()) {
                    System.out.println("Adding " + rule.regexMetScore + " points for regex");

                    score += rule.regexMetScore;
                }
            }

            //formats
            if (rule.formatsCode != null && rule.formatsMetScore != null && rule.formatsMinimumPercThresh != null) {
                Integer formatCount = formatListener.getFormatComplianceCount().get(rule.id);
                if (formatCount != null && formatCount >= profilingColRes.percentagePopulated.doubleValue() * 0.01 * profilingColRes.totalCount * rule.formatsMinimumPercThresh.doubleValue()) {
                    System.out.println("Adding " + rule.formatsMetScore + " points for format");
                    score += rule.formatsMetScore;
                }
            }

//            results.put(rule.id, score);
            if (rule.confPointsThresh.doubleValue() <= score) {
                cm.coreAttributeName = rule.coreColumn;
                cm.coreAttributeType = rule.coreType;
                cm.coreTableName = rule.coreTable;
                cm.score = (int) score;
                cm.confidenceFlag = true;
                System.out.println("Confidence match found for " + dataColumn.name + "rule: " + rule.coreTable + ":" + rule.coreColumn + "\tthreshold: " + rule.confPointsThresh + "\tscore:" + score);
                break; // found confidence match
            } else if (rule.maybePointsThresh.doubleValue() <= score) {
                cm.coreAttributeName = rule.coreColumn;
                cm.coreAttributeType = rule.coreType;
                cm.coreTableName = rule.coreTable;
                cm.score = (int) score;
                cm.maybeFlag = true;
                cm.confidenceFlag = false;
                System.out.println("Maybe match found for " + dataColumn.name + "rule: " + rule.coreTable + ":" + rule.coreColumn + "\tthreshold: " + rule.maybePointsThresh + "\tscore:" + score);
            } else {
                System.out.println("No match found for " + dataColumn.name + "rule: " + rule.coreTable + ":" + rule.coreColumn + "\tthreshold: " + rule.maybePointsThresh + "\tscore:" + score);
            }
        }
        cm.save(mtd.server().getName());
    }
}
