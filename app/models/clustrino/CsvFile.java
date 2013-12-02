package models.clustrino;


import com.neutrino.csv.DataCategory;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import models.configuration.User;
import org.apache.commons.codec.binary.Hex;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Entity
public class CsvFile extends Model {

    @Id
    public Long id;

    @Required
    public String fileName;

    @Required
    public Long uploadedAt;

    @ManyToOne
    @JoinColumn(name="owner_id")
    public User user;

    @OneToOne
    public CsvMetadata metadata;

    public static Finder<Long,CsvFile> find = new Finder(
            Long.class, CsvFile.class
    );

    public CsvMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(CsvMetadata metadata) {
        this.metadata = metadata;
    }

    public static List<CsvFile> all() {
        return find.all();
    }

    public static void create(CsvFile file) {
        file.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public String getSavedFileName() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("I can haz a SHA256 algorithm?", e);
        }
        md.update(fileName.getBytes());
        return Hex.encodeHexString(md.digest()) + "_" + user.id + "_" + uploadedAt;
    }

    public List<DataCategory> getHeaderCategories() {
        if (this.getMetadata() != null && this.getMetadata().getColumnNames() != null) {
            Iterable<String> iter = Splitter.on(',').split(this.getMetadata().getColumnNames());
            return Lists.transform(Lists.newArrayList(iter), new Function<String, DataCategory>() {
                @Nullable
                @Override
                public DataCategory apply(@Nullable String s) {
                    return DataCategory.valueOf(s);
                }
            });
        } else {
            return null;
        }
    }

}