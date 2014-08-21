/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.shared.protocol;

import org.doomdark.uuid.UUIDGenerator;

import java.util.*;

/**
 * @author dilshat
 */
public class Util {

	public static UUID generateTimeBasedUUID() {
		return java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
	}

	public static boolean isCollectionEmpty(Collection col) {
		return col == null || col.isEmpty();
	}

	public static void retainValues(Set col1, Set col2) {
		if (col1 != null && col2 != null) {
			col1.retainAll(col2);
		}
	}

	public static void removeValues(Set col1, Set col2) {
		if (col1 != null && col2 != null) {
			col1.removeAll(col2);
		}
	}

	public static String getAgentIpByMask(Agent agent, String mask) {
		if (agent != null) {
			if (agent.getListIP() != null && !agent.getListIP().isEmpty()) {
				for (String ip : agent.getListIP()) {
					if (ip.matches(mask)) {
						return ip;
					}
				}
			}
			return agent.getHostname();
		}
		return null;
	}

	public static Set<Agent> filterLxcAgents(Set<Agent> agents) {
		Set<Agent> filteredAgents = new HashSet<>();
		if (agents != null) {
			for (Agent agent : agents) {
				if (agent.isIsLXC()) {
					filteredAgents.add(agent);
				}
			}
		}
		return filteredAgents;
	}

	public static Set<Agent> filterPhysicalAgents(Set<Agent> agents) {
		Set<Agent> filteredAgents = new HashSet<>();
		if (agents != null) {
			for (Agent agent : agents) {
				if (!agent.isIsLXC()) {
					filteredAgents.add(agent);
				}
			}
		}
		return filteredAgents;
	}

	public static String removeAllWhitespace(String str) {
		if (!isStringEmpty(str)) {
			return str.replaceAll("\\s+", "");
		} else if (str != null) {
			return "";
		}
		return null;
	}

	public static boolean isStringEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static int countNumberOfOccurences(String strToSearch, String strToCount) {
		int idx = strToSearch.indexOf(strToCount);
		int count = 0;
		while (idx > -1) {
			count++;
			idx = strToSearch.indexOf(strToCount, idx + 1);
		}
		return count;
	}

	public static Set<Agent> wrapAgentToSet(Agent agent) {
		if (agent != null) {
			return new HashSet<>(Arrays.asList(agent));
		}
		return new HashSet<>();
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
		}

		return false;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V>
	sortMapByValueAsc(Map<K, V> map) {
		List<Map.Entry<K, V>> list
				= new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V>
	sortMapByValueDesc(Map<K, V> map) {
		List<Map.Entry<K, V>> list
				= new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
