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

import se.uu.ub.cora.bookkeeper.data.Data;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public class RecordStorageCreateUpdateSpy implements RecordStorage {

	public DataGroup createRecord;
	public DataGroup updateRecord;
	public String dataDivider;
	public boolean createWasCalled = false;

	public boolean modifiableLinksExistsForRecord = false;

	@Override
	public DataGroup read(String type, String id) {
		if (type.equals("recordType") && id.equals("typeWithAutoGeneratedId")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "placeNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "place"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));

			return group;
		}
		if (type.equals("recordType") && id.equals("typeWithUserGeneratedId")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "placeNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "place"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			return group;
		}
		if (type.equals("typeWithUserGeneratedId") && id.equals("uppsalaRecord1")) {
			DataGroup group = DataGroup.withNameInData("typeWithUserGeneratedId");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "placeNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "place"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("unit", "Uppsala"));
			return group;
		}
		if (type.equals("typeWithUserGeneratedId") && id.equals("gothenburgRecord1")) {
			DataGroup group = DataGroup.withNameInData("typeWithUserGeneratedId");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "placeNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "place"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("unit", "gothenburg"));
			return group;
		}
		if (type.equals("recordType") && id.equals("recordType")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "recordTypeNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("recordInfo", "recordInfo"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "recordType"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			return group;
		}
		if (type.equals("recordType") && id.equals("image")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "imageNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "image"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			group.addChild(DataAtomic.withNameInDataAndValue("parentId", "binary"));
			return group;
		}
		if (type.equals("recordType") && id.equals("binary")) {
			DataGroup group = DataGroup.withNameInData("recordType");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "binaryNew"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "binary"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "true"));
			return group;
		}
		if (type.equals("recordType") && id.equals("metadataGroup")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "metadataGroupNewGroup"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "metadataGroupGroup"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			return group;
		}
		if (type.equals("recordType") && id.equals("metadataCollectionVariable")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "metadataCollectionVariableNewGroup"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "metadataCollectionVariableGroup"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			return group;
		}
		if (type.equals("recordType") && id.equals("metadataRecordLink")) {
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("newMetadataId", "metadataRecordLinkNewGroup"));
			group.addChild(DataAtomic.withNameInDataAndValue("metadataId", "metadataRecordLinkGroup"));
			group.addChild(DataAtomic.withNameInDataAndValue("userSuppliedId", "true"));
			group.addChild(DataAtomic.withNameInDataAndValue("abstract", "false"));
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroup")) {
			DataGroup group = DataGroup.withNameInData("testGroup");

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			group.addChild(childReferences);
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
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroupWithOneChild")) {
			DataGroup group = DataGroup.withNameInData("testGroupWithOneChild");

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			group.addChild(childReferences);
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroupWithTwoChildren")) {
			DataGroup group = DataGroup.withNameInData("testGroupWithTwoChildren");

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			childReferences.addChild(createChildReference("childWithSameNameInDataAsChildTwo", "0", "1"));
			group.addChild(childReferences);
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("testGroupWithThreeChildren")) {
			DataGroup group = DataGroup.withNameInData("testGroupWithTwoChildren");

			DataGroup childReferences = DataGroup.withNameInData("childReferences");
			childReferences.addChild(createChildReference("childOne", "1", "1"));
			childReferences.addChild(createChildReference("childTwo", "0", "1"));
			childReferences.addChild(createChildReference("childThree", "1", "1"));
			group.addChild(childReferences);
			return group;
		}
		if (type.equals("metadataTextVariable") && id.equals("childWithSameNameInDataAsChildTwo")) {
			//name in data is not same as id to test same scenario as recordInfoGroup/recordInfoNewGroup
			//different id, same name in data
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "childTwo"));
			return group;
		}
		if (type.equals("metadataGroup") && id.equals("childThree")) {
			throw new RecordNotFoundException("No record exists with recordId: childThree");
		}
		if (id.equals("testItemCollection")) {
			DataGroup group = DataGroup.withNameInData("metadata");

			DataGroup itemReferences = DataGroup.withNameInData("collectionItemReferences");
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thisItem"));
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thatItem"));
			group.addChild(itemReferences);
			return group;
		}
		if (id.equals("testParentMissingItemCollectionVar")) {
			DataGroup group = DataGroup.withNameInData("metadata");

			group.addChild(DataAtomic.withNameInDataAndValue("refCollectionId", "testParentMissingItemCollection"));
			return group;
		}
		if (id.equals("testParentMissingItemCollection")) {
			DataGroup group = DataGroup.withNameInData("metadata");

			DataGroup itemReferences = DataGroup.withNameInData("collectionItemReferences");
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thisItem"));
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thoseItem"));
			group.addChild(itemReferences);
			return group;
		}
		if (id.equals("testParentCollectionVar")) {
			DataGroup group = DataGroup.withNameInData("metadata");

			group.addChild(DataAtomic.withNameInDataAndValue("refCollectionId", "testParentItemCollection"));
			return group;
		}
		if (id.equals("testParentItemCollection")) {
			DataGroup group = DataGroup.withNameInData("metadata");

			DataGroup itemReferences = DataGroup.withNameInData("collectionItemReferences");
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thisItem"));
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thatItem"));
			itemReferences.addChild(DataAtomic.withNameInDataAndValue("ref", "thoseItem"));
			group.addChild(itemReferences);
			return group;
		}
		if(type.equals("metadataCollectionItem") && id.equals("thisItem")){
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "this"));
			return group;
		}
		if(type.equals("metadataCollectionItem") && id.equals("thatItem")){
			DataGroup group = DataGroup.withNameInData("metadata");
			group.addChild(DataAtomic.withNameInDataAndValue("nameInData", "that"));
			return group;
		}
		return null;
	}

	private DataGroup createChildReference(String refId, String repeatMin, String repeatMax) {
		DataGroup childReference = DataGroup.withNameInData("childReference");
		childReference.addChild(DataAtomic.withNameInDataAndValue("ref", refId));
		childReference.addChild(DataAtomic.withNameInDataAndValue("repeatMin", repeatMin));
		childReference.addChild(DataAtomic.withNameInDataAndValue("repeatMax", repeatMax));
		return childReference;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup linkList,
			String dataDivider) {
		createRecord = record;
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
	public void update(String type, String id, DataGroup record, DataGroup linkList,
			String dataDivider) {
		updateRecord = record;
		this.dataDivider = dataDivider;
	}

	@Override
	public Collection<DataGroup> readList(String type) {
		ArrayList<DataGroup> recordTypeList = new ArrayList<>();

		DataGroup metadataGroup = DataGroup.withNameInData("recordType");
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", "metadataGroup"));
		metadataGroup.addChild(recordInfo);

		metadataGroup.addChild(DataAtomic.withNameInDataAndValue("parentId", "metadata"));
		recordTypeList.add(metadataGroup);


		DataGroup metadataTextVariable = DataGroup.withNameInData("recordType");
		DataGroup recordInfoTextVariable = DataGroup.withNameInData("recordInfo");
		recordInfoTextVariable.addChild(DataAtomic.withNameInDataAndValue("id", "metadataTextVariable"));
		metadataTextVariable.addChild(recordInfoTextVariable);

		metadataTextVariable.addChild(DataAtomic.withNameInDataAndValue("parentId", "metadata"));
		recordTypeList.add(metadataTextVariable);

		DataGroup presentationVar = DataGroup.withNameInData("recordType");
		DataGroup recordInfoPresentationVar = DataGroup.withNameInData("recordInfo");
		recordInfoPresentationVar.addChild(DataAtomic.withNameInDataAndValue("id", "presentationVar"));
		metadataTextVariable.addChild(recordInfoTextVariable);

		presentationVar.addChild(DataAtomic.withNameInDataAndValue("parentId", "presentation"));
		recordTypeList.add(presentationVar);

		return recordTypeList;


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
	public boolean recordExistsForRecordTypeAndRecordId(String type, String id) {
		return false;
	}

}
