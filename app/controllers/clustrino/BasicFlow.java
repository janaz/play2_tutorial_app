package controllers.clustrino;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class BasicFlow extends Controller {



    public static Result login() {
        return ok(views.html.clustrino.login.render());
    }

    public static Result loginPost() {
        return redirect(controllers.clustrino.routes.BasicFlow.index());
    }

    public static Result index() {
        return ok(views.html.clustrino.index.render());
    }

    public static Result upload() {
        return ok(views.html.clustrino.upload.render());
    }

    public static Result uploadFile() {
        ObjectNode result = Json.newObject();

        AWSCredentials creds = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return "AKIAI2EWM5MYECY7GY3Q";
            }

            @Override
            public String getAWSSecretKey() {
                return "8U4Lbqw+/hnUIsiHABfF/doE1JrqrPrjJRQjHyug";}
        };
        AmazonS3Client client = new AmazonS3Client(creds);

        Region myReg = Region.getRegion(Regions.US_EAST_1);
        client.setRegion(myReg);
        File f = request().body().asMultipartFormData().getFiles().iterator().next().getFile();
        CSVReader r = null;
        try {
            r = new CSVReader(new FileReader(f));
            client.putObject("clustrino_csv_files", f.getName(), f);
            List<String[]> data = r.readAll();
            result.put("status", "OK, Super");
            result.put("message", "Hello " + data.size()+" x "+data.iterator().next().length);
            return ok(result);
        } catch (FileNotFoundException e) {
            result.put("status", "Fail");
            result.put("message", "File not found");
            return badRequest(result);
        } catch (IOException e) {
            result.put("status", "Fail");
            result.put("message", "Invalid CSV");
            return badRequest(result);
        } finally {
            try {
                r.close();
            } catch (IOException e) {
            }
        }

    }





}
