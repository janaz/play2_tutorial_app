package controllers.clustrino;

import au.com.bytecode.opencsv.CSVReader;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.clustrino.csv.CSVFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Application;
import controllers.Secured;
import models.clustrino.CsvFile;
import org.springframework.util.FileCopyUtils;
import play.api.templates.Html;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Security.Authenticated(Secured.class)
public class BasicFlow extends Controller {

    public static Result index() {
        return ok(views.html.clustrino.index.render());
    }

    public static Result testAction() {
        return ok(views.html.clustrino.test_action.render());
    }


}
