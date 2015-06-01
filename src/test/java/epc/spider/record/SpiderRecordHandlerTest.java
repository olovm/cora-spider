package epc.spider.record;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import epc.beefeater.Authorizator;
import epc.beefeater.AuthorizatorImp;
import epc.metadataformat.validator.DataValidator;
import epc.spider.data.DataMissingException;
import epc.spider.data.SpiderDataAtomic;
import epc.spider.data.SpiderDataGroup;
import epc.spider.data.SpiderDataRecord;
import epc.spider.data.SpiderRecordList;
import epc.spider.record.storage.RecordIdGenerator;
import epc.spider.record.storage.RecordNotFoundException;
import epc.spider.record.storage.RecordStorage;
import epc.spider.record.storage.TimeStampIdGenerator;
import epc.spider.testdata.TestDataRecordInMemoryStorage;

public class SpiderRecordHandlerTest {
	private RecordStorage recordStorage;
	private Authorizator authorization;
	private RecordIdGenerator idGenerator;
	private PermissionKeyCalculator keyCalculator;
	private SpiderRecordHandler recordHandler;
	private DataValidator dataValidator;

	@BeforeMethod
	public void beforeMethod() {
		authorization = new AuthorizatorImp();
		dataValidator = new DataValidatorAlwaysValidSpy();
		recordStorage = TestDataRecordInMemoryStorage.createRecordStorageInMemoryWithTestData();
		idGenerator = new TimeStampIdGenerator();
		keyCalculator = new RecordPermissionKeyCalculator();
		recordHandler = SpiderRecordHandlerImp
				.usingAuthorizationAndDataValidatorAndRecordStorageAndIdGeneratorAndKeyCalculator(
						authorization, dataValidator, recordStorage, idGenerator, keyCalculator);

	}

	@Test
	public void testReadListAuthorized() {
		String userId = "userId";
		String type = "place";
		SpiderRecordList readRecordList = recordHandler.readRecordList(userId, type);
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "1",
				"Total number of records should be 1");
		assertEquals(readRecordList.getFromNo(), "0");
		assertEquals(readRecordList.getToNo(), "1");
		List<SpiderDataRecord> records = readRecordList.getRecords();
		SpiderDataRecord spiderDataRecord = records.iterator().next();
		assertNotNull(spiderDataRecord);
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testReadListUnauthorized() {
		recordHandler.readRecordList("unauthorizedUserId", "place");
	}

	@Test
	public void testReadAuthorized() {
		SpiderDataRecord record = recordHandler.readRecord("userId", "place", "place:0001");
		SpiderDataGroup groupOut = record.getSpiderDataGroup();
		Assert.assertEquals(groupOut.getDataId(), "authority",
				"recordOut.getDataId should be authority");
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testReadUnauthorized() {
		recordHandler.readRecord("unauthorizedUserId", "place", "place:0001");
	}

	@Test(expectedExceptions = DataException.class)
	public void testCreateRecordInvalidData() {
		DataValidator dataValidator = new DataValidatorAlwaysInvalidSpy();
		SpiderRecordHandler recordHandler = SpiderRecordHandlerImp
				.usingAuthorizationAndDataValidatorAndRecordStorageAndIdGeneratorAndKeyCalculator(
						authorization, dataValidator, recordStorage, idGenerator, keyCalculator);
		SpiderDataGroup spiderDataGroup = SpiderDataGroup.withDataId("dataId");
		recordHandler.createAndStoreRecord("userId", "recordType", spiderDataGroup);
	}

	@Test
	public void testCreateRecordAutogeneratedId() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("place");

		SpiderDataRecord recordOut = recordHandler.createAndStoreRecord("userId", "place", record);
		SpiderDataGroup groupOut = recordOut.getSpiderDataGroup();
		SpiderDataGroup recordInfo = groupOut.extractGroup("recordInfo");
		String recordId = recordInfo.extractAtomicValue("id");

		Assert.assertNotNull(recordId, "A new record should have an id");

		Assert.assertEquals(recordInfo.extractAtomicValue("createdBy"), "userId");
		Assert.assertEquals(recordInfo.extractAtomicValue("type"), "place");

		SpiderDataRecord recordRead = recordHandler.readRecord("userId", "place", recordId);
		SpiderDataGroup groupRead = recordRead.getSpiderDataGroup();
		Assert.assertEquals(groupOut.getDataId(), groupRead.getDataId(),
				"Returned and read record should have the same dataId");
	}

	@Test
	public void testCreateRecordUserSuppliedId() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("recordType");
		SpiderDataGroup createRecordInfo = SpiderDataGroup.withDataId("recordInfo");
		createRecordInfo.addChild(SpiderDataAtomic.withDataIdAndValue("id", "place"));
		record.addChild(createRecordInfo);

		SpiderDataRecord recordOut = recordHandler.createAndStoreRecord("userId", "recordType",
				record);
		SpiderDataGroup groupOut = recordOut.getSpiderDataGroup();
		SpiderDataGroup recordInfo = groupOut.extractGroup("recordInfo");
		String recordId = recordInfo.extractAtomicValue("id");
		Assert.assertNotNull(recordId, "A new record should have an id");

