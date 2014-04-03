package com.neutrino.models.metadata;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QSPassKey {
    @Column(name="DataSetID")
    public Integer dataSetID;
    @Column(name="ID")
    public Integer passID;

    public QSPassKey(Integer dataSetID, Integer passID) {
        this.dataSetID = dataSetID;
        this.passID = passID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QSPassKey other = (QSPassKey) obj;
        if ((this.dataSetID == null) ? (other.dataSetID != null) : !this.dataSetID.equals(other.dataSetID)) {
            return false;
        }
        if ((this.passID == null) ? (other.passID != null) : !this.passID.equals(other.passID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.dataSetID != null ? this.dataSetID.hashCode() : 0);
        hash = 89 * hash + (this.passID != null ? this.passID.hashCode() : 0);
        return hash;
    }
}
