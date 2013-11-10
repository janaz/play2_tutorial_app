package com.clustrino.csv;

import au.com.bytecode.opencsv.CSVReader;
import models.clustrino.CsvFile;
import org.springframework.util.FileCopyUtils;
import play.mvc.Http;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 10/11/13
 * Time: 9:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadedFile {
    private final File file;
    private final String name;

    public UploadedFile(File file, String name) {
        this.file = file;
        this.name = name;
    }

    public String getFileName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public void persist() throws PersistException {
        UploadedFilePersistService persist = UploadedFilePersistService.getService();
        persist.persist(this);
    }
}



