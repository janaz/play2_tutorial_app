package com.neutrino.datamappingdiscovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {
    public static interface ListToMapConverter<K, V> {
        public K getKey(V item);
    }

    public static <K, V> Map<K, Collection<V>> listAsMap(Collection<V> sourceList, ListToMapConverter<K, V> converter) {
        Map<K, Collection<V>> newMap = new HashMap<>();
        for (V item : sourceList) {
            Collection c = newMap.get(converter.getKey(item));
            if (c == null) {
                c = new ArrayList<>();
                newMap.put(converter.getKey(item), c);
            }
            c.add(item);
        }
        return newMap;
    }

    public static <K, V> Map<K, V> listAsUniqueMap(Collection<V> sourceList, ListToMapConverter<K, V> converter) {
        Map<K, V> newMap = new HashMap<K, V>();
        for (V item : sourceList) {
            newMap.put(converter.getKey(item), item);
        }
        return newMap;
    }

}
