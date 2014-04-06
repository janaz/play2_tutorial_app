package controllers.neutrino.secured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neutrino.models.configuration.User;
import com.neutrino.models.core_common.ReferenceData;
import com.neutrino.models.metadata.ColumnMapping;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.MetadataSchema;
import controllers.Application;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

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
        final ObjectNode result = Json.newObject();
        JsonNode reqNode = request().body().asJson();

        Map<String, Map<String, String>> data = Json.fromJson(reqNode, Map.class);

        /*validation
        [ ] there has to be exactly one sourceID column selected
        [ ] unique values per dataset
        */
        for (String mappingId : data.keySet()) {
            ColumnMapping mapping = ColumnMapping.find(metadataServerName()).byId(Integer.parseInt(mappingId));
            Map<String, String> jsonMapping = data.get(mappingId);
            mapping.updateFromJson(jsonMapping, metadataServerName());
            mapping.getDataSet().setState(DataSet.State.MANUAL_MAPPING_DONE);
            mapping.getDataSet().save(metadataServerName());
        }
        result.put("data_count", data.size());
        return ok(result);
    }


}
