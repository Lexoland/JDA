package dev.lexoland.jda.api;

import net.dv8tion.jda.api.utils.data.DataObject;

public interface DataSerializable {

    default DataObject toJson() {
        DataObject obj = DataObject.empty();
        toJson(obj);
        return obj;
    }

    void toJson(DataObject obj);

    void fromJson(DataObject obj);

}
