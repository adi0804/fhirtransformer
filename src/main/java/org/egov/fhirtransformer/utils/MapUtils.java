package org.egov.fhirtransformer.utils;

import java.util.Map;
import java.util.Objects;

/**
 * Utility methods for safely extracting typed values from a Map.
 */
public final class MapUtils {

    /**
     * Retrieves a String value from the map.
     * Returns null if the map, key, or value is null.
     */
    public static String getString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Retrieves a Long value from the map.
     * Works with Number and String types.
     */
    public static Long getLong(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? (Long) value : null;
    }
}
