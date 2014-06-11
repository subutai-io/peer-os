package org.safehaus.subutai.impl.packagemanager.storage;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import org.safehaus.subutai.impl.packagemanager.info.SelectionState;

class SelectionStateAdapter extends AbbrEnumTypeAdapter<SelectionState> {

    @Override
    public SelectionState read(JsonReader reader) throws IOException {
        if(reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        char ch = reader.nextString().charAt(0);
        return SelectionState.getByAbbrev(ch);
    }

}
