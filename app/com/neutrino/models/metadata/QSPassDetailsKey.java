package com.neutrino.models.metadata;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QSPassDetailsKey {
    @Column(name="DataSetID")
    public Integer dataSetID;
    @Column(name="PassID")
    public Integer passID;
    @Column(name="SortID")
    public Integer sortID;

    public QSPassDetailsKey(Integer dataSetID, Integer passID, Integer sortID) {
        this.dataSetID = dataSetID;
        this.passID = passID;
        this.sortID = sortID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QSPassDetailsKey other = (QSPassDetailsKey) obj;
        if ((this.dataSetID == null) ? (other.dataSetID != null) : !this.dataSetID.equals(other.dataSetID)) {
            return false;
        }
        if ((this.passID == null) ? (other.passID != null) : !this.passID.equals(other.passID)) {
            return false;
        }
        if ((this.sortID == null) ? (other.sortID != null) : !this.sortID.equals(other.sortID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.dataSetID != null ? this.dataSetID.hashCode() : 0);
        hash = 89 * hash + (this.passID != null ? this.passID.hashCode() : 0);
        hash = 89 * hash + (this.sortID != null ? this.sortID.hashCode() : 0);
        return hash;
    }
}
