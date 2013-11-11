package models.clustrino;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import models.User;
import org.apache.commons.codec.binary.Hex;
import play.data.validation.Constraints.*;

import play.db.ebean.*;

import javax.persistence.*;

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

    public static Finder<Long,CsvFile> find = new Finder(
            Long.class, CsvFile.class
    );

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
}