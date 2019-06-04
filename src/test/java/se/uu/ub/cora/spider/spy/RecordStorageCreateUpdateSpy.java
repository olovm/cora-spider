/*
 * Copyright 2015 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.spider.spy;

import java.util.ArrayList;
import java.util.Collection;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.spider.testdata.DataCreator;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.SpiderReadResult;

public class RecordStorageCreateUpdateSpy implements RecordStorage {

	public DataGroup createRecord;
	public DataGroup updateRecord;
	public String dataDivider;
	public boolean createWasCalled = false;

	public boolean modifiableLinksExistsForRecord = false;
	public DataGroup group;
	public String type;
	public String id;
	public DataGroup collectedTerms;

	@Override
	public DataGroup read(String type, String id) {
		if (type.equals("recordType") && id.equals("typeWithAutoGeneratedId")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			addMetadataIdAndMetadataIdNew(group, "place", "placeNew");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));

			return group;
		}
		if (type.equals("recordType") && id.equals("typeWithUserGeneratedId")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			addMetadataIdAndMetadataIdNew(group, "place", "placeNew");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			return group;
		}
		if (type.equals("typeWithAutoGeneratedId") && id.equals("somePlace")) {
			if (null == group) {
				group = DataGroup.withNameInData("typeWithAutoGeneratedId");
				addMetadataIdAndMetadataIdNew(group, "place", "placeNew");
				group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
				group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
				group.addChild(DataAtomic.withNameInDataAndValue("unit", "Uppsala"));
				group.addChild(DataGroup.withNameInData("recordInfo"));
			}
			return group;
		}
		if (type.equals("typeWithUserGeneratedId") && id.equals("uppsalaRecord1")) {
			DataGroup group = DataGroup.withNameInData("typeWithUserGeneratedId");
			addMetadataIdAndMetadataIdNew(group, "place", "placeNew");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("unit", "Uppsala"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("typeWithUserGeneratedId") && id.equals("gothenburgRecord1")) {
			DataGroup group = DataGroup.withNameInData("typeWithUserGeneratedId");
			addMetadataIdAndMetadataIdNew(group, "place", "placeNew");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("unit", "gothenburg"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("recordType") && id.equals("recordType")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			addMetadataIdAndMetadataIdNew(group, "recordType", "recordTypeNew");
			group.addChild(DataAtomic.withNameInDataAndValue("recordInfo", "recordInfo"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("recordType") && id.equals("image")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			addMetadataIdAndMetadataIdNew(group, "image", "imageNew");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup parentIdGroup = DataGroup.withNameInData("parentId");
			parentIdGroup
					.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", "recordType"));
			parentIdGroup.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", "binary"));
			group.addChild(parentIdGroup);
			return group;
		}
		if (type.equals("recordType") && id.equals("binary")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			addMetadataIdAndMetadataIdNew(group, "binary", "binaryNew");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "true"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("recordType") && id.equals("metadataGroup")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			addMetadataIdAndMetadataIdNew(group, "metadataGroupGroup", "metadataGroupNewGroup");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("recordType") && id.equals("metadataCollectionVariable")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			addMetadataIdAndMetadataIdNew(group, "metadataCollectionVariableGroup",
					"metadataCollectionVariableNewGroup");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("recordType") && id.equals("metadataRecordLink")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			addMetadataIdAndMetadataIdNew(group, "metadataRecordLinkGroup",
					"metadataRecordLinkNewGroup");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("recordType") && id.equals("typeWithAutoGeneratedIdWrongRecordInfo")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			addMetadataIdAndMetadataIdNew(group, "typeWithAutoGeneratedIdWrongRecordInfo", "typeWithAutoGeneratedIdWrongRecordInfoGroup");
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));

			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroup")) {
			DataGroup group = DataGroup.withNameInData("testGroup");

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			group.addChild(childReferences);
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("childOne")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "childOne"));
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("childTwo")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "childTwo"));
			group.addChild(DataGroup.withNameInData("recordInfo"));
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroupWithOneChild")) {
			DataGroup group = DataGroup.withNameInData("testGroupWithOneChild");
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			group.addChild(childReferences);
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroupWithTwoChildren")) {
			DataGroup group = DataGroup.withNameInData("testGroupWithTwoChildren");
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			childReferences
					.addChild(createChildReference("childWithSameNameInDataAsChildTwo", "0", "1"));
			group.addChild(childReferences);
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroupWithThreeChildren")) {
			DataGroup group = DataGroup.withNameInData("testGroupWithTwoChildren");
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			childReferences.addChild(createChildReference("childTwo", "0", "1"));
			childReferences.addChild(createChildReference("childThree", "1", "1"));
			group.addChild(childReferences);
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("childWithSameNameInDataAsChildTwo")) {
			// name in data is not same as id to test same scenario as
			// recordInfoGroup/recordInfoNewGroup
			// different id, same name in data
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "childTwo"));
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("childThree")) {
			throw new RecordNotFoundException("No record exists with recordId: childThree");
		}
		if (id.equals("testItemCollection")) {
			DataGroup group = DataGroup.withNameInData("metadata");

			DataGroup itemReferences = DataGroup.withNameInData("collectionItemReferences");

			createAndAddItemReference(itemReferences, "thisItem", "one");
			createAndAddItemReference(itemReferences, "thatItem", "two");

			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thisItem"));
			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thatItem"));
			group.addChild(itemReferences);
			return group;
		}
		if (id.equals("testParentMissingItemCollectionVar")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup refCollection = DataGroup.withNameInData("refCollection");
			refCollection.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType",
					"metadataItemCollection"));
			refCollection.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId",
					"testParentMissingItemCollection"));
			group.addChild(refCollection);

			// group.addChild(DataAtomic.withNameInDataAndValue("refCollectionId",
			// "testParentMissingItemCollection"));
			return group;
		}
		if (id.equals("testParentMissingItemCollection")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup itemReferences = DataGroup.withNameInData("collectionItemReferences");

			createAndAddItemReference(itemReferences, "thisItem", "one");
			createAndAddItemReference(itemReferences, "thoseItem", "two");
			//
			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thisItem"));
			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thoseItem"));
			group.addChild(itemReferences);
			return group;
		}
		if (id.equals("testParentCollectionVar")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));
			DataGroup refCollection = DataGroup.withNameInData("refCollection");
			refCollection.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType",
					"metadataItemCollection"));
			refCollection.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId",
					"testParentItemCollection"));
			group.addChild(refCollection);
			// group.addChild(DataAtomic.withNameInDataAndValue("refCollectionId",
			// "testParentItemCollection"));
			return group;
		}
		if (id.equals("testParentItemCollection")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));

			DataGroup itemReferences = DataGroup.withNameInData("collectionItemReferences");

			createAndAddItemReference(itemReferences, "thisItem", "one");
			createAndAddItemReference(itemReferences, "thatItem", "two");
			createAndAddItemReference(itemReferences, "thoseItem", "three");

			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thisItem"));
			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thatItem"));
			// itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref",
			// "thoseItem"));
			group.addChild(itemReferences);
			return group;
		}
		if (type.equals("metadataCollectionItem") && id.equals("thisItem")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "this"));
			return group;
		}
		if (type.equals("metadataCollectionItem") && id.equals("thatItem")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataGroup.withNameInData("recordInfo"));
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "that"));
			return group;
		}
		if ("image".equals(type) && "image:123456789".equals(id)) {
			return DataCreator.createRecordWithNameInDataAndIdAndLinkedRecordId("image",
					"image:123456789", "cora").toDataGroup();
		}
		DataGroup dataGroupToReturn = DataGroup.withNameInData("someNameInData");
		dataGroupToReturn.addChild(DataGroup.withNameInData("recordInfo"));
		return dataGroupToReturn;
	}

	private void addMetadataIdAndMetadataIdNew(DataGroup group, String metadataId,
			String metadataIdNew) {
		group.addChild(createChildWithNamInDataLinkedTypeLinkedId("newMetadataId", "metadataGroup",
				metadataId));
		group.addChild(createChildWithNamInDataLinkedTypeLinkedId("metadataId", "metadataGroup",
				metadataIdNew));
	}

	private DataGroup createChildReference(String refId, String repeatMin, String repeatMax) {
		DataGroup childReference = DataGroup.withNameInData("childReference");

		DataGroup ref = DataGroup.withNameInData("ref");
		ref.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", "metadataGroup"));
		ref.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", refId));
		ref.addAttributeByIdWithValue("type", "group");
		childReference.addChild(ref);
		childReference.addChild(DataAtomic.withNameInDataAndValue("repeatMin", repeatMin));
		childReference.addChild(DataAtomic.withNameInDataAndValue("repeatMax", repeatMax));
		return childReference;
	}

	private static DataGroup createChildWithNamInDataLinkedTypeLinkedId(String nameInData,
			String linkedRecordType, String id) {
		DataGroup metadataId = DataGroup.withNameInData(nameInData);
		metadataId
				.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", linkedRecordType));
		metadataId.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", id));
		return metadataId;
	}

	private void createAndAddItemReference(DataGroup collectionItemReferences,
			String linkedRecordId, String repeatId) {
		DataGroup ref1 = DataGroup.withNameInData("ref");
		ref1.setRepeatId(repeatId);
		ref1.addChild(
				DataAtomic.withNameInDataAndValue("linkedRecordType", "metadataCollectionItem"));
		ref1.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", linkedRecordId));
		collectionItemReferences.addChild(ref1);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		this.type = type;
		this.id = id;
		createRecord = record;
		this.collectedTerms = collectedTerms;
		this.dataDivider = dataDivider;
		createWasCalled = true;
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		return modifiableLinksExistsForRecord;
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		updateRecord = record;
		this.collectedTerms = collectedTerms;
		this.dataDivider = dataDivider;
	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) {
		ArrayList<DataGroup> recordTypeList = new ArrayList<>();

		DataGroup metadataGroup = DataGroup.withNameInData("recordType");
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", "metadata"));
		metadataGroup.addChild(recordInfo);
		recordTypeList.add(metadataGroup);

		DataGroup metadataGroupGroup = DataGroup.withNameInData("recordType");
		DataGroup recordInfoMetadataGroup = DataGroup.withNameInData("recordInfo");
		recordInfoMetadataGroup.addChild(DataAtomic.withNameInDataAndValue("id", "metadataGroup"));
		metadataGroupGroup.addChild(recordInfoMetadataGroup);

		DataGroup parentIdGroup = DataGroup.withNameInData("parentId");
		parentIdGroup.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", "recordType"));
		parentIdGroup.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", "binary"));
		metadataGroupGroup.addChild(parentIdGroup);

		recordTypeList.add(metadataGroupGroup);

		DataGroup metadataTextVariable = DataGroup.withNameInData("recordType");
		DataGroup recordInfoTextVariable = DataGroup.withNameInData("recordInfo");
		recordInfoTextVariable
				.addChild(DataAtomic.withNameInDataAndValue("id", "metadataTextVariable"));
		metadataTextVariable.addChild(recordInfoTextVariable);

		DataGroup parentIdGroupTextVar = DataGroup.withNameInData("parentId");
		parentIdGroupTextVar
				.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", "recordType"));
		parentIdGroupTextVar
				.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", "binary"));
		metadataTextVariable.addChild(parentIdGroupTextVar);

		// metadataTextVariable.addChild(DataAtomic.withNameInDataAndValue("parentId",
		// "metadata"));
		recordTypeList.add(metadataTextVariable);

		DataGroup presentationVar = DataGroup.withNameInData("recordType");
		DataGroup recordInfoPresentationVar = DataGroup.withNameInData("recordInfo");
		recordInfoPresentationVar
				.addChild(DataAtomic.withNameInDataAndValue("id", "presentationVar"));

		DataGroup parentIdGroupPresentationVar = DataGroup.withNameInData("parentId");
		parentIdGroupPresentationVar
				.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", "recordType"));
		parentIdGroupPresentationVar
				.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", "binary"));
		presentationVar.addChild(parentIdGroupPresentationVar);
		presentationVar.addChild(recordInfoTextVariable);

		// presentationVar.addChild(DataAtomic.withNameInDataAndValue("parentId",
		// "presentation"));
		recordTypeList.add(presentationVar);
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = recordTypeList;
		return spiderReadResult;

	}

	@Override
	public SpiderReadResult readAbstractList(String type, DataGroup filter) {
		return null;
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		return false;
	}

}
