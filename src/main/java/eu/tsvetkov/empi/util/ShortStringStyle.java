package eu.tsvetkov.empi.util;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

import java.util.Collection;
import java.util.Map;

/**
 * Style defining how Java objects are represented by their toString() method.
 *
 * @author Vadim Tsvetkov <dev@tsvetkov.eu>
 * Created on 09.12.16
 */
public class ShortStringStyle extends StandardToStringStyle {

    public static final ShortStringStyle INSTANCE = new ShortStringStyle(false);
    public static final ShortStringStyle INSTANCE_WITH_COLLECTIONS = new ShortStringStyle(true);
    private boolean printCollections;

    public ShortStringStyle(boolean printCollections) {
        super();
        this.printCollections = printCollections;
        setUseShortClassName(true);
        setFieldSeparator(", ");
        setArrayContentDetail(false);
        setContentStart(" [");
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection<?> coll) {
        if(printCollections) {
            super.appendDetail(buffer, fieldName, coll);
        }
        else {
            super.appendDetail(buffer, fieldName, "[" + coll.size() + " items]");
        }
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Map<?, ?> map) {
        super.appendDetail(buffer, fieldName, "[" + map.size() + " items]");
    }

    public static String toString(Object object, boolean printCollections) {
        return ReflectionToStringBuilder.toString(object, (printCollections ? INSTANCE_WITH_COLLECTIONS: INSTANCE));
    }

    public static String toString(Object object) {
        return toString(object, false);
    }
}
