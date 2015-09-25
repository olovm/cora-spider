package epc.spider.record;

import java.util.HashSet;
import java.util.Set;

import epc.metadataformat.data.DataGroup;

public class KeyCalculatorTest implements PermissionKeyCalculator {

	@Override
	public Set<String> calculateKeys(String accessType, String recordType, DataGroup record) {
		Set<String> keys = new HashSet<>();
		String unit = record.getFirstAtomicValueWithDataId("unit");
		String key = String.join(":", accessType, recordType, "SYSTEM", unit,"*").toUpperCase();
		keys.add(key);
		return keys;
	}
	

	@Override
	public Set<String> calculateKeysForList(String accessType, String recordType) {
		return null;
	}
}
