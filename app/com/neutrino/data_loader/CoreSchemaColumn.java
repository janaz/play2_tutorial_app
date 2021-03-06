package com.neutrino.data_loader;

import org.polyjdbc.core.schema.model.AttributeBuilder;
import org.polyjdbc.core.schema.model.RelationBuilder;

/**
 * Created by tomasz.janowski on 31/03/14.
 */
public class CoreSchemaColumn{
    private final String name;
    private final String type;
    private boolean notNull = false;
    private boolean unique = false;

    private boolean primary = false;
    private boolean selectable = false;
    private int length;
    private CoreSchemaTable foreignKey;
    private boolean selected;
    private boolean full;

    public CoreSchemaColumn(String name, String type) {
        this(name, type, -1);
    }

    public CoreSchemaColumn(String name, String type, int length) {
        this.name = name;
        this.type = type;
        this.length = length;
    }


    public String getName() {
        return name;
    }

    public void setLength(int l) {
        length = l;
    }

    public CoreSchemaColumn notNull() {
        notNull = true;
        return this;
    }

    public CoreSchemaColumn unique() {
        unique = true;
        return this;
    }

    public CoreSchemaColumn id() {
        notNull = true;
        primary = true;
        return this;
    }

    public CoreSchemaColumn selectable() {
        if (this.full) {
            throw new RuntimeException("Can't be full and selectable");
        }
        selectable = true;
        return this;
    }

    public CoreSchemaColumn full() {
        if (this.selectable) {
            throw new RuntimeException("Can't be full and selectable");
        }
        full = true;
        return this;
    }

    public CoreSchemaColumn foreignKey(CoreSchemaTable foreignTable) {
        foreignKey = foreignTable;
        return this;
    }

    public CoreSchemaColumn foreignKey(CoreSchemaTypeTable foreignTable) {
        foreignKey = foreignTable.table();
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isPrimary() {
        return primary;
    }

    public CoreSchemaTable getForeignKey() {
        return foreignKey;
    }


    public AttributeBuilder buildAttribute(RelationBuilder builder) {
        AttributeBuilder attr = null;
        if (type.startsWith("VARCHAR")) {
            attr = builder.withAttribute().string(getName()).withMaxLength(length);
        } else if (type.equals("INTEGER")) {
            attr = builder.withAttribute().integer(getName());
            if (primary) {
                attr.withAdditionalModifiers("AUTO_INCREMENT");
            }
        } else if (type.equals("TIMESTAMP")) {
            attr = builder.withAttribute().timestamp(getName());
        } else if (type.equals("DATE")) {
            attr = builder.withAttribute().date(getName());
        }
        if (attr != null) {
            if (notNull) attr.notNull();
            if (unique) attr.unique();
        }
        return attr;
    }

    public String getType() {
        return type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public CoreSchemaColumn dup() {
        CoreSchemaColumn nc = new CoreSchemaColumn(name, type, length);
        nc.notNull = notNull;
        nc.unique = unique;
        nc.primary = primary;
        nc.selectable = selectable;
        nc.foreignKey = foreignKey;
        nc.selected = selected;
        nc.full = full;
        return nc;
    }

    public void select() {
        if (selectable) {
            selected = true;
        }
    }

    public boolean isSelected() {
        return selected || !selectable;
    }

    public void adjustLength(int length) {
        this.length = length;
    }

    public boolean isFull() {
        return this.full;
    }

    public boolean isSelectable() {
        return selectable;
    }
}
