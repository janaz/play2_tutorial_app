package models.clustrino;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.List;

@Entity
public class CsvMetadata  extends Model {
    @Id
    public Long id;


    @javax.persistence.Column(columnDefinition="TEXT")
    public String columnNames;

    @OneToOne(mappedBy="metadata")
    public CsvFile file;

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public static Finder<Long,CsvMetadata> find = new Finder(
            Long.class, CsvMetadata.class
    );

    public static List<CsvMetadata> all() {
        return find.all();
    }

    public static void create(CsvMetadata file) {
        file.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

}
