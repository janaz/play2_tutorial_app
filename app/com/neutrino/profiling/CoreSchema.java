package com.neutrino.profiling;

public class CoreSchema {
    private final Integer userId;

    public CoreSchema(Integer userId) {
        this.userId = userId;
    }

    private String databaseName() {
        return String.format("Core%03d", this.userId);
    }

    public boolean isCreated() {
        return EbeanServerManager.getManager().isCreated(databaseName());
    }

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());

    }
}
