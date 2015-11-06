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

package se.uu.ub.cora.spider.record.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataElement;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.bookkeeper.data.DataRecordLink;
import se.uu.ub.cora.bookkeeper.metadata.MetadataTypes;
import se.uu.ub.cora.bookkeeper.storage.MetadataStorage;
import se.uu.ub.cora.spider.data.SpiderDataGroup;

public class RecordStorageInMemory implements RecordStorage, MetadataStorage {
	private Map<String, Map<String, DataGroup>> records = new HashMap<>();
	private Map<String, Map<String, DataGroup>> linkLists = new HashMap<>();
	private Map<String, Map<String, Map<String, Map<String, DataGroup>>>> incomingLinks = new HashMap<>();

	public RecordStorageInMemory() {
		// Make it possible to use default empty record storage
	}

	public RecordStorageInMemory(Map<String, Map<String, DataGroup>> records) {
		throwErrorIfConstructorArgumentIsNull(records);
		this.records = records;
	}

	private void throwErrorIfConstructorArgumentIsNull(
			Map<String, Map<String, DataGroup>> records) {
		if (null == records) {
			throw new IllegalArgumentException("Records must not be null");
		}
	}

	@Override
	public void create(String recordType, String recordId, DataGroup record, DataGroup linkList) {
		ensureStorageExistsForRecordType(recordType);
		checkNoConflictOnRecordId(recordType, recordId);
		storeIndependentRecordByRecordTypeAndRecordId(recordType, recordId, record);
		storeLinks(recordType, recordId, linkList);
	}

	private void ensureStorageExistsForRecordType(String recordType) {
		if (holderForRecordTypeDoesNotExistInStorage(recordType)) {
			createHolderForRecordTypeInStorage(recordType);
		}
	}

	private boolean holderForRecordTypeDoesNotExistInStorage(String recordType) {
		return !records.containsKey(recordType);
	}

	private void createHolderForRecordTypeInStorage(String recordType) {
		records.put(recordType, new HashMap<String, DataGroup>());
		linkLists.put(recordType, new HashMap<String, DataGroup>());
	}

	private void checkNoConflictOnRecordId(String recordType, String recordId) {
		if (recordWithTypeAndIdAlreadyExists(recordType, recordId)) {
			throw new RecordConflictException(
					"Record with recordId: " + recordId + " already exists");
		}
	}

	private boolean recordWithTypeAndIdAlreadyExists(String recordType, String recordId) {
		return records.get(recordType).containsKey(recordId);
	}

	private void storeIndependentRecordByRecordTypeAndRecordId(String recordType, String recordId,
			DataGroup record) {
		DataGroup recordIndependentOfEnteredRecord = createIndependentCopy(record);
		storeRecordByRecordTypeAndRecordId(recordType, recordId, recordIndependentOfEnteredRecord);
	}

	private DataGroup createIndependentCopy(DataGroup record) {
		return SpiderDataGroup.fromDataGroup(record).toDataGroup();
	}

	private DataGroup storeRecordByRecordTypeAndRecordId(String recordType, String recordId,
			DataGroup recordIndependentOfEnteredRecord) {
		return records.get(recordType).put(recordId, recordIndependentOfEnteredRecord);
	}

	private void storeLinks(String recordType, String recordId, DataGroup linkList) {
		DataGroup linkListIndependentFromEntered = createIndependentCopy(linkList);
		storeLinkList(recordType, recordId, linkListIndependentFromEntered);
		storeLinksInIncomingLinks(linkListIndependentFromEntered);
	}

	private void storeLinkList(String recordType, String recordId,
			DataGroup linkListIndependentFromEntered) {
		linkLists.get(recordType).put(recordId, linkListIndependentFromEntered);
	}

	private void storeLinksInIncomingLinks(DataGroup incomingLinkList) {
		for (DataElement linkElement : incomingLinkList.getChildren()) {
			storeLinkInIncomingLinks((DataGroup) linkElement);
		}
	}

	private void storeLinkInIncomingLinks(DataGroup link) {
		Map<String, Map<String, DataGroup>> toPartOfIncomingLinks = getIncomingLinkStorageForLink(
				link);
		storeLinkInIncomingLinks(link, toPartOfIncomingLinks);
	}

