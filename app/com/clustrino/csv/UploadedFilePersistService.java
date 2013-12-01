package com.clustrino.csv;

import com.amazonaws.services.s3.model.S3Object;
import com.clustrino.AppConfiguration;
import com.clustrino.aws.S3Client;
import com.clustrino.profiling.EbeanServerManager;
import com.clustrino.profiling.MetadataSchema;
import com.clustrino.profiling.QueryCallable;
import com.clustrino.profiling.StagingSchema;
import com.clustrino.profiling.metadata.*;
import models.configuration.ProfilingTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class UploadedFilePersistService {
    public void persist(UploadedFile uploadedFile) throws PersistException {
        _persist(uploadedFile);
    }

    public void importToDB(DataSet dataSet) throws IOException {
        CSVFile csvFile = new CSVFile(dataSet);
        DBSaver saver = new DBSaver(dataSet.userId, dataSet.id);
        csvFile.addReadListener(saver);
        csvFile.readFile();
    }

    public void runProfiling(DataSet dataSet) {
        final StagingSchema stg = new StagingSchema(dataSet.userId, dataSet.id);
        final MetadataSchema mtd = new MetadataSchema(dataSet.userId);
        for (final DataColumn col : dataSet.getColumns()) {
            if (col.dataType != DataCategory.UNKNOWN) {
                for (final ProfilingTemplate template : ProfilingTemplate.find.all()) {
                    EbeanServerManager.getManager().executeQuery(stg.server(), new QueryCallable<Void>() {
                        @Override
                        public Void call(PreparedStatement pstmt) throws SQLException {
                            ResultSet res = pstmt.executeQuery();
                            ResultSetMetaData rsmd = res.getMetaData();
                            List<String> colNames = new ArrayList<>();
                            int colCount = rsmd.getColumnCount();
                            for (int i = 0; i < colCount; i++) {
                                colNames.add(rsmd.getColumnName(i + 1));
                            }
                            while (res.next()) {
                                Map<String, String> retVal = new HashMap<>();
                                for (int i = 0; i < colCount; i++) {
                                    retVal.put(colNames.get(i), res.getString(i + 1));
                                }
                                if (template.targetTableName.equals("ProfilingResultColumn")) {
                                    ProfilingResultsColumn.addResult(mtd, stg, template, col, retVal);
                                } else if (template.targetTableName.equals("ProfilingResultFormat")) {
                                    ProfilingResultsFormat.addResult(mtd, stg, template, col, retVal);
                                } else if (template.targetTableName.equals("ProfilingResultValue")) {
                                    ProfilingResultsValue.addResult(mtd, stg, template, col, retVal);
                                }

                            }
                            return null;

                        }

                        @Override
                        public String getQuery() {
                            return template.templateQuery.replaceAll("#SchemaName#", stg.databaseName()).
                                    replaceAll("#TableName#", stg.dataSetTableName()).
                                    replaceAll("#ColumnName#", col.name);
                        }

                        @Override
                        public void setup(PreparedStatement pstmt) throws SQLException {
                        }
                    });

                }
            }
        }
    }

    public abstract void _persist(UploadedFile uploadedFile) throws PersistException;

    public static UploadedFilePersistService getService() {
        final String store = AppConfiguration.get().getString("uploaded_files_store");
        if (store.equals("filesystem")) {
            return new FileSystemPersistService(AppConfiguration.get().getString("uploaded_files_root"));
        } else if (store.equals("s3")) {
            return new S3PersistService(AppConfiguration.get().getString("uploaded_files_s3_bucket"));
        } else {
            throw new UnsupportedOperationException("uploaded_files store is not configured properly");
        }
    }

    public abstract Reader getReader(DataSet model) throws IOException;

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
            String destination = fullPath(uploadedFile.getFileName());

            try {
                FileCopyUtils.copy(uploadedFile.getFile(), new File(destination));
            } catch (IOException e) {
                throw new PersistException(e);
            }
        }

        @Override
        public Reader getReader(DataSet model) throws FileNotFoundException {
            System.out.println("opening " + fullPath(model.getFile().getFileLocation()));
            return new FileReader(fullPath(model.getFile().getFileLocation()));
        }
    }

    private static class S3PersistService extends UploadedFilePersistService {
        private final S3Client s3client;

        public S3PersistService(String bucket) {
            this.s3client = S3Client.inDefaultRegion(bucket);
        }

        @Override
        public void _persist(UploadedFile uploadedFile) throws PersistException {
            if (!s3client.put(uploadedFile.getFileName(), uploadedFile.getFile())) {
                throw new PersistException("failed to put object in s3");
            }
        }

        @Override
        public Reader getReader(DataSet model) throws FileNotFoundException {
            S3Object object = s3client.get(model.getFile().fileLocation);
            return new InputStreamReader(object.getObjectContent());
        }
    }

}
