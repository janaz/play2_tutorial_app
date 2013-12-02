package com.neutrino.profiling;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryCallable<T> {
    public T call(PreparedStatement pstmt) throws SQLException;
    public String getQuery();
    void setup(PreparedStatement pstmt) throws SQLException;
}
