package it.desimone.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MapUtils {

	public static Map sortByValue(Map map, boolean reversed) {
		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		};
		if (reversed){
			comparator = Collections.reverseOrder(comparator);
		}
		List<Map.Entry> list = new LinkedList<Map.Entry>(map.entrySet());
		Collections.sort(list, comparator);
		Map result = new LinkedHashMap();
		for (ListIterator<Map.Entry> it = list.listIterator(); it.hasNext();) {
			Map.Entry entry = it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
}
