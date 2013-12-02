package models.clustrino;

import com.neutrino.csv.DuplicteColumnException;
import com.google.common.base.Splitter;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void setColumnNames(String columnNames) throws DuplicteColumnException {
        Map<String, Boolean> columns= new HashMap<>();

        for (String col : Splitter.on(",").split(columnNames)) {
            if (columns.containsKey(col)) {
                throw new DuplicteColumnException("Duplicate column "+col);
            } else {
                columns.put(col, Boolean.TRUE);
            }
        }

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
