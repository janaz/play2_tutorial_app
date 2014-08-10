package controllers.neutrino.secured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neutrino.data_loader.CoreSchema;
import com.neutrino.datamappingdiscovery.CollectionUtils;
import com.neutrino.models.configuration.MappingDiscoveryRule;
import com.neutrino.models.configuration.User;
import com.neutrino.models.core_common.ReferenceData;
import com.neutrino.models.metadata.ColumnMapping;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.MetadataSchema;
import controllers.Application;
import jobs.DataLoaderJob;
import play.libs.Json;
import play.mvc.Result;

import java.util.*;

public class ManualMapping extends Secured {

    private static User currentUser() {
        return Application.getLocalUser(session());
    }

    private static  MetadataSchema metadataSchema() {
        return new MetadataSchema(currentUser().id);
    }

    private static List<DataSet> automappedFiles() {
        final MetadataSchema met = metadataSchema();
        return DataSet.find(met.server().getName()).where().eq("state", DataSet.State.AUTO_MAPPING_DONE).findList();
    }

    public static Result index() {
        ReferenceData ref =  ReferenceData.forCore(currentUser().id);
        JsonNode js = Json.toJson(ref.toJSON());

        return ok(views.html.neutrino.mapping.render(automappedFiles(), ref.getOptions(), Json.stringify(js)));
    }

    private static String metadataServerName() {
        return new MetadataSchema(currentUser().id).server().getName();
    }

    public static Result update() {
        ReferenceData ref =  ReferenceData.forCore(currentUser().id);

        final ObjectNode result = Json.newObject();
        JsonNode reqNode = request().body().asJson();

        Map<String, Map<String, String>> data = Json.fromJson(reqNode, Map.class);

        Map<Integer, Collection<ColumnMapping>> map = new HashMap<>();

        /*validation
        [ ] there has to be exactly one sourceID column selected
        [ ] unique values per dataset
        */
        for (String mappingId : data.keySet()) {
            ColumnMapping mapping = ColumnMapping.find(metadataServerName()).byId(Integer.parseInt(mappingId));
            Map<String, String> jsonMapping = data.get(mappingId);
            mapping.updateFromJson(jsonMapping);
            Collection<ColumnMapping> c = map.get(mapping.getDataSet().getId());
            if (c == null) {
                c = new ArrayList<>();
                map.put(mapping.getDataSet().getId(), c);
            }
            c.add(mapping);
        }
        //perform validation
        Collection<MappingDiscoveryRule> mandatoryRules = new ArrayList<>();
        for (MappingDiscoveryRule rule : MappingDiscoveryRule.find.all()) {
            if (rule.mandatoryFlag != null && rule.mandatoryFlag.booleanValue()) {
                mandatoryRules.add(rule);
            }
        }

        for (Integer dataSetId : map.keySet()) {
            DataSet ds = DataSet.find(metadataServerName()).byId(dataSetId);
            Set<String> uniqueMappings = new HashSet<>();
            Map<Integer, Boolean> mandatoryRuleMatched = new HashMap<>();
            for (MappingDiscoveryRule rule : mandatoryRules) {
                mandatoryRuleMatched.put(rule.id, false);
            }
            for (ColumnMapping mapping : map.get(dataSetId)) {
                if (mapping.getCoreTableName() == null &&
                        mapping.getCoreAttributeName() == null) {
                    continue;
                }
                String setKey = new StringBuilder()
                        .append(mapping.getCoreTableName()).append("/")
                        .append(mapping.getCoreAttributeName()).append("/")
                        .append(mapping.getCoreAttributeType()).append("/")
                        .toString();
                boolean forceUnique = ref.isUniqueMapping(mapping.getCoreTableName(), mapping.getCoreAttributeName());
                if (uniqueMappings.contains(setKey) && forceUnique) {
                    String msg = "Mapping not unique for " +ds.getFile().getOriginalFileName()+ ": " +setKey;
                    result.put("error", msg);
                    return badRequest(result);
                } else {
                    uniqueMappings.add(setKey);
                }
                for (MappingDiscoveryRule rule : mandatoryRules) {
                    if (rule.coreTable.equals(mapping.getCoreTableName())) {
                        System.out.println("Table matched for " + rule.coreTable);
                        System.out.println("Column name is " + rule.coreColumn);
                        System.out.println("Mapping column name is -" + mapping.getCoreAttributeName()+"-");
                        if (rule.coreColumn == null) {
                            if (rule.coreType!= null && rule.coreType.equals(mapping.getCoreAttributeType())) {
                                mandatoryRuleMatched.put(rule.id, true);
                            }
                        }else if (rule.coreColumn != null && rule.coreColumn.equals(mapping.getCoreAttributeName())) {
                            System.out.println("Column matched for " + rule.coreColumn);
                            if (rule.coreType == null || rule.coreType.equals("CHOOSE") || rule.coreType.equals(mapping.getCoreAttributeType())) {
                                mandatoryRuleMatched.put(rule.id, true);
                            }
                        }
                    }
                }
            }
            for (MappingDiscoveryRule rule : mandatoryRules) {
                if (!mandatoryRuleMatched.get(rule.id)) {
                    String msg = "Missing mandatory mapping for: " + rule.coreTable+"/"+rule.coreColumn+"/"+rule.coreType;
                    result.put("error", msg);
                    return badRequest(result);
                }
            }

        }

        //save
        for (Integer dataSetId : map.keySet()) {
            DataSet ds = DataSet.find(metadataServerName()).byId(dataSetId);
            ds.setState(DataSet.State.MANUAL_MAPPING_DONE);
            for (ColumnMapping mapping : map.get(dataSetId)) {
                mapping.save(metadataServerName());
            }
            ds.save(metadataServerName());
        }

        result.put("data_count", data.size());
        DataLoaderJob.runDataLoader(currentUser().id);
        return ok(result);
    }

}
