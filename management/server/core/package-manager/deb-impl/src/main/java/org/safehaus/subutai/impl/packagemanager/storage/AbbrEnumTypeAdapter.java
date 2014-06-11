package org.safehaus.subutai.impl.packagemanager.storage;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.safehaus.subutai.impl.packagemanager.info.Abbreviation;

abstract class AbbrEnumTypeAdapter<T extends Abbreviation> extends TypeAdapter<T> {

    @Override
    public void write(JsonWriter writer, T t) throws IOException {
        if(t != null) writer.value(String.valueOf(t.getAbbrev()));
        else writer.nullValue();
    }

}
