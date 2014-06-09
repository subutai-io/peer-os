package org.safehaus.subutai.impl.packagemanager.storage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.storage.PackageInfoStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePackageInfoStorage implements PackageInfoStorage {

    private static final Logger logger = LoggerFactory.getLogger(FilePackageInfoStorage.class);
    private final Path parent;

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
        File f = parent.resolve(filePath).toFile();
        if(!f.getParentFile().exists()) f.getParentFile().mkdirs();

        ObjectOutputStream out = null;
        try {
            FileOutputStream fos = new FileOutputStream(f);
            out = new ObjectOutputStream(fos);
            // wrap collection in list to ensure serializable collection
            ArrayList ls = new ArrayList(col);
            out.writeObject(ls);
        } finally {
            if(out != null) out.close();
        }
    }

    private List<PackageInfo> deserialize(String filePath) throws IOException {
        File f = parent.resolve(filePath).toFile();
        if(!f.exists()) return null;

        List<PackageInfo> res = null;
        ObjectInputStream in = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            in = new ObjectInputStream(fis);
            res = (List<PackageInfo>)in.readObject();
        } catch(FileNotFoundException ex) {
            // failed to open specified file
            logger.warn("Failed to open file", ex);
        } catch(ClassNotFoundException ex) {
            // invalid data
            logger.warn("Invalid data", ex);
        } finally {
            if(in != null) in.close();
        }
        return res;
    }

}
