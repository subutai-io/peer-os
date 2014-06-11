package org.safehaus.subutai.impl.packagemanager.storage;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.storage.PackageInfoStorage;
import org.safehaus.subutai.impl.packagemanager.info.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePackageInfoStorage implements PackageInfoStorage {

    private static final Logger logger = LoggerFactory.getLogger(FilePackageInfoStorage.class);
    private static final Gson gson;
    private final Path parent;
    private final Charset charset = Charset.defaultCharset();

    static {
        // DO NOT USE PRETTY-PRINTING!!! Elements serialized per line.
        gson = new GsonBuilder().serializeNulls()
                .registerTypeAdapter(PackageFlag.class, new PackageFlagAdapter())
                .registerTypeAdapter(PackageState.class, new PackageStateAdapter())
                .registerTypeAdapter(SelectionState.class, new SelectionStateAdapter())
                .create();
    }

    public FilePackageInfoStorage(String parentDir) {
        File f = new File(parentDir);
        if(!f.exists()) f.mkdirs();
        else if(!f.isDirectory()) parentDir = "/tmp/subutai/packages";

        this.parent = Paths.get(parentDir);
    }

    @Override
    public Collection<PackageInfo> retrieve(String key) {
        try {
            return deserialize(key);
        } catch(IOException ex) {
            logger.error("Failed to deserialize", ex);
            return null;
        }
    }

    @Override
    public boolean persist(String key, Collection<PackageInfo> packages) {
        try {
            serialize(packages, key);
            return true;
        } catch(IOException ex) {
            logger.error("Failed to serialize", ex);
            return false;
        }
    }

    @Override
    public boolean delete(String key) {
        File f = parent.resolve(key).toFile();
        return f.exists() ? f.delete() : false;
    }

    private void serialize(Collection<PackageInfo> col, String filePath) throws IOException {
        Path path = parent.resolve(filePath);
        File parentDir = path.toFile().getParentFile();
        if(!parentDir.exists()) parentDir.mkdirs();

        BufferedWriter out = null;
        try {
            out = Files.newBufferedWriter(path, charset);
            for(PackageInfo p : col) {
                out.write(gson.toJson(p));
                out.newLine();
            }
        } finally {
            if(out != null) out.close();
        }
    }

    private List<PackageInfo> deserialize(String filePath) throws IOException {
        Path path = parent.resolve(filePath);
        if(!path.toFile().exists()) return null;

        BufferedReader in = null;
        try {
            in = Files.newBufferedReader(path, charset);
            String line;
            List<PackageInfo> res = new ArrayList<>();
            while((line = in.readLine()) != null) {
                if(line.isEmpty()) continue;
                DebPackageInfo deb = gson.fromJson(line, DebPackageInfo.class);
                if(deb != null) res.add(deb);
            }
            return res;
        } catch(JsonParseException ex) {
            logger.error("Invalid source file", ex);
        } finally {
            if(in != null) in.close();
        }
        return null;
    }

}
