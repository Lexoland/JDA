package dev.lexoland.jda.api;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

public interface DataSerializable extends SerializableData {

    default DataObject toJson() {
        DataObject obj = DataObject.empty();
        toJson(obj);
        return obj;
    }

    void toJson(DataObject obj);

    void fromJson(DataObject obj);

    @NotNull
    @Override
    default DataObject toData() {
        return toJson();
    }
}
