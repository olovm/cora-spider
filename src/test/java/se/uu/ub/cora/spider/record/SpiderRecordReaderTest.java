/*
 * Copyright 2015, 2016 Uppsala University Library
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

package se.uu.ub.cora.spider.record;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.beefeater.Authorizator;
import se.uu.ub.cora.spider.authentication.AuthenticationException;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.spider.authentication.AuthenticatorSpy;
import se.uu.ub.cora.spider.data.Action;
import se.uu.ub.cora.spider.data.SpiderDataAtomic;
import se.uu.ub.cora.spider.data.SpiderDataGroup;
import se.uu.ub.cora.spider.data.SpiderDataList;
import se.uu.ub.cora.spider.data.SpiderDataRecord;
import se.uu.ub.cora.spider.data.SpiderDataRecordLink;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProviderSpy;
import se.uu.ub.cora.spider.dependency.SpiderInstanceFactory;
import se.uu.ub.cora.spider.dependency.SpiderInstanceFactoryImp;
import se.uu.ub.cora.spider.dependency.SpiderInstanceProvider;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.spider.record.storage.RecordStorage;
import se.uu.ub.cora.spider.spy.AuthorizatorAlwaysAuthorizedSpy;
import se.uu.ub.cora.spider.spy.RecordPermissionKeyCalculatorStub;
import se.uu.ub.cora.spider.spy.RecordStorageSpy;
import se.uu.ub.cora.spider.testdata.TestDataRecordInMemoryStorage;

public class SpiderRecordReaderTest {
	private RecordStorage recordStorage;
	private Authenticator authenticator;
	private Authorizator authorizator;
	private PermissionKeyCalculator keyCalculator;
	private SpiderDependencyProviderSpy dependencyProvider;
	private SpiderRecordReader recordReader;

	@BeforeMethod
	public void beforeMethod() {
		authenticator = new AuthenticatorSpy();
		authorizator = new AuthorizatorAlwaysAuthorizedSpy();
		recordStorage = TestDataRecordInMemoryStorage.createRecordStorageInMemoryWithTestData();
		keyCalculator = new RecordPermissionKeyCalculatorStub();
		setUpDependencyProvider();
	}

	private void setUpDependencyProvider() {
		dependencyProvider = new SpiderDependencyProviderSpy();
		dependencyProvider.authenticator = authenticator;
		dependencyProvider.authorizator = authorizator;
		dependencyProvider.recordStorage = recordStorage;
		dependencyProvider.keyCalculator = keyCalculator;
		SpiderInstanceFactory factory = SpiderInstanceFactoryImp
				.usingDependencyProvider(dependencyProvider);
		SpiderInstanceProvider.setSpiderInstanceFactory(factory);
		recordReader = SpiderRecordReaderImp.usingDependencyProvider(dependencyProvider);
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testAuthenticationNotAuthenticated() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		recordReader.readRecord("dummyNonAuthenticatedToken", "spyType", "spyId");
	}

	@Test
	public void testReadAuthorized() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "place",
				"place:0001");
		SpiderDataGroup groupOut = record.getSpiderDataGroup();
		Assert.assertEquals(groupOut.getNameInData(), "authority",
				"recordOut.getNameInData should be authority");
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testReadUnauthorized() {
		authorizator = new NeverAuthorisedStub();
		setUpDependencyProvider();
		recordReader.readRecord("unauthorizedUserId", "place", "place:0001");
	}

	@Test
	public void testReadIncomingLinks() {
		SpiderDataList linksPointingToRecord = recordReader.readIncomingLinks("someToken78678567",
				"place", "place:0001");
		assertEquals(linksPointingToRecord.getTotalNumberOfTypeInStorage(), "1");
		assertEquals(linksPointingToRecord.getFromNo(), "1");
		assertEquals(linksPointingToRecord.getToNo(), "1");

		SpiderDataGroup link = (SpiderDataGroup) linksPointingToRecord.getDataList().iterator()
				.next();
		assertEquals(link.getNameInData(), "recordToRecordLink");

		SpiderDataRecordLink from = (SpiderDataRecordLink) link.getFirstChildWithNameInData("from");
		SpiderDataAtomic linkedRecordType = (SpiderDataAtomic) from
				.getFirstChildWithNameInData("linkedRecordType");
		SpiderDataAtomic linkedRecordId = (SpiderDataAtomic) from
				.getFirstChildWithNameInData("linkedRecordId");

		assertEquals(linkedRecordType.getValue(), "place");
		assertEquals(linkedRecordId.getValue(), "place:0002");
		assertEquals(from.getActions().size(), 1);
		assertTrue(from.getActions().contains(Action.READ));

		SpiderDataRecordLink to = (SpiderDataRecordLink) link.getFirstChildWithNameInData("to");
		SpiderDataAtomic toLinkedRecordType = (SpiderDataAtomic) to
				.getFirstChildWithNameInData("linkedRecordType");
		SpiderDataAtomic toLinkedRecordId = (SpiderDataAtomic) to
				.getFirstChildWithNameInData("linkedRecordId");

		assertEquals(toLinkedRecordType.getValue(), "place");
		assertEquals(toLinkedRecordId.getValue(), "place:0001");

	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testReadIncomingLinksAuthenticationNotAuthenticated() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		recordReader.readIncomingLinks("dummyNonAuthenticatedToken", "place", "place:0001");
	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testReadIncomingLinksUnauthorized() {
		authorizator = new NeverAuthorisedStub();
		setUpDependencyProvider();
		recordReader.readIncomingLinks("unauthorizedUserId", "place", "place:0001");
	}

	@Test(expectedExceptions = MisuseException.class)
	public void testReadIncomingLinksAbstractType() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();

		recordReader.readIncomingLinks("someToken78678567", "abstract", "place:0001");
	}

	@Test
	public void testActionsOnReadRecordWithIncomingLink() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "place",
				"place:0001");
		assertEquals(record.getActions().size(), 3);
		assertTrue(record.getActions().contains(Action.READ));
		assertTrue(record.getActions().contains(Action.UPDATE));
		assertTrue(record.getActions().contains(Action.READ_INCOMING_LINKS));
		assertFalse(record.getActions().contains(Action.DELETE));
	}

	@Test
	public void testActionsOnReadRecordWithNoIncomingLink() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "place",
				"place:0002");
		assertEquals(record.getActions().size(), 3);
		assertTrue(record.getActions().contains(Action.DELETE));
		assertFalse(record.getActions().contains(Action.READ_INCOMING_LINKS));
	}

	@Test
	public void testActionsOnReadRecordType() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "recordType",
				"recordType");
		assertEquals(record.getActions().size(), 6);
		assertTrue(record.getActions().contains(Action.READ));
		assertTrue(record.getActions().contains(Action.UPDATE));
		assertTrue(record.getActions().contains(Action.DELETE));

		assertTrue(record.getActions().contains(Action.CREATE));
		assertTrue(record.getActions().contains(Action.LIST));
		assertTrue(record.getActions().contains(Action.SEARCH));
	}

	@Test
	public void testActionsOnReadRecordNoIncomingLinks() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "place",
				"place:0002");
		assertEquals(record.getActions().size(), 3);
		assertFalse(record.getActions().contains(Action.READ_INCOMING_LINKS));
	}

	@Test
	public void testActionsOnReadAbstractRecordTypeNoCreate() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "recordType",
				"abstractAuthority");
		assertEquals(record.getActions().size(), 5);
	}

	@Test
	public void testActionsOnReadRecordTypeBinary() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "recordType",
				"binary");
		assertEquals(record.getActions().size(), 5);
		assertTrue(record.getActions().contains(Action.READ));
		assertTrue(record.getActions().contains(Action.UPDATE));
		assertTrue(record.getActions().contains(Action.DELETE));

		assertTrue(record.getActions().contains(Action.LIST));
		assertTrue(record.getActions().contains(Action.SEARCH));
		assertFalse(record.getActions().contains(Action.UPLOAD));

	}

	@Test
	public void testActionsOnReadRecordTypeImage() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "recordType",
				"image");
		assertEquals(record.getActions().size(), 6);
		assertTrue(record.getActions().contains(Action.READ));
		assertTrue(record.getActions().contains(Action.UPDATE));
		assertTrue(record.getActions().contains(Action.DELETE));
		assertTrue(record.getActions().contains(Action.CREATE));

		assertTrue(record.getActions().contains(Action.LIST));
		assertTrue(record.getActions().contains(Action.SEARCH));
		assertFalse(record.getActions().contains(Action.UPLOAD));

	}

	@Test(expectedExceptions = MisuseException.class)
	public void testReadRecordAbstractRecordType() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		recordReader.readRecord("someToken78678567", "abstract", "xxx");
	}

	@Test(expectedExceptions = RecordNotFoundException.class)
	public void testReadingDataForANonExistingRecordType() {
		recordReader.readRecord("someToken78678567", "nonExistingRecordType", "anId");
	}

	@Test
	public void testReadRecordWithDataRecordLinkHasReadActionTopLevel() {
		recordStorage = new RecordLinkTestsRecordStorage();
		setUpDependencyProvider();

		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "dataWithLinks",
				"oneLinkTopLevel");

		RecordLinkTestsAsserter.assertTopLevelLinkContainsReadActionOnly(record);
	}

	@Test
	public void testReadRecordWithDataRecordLinkHasReadActionOneLevelDown() {
		recordStorage = new RecordLinkTestsRecordStorage();
		setUpDependencyProvider();

		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "dataWithLinks",
				"oneLinkOneLevelDown");

		RecordLinkTestsAsserter.assertOneLevelDownLinkContainsReadActionOnly(record);
	}

	@Test
	public void testReadRecordWithDataResourceLinkHasReadActionTopLevel() {
		recordStorage = new RecordLinkTestsRecordStorage();
		setUpDependencyProvider();

		SpiderDataRecord record = recordReader.readRecord("someToken78678567",
				"dataWithResourceLinks", "oneResourceLinkTopLevel");

		RecordLinkTestsAsserter.assertTopLevelResourceLinkContainsReadActionOnly(record);
	}

	@Test
	public void testReadRecordWithDataResourceLinkHasReadActionOneLevelDown() {
		recordStorage = new RecordLinkTestsRecordStorage();
		setUpDependencyProvider();

		SpiderDataRecord record = recordReader.readRecord("someToken78678567",
				"dataWithResourceLinks", "oneResourceLinkOneLevelDown");

		RecordLinkTestsAsserter.assertOneLevelDownResourceLinkContainsReadActionOnly(record);
	}

	@Test
	public void testActionsOnReadImage() {
		SpiderDataRecord record = recordReader.readRecord("someToken78678567", "image",
				"image:0001");
		assertEquals(record.getActions().size(), 4);
		assertTrue(record.getActions().contains(Action.READ));
		assertTrue(record.getActions().contains(Action.UPDATE));
		assertTrue(record.getActions().contains(Action.DELETE));
		assertTrue(record.getActions().contains(Action.UPLOAD));
	}
}