	private Map<String, Map<String, DataGroup>> getIncomingLinkStorageForLink(DataGroup link) {
		DataRecordLink to = (DataRecordLink) link.getFirstChildWithNameInData("to");
		String toType = to.getLinkedRecordType();
		String toId = to.getLinkedRecordId();

		ensureInIncomingLinksHolderForRecordTypeAndRecordId(toType, toId);

		return incomingLinks.get(toType).get(toId);
	}

	private void ensureInIncomingLinksHolderForRecordTypeAndRecordId(String toType, String toId) {
		if (isIncomingLinksHolderForRecordTypeMissing(toType)) {
			incomingLinks.put(toType, new HashMap<>());
		}
		if (isIncomingLinksHolderForRecordIdMissing(toType, toId)) {
			incomingLinks.get(toType).put(toId, new HashMap<>());
		}
	}

	private boolean isIncomingLinksHolderForRecordTypeMissing(String toType) {
		return !incomingLinkStorageForRecordTypeExists(toType);
	}

	private boolean isIncomingLinksHolderForRecordIdMissing(String toType, String toId) {
		return !incomingLinksHolderForRecordIdExists(toType, toId);
	}

	private void storeLinkInIncomingLinks(DataGroup link,
			Map<String, Map<String, DataGroup>> toPartOfIncomingLinks) {
		DataRecordLink from = (DataRecordLink) link.getFirstChildWithNameInData("from");
		String fromType = from.getLinkedRecordType();
		String fromId = from.getLinkedRecordId();

		ensureIncomingLinksHolderExistsForFromRecordType(toPartOfIncomingLinks, fromType);
		toPartOfIncomingLinks.get(fromType).put(fromId, link);
	}

	private void ensureIncomingLinksHolderExistsForFromRecordType(
			Map<String, Map<String, DataGroup>> toPartOfIncomingLinks, String fromType) {
		if (!toPartOfIncomingLinks.containsKey(fromType)) {
			toPartOfIncomingLinks.put(fromType, new HashMap<>());
		}
	}

	@Override
	public Collection<DataGroup> readList(String type) {
		Map<String, DataGroup> typeRecords = records.get(type);
		if (null == typeRecords) {
			throw new RecordNotFoundException("No records exists with recordType: " + type);
		}
		return typeRecords.values();
	}

	@Override
	public DataGroup read(String recordType, String recordId) {
		checkRecordExists(recordType, recordId);
		return records.get(recordType).get(recordId);
	}

	private void checkRecordExists(String recordType, String recordId) {
		if (holderForRecordTypeDoesNotExistInStorage(recordType)) {
			throw new RecordNotFoundException("No records exists with recordType: " + recordType);
		}
		if (null == records.get(recordType).get(recordId)) {
			throw new RecordNotFoundException("No record exists with recordId: " + recordId);
		}
	}

	@Override
	public DataGroup readLinkList(String recordType, String recordId) {
		if (!linkLists.containsKey(recordType)) {
			throw new RecordNotFoundException("No linkList exists with recordType: " + recordType);
		}
		if (!linkLists.get(recordType).containsKey(recordId)) {
			throw new RecordNotFoundException("No linkList exists with recordId: " + recordId);
		}
		return linkLists.get(recordType).get(recordId);
	}

	@Override
	public void deleteByTypeAndId(String recordType, String recordId) {
		checkRecordExists(recordType, recordId);
		removeIncomingLinks(recordType, recordId);
		linkLists.get(recordType).remove(recordId);
		if (linkLists.get(recordType).isEmpty()) {
			linkLists.remove(recordType);
		}
		records.get(recordType).remove(recordId);
		if (records.get(recordType).isEmpty()) {
			records.remove(recordType);
		}

	}

	@Override
	public DataGroup generateLinkCollectionPointingToRecord(String type, String id) {
		if (linksExistForRecord(type, id)) {
			return generateLinkCollectionFromStoredLinks(type, id);
		}
		return DataGroup.withNameInData("incomingRecordLinks");
	}

	private DataGroup generateLinkCollectionFromStoredLinks(String type, String id) {
		DataGroup generatedLinkList = DataGroup.withNameInData("incomingRecordLinks");
		Map<String, Map<String, DataGroup>> linkStorageForRecord = incomingLinks.get(type).get(id);
		addLinksForRecordFromAllRecordTypes(generatedLinkList, linkStorageForRecord);
		return generatedLinkList;
	}

