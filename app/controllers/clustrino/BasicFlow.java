package controllers.clustrino;

import au.com.bytecode.opencsv.CSVReader;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.clustrino.csv.CSVFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.clustrino.CsvFile;
import org.springframework.util.FileCopyUtils;
import play.api.templates.Html;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BasicFlow extends Controller {


    public static Result login() {
        return ok(views.html.clustrino.login.render());
    }

    public static Result loginPost() {
        return redirect(controllers.clustrino.routes.BasicFlow.dashboard());
    }

    public static Result dashboard() {
        return ok(views.html.clustrino.dashboard.render());
    }



    public static Result index() {
        return ok(views.html.clustrino.index.render());
    }

    private static Result pjaxWrapper(Html c) {
        if(request().getHeader("X-PJAX") != null) {
            return ok(c);
        } else {
            return ok(views.html.clustrino.admin.apply(c));
        }

    }

    public static Result upload() {
        session().remove("csv_filename");
        return pjaxWrapper(views.html.clustrino.upload.render());
    }

    private static final AWSCredentials AWS_CREDS = new AWSCredentials() {
        @Override
        public String getAWSAccessKeyId() {
            return play.Play.application().configuration().getConfig("aws").getString("access_key");
        }

        @Override
        public String getAWSSecretKey() {
            return play.Play.application().configuration().getConfig("aws").getString("secret_key");
        }
    };

    private static AmazonS3Client s3Client() {
        AmazonS3Client client = new AmazonS3Client(AWS_CREDS);
        client.setRegion(Region.getRegion(Regions.US_EAST_1));
        return client;
    }

    public static class UploadedFile {
        private FilePart part;

        public UploadedFile(FilePart part) {
            this.part = part;
        }

        public String getFileName() {
            return part.getFilename();
        }

        public File getFile() {
            return part.getFile();
        }

        private Reader getReader() throws IOException {
            return new FileReader(getFile());
        }

        public List<String[]> getLines() {
            CSVReader r = null;
            try {
                r = new CSVReader(getReader());
                return r.readAll();
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    r.close();
                } catch (IOException e) {
                }
            }
        }

        public CsvFile persist() {
            //move to background s3Client().putObject("clustrino_csv_files", getFileName(), getFile());
            try {
                String root = "/tmp/uploaded_files";
                FileCopyUtils.copy(getFile(), new File(root+"/"+getFileName()));
                getFile().delete();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            CsvFile fileModel = new CsvFile();
            fileModel.fileName = getFileName();
            fileModel.ownerId = 1L; //sessionLoggedUserId
            fileModel.save();
            getFile().delete();
            return fileModel;
        }
    }

    public static Result uploadFile() {
        ObjectNode result = Json.newObject();

        UploadedFile uf = new UploadedFile(request().body().asMultipartFormData().getFiles().iterator().next());
        uf.persist();
        result.put("status", "OK, Super");
        result.put("message", "File has been uploaded successfully. ");
        session().put("csv_filename", uf.getFileName());
        return ok(result);

    }

    public static Result testAction() {
        return pjaxWrapper(views.html.clustrino.test_action.render());
    }

    public static Result showFile() {
//        S3Object obj = s3Client().getObject("clustrino_csv_files", session().get("csv_filename"));
//        InputStream obj_stream = obj.getObjectContent();
        String root="/tmp/uploaded_files";
        String fileName = root+"/"+session().get("csv_filename");
        CSVFile csvFile = new CSVFile(fileName);
        JsonNode headers = Json.toJson(Collections.emptyList());
        JsonNode sample = Json.toJson(Collections.emptyList());
        try{
            headers = Json.toJson(csvFile.headerNames());
            sample = Json.toJson(csvFile.dataSample());
        } catch (Exception e) {

        }

        return pjaxWrapper(views.html.clustrino.show_file.render(Json.stringify(headers), Json.stringify(sample)));

    }


}
