package controllers.clustrino.secured;

import com.clustrino.csv.CSVFile;
import com.clustrino.csv.PersistException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import controllers.Application;
import models.User;
import models.clustrino.CsvFile;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class UploadedFile extends Secured {
    public static Result upload() {
        return ok(views.html.clustrino.upload.render());
    }

    public static Result myFiles() {
        final User localUser = Application.getLocalUser(session());
        return ok(views.html.clustrino.my_files.render(localUser.files));
    }


    public static Result uploadFile() {
        final ObjectNode result = Json.newObject();
        final User localUser = Application.getLocalUser(session());
        final Http.MultipartFormData.FilePart part = request().body().asMultipartFormData().getFiles().iterator().next();

        try {
            CsvFile fileModel = new CsvFile();
            fileModel.fileName = part.getFilename();
            fileModel.uploadedAt = Calendar.getInstance().getTimeInMillis();
            fileModel.user = localUser;
            final com.clustrino.csv.UploadedFile uploadedFile = com.clustrino.csv.UploadedFile.fromUpload(fileModel, part.getFile());
            uploadedFile.persist();
            uploadedFile.getFile().delete();
            fileModel.save();
            result.put("status", "OK, Super");
            result.put("message", "File has been uploaded successfully.");
            result.put("view_url", controllers.clustrino.secured.routes.UploadedFile.showFile(fileModel.id).url());
            return ok(result);
        } catch (PersistException e) {
            result.put("status", "Failure: " + e.getMessage());
            result.put("message", "An error occurred while uploading the file.");
            return internalServerError(result);
        }
    }


    public static Result showFile(final Long id) {
        final User localUser = Application.getLocalUser(session());
        Collection<CsvFile> filtered = Collections2.filter(localUser.files, new Predicate<CsvFile>() {
            @Override
            public boolean apply(@Nullable CsvFile csvFile) {
                return csvFile.id == id;
            }
        });
        CsvFile fileModel = filtered.iterator().next();
        CSVFile csvFile = new CSVFile(fileModel);
        JsonNode headers = Json.toJson(Collections.emptyList());
        JsonNode sample = Json.toJson(Collections.emptyList());
        try{
            headers = Json.toJson(csvFile.headerNames());
            sample = Json.toJson(csvFile.dataSample());
        } catch (Exception e) {

        }

        return ok(views.html.clustrino.show_file.render(fileModel.fileName, Json.stringify(headers), Json.stringify(sample)));

    }


}
