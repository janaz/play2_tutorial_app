package com.neutrino.models.core_common;

public interface CoreTable {
    public Class<? extends CoreType> getCoreTypeClass();
    public void setTypeByName(String typeName, String serverName);
    public void setHeader(PersonHeader header);

}
