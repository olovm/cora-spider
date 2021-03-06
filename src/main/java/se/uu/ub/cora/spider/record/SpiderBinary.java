package se.uu.ub.cora.spider.record;

import se.uu.ub.cora.beefeater.authentication.User;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.storage.RecordStorage;

public abstract class SpiderBinary {

	protected RecordStorage recordStorage;
	protected String recordType;
	protected Authenticator authenticator;
	protected String authToken;
	protected User user;

	public void checkRecordTypeIsChildOfBinary() {
		DataGroup recordTypeDefinition = getRecordTypeDefinition();
		if (!recordTypeIsChildOfBinary(recordTypeDefinition)) {
			throw new MisuseException(
					"It is only possible to upload files to recordTypes that are children of binary");
		}
	}

	private DataGroup getRecordTypeDefinition() {
		return recordStorage.read("recordType", recordType);
	}

	private boolean recordTypeIsChildOfBinary(DataGroup dataGroup) {
		return dataGroup.containsChildWithNameInData("parentId")
				&& "binary".equals(getParentId(dataGroup));
	}

	private String getParentId(DataGroup recordTypeDefinition) {
		DataGroup parentIdGroup = recordTypeDefinition.getFirstGroupWithNameInData("parentId");
		return parentIdGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	protected String extractDataDividerFromData(DataGroup dataGroup) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		return dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	protected void tryToGetActiveUser() {
		user = authenticator.getUserForToken(authToken);
	}
}
