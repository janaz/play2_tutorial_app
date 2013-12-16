package controllers.neutrino;

import com.neutrino.datamappingdiscovery.DataMapping;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.MetadataSchema;
import play.mvc.Controller;
import play.mvc.Result;

public class BasicFlow extends Controller {

    public static Result index() {
        MetadataSchema mtd = new MetadataSchema(8);
        DataSet ds = DataSet.find(mtd.server().getName()).where().eq("id", 10).findUnique();
        DataMapping dm = new DataMapping(ds);
        dm.process();
        return ok(views.html.neutrino.index.render());
    }

    public static Result testAction() {
        return ok(views.html.neutrino.test_action.render());
    }


}
