package controllers.clustrino;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import models.clustrino.CsvFile;
import play.api.mvc.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import au.com.bytecode.opencsv.CSVReader;

import java.util.*;
import java.io.*;
import java.security.MessageDigest;


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
        session().remove("csv_filename");
        return ok(views.html.clustrino.upload.render());
    }

    private static final AWSCredentials AWS_CREDS = new AWSCredentials() {
        @Override
        public String getAWSAccessKeyId() {
            return "AKIAI2EWM5MYECY7GY3Q";
        }

        @Override
        public String getAWSSecretKey() {
            return "8U4Lbqw+/hnUIsiHABfF/doE1JrqrPrjJRQjHyug";
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

        public List<Map<String,String>> getLines() {
            CSVReader r = null;
            try {
                r = new CSVReader(getReader());
                List<Map<String,String>> retVal = new ArrayList<Map<String,String>>();
                while (true) {
                    String[] line = r.readNext();
                    if (line == null) {
                        break;
                    }
                    Map<String, String> map = new HashMap<String, String>();
                    retVal.add(map);
                    int i=0;
                    for (String el:line) {
                        i++;
                        String key = "column" + i;

                        map.put(key, el);
                    }
                }
                return retVal;

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
            s3Client().putObject("clustrino_csv_files", getFileName(), getFile());
            CsvFile fileModel = new CsvFile();
            fileModel.fileName = getFileName();
            fileModel.ownerId = 1L;
            fileModel.save();
            getFile().delete();
            return fileModel;
        }
    }

    public static Result uploadFile() {
        ObjectNode result = Json.newObject();

        UploadedFile uf = new UploadedFile(request().body().asMultipartFormData().getFiles().iterator().next());
        List<Map<String,String>> lines = uf.getLines();
        uf.persist();
        result.put("status", "OK, Super");
        result.put("message", "Hello " + lines.size()+" x "+lines.iterator().next().size());
        session().put("csv_filename", uf.getFileName());
        return ok(result);

    }

    public static Result showFile() {
        S3Object obj = s3Client().getObject("clustrino_csv_files", session().get("csv_filename"));
        List<Map<String,String>> data = null;
        CSVReader r = null;
        try {
            r = new CSVReader(new InputStreamReader(obj.getObjectContent()));
            data = new ArrayList<Map<String,String>>();
            while (true) {
                String[] line = r.readNext();
                if (line == null) {
                    break;
                }
                Map<String, String> map = new HashMap<String, String>();
                data.add(map);
                int i=0;
                for (String el:line) {
                    i++;
                    String key = "column" + i;

                    map.put(key, el);
                }
            }

        } catch (IOException e) {
        } finally {
            try {
                r.close();
            } catch (IOException e) {
            }
        }
        JsonNode json = Json.toJson(data);

        return ok(views.html.clustrino.show_file.render(data, Json.stringify(json)));

    }


}
