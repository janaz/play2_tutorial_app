package com.clustrino.csv;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

public class CSVFile {
    private CSVReader reader;
    List<String[]> data;

    public CSVFile(InputStream is) throws IOException {
        this(new InputStreamReader(is));
    }

    public CSVFile(Reader r) throws IOException {
        reader = new CSVReader(r);
    }

    private boolean dataIncludesHeader() {
        return true;
    }

    private String[] madeUpHeader() {
        return new String[]{"a","b","C"};
    }

    private int maxCols() {
        List<Integer> lengths = Lists.transform(getData(), new Function<String[], Integer>() {
            @Override
            public Integer apply(String[] strings) {
                return strings.length;
            }
        });
        return Collections.max(lengths);
    }

    public String[] header() {
        if (this.dataIncludesHeader()) {
            return getData().iterator().next();
        } else {
            return this.madeUpHeader();
        }
    }

    public List<String[]> data() {
        if (dataIncludesHeader()) {
            return getData().subList(1, getData().size() - 1);
        } else {
            return getData();
        }
    }

    private List<String[]> getData() {
      if (data == null) {
          try {
              data = reader.readAll();
          } catch (IOException e1) {
              data = Collections.EMPTY_LIST;
          } finally {
              try {
                  reader.close();
              } catch (IOException e) {
              }
          }
      }
      return data;
    }

}
