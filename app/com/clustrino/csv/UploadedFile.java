package com.clustrino.csv;

import com.clustrino.profiling.metadata.DataSet;
import models.clustrino.CsvFile;

import java.io.File;

public class UploadedFile {
    private final File file;
    private final DataSet model;

    public static UploadedFile fromUpload(DataSet model, File file) {
        return new UploadedFile(model, file);
    }

    private UploadedFile(DataSet model, File file) {
        this.file = file;
        this.model = model;
    }

    public UploadedFile(DataSet model) {
        this(model, null);
    }

    public String getFileName() {
        return model.getFile().fileLocation;
    }

    public File getFile() {
        return file;
    }

    public UploadedFilePersistService persistService() {
        return UploadedFilePersistService.getService();
    }

    public void persist() throws PersistException {
        persistService().persist(this);
    }
}



