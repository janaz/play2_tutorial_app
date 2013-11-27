package com.clustrino.csv;

import com.amazonaws.services.s3.model.S3Object;
import com.clustrino.AppConfiguration;
import com.clustrino.aws.S3Client;
import com.clustrino.profiling.MetadataSchema;
import com.clustrino.profiling.StagingSchema;
import com.clustrino.profiling.metadata.Column;
import models.clustrino.CsvFile;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Calendar;


public abstract class UploadedFilePersistService {
    public void persist(UploadedFile uploadedFile) throws PersistException {
        _persist(uploadedFile);
    }

    public void importToDB(CsvFile fileModel) throws IOException {
        CSVFile csvFile = new CSVFile(fileModel);
        DBSaver saver = new DBSaver(fileModel.user.id, fileModel.id);
        csvFile.addReadListener(saver);
        MetadataSchema met = new  MetadataSchema(fileModel.user.id);
        StagingSchema stg = new  StagingSchema(fileModel.user.id, fileModel.id);
        if (!stg.isCreated()) {
            stg.createDatabase();
        }

        if (!met.isCreated()) {
            met.createDatabase();
            met.createTables();
        }
        met.medadataServer();
        for (DataCategory cat : csvFile.categories()) {
            Column col = new Column();
            col.createdAt = Calendar.getInstance().getTimeInMillis();
            col.updatedAt = Calendar.getInstance().getTimeInMillis();
            col.dataType = cat.dbType();
            col.length = 256;
            col.name = cat.name();
            col.save(met.serverName());
        }
        csvFile.readFile();
    }

    public abstract void _persist(UploadedFile uploadedFile) throws PersistException;

    public static UploadedFilePersistService getService() {
        final String store = AppConfiguration.get().getString("uploaded_files_store");
        if (store.equals("filesystem")) {
            return new FileSystemPersistService(AppConfiguration.get().getString("uploaded_files_root"));
        } else if (store.equals("s3")) {
            return new S3PersistService(AppConfiguration.get().getString("uploaded_files_s3_bucket"));
        }else {
            throw new UnsupportedOperationException("uploaded_files store is not configured properly");
        }
    }

    public abstract Reader getReader(CsvFile model) throws IOException;

    private static class FileSystemPersistService extends UploadedFilePersistService {
        private final String root;
        public FileSystemPersistService(String root) {
            this.root = root;
        }

        private String fullPath(String fileName) {
            return root + File.separator + fileName;
        }

        @Override
        public void _persist(UploadedFile uploadedFile) throws PersistException {
            String destination = fullPath(uploadedFile.getFileName()) ;

            try {
                FileCopyUtils.copy(uploadedFile.getFile(), new  File(destination));
            } catch (IOException e) {
                throw new PersistException(e);
            }
        }

        @Override
        public Reader getReader(CsvFile model) throws FileNotFoundException {
            return new FileReader(fullPath(model.getSavedFileName()));
        }
    }

    private static class S3PersistService extends UploadedFilePersistService {
        private final S3Client s3client;

        public S3PersistService(String bucket) {
            this.s3client = S3Client.inDefaultRegion(bucket);
        }

        @Override
        public void _persist(UploadedFile uploadedFile) throws PersistException {
            if (!s3client.put(uploadedFile.getFileName(),uploadedFile.getFile())) {
                throw new PersistException("failed to put object in s3");
            }
        }

        @Override
        public Reader getReader(CsvFile model) throws FileNotFoundException {
            S3Object object = s3client.get(model.getSavedFileName());
            return new InputStreamReader(object.getObjectContent());
        }
    }

}
