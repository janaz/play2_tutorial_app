package com.clustrino.profiling.metadata;

import org.apache.commons.codec.binary.Hex;
import org.pojava.datetime.DateTime;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Entity
@Table(name="File")
public class File extends Model {
    @Id
    public Integer id;

    @NotNull
    @Column(name="OriginalFileName", length=255)
    public String originalFileName;

    @NotNull
    @Column(name="FileLocation", length=255)
    public String fileLocation;

    @NotNull
    @Column(name="FileFormat", length=30)
    public String fileFormat;

    @NotNull
    @Column(name="Delimiter", length=1)
    public String delimiter;

    @NotNull
    @Column(name="Quote", length=1)
    public String quote;

    @NotNull
    @Column(name="HeaderFlag")
    public Boolean headerFlag;

    @OneToOne
    @NotNull
    @JoinColumn(name="DataSetID")
    public DataSet dataSet;

    @NotNull
    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, File> find(String serverName) {
        return new Finder<Integer, File>(
            serverName, Integer.class, File.class);
    }

    public void setSavedFileName() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("I can haz a SHA256 algorithm?", e);
        }
        md.update(originalFileName.getBytes());
        fileLocation = Hex.encodeHexString(md.digest()) + "_" + getDataSet().userId;
    }


    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }
}
