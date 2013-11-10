package com.clustrino.csv;

import org.springframework.util.FileCopyUtils;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 10/11/13
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class UploadedFilePersistService {
    public abstract void persist(UploadedFile uploadedFile) throws PersistException;

    public static UploadedFilePersistService getService() {
        return new FileSystemPersistService("/tmp/uploaded_files");
    }

    public abstract Reader getReader(String filename) throws IOException;

    private static class FileSystemPersistService extends UploadedFilePersistService {
        private final String root;
        public FileSystemPersistService(String root) {
            this.root = root;
        }

        private String fullPath(String fileName) {
            return root +File.pathSeparator + fileName;
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
        public Reader getReader(String filename) throws FileNotFoundException {
            return new FileReader(fullPath(filename));
        }
    }
}
