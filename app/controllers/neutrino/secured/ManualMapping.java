package controllers.neutrino.secured;

import com.fasterxml.jackson.databind.JsonNode;
import com.neutrino.models.configuration.User;
import com.neutrino.models.core_common.ReferenceData;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.MetadataSchema;
import controllers.Application;
import play.libs.Json;
import play.mvc.Result;

import java.util.List;

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


}