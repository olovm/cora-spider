package epc.spider.record;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import epc.beefeater.Authorizator;
import epc.beefeater.AuthorizatorImp;
import epc.spider.data.DataMissingException;
import epc.spider.data.SpiderDataAtomic;
import epc.spider.data.SpiderDataGroup;
import epc.spider.record.storage.RecordIdGenerator;
import epc.spider.record.storage.RecordNotFoundException;
import epc.spider.record.storage.RecordStorage;
import epc.spider.record.storage.TimeStampIdGenerator;
import epc.spider.testdata.TestDataRecordInMemoryStorage;

public class RecordHandlerTest {
	private RecordStorage recordStorage;
	private Authorizator authorization;
	private RecordIdGenerator idGenerator;
	private PermissionKeyCalculator keyCalculator;
	private SpiderRecordHandler recordHandler;

	@BeforeMethod
	public void beforeMethod() {
		recordStorage = TestDataRecordInMemoryStorage.createRecordStorageInMemoryWithTestData();
		authorization = new AuthorizatorImp();
		idGenerator = new TimeStampIdGenerator();
		keyCalculator = new RecordPermissionKeyCalculator();
		recordHandler = SpiderRecordHandlerImp
				.usingAuthorizationAndRecordStorageAndIdGeneratorAndKeyCalculator(authorization,
						recordStorage, idGenerator, keyCalculator);

	}

	@Test
	public void testReadAuthorized() {
		SpiderDataGroup record = recordHandler.readRecord("userId", "place", "place:0001");
		Assert.assertEquals(record.getDataId(), "authority",
				"recordOut.getDataId should be authority");
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testReadUnauthorized() {
		recordHandler.readRecord("unauthorizedUserId", "place", "place:0001");
	}

	@Test
	public void testCreateRecord() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");

		SpiderDataGroup recordOut = recordHandler.createAndStoreRecord("userId", "type", record);

		SpiderDataGroup recordInfo = (SpiderDataGroup) recordOut.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic recordId = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();

		Assert.assertNotNull(recordId.getValue(), "A new record should have an id");

		SpiderDataAtomic createdBy = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("createdBy")).findFirst().get();
		Assert.assertEquals(createdBy.getValue(), "userId");
		SpiderDataAtomic recordType = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("type")).findFirst().get();
		Assert.assertEquals(recordType.getValue(), "type");

		SpiderDataGroup recordRead = recordHandler
				.readRecord("userId", "type", recordId.getValue());
		Assert.assertEquals(recordOut.getDataId(), recordRead.getDataId(),
				"Returned and read record should have the same dataId");

	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testCreateRecordUnauthorized() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		recordHandler.createAndStoreRecord("unauthorizedUserId", "type", record);
	}

	@Test
	public void testDeleteAuthorized() {
		recordHandler.deleteRecord("userId", "place", "place:0001");

		// TODO: try to read and make sure it is GOOOOOOne
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
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		record = recordHandler.createAndStoreRecord("userId", "type", record);
		record.addChild(SpiderDataAtomic.withDataIdAndValue("atomicId", "atomicValue"));

		SpiderDataGroup recordInfo = (SpiderDataGroup) record.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();

		SpiderDataGroup recordUpdated = recordHandler.updateRecord("userId", "type", id.getValue(),
				record);

		SpiderDataAtomic childOut = (SpiderDataAtomic) recordUpdated.getChildren().get(1);
		assertEquals(childOut.getValue(), "atomicValue");

		SpiderDataGroup recordRead = recordHandler.readRecord("userId", "type", id.getValue());
		SpiderDataAtomic childRead = (SpiderDataAtomic) recordRead.getChildren().get(1);
		assertEquals(childRead.getValue(), "atomicValue");

	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testUpdateUnauthorized() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		recordHandler.createAndStoreRecord("userId", "type", record);

		SpiderDataGroup recordInfo = (SpiderDataGroup) record.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();

		record.addChild(SpiderDataAtomic.withDataIdAndValue("atomicId", "atomicValue"));
		recordHandler.updateRecord("unauthorizedUserId", "type", id.getValue(), record);
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
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		record = recordHandler.createAndStoreRecord("userId", "type", record);

		SpiderDataGroup recordInfo = (SpiderDataGroup) record.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();
		record.getChildren().clear();
		record.addChild(SpiderDataGroup.withDataId("childGroupId"));
		recordHandler.updateRecord("userId", "type", id.getValue(), record);
	}

	@Test(expectedExceptions = DataMissingException.class)
	public void testUpdateRecordRecordInfoContenceMissing() {
		SpiderDataGroup record = SpiderDataGroup.withDataId("authority");
		record = recordHandler.createAndStoreRecord("userId", "type", record);

		SpiderDataGroup recordInfo = (SpiderDataGroup) record.getChildren().stream()
				.filter(p -> p.getDataId().equals("recordInfo")).findFirst().get();
		SpiderDataAtomic id = (SpiderDataAtomic) recordInfo.getChildren().stream()
				.filter(p -> p.getDataId().equals("id")).findFirst().get();
		((SpiderDataGroup) record.getChildren().get(0)).getChildren().clear();
		recordHandler.updateRecord("userId", "type", id.getValue(), record);
	}

}
