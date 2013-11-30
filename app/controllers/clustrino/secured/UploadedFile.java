package controllers.clustrino.secured;

import com.clustrino.csv.*;
import com.clustrino.profiling.MetadataSchema;
import com.clustrino.profiling.metadata.DataColumn;
import com.clustrino.profiling.metadata.DataSet;
import com.clustrino.profiling.metadata.File;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Application;
import jobs.CSVFileParser;
import models.configuration.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UploadedFile extends Secured {
    public static Result myFiles() {
        final User localUser = Application.getLocalUser(session());
        final MetadataSchema met = new MetadataSchema(localUser.id);
        List<DataSet> list =  DataSet.find(met.server().getName()).where().eq("type", DataSet.Type.FILE).findList();
        return ok(views.html.clustrino.my_files.render(list));
    }


    public static Result uploadFile() {
        final ObjectNode result = Json.newObject();
        final User localUser = Application.getLocalUser(session());
        final Http.MultipartFormData.FilePart part = request().body().asMultipartFormData().getFiles().iterator().next();

        try {
            MetadataSchema met = new MetadataSchema(localUser.id);
            DataSet model = new DataSet();
            model.userId = localUser.id;
            model.type = DataSet.Type.FILE;
            model.creationTimestamp = new Date();
            model.modificationTimestamp = new Date();

            File fileModel = new File();
            fileModel.setDataSet(model);
            fileModel.originalFileName = part.getFilename();
            fileModel.creationTimestamp = model.creationTimestamp;
            fileModel.fileFormat = "csv";
            fileModel.setSavedFileName();
            model.setFile(fileModel);

            final com.clustrino.csv.UploadedFile uploadedFile = com.clustrino.csv.UploadedFile.fromUpload(model, part.getFile());
            uploadedFile.persist();
            uploadedFile.getFile().delete();

            CSVFile csvFile = new CSVFile(model);
            fileModel.delimiter = String.valueOf(csvFile.getSeparator());
            fileModel.quote = "\"";
            fileModel.headerFlag = csvFile.dataIncludesHeader();

            model.getColumns().clear();
            for (DataCategory cat : csvFile.categoriesInFirstLine()) {
                DataColumn col = new DataColumn();
                col.creationTimestamp = new Date();
                col.modificationTimestamp = new Date();
                col.dataType = cat;
                col.length = "256";
                col.name = cat.name();
                model.getColumns().add(col);
                col.setDataSet(model);
                col.save(met.server().getName());
            }
            //fileModel.save(met.server().getName());
            model.save(met.server().getName());

            CSVFileParser.parseFile(model);
            result.put("status", "OK, Super");
            result.put("id", fileModel.id);
            result.put("message", "File has been uploaded successfully.");
            result.put("view_url", controllers.clustrino.secured.routes.UploadedFile.showFile(model.id).url());
            return ok(result);
        } catch (PersistException e) {
            result.put("status", "Failure: " + e.getMessage());
            result.put("message", "An error occurred while uploading the file.");
            return internalServerError(result);
        } catch (IOException e) {
            result.put("status", "Failure: " + e.getMessage());
            result.put("message", "An error occurred while uploading the file.");
            return internalServerError(result);
        }
    }

    private static DataSet getLoggedinUserDataSet(final Integer id) {
        final User localUser = Application.getLocalUser(session());
        final MetadataSchema met = new MetadataSchema(localUser.id);
        return DataSet.find(met.server().getName()).where().eq("type", DataSet.Type.FILE).eq("userId",localUser.id).eq("id", id).findUnique();
    }

    public static Result updateColumns(final Integer id) {
        final ObjectNode result = Json.newObject();
        JsonNode reqNode = request().body().asJson();

        List<String> data = Json.fromJson(reqNode, List.class);

        final DataSet model = getLoggedinUserDataSet(id);

        try {
            MetadataSchema met = new MetadataSchema(model.userId);
            model.getColumns().clear();
            for (String colName : data) {
                DataCategory cat = DataCategory.valueOf(colName);
                DataColumn col = new DataColumn();
                col.creationTimestamp = new Date();
                col.modificationTimestamp = new Date();
                col.dataType = cat;
                col.length = "256";
                col.name = cat.name();
                model.getColumns().add(col);
                col.setDataSet(model);
                //col.save(met.server().getName());
            }
            model.save(met.server().getName());

            result.put("status", "OK, Super");
            CSVFileParser.parseFile(model);
            return ok(result);
        } catch (Exception e) {
            result.put("status", "Error");
            result.put("message", e.getMessage());
            return badRequest(result);
        }
    }

    public static Result showFile(final Integer id) {
        final DataSet model = getLoggedinUserDataSet(id);
        CSVFile csvFile = new CSVFile(model);

        SampleReader sampleReader = new SampleReader(1000);
        StatisticsGatherer stats = new StatisticsGatherer(1000);
        csvFile.addReadListener(sampleReader);
        csvFile.addReadListener(stats);

        JsonNode headers = Json.toJson(Collections.emptyList());
        JsonNode sample = Json.toJson(Collections.emptyList());
        JsonNode population = Json.toJson(Collections.emptyList());
        try {
            csvFile.readFile();

            headers = Json.toJson(sampleReader.getStringHeader());
            sample = Json.toJson(sampleReader.getSampleLines());
            population = Json.toJson(stats.getPercentagePopulated());

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return ok(views.html.clustrino.show_file.render(model.getFile().originalFileName,
                Json.stringify(headers),
                Json.stringify(sample),
                Json.stringify(population),
                Json.stringify(Json.toJson(DataCategory.names())),
                model
        ));

    }


}
