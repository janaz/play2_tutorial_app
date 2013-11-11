package com.clustrino.csv;

import com.clustrino.AppConfiguration;
import models.clustrino.CsvFile;
import org.springframework.util.FileCopyUtils;

import java.io.*;


public abstract class UploadedFilePersistService {
    public abstract void persist(UploadedFile uploadedFile) throws PersistException;

    public static UploadedFilePersistService getService() {
        return new FileSystemPersistService(AppConfiguration.get().getString("uploaded_files_root"));
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
        public void persist(UploadedFile uploadedFile) throws PersistException {
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
}
