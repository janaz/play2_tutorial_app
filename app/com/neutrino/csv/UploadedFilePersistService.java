package com.neutrino.csv;

import com.amazonaws.services.s3.model.S3Object;
import com.google.gdata.util.io.base.UnicodeReader;
import com.neutrino.AppConfiguration;
import com.neutrino.aws.S3Client;
import com.neutrino.profiling.EbeanServerManager;
import com.neutrino.profiling.MetadataSchema;
import com.neutrino.profiling.QueryCallable;
import com.neutrino.profiling.StagingSchema;
import com.neutrino.models.metadata.*;
import com.neutrino.models.configuration.ProfilingTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;


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
            for (final ProfilingTemplate template : ProfilingTemplate.find.all()) {
                EbeanServerManager.getManager().executeQuery(mtd.server(), new QueryCallable<Boolean>() {
                    @Override
                    public Boolean call(PreparedStatement pstmt) throws SQLException {
                        return pstmt.execute();
                    }

                    @Override
                    public String getQuery() {
                        String subQuery = template.templateQuery.replaceAll("#SchemaName#", stg.databaseName()).
                                replaceAll("#TableName#", stg.dataSetTableName()).
                                replaceAll("#ColumnName#", col.name);
                        String columnList = null;
                        if (template.targetTableName.equals("ProfilingResultColumn")) {
                            columnList = "TotalCount,DistinctCount,NullCount,PercentagePopulated,PercentageUnique,MinimumLength,MaximumLength,MinimumValue,MaximumValue";
                        } else if (template.targetTableName.equals("ProfilingResultFormat")) {
                            columnList = "Format, Cardinality";
                        } else if (template.targetTableName.equals("ProfilingResultValue")) {
                            columnList = "Value, Cardinality";
                        }
                        String query = "INSERT INTO " + template.targetTableName + "(ProfilingTemplateID, TableName,ColumnName,DataColumnID,CreationTimestamp, " + columnList +
                                ") SELECT " + template.id + ",'" + stg.dataSetTableName() + "','" + col.name + "'," + col.id + ",CURRENT_TIMESTAMP," + columnList + " FROM (" + subQuery + ") profiling_subquery";

                        System.out.println("PROFILE QUERY " + query);
                        return query;
                    }

                    @Override
                    public void setup(PreparedStatement pstmt) throws SQLException {
                    }
                });

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

    public abstract InputStream getInputStream(DataSet model) throws IOException;

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
        public InputStream getInputStream(DataSet model) throws IOException {
            System.out.println("opening " + fullPath(model.getFile().getFileLocation()));
            return new FileInputStream(fullPath(model.getFile().getFileLocation()));
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
        public InputStream getInputStream(DataSet model) throws FileNotFoundException {
            S3Object object = s3client.get(model.getFile().fileLocation);

            return object.getObjectContent();
        }
    }

}
