package controllers.clustrino.secured;

import com.clustrino.csv.CSVFile;
import com.clustrino.csv.PersistException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Application;
import models.User;
import models.clustrino.CsvFile;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Calendar;
import java.util.Collections;

public class UploadedFile extends Secured {
    public static Result upload() {
        session().remove("csv_filename");
        return ok(views.html.clustrino.upload.render());
    }

    public static Result uploadFile() {
        final ObjectNode result = Json.newObject();
        final User localUser = Application.getLocalUser(session());
        final Http.MultipartFormData.FilePart part = request().body().asMultipartFormData().getFiles().iterator().next();
        final com.clustrino.csv.UploadedFile uploadedFile = new com.clustrino.csv.UploadedFile(part.getFile(), part.getFilename());

        try {
            uploadedFile.persist();
            uploadedFile.getFile().delete();
            CsvFile fileModel = new CsvFile();
            fileModel.fileName = uploadedFile.getFileName();
            fileModel.uploadedAt = Calendar.getInstance().getTimeInMillis();
            fileModel.ownerId = localUser.id;
            fileModel.save();
            result.put("status", "OK, Super");
            result.put("message", "File has been uploaded successfully.");
            session().put("csv_filename", uploadedFile.getFileName());
            return ok(result);
        } catch (PersistException e) {
            result.put("status", "Failure: " + e.getMessage());
            result.put("message", "An error occurred while uploading the file.");
            session().remove("csv_filename");
            return internalServerError(result);
        }
    }


    public static Result showFile() {
        CSVFile csvFile = new CSVFile(session().get("csv_filename"));
        JsonNode headers = Json.toJson(Collections.emptyList());
        JsonNode sample = Json.toJson(Collections.emptyList());
        try{
            headers = Json.toJson(csvFile.headerNames());
            sample = Json.toJson(csvFile.dataSample());
        } catch (Exception e) {

        }

        return ok(views.html.clustrino.show_file.render(Json.stringify(headers), Json.stringify(sample)));

    }


}
