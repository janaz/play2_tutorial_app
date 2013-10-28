package models.clustrino;


import java.util.List;
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
    public Long ownerId;

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
}