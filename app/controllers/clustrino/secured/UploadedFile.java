package controllers.clustrino.secured;

import com.clustrino.csv.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import controllers.Application;
import jobs.CSVFileParser;
import models.User;
import models.clustrino.CsvFile;
import models.clustrino.CsvMetadata;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.annotation.Nullable;
import java.util.*;

public class UploadedFile extends Secured {
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
            CSVFileParser.parseFile(fileModel);
            result.put("status", "OK, Super");
            result.put("id", fileModel.id);
            result.put("message", "File has been uploaded successfully.");
            result.put("view_url", controllers.clustrino.secured.routes.UploadedFile.showFile(fileModel.id).url());
            return ok(result);
        } catch (PersistException e) {
            result.put("status", "Failure: " + e.getMessage());
            result.put("message", "An error occurred while uploading the file.");
            return internalServerError(result);
        }
    }

    private static CsvFile getLoggedinUserFile(final Long id) {
        final User localUser = Application.getLocalUser(session());
        Collection<CsvFile> filtered = Collections2.filter(localUser.files, new Predicate<CsvFile>() {
            @Override
            public boolean apply(@Nullable CsvFile csvFile) {
                return csvFile.id == id;
            }
        });
        return filtered.iterator().next();
    }

    public static Result updateColumns(final Long id) {
        final ObjectNode result = Json.newObject();
        JsonNode reqNode = request().body().asJson();

        List<String> data = Json.fromJson(reqNode, List.class);

        final CsvFile fileModel = getLoggedinUserFile(id);
        try {
            if (fileModel.getMetadata() == null) {
                fileModel.setMetadata(new CsvMetadata());
            }
            fileModel.getMetadata().setColumnNames(Joiner.on(',').join(data));
            fileModel.getMetadata().save();
            fileModel.save();
            result.put("status", "OK, Super");
            CSVFileParser.parseFile(fileModel);
            return ok(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
//            result.put("status", "Error "+e.getMessage());
//            return internalServerError(result);
        }
    }

    public static Result showFile(final Long id) {
        final CsvFile fileModel = getLoggedinUserFile(id);
        CSVFile csvFile = new CSVFile(fileModel);

        SampleReader sampleReader = new SampleReader(1000);
        StatisticsGatherer stats = new StatisticsGatherer(1000);
        csvFile.addReadListener(sampleReader);
        csvFile.addReadListener(stats);

        JsonNode headers = Json.toJson(Collections.emptyList());
        JsonNode sample = Json.toJson(Collections.emptyList());
        JsonNode population = Json.toJson(Collections.emptyList());
        try{
            csvFile.readFile();

            headers = Json.toJson(sampleReader.getStringHeader());
            sample = Json.toJson(sampleReader.getSampleLines());
            population = Json.toJson(stats.getPercentagePopulated());

        } catch (Exception e) {

           throw new RuntimeException(e);
        }

        return ok(views.html.clustrino.show_file.render(fileModel.fileName,
                Json.stringify(headers),
                Json.stringify(sample),
                Json.stringify(population),
                Json.stringify(Json.toJson(DataCategory.names())),
                fileModel
        ));

    }


}
