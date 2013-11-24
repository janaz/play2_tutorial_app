package controllers.clustrino;

import com.clustrino.profiling.MetadataSchema;
import org.pojava.datetime.DateTime;
import org.pojava.datetime.DateTimeConfig;
import play.mvc.Controller;
import play.mvc.Result;

public class BasicFlow extends Controller {

    public static Result index() {
        MetadataSchema metadata = new MetadataSchema(15L);
//        if (!metadata.isCreated()) {
//            metadata.createDatabase();
//            metadata.createTables();
//        }
        DateTimeConfig.globalEuropeanDateFormat();
        DateTime dt = DateTime.parse("2013-02-20 11:12");
        System.out.println(dt.toString());
        System.out.println(dt.toMillis());
        dt.toMillis();
        return ok(views.html.clustrino.index.render());
    }

    public static Result testAction() {
        return ok(views.html.clustrino.test_action.render());
    }


}
