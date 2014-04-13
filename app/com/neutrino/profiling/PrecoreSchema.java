package com.neutrino.profiling;

public class PrecoreSchema {
    private final Integer userId;

    public PrecoreSchema(Integer userId) {
        this.userId = userId;
    }

    public String databaseName() {
        return String.format("PreCore%03d", this.userId);
    }

    public boolean isCreated() {
        return EbeanServerManager.getManager().isCreated(databaseName());
    }

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());

    }

}