	private void addLinksForRecordFromAllRecordTypes(DataGroup generatedLinkList,
			Map<String, Map<String, DataGroup>> linkStorageForRecord) {
		for (Map<String, DataGroup> mapOfId : linkStorageForRecord.values()) {
			addLinksForRecordForThisRecordType(generatedLinkList, mapOfId);
		}
	}

	private void addLinksForRecordForThisRecordType(DataGroup generatedLinkList,
			Map<String, DataGroup> mapOfId) {
		for (DataGroup dataGroup : mapOfId.values()) {
			generatedLinkList.addChild(dataGroup);
		}
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		return incomingLinkStorageForRecordTypeExists(type)
				&& incomingLinksHolderForRecordIdExists(type, id);
	}

	private boolean incomingLinksHolderForRecordIdExists(String type, String id) {
		return incomingLinks.get(type).containsKey(id);
	}

	private boolean incomingLinkStorageForRecordTypeExists(String type) {
		return incomingLinks.containsKey(type);
	}

	@Override
	public void update(String recordType, String recordId, DataGroup record, DataGroup linkList) {
		checkRecordExists(recordType, recordId);
		removeIncomingLinks(recordType, recordId);
		storeIndependentRecordByRecordTypeAndRecordId(recordType, recordId, record);
		storeLinks(recordType, recordId, linkList);
	}

	private void removeIncomingLinks(String recordType, String recordId) {
		DataGroup oldLinkList = readLinkList(recordType, recordId);
		for (DataElement linkElement : oldLinkList.getChildren()) {
			removeIncomingLink(linkElement);
		}
	}

	private void removeIncomingLink(DataElement linkElement) {
		DataGroup link = (DataGroup) linkElement;
		DataRecordLink recordLinkTo = (DataRecordLink) link.getFirstChildWithNameInData("to");

		Map<String, Map<String, DataGroup>> toPartOfIncomingLinks = extractToPartOfIncomingLinks(
				recordLinkTo);

		removeLinkAndFromHolderFromIncomingLinks(link, toPartOfIncomingLinks);

		removeToHolderFromIncomingLinks(recordLinkTo, toPartOfIncomingLinks);
	}

	private Map<String, Map<String, DataGroup>> extractToPartOfIncomingLinks(DataRecordLink to) {
		String toType = to.getLinkedRecordType();
		String toId = to.getLinkedRecordId();
		Map<String, Map<String, DataGroup>> toPartOfLinksPointingToRecordStorage = incomingLinks
				.get(toType).get(toId);
		return toPartOfLinksPointingToRecordStorage;
	}

	private void removeLinkAndFromHolderFromIncomingLinks(DataGroup link,
			Map<String, Map<String, DataGroup>> linksForToPart) {
		DataRecordLink from = (DataRecordLink) link.getFirstChildWithNameInData("from");
		String fromType = from.getLinkedRecordType();
		String fromId = from.getLinkedRecordId();

		linksForToPart.get(fromType).remove(fromId);

		if (linksForToPart.get(fromType).isEmpty()) {
			linksForToPart.remove(fromType);
		}
	}

	private void removeToHolderFromIncomingLinks(DataRecordLink to,
			Map<String, Map<String, DataGroup>> toPartOfIncomingLinks) {
		String toType = to.getLinkedRecordType();
		String toId = to.getLinkedRecordId();
		if (toPartOfIncomingLinks.isEmpty()) {
			incomingLinks.get(toType).remove(toId);
		}
		if (incomingLinks.get(toType).isEmpty()) {
			incomingLinks.remove(toType);
		}
	}

	@Override
	public Collection<DataGroup> getMetadataElements() {
		Collection<DataGroup> readDataGroups = new ArrayList<>();
		for (MetadataTypes metadataType : MetadataTypes.values()) {
			readDataGroups.addAll(readList(metadataType.type));
		}
		return readDataGroups;
	}

	@Override
	public Collection<DataGroup> getPresentationElements() {
		return readList("presentation");
	}

	@Override
	public Collection<DataGroup> getTexts() {
		return readList("text");
	}

	@Override
	public Collection<DataGroup> getRecordTypes() {
		return readList("recordType");
	}

}
