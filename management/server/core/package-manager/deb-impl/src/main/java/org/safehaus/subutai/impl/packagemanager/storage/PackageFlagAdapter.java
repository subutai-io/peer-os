package org.safehaus.subutai.impl.packagemanager.storage;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import org.safehaus.subutai.impl.packagemanager.info.PackageFlag;

class PackageFlagAdapter extends AbbrEnumTypeAdapter<PackageFlag> {

    @Override
    public PackageFlag read(JsonReader reader) throws IOException {
        if(reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        char ch = reader.nextString().charAt(0);
        return PackageFlag.getByAbbrev(ch);
    }

}
