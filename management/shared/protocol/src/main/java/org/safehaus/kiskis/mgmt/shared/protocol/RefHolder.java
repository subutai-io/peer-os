package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by dilshat on 4/26/14.
 */
public class RefHolder {
    private static RefHolder INSTANCE = new RefHolder();
    private Queue<Object> references = new ConcurrentLinkedQueue<>();

    private RefHolder() {
    }

    public static <T> T getRef(Class<T> clazz) {
        for (Object o : INSTANCE.references) {
            if (clazz.isInstance(o)) {
                return clazz.cast(o);
            }
        }
        return null;
    }

    public static void addRef(Object ref) {
        INSTANCE.references.add(ref);
    }

}