		Assert.assertEquals(recordInfo.extractAtomicValue("createdBy"), "userId");
		Assert.assertEquals(recordInfo.extractAtomicValue("type"), "recordType");

		SpiderDataRecord recordRead = recordHandler.readRecord("userId", "recordType", recordId);
		SpiderDataGroup groupRead = recordRead.getSpiderDataGroup();
		Assert.assertEquals(groupOut.getDataId(), groupRead.getDataId(),
				"Returned and read record should have the same dataId");

	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testCreateRecordUnauthorized() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		recordHandler.createAndStoreRecord("unauthorizedUserId", "place", record);
	}

	@Test(expectedExceptions = DataException.class)
	public void testNonExistingRecordType() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		recordHandler.createAndStoreRecord("userId", "recordType_NOT_EXISTING", record);
	}

	@Test
	public void testDeleteAuthorized() {
		RecordStorageDeleteSpy recordStorage = new RecordStorageDeleteSpy();
		recordHandler = SpiderRecordHandlerImp
				.usingAuthorizationAndDataValidatorAndRecordStorageAndIdGeneratorAndKeyCalculator(
						authorization, dataValidator, recordStorage, idGenerator, keyCalculator);
		recordHandler.deleteRecord("userId", "place", "place:0001");
		assertTrue(recordStorage.deleteWasCalled);
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testDeleteUnauthorized() {
		recordHandler.deleteRecord("unauthorizedUserId", "place", "place:0001");
	}

	@Test(expectedExceptions = RecordNotFoundException.class)
	public void testDeleteNotFound() {
		recordHandler.deleteRecord("userId", "place", "place:0001_NOT_FOUND");
	}

	@Test
	public void testUpdateRecord() {
		SpiderDataGroup dataGroup = SpiderDataGroup.withDataId("authority");
		SpiderDataGroup createRecordInfo = SpiderDataGroup.withDataId("recordInfo");
		createRecordInfo.addChild(SpiderDataAtomic.withDataIdAndValue("id", "place"));
		dataGroup.addChild(createRecordInfo);
		SpiderDataRecord dataRecord = recordHandler.createAndStoreRecord("userId", "recordType",
				dataGroup);
		dataGroup = dataRecord.getSpiderDataGroup();
		dataGroup.addChild(SpiderDataAtomic.withDataIdAndValue("atomicId", "atomicValue"));

		SpiderDataGroup recordInfo = (SpiderDataGroup) dataGroup.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();

		SpiderDataRecord recordUpdated = recordHandler.updateRecord("userId", "recordType",
				id.getValue(), dataGroup);
		SpiderDataGroup groupUpdated = recordUpdated.getSpiderDataGroup();
		assertEquals(groupUpdated.extractAtomicValue("atomicId"), "atomicValue");

		SpiderDataRecord recordRead = recordHandler.readRecord("userId", "recordType",
				id.getValue());
		SpiderDataGroup groupRead = recordRead.getSpiderDataGroup();
		assertEquals(groupRead.extractAtomicValue("atomicId"), "atomicValue");

	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testUpdateUnauthorized() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		recordHandler.createAndStoreRecord("userId", "place", record);

		SpiderDataGroup recordInfo = (SpiderDataGroup) record.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();

		record.addChild(SpiderDataAtomic.withDataIdAndValue("atomicId", "atomicValue"));
		recordHandler.updateRecord("unauthorizedUserId", "place", id.getValue(), record);
	}

	@Test(expectedExceptions = RecordNotFoundException.class)
	public void testUpdateNotFound() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		SpiderDataGroup recordInfo = SpiderDataGroup.withDataId("recordInfo");
		SpiderDataAtomic idData = SpiderDataAtomic.withDataIdAndValue("id", "NOT_FOUND");
		recordInfo.addChild(idData);
		recordInfo.addChild(SpiderDataAtomic.withDataIdAndValue("type", "type"));
		recordInfo.addChild(SpiderDataAtomic.withDataIdAndValue("createdBy", "userId"));
		record.addChild(recordInfo);
		recordHandler.updateRecord("userId", "type", "id", record);
	}

	@Test(expectedExceptions = DataMissingException.class)
	public void testUpdateRecordRecordInfoMissing() {
		SpiderDataGroup group = SpiderDataGroup.withDataId("authority");
		SpiderDataRecord record = recordHandler.createAndStoreRecord("userId", "place", group);
		group = record.getSpiderDataGroup();
		SpiderDataGroup recordInfo = (SpiderDataGroup) group.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();
		group.getChildren().clear();
		group.addChild(SpiderDataGroup.withDataId("childGroupId"));
		recordHandler.updateRecord("userId", "recordType", id.getValue(), group);
	}

	@Test(expectedExceptions = DataMissingException.class)
	public void testUpdateRecordRecordInfoContenceMissing() {
		SpiderDataGroup group = SpiderDataGroup.withDataId("authority");
		SpiderDataRecord record = recordHandler.createAndStoreRecord("userId", "place", group);
		group = record.getSpiderDataGroup();
		SpiderDataGroup recordInfo = (SpiderDataGroup) group.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();
		((SpiderDataGroup) group.getChildren().get(0)).getChildren().clear();
		recordHandler.updateRecord("userId", "recordType", id.getValue(), group);
	}

}