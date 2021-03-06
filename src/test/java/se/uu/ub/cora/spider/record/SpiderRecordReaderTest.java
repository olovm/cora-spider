/*
 * Copyright 2015, 2016, 2017, 2019 Uppsala University Library
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.termcollector.DataGroupTermCollector;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.data.copier.DataCopierProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.authentication.AuthenticationException;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.spider.authentication.AuthenticatorSpy;
import se.uu.ub.cora.spider.authorization.AlwaysAuthorisedExceptStub;
import se.uu.ub.cora.spider.authorization.AuthorizationException;
import se.uu.ub.cora.spider.authorization.NeverAuthorisedStub;
import se.uu.ub.cora.spider.authorization.PermissionRuleCalculator;
import se.uu.ub.cora.spider.authorization.SpiderAuthorizator;
import se.uu.ub.cora.spider.data.DataAtomicFactorySpy;
import se.uu.ub.cora.spider.data.DataGroupFactorySpy;
import se.uu.ub.cora.spider.dependency.MetadataStorageProviderSpy;
import se.uu.ub.cora.spider.dependency.RecordStorageProviderSpy;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProviderSpy;
import se.uu.ub.cora.spider.log.LoggerFactorySpy;
import se.uu.ub.cora.spider.spy.AuthorizatorAlwaysAuthorizedSpy;
import se.uu.ub.cora.spider.spy.DataGroupTermCollectorSpy;
import se.uu.ub.cora.spider.spy.NoRulesCalculatorStub;
import se.uu.ub.cora.spider.spy.RecordStorageSpy;
import se.uu.ub.cora.spider.testdata.TestDataRecordInMemoryStorage;
import se.uu.ub.cora.storage.MetadataStorage;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

public class SpiderRecordReaderTest {
	private RecordStorage recordStorage;
	private Authenticator authenticator;
	private SpiderAuthorizator authorizator;
	private PermissionRuleCalculator keyCalculator;
	private SpiderDependencyProviderSpy dependencyProvider;
	private SpiderRecordReader recordReader;
	private DataGroupToRecordEnhancerSpy dataGroupToRecordEnhancer;
	private DataGroupTermCollector termCollector;
	private LoggerFactorySpy loggerFactorySpy;
	private DataGroupFactory dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;
	private DataCopierFactorySpy dataCopierFactory;

	@BeforeMethod
	public void beforeMethod() {
		setUpFactoriesAndProviders();
		authenticator = new AuthenticatorSpy();
		authorizator = new AuthorizatorAlwaysAuthorizedSpy();
		recordStorage = TestDataRecordInMemoryStorage.createRecordStorageInMemoryWithTestData();
		keyCalculator = new NoRulesCalculatorStub();
		termCollector = new DataGroupTermCollectorSpy();
		setUpDependencyProvider();
	}

	private void setUpFactoriesAndProviders() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		dataCopierFactory = new DataCopierFactorySpy();
		DataCopierProvider.setDataCopierFactory(dataCopierFactory);
	}

	private void setUpDependencyProvider() {
		dependencyProvider = new SpiderDependencyProviderSpy(new HashMap<>());
		dependencyProvider.authenticator = authenticator;
		dependencyProvider.spiderAuthorizator = authorizator;

		RecordStorageProviderSpy recordStorageProviderSpy = new RecordStorageProviderSpy();
		recordStorageProviderSpy.recordStorage = recordStorage;
		dependencyProvider.setRecordStorageProvider(recordStorageProviderSpy);

		MetadataStorageProviderSpy metadataStorageProviderSpy = new MetadataStorageProviderSpy();
		metadataStorageProviderSpy.metadataStorage = (MetadataStorage) recordStorage;
		dependencyProvider.setMetadataStorageProvider(metadataStorageProviderSpy);

		dependencyProvider.ruleCalculator = keyCalculator;
		dataGroupToRecordEnhancer = new DataGroupToRecordEnhancerSpy();
		dependencyProvider.searchTermCollector = termCollector;

		recordReader = SpiderRecordReaderImp.usingDependencyProviderAndDataGroupToRecordEnhancer(
				dependencyProvider, dataGroupToRecordEnhancer);
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testAuthenticationNotAuthenticated() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		recordReader.readRecord("dummyNonAuthenticatedToken", "spyType", "spyId");
	}

	@Test
	public void testRecordEnhancerCalled() {
		recordReader.readRecord("someToken78678567", "place", "place:0001");
		assertEquals(dataGroupToRecordEnhancer.user.id, "12345");
		assertEquals(dataGroupToRecordEnhancer.recordType, "place");
		assertEquals(dataGroupToRecordEnhancer.dataGroup.getFirstGroupWithNameInData("recordInfo")
				.getFirstAtomicValueWithNameInData("id"), "place:0001");
	}

	@Test
	public void testReadAuthorized() {
		DataRecord record = recordReader.readRecord("someToken78678567", "place", "place:0001");
		DataGroup groupOut = record.getDataGroup();
		assertEquals(groupOut.getNameInData(), "authority",
				"recordOut.getNameInData should be authority");
	}

	@Test
	public void testReadAuthorized2() {
		DataRecord record = recordReader.readRecord("someToken78678567", "place", "place:0001");
		DataGroup groupOut = record.getDataGroup();
		assertEquals(groupOut.getNameInData(), "authority");

		AuthorizatorAlwaysAuthorizedSpy authorizatorSpy = ((AuthorizatorAlwaysAuthorizedSpy) authorizator);
		assertEquals(authorizatorSpy.actions.get(0), "read");
		assertEquals(authorizatorSpy.users.get(0).id, "12345");
		assertEquals(authorizatorSpy.recordTypes.get(0), "place");

		DataGroupTermCollectorSpy dataGroupTermCollectorSpy = (DataGroupTermCollectorSpy) termCollector;
		assertEquals(dataGroupTermCollectorSpy.metadataId, "place");
		assertEquals(dataGroupTermCollectorSpy.dataGroup,
				recordStorage.read("place", "place:0001"));

		assertEquals(authorizatorSpy.calledMethods.get(0),
				"checkUserIsAuthorizedForActionOnRecordTypeAndCollectedData");
		DataGroup returnedCollectedTerms = dataGroupTermCollectorSpy.collectedTerms;
		assertEquals(authorizatorSpy.collectedTerms.get(0), returnedCollectedTerms);
	}

	@Test
	public void testReadRecordAbstractRecordType() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		DataRecord readRecord = recordReader.readRecord("someToken78678567", "abstractAuthority",
				"place:0001");
		assertNotNull(readRecord);

		AuthorizatorAlwaysAuthorizedSpy authorizatorSpy = ((AuthorizatorAlwaysAuthorizedSpy) authorizator);
		assertEquals(authorizatorSpy.actions.get(0), "read");
		assertEquals(authorizatorSpy.users.get(0).id, "12345");
		assertEquals(authorizatorSpy.recordTypes.get(0), "abstractAuthority");

		DataGroupTermCollectorSpy dataGroupTermCollectorSpy = (DataGroupTermCollectorSpy) termCollector;
		assertEquals(dataGroupTermCollectorSpy.metadataId, "place");
		assertEquals(dataGroupTermCollectorSpy.dataGroup,
				recordStorage.read("abstractAuthority", "place:0001"));

		assertEquals(authorizatorSpy.calledMethods.get(0),
				"checkUserIsAuthorizedForActionOnRecordTypeAndCollectedData");
		DataGroup returnedCollectedTerms = dataGroupTermCollectorSpy.collectedTerms;
		assertEquals(authorizatorSpy.collectedTerms.get(0), returnedCollectedTerms);
	}

	@Test
	public void testUnauthorizedForRecordTypeShouldNeverReadRecordFromStorage() {
		recordStorage = new RecordStorageSpy();
		authorizator = new NeverAuthorisedStub();
		setUpDependencyProvider();

		boolean exceptionWasCaught = false;
		try {
			recordReader.readRecord("unauthorizedUserId", "place", "place:0001");
		} catch (Exception e) {
			assertEquals(e.getClass(), AuthorizationException.class);
			exceptionWasCaught = true;
		}
		assertTrue(exceptionWasCaught);

		assertOnlyReadForRecordTypeHandler();
	}

	private void assertOnlyReadForRecordTypeHandler() {
		assertEquals(((RecordStorageSpy) recordStorage).type, "recordType");
		assertEquals(((RecordStorageSpy) recordStorage).numOfTimesReadWasCalled, 1);
	}

	@Test
	public void testUnauthorizedForRulesAgainsRecord() {
		recordStorage = new RecordStorageSpy();
		authorizator = new AuthorizatorNotAuthorizedRequiredRulesButForActionOnRecordType();
		setUpDependencyProvider();

		boolean exceptionWasCaught = false;
		try {
			recordReader.readRecord("unauthorizedUserId", "place", "place:0001");
		} catch (Exception e) {
			assertEquals(e.getClass(), AuthorizationException.class);
			exceptionWasCaught = true;
		}
		assertTrue(exceptionWasCaught);
		assertEquals(((RecordStorageSpy) recordStorage).readWasCalled, true);
	}

	@Test(expectedExceptions = RecordNotFoundException.class)
	public void testReadingDataForANonExistingRecordType() {
		recordReader.readRecord("someToken78678567", "nonExistingRecordType", "anId");
	}

	@Test
	public void testReadNotAuthorizedToReadRecordTypeButPublicRecordType() throws Exception {
		recordStorage = new RecordStorageSpy();
		authorizator = new AlwaysAuthorisedExceptStub();
		AlwaysAuthorisedExceptStub authorisedExceptStub = (AlwaysAuthorisedExceptStub) authorizator;
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.add("read");

		authorisedExceptStub.notAuthorizedForRecordTypeAndActions.put("publicReadType", hashSet);
		setUpDependencyProvider();
		// publicReadType
		DataRecord readRecord = recordReader.readRecord("unauthorizedUserId", "publicReadType",
				"publicReadType:0001");
		assertNotNull(readRecord);
	}

	@Test
	public void testReadPublicRecordType() throws Exception {
		recordStorage = new RecordStorageSpy();
		authorizator = new AuthorizatorNotAuthorizedRequiredRulesButForActionOnRecordType();
		setUpDependencyProvider();
		// publicReadType
		DataRecord readRecord = recordReader.readRecord("unauthorizedUserId", "publicReadType",
				"publicReadType:0001");

		assertNotNull(readRecord);
	}
}
