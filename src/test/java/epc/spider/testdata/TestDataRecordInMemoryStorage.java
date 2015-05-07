package epc.spider.testdata;

import java.util.HashMap;
import java.util.Map;

import epc.metadataformat.data.DataAtomic;
import epc.metadataformat.data.DataGroup;
import epc.spider.record.storage.RecordStorageInMemory;

public class TestDataRecordInMemoryStorage {
	private static Map<String, Map<String, DataGroup>> records = new HashMap<>();

	public static RecordStorageInMemory createRecordStorageInMemoryWithTestData() {
		addPlace();
		addMetadata();
		addPresentation();
		addText();
		records.put("recordType", new HashMap<String, DataGroup>());
		addRecordType();
		addRecordTypeRecordType();
		addRecordTypePlace();

		RecordStorageInMemory recordsInMemory = new RecordStorageInMemory(records);
		return recordsInMemory;
	}

	private static void addPlace() {
		records.put("place", new HashMap<String, DataGroup>());

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", "place"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "place:0001"));

		/**
		 * <pre>
		 * 		recordInfo
		 * 			type
		 * 			id
		 * 			organisation
		 * 			user
		 * 			tsCreated (recordCreatedDate)
		 * 			list tsUpdated (recordUpdatedDate)
		 * 			catalog Language
		 * </pre>
		 */

		DataGroup dataGroup = DataGroup.withDataId("authority");
		dataGroup.addChild(recordInfo);

		records.get("place").put("place:0001", dataGroup);
	}

	private static void addMetadata() {
		String metadata = "metadata";
		records.put(metadata, new HashMap<String, DataGroup>());
		DataGroup dataGroup = DataGroup.withDataId(metadata);

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "place"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", metadata));
		dataGroup.addChild(recordInfo);

		records.get(metadata).put("place", dataGroup);
	}

	private static void addPresentation() {
		String presentation = "presentation";
		records.put(presentation, new HashMap<String, DataGroup>());
		DataGroup dataGroup = DataGroup.withDataId(presentation);

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "placeView"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", presentation));
		dataGroup.addChild(recordInfo);

		records.get(presentation).put("placeView", dataGroup);
	}

	private static void addText() {
		String text = "text";
		records.put(text, new HashMap<String, DataGroup>());
		DataGroup dataGroup = DataGroup.withDataId(text);

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "placeText"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", text));
		dataGroup.addChild(recordInfo);

		records.get(text).put("placeText", dataGroup);
	}

	private static void addRecordType() {
		String recordType = "recordType";
		DataGroup dataGroup = DataGroup.withDataId(recordType);

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "metadata"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", recordType));
		dataGroup.addChild(recordInfo);

		records.get(recordType).put("metadata", dataGroup);
	}

	private static void addRecordTypeRecordType() {
		String recordType = "recordType";
		DataGroup dataGroup = DataGroup.withDataId(recordType);

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "recordType"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", recordType));
		dataGroup.addChild(recordInfo);

		dataGroup.addChild(DataAtomic.withDataIdAndValue("id", "recordType"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("metadataId", "metadata:recordType"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("presentationViewId",
				"presentation:pgRecordTypeView"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("presentationFormId",
				"presentation:pgRecordTypeForm"));
		dataGroup
				.addChild(DataAtomic.withDataIdAndValue("newMetadataId", "metadata:recordTypeNew"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("newPresentationFormId",
				"presentation:pgRecordTypeFormNew"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("listPresentationViewId",
				"presentation:pgRecordTypeViewList"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("searchMetadataId",
				"metadata:recordTypeSearch"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("searchPresentationFormId",
				"presentation:pgRecordTypeSearchForm"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("userSuppliedId", "true"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("permissionKey", "RECORDTYPE_RECORDTYPE"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("selfPresentationViewId",
				"presentation:pgrecordTypeRecordType"));
		records.get(recordType).put("recordType", dataGroup);

	}

	private static void addRecordTypePlace() {
		String recordType = "recordType";
		DataGroup dataGroup = DataGroup.withDataId(recordType);

		DataGroup recordInfo = DataGroup.withDataId("recordInfo");
		recordInfo.addChild(DataAtomic.withDataIdAndValue("id", "place"));
		recordInfo.addChild(DataAtomic.withDataIdAndValue("type", recordType));
		dataGroup.addChild(recordInfo);

		dataGroup.addChild(DataAtomic.withDataIdAndValue("id", "place"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("metadataId", "metadata:place"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("presentationViewId",
				"presentation:pgPlaceView"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("presentationFormId",
				"presentation:pgPlaceForm"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("newMetadataId", "metadata:placeNew"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("newPresentationFormId",
				"presentation:pgPlaceFormNew"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("listPresentationViewId",
				"presentation:pgPlaceViewList"));
		dataGroup.addChild(DataAtomic
				.withDataIdAndValue("searchMetadataId", "metadata:placeSearch"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("searchPresentationFormId",
				"presentation:pgPlaceSearchForm"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("userSuppliedId", "false"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("permissionKey", "RECORDTYPE_PLACE"));
		dataGroup.addChild(DataAtomic.withDataIdAndValue("selfPresentationViewId",
				"presentation:pgPlaceRecordType"));
		records.get(recordType).put("place", dataGroup);

	}
}
