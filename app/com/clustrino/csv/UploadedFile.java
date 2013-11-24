package com.clustrino.csv;

import models.clustrino.CsvFile;

import java.io.File;

public class UploadedFile {
    private final File file;
    private final CsvFile model;

    public static UploadedFile fromUpload(CsvFile model, File file) {
        return new UploadedFile(model, file);
    }

    private UploadedFile(CsvFile model, File file) {
        this.file = file;
        this.model = model;
    }

    public UploadedFile(CsvFile model) {
        this(model, null);
    }

    public String getFileName() {
        return model.getSavedFileName();
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



