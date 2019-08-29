/*
 * Copyright 2015, 2016, 2018, 2019 Uppsala University Library
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
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.validator.DataValidator;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.spider.authentication.AuthenticationException;
import se.uu.ub.cora.spider.authentication.Authenticator;
import se.uu.ub.cora.spider.authentication.AuthenticatorSpy;
import se.uu.ub.cora.spider.authorization.AlwaysAuthorisedExceptStub;
import se.uu.ub.cora.spider.authorization.AuthorizationException;
import se.uu.ub.cora.spider.authorization.NeverAuthorisedStub;
import se.uu.ub.cora.spider.authorization.PermissionRuleCalculator;
import se.uu.ub.cora.spider.authorization.SpiderAuthorizator;
import se.uu.ub.cora.spider.data.SpiderData;
import se.uu.ub.cora.spider.data.SpiderDataAtomic;
import se.uu.ub.cora.spider.data.SpiderDataGroup;
import se.uu.ub.cora.spider.data.SpiderDataList;
import se.uu.ub.cora.spider.data.SpiderDataRecord;
import se.uu.ub.cora.spider.dependency.RecordStorageProviderSpy;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProviderSpy;
import se.uu.ub.cora.spider.log.LoggerFactorySpy;
import se.uu.ub.cora.spider.spy.AuthorizatorAlwaysAuthorizedSpy;
import se.uu.ub.cora.spider.spy.DataValidatorAlwaysInvalidSpy;
import se.uu.ub.cora.spider.spy.DataValidatorAlwaysValidSpy;
import se.uu.ub.cora.spider.spy.NoRulesCalculatorStub;
import se.uu.ub.cora.spider.spy.RecordStorageSpy;
import se.uu.ub.cora.spider.spy.RuleCalculatorSpy;
import se.uu.ub.cora.spider.testdata.TestDataRecordInMemoryStorage;
import se.uu.ub.cora.storage.RecordStorage;

public class SpiderRecordListReaderTest {

	private RecordStorage recordStorage;
	private Authenticator authenticator;
	private SpiderAuthorizator authorizator;
	private PermissionRuleCalculator keyCalculator;
	private SpiderRecordListReader recordListReader;
	private DataGroupToRecordEnhancerSpy dataGroupToRecordEnhancer;
	private DataValidator dataValidator;
	private SpiderDataGroup emptyFilter;
	private SpiderDataGroup exampleFilter;
	private LoggerFactorySpy loggerFactorySpy;

	private static final String SOME_USER_TOKEN = "someToken78678567";
	private static final String SOME_RECORD_TYPE = "place";

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		emptyFilter = SpiderDataGroup.withNameInData("filter");
		exampleFilter = SpiderDataGroup.withNameInData("filter");
		authenticator = new AuthenticatorSpy();
		authorizator = new AuthorizatorAlwaysAuthorizedSpy();
		recordStorage = TestDataRecordInMemoryStorage.createRecordStorageInMemoryWithTestData();
		keyCalculator = new NoRulesCalculatorStub();
		dataValidator = new DataValidatorAlwaysValidSpy();
		setUpDependencyProvider();
	}

	private void setUpDependencyProvider() {
		SpiderDependencyProviderSpy dependencyProvider = new SpiderDependencyProviderSpy(
				new HashMap<>());
		dependencyProvider.authenticator = authenticator;
		dependencyProvider.spiderAuthorizator = authorizator;

		RecordStorageProviderSpy recordStorageProviderSpy = new RecordStorageProviderSpy();
		recordStorageProviderSpy.recordStorage = recordStorage;
		dependencyProvider.setRecordStorageProvider(recordStorageProviderSpy);

		dependencyProvider.ruleCalculator = keyCalculator;
		dependencyProvider.dataValidator = dataValidator;
		dataGroupToRecordEnhancer = new DataGroupToRecordEnhancerSpy();
		recordListReader = SpiderRecordListReaderImp
				.usingDependencyProviderAndDataGroupToRecordEnhancer(dependencyProvider,
						dataGroupToRecordEnhancer);
	}

	@Test
	public void testExternalDependenciesAreCalled() {
		recordStorage = new RecordStorageSpy();
		keyCalculator = new RuleCalculatorSpy();
		setUpDependencyProvider();

		SpiderDataGroup nonEmptyFilter = createNonEmptyFilter();
		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, nonEmptyFilter);

		AuthorizatorAlwaysAuthorizedSpy authorizatorSpy = (AuthorizatorAlwaysAuthorizedSpy) authorizator;
		assertTrue(authorizatorSpy.authorizedWasCalled);

		DataValidatorAlwaysValidSpy dataValidatorAlwaysValidSpy = (DataValidatorAlwaysValidSpy) dataValidator;
		assertTrue(dataValidatorAlwaysValidSpy.validateDataWasCalled);

		assertDataGroupEquality(dataValidatorAlwaysValidSpy.dataGroup,
				nonEmptyFilter.toDataGroup());

		assertTrue(((RecordStorageSpy) recordStorage).readListWasCalled);
	}

	@Test
	public void testFilterValidationIsCalledCorrectlyWithOtherFilter() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		SpiderDataGroup filter = SpiderDataGroup.withNameInData("filter2");
		SpiderDataGroup part = SpiderDataGroup.withNameInData("part");
		filter.addChild(part);

		SpiderDataGroup nonEmptyFilter = filter;
		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, nonEmptyFilter);

		DataGroup dataGroup = ((DataValidatorAlwaysValidSpy) dataValidator).dataGroup;
		assertDataGroupEquality(dataGroup, nonEmptyFilter.toDataGroup());
	}

	@Test
	public void testFilterValidationIsCalledCorrectlyForStart() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();

		exampleFilter.addChild(SpiderDataAtomic.withNameInDataAndValue("start", "1"));

		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, exampleFilter);

		DataGroup dataGroup = ((DataValidatorAlwaysValidSpy) dataValidator).dataGroup;
		assertDataGroupEquality(dataGroup, exampleFilter.toDataGroup());
	}

	@Test
	public void testFilterValidationIsCalledCorrectlyForRows() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();

		exampleFilter.addChild(SpiderDataAtomic.withNameInDataAndValue("rows", "1"));

		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, exampleFilter);

		DataGroup dataGroup = ((DataValidatorAlwaysValidSpy) dataValidator).dataGroup;
		assertDataGroupEquality(dataGroup, exampleFilter.toDataGroup());
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testAuthenticationNotAuthenticated() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		recordListReader.readRecordList("dummyNonAuthenticatedToken", "spyType", emptyFilter);
	}

	@Test
	public void testReadListAuthorized() {
		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);
		assertEquals(readRecordList.getContainDataOfType(), SOME_RECORD_TYPE);
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "177");
		assertEquals(readRecordList.getFromNo(), "1");
		assertEquals(readRecordList.getToNo(), "5");
		List<SpiderData> records = readRecordList.getDataList();
		SpiderDataRecord spiderDataRecord = (SpiderDataRecord) records.iterator().next();
		assertNotNull(spiderDataRecord);
	}

	@Test
	public void testReadListReturnedNumbersAreFromStorage() {
		recordStorage = new RecordStorageResultListCreatorSpy();
		setUpDependencyProvider();
		RecordStorageResultListCreatorSpy recordStorageSpy = (RecordStorageResultListCreatorSpy) recordStorage;
		recordStorageSpy.start = 3;
		recordStorageSpy.totalNumberOfMatches = 1500;
		List<DataGroup> list = new ArrayList<>();
		list.add(DataGroup.withNameInData("someName"));
		recordStorageSpy.listOfDataGroups = list;

		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);

		assertEquals(readRecordList.getFromNo(), "3");
		assertEquals(readRecordList.getToNo(), "4");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "1500");
	}

	@Test
	public void testReadListReturnedOtherNumbersAreFromStorage() {
		recordStorage = new RecordStorageResultListCreatorSpy();
		setUpDependencyProvider();
		RecordStorageResultListCreatorSpy recordStorageSpy = (RecordStorageResultListCreatorSpy) recordStorage;
		recordStorageSpy.start = 50;
		recordStorageSpy.totalNumberOfMatches = 1300;
		recordStorageSpy.listOfDataGroups = createListOfDummyDataGroups(50);

		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);

		assertEquals(readRecordList.getFromNo(), "50");
		assertEquals(readRecordList.getToNo(), "100");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "1300");
	}

	@Test
	public void testReadListReturnedNoMatches() {
		recordStorage = new RecordStorageResultListCreatorSpy();
		setUpDependencyProvider();
		RecordStorageResultListCreatorSpy recordStorageSpy = (RecordStorageResultListCreatorSpy) recordStorage;
		recordStorageSpy.start = 0;
		recordStorageSpy.totalNumberOfMatches = 0;
		recordStorageSpy.listOfDataGroups = createListOfDummyDataGroups(0);

		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);

		assertEquals(readRecordList.getFromNo(), "0");
		assertEquals(readRecordList.getToNo(), "0");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "0");
	}

	@Test
	public void testReadListReturnedOneMatches() {
		recordStorage = new RecordStorageResultListCreatorSpy();
		setUpDependencyProvider();
		RecordStorageResultListCreatorSpy recordStorageSpy = (RecordStorageResultListCreatorSpy) recordStorage;
		recordStorageSpy.start = 0;
		recordStorageSpy.totalNumberOfMatches = 1;
		recordStorageSpy.listOfDataGroups = createListOfDummyDataGroups(1);

		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);

		assertEquals(readRecordList.getFromNo(), "0");
		assertEquals(readRecordList.getToNo(), "1");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "1");
	}

	@Test
	public void testReadListReturnedNoMatchesButHasMatches() {
		recordStorage = new RecordStorageResultListCreatorSpy();
		setUpDependencyProvider();
		RecordStorageResultListCreatorSpy recordStorageSpy = (RecordStorageResultListCreatorSpy) recordStorage;
		recordStorageSpy.start = 0;
		recordStorageSpy.totalNumberOfMatches = 15;
		recordStorageSpy.listOfDataGroups = createListOfDummyDataGroups(0);

		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);

		assertEquals(readRecordList.getFromNo(), "0");
		assertEquals(readRecordList.getToNo(), "0");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "15");
	}

	@Test
	public void testReadAbstractListReturnedStartIsFromStorage() {
		recordStorage = new RecordStorageResultListCreatorSpy();
		setUpDependencyProvider();
		RecordStorageResultListCreatorSpy recordStorageSpy = (RecordStorageResultListCreatorSpy) recordStorage;
		recordStorageSpy.abstractString = "true";
		recordStorageSpy.start = 3;
		recordStorageSpy.totalNumberOfMatches = 765;
		recordStorageSpy.listOfDataGroups = createListOfDummyDataGroups(3);

		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);

		assertEquals(readRecordList.getFromNo(), "3");
		assertEquals(readRecordList.getToNo(), "6");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "765");
	}

	private List<DataGroup> createListOfDummyDataGroups(int numberOfGroups) {
		List<DataGroup> list = new ArrayList<>();
		for (int i = 0; i < numberOfGroups; i++) {
			list.add(createDataGroupWithRecordInfo());
		}
		return list;
	}

	private DataGroup createDataGroupWithRecordInfo() {
		DataGroup dataGroup = DataGroup.withNameInData("someName");
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		dataGroup.addChild(recordInfo);
		DataGroup typeGroup = DataGroup.withNameInData("type");
		recordInfo.addChild(typeGroup);
		typeGroup.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", "someType"));
		return dataGroup;
	}

	@Test
	public void testReadListFilterIsPassedOnToStorage() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();

		SpiderDataGroup filter = SpiderDataGroup.withNameInData("filter");
		SpiderDataGroup part = SpiderDataGroup.withNameInData("part");
		filter.addChild(part);
		part.addChild(SpiderDataAtomic.withNameInDataAndValue("key", "someKey"));
		part.addChild(SpiderDataAtomic.withNameInDataAndValue("value", "someValue"));

		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, filter);

		DataGroup filterFromStorage = ((RecordStorageSpy) recordStorage).filters.get(0);
		assertEquals(filterFromStorage.getNameInData(), "filter");
		DataGroup extractedPart = filterFromStorage.getFirstGroupWithNameInData("part");
		assertEquals(extractedPart.getFirstAtomicValueWithNameInData("key"), "someKey");
		assertEquals(extractedPart.getFirstAtomicValueWithNameInData("value"), "someValue");
	}

	@Test
	public void testReadListAuthorizedButNoReadLinks() {
		dataGroupToRecordEnhancer.addReadAction = false;
		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN,
				SOME_RECORD_TYPE, emptyFilter);
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "177");
		List<SpiderData> records = readRecordList.getDataList();
		assertEquals(records.size(), 0);
	}

	@Test
	public void testRecordEnhancerCalled() {
		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, emptyFilter);
		assertEquals(dataGroupToRecordEnhancer.user.id, "12345");
		assertEquals(dataGroupToRecordEnhancer.recordType, SOME_RECORD_TYPE);
		assertEquals(dataGroupToRecordEnhancer.dataGroup.getFirstGroupWithNameInData("recordInfo")
				.getFirstAtomicValueWithNameInData("id"), "place:0004");
	}

	@Test
	public void testReadListAbstractRecordType() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		SpiderDataList spiderDataList = recordListReader.readRecordList(SOME_USER_TOKEN, "abstract",
				emptyFilter);
		assertEquals(spiderDataList.getTotalNumberOfTypeInStorage(), "199");

		String type1 = extractTypeFromChildInListUsingIndex(spiderDataList, 0);
		assertEquals(type1, "implementing1");
		String type2 = extractTypeFromChildInListUsingIndex(spiderDataList, 1);
		assertEquals(type2, "implementing2");
	}

	@Test
	public void testRecordEnhancerCalledForAbstractType() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();
		recordListReader.readRecordList(SOME_USER_TOKEN, "abstract", emptyFilter);
		assertEquals(dataGroupToRecordEnhancer.user.id, "12345");
		assertEquals(dataGroupToRecordEnhancer.recordType, "implementing2");
		assertEquals(dataGroupToRecordEnhancer.dataGroup.getFirstGroupWithNameInData("recordInfo")
				.getFirstAtomicValueWithNameInData("id"), "child2_2");
	}

	private String extractTypeFromChildInListUsingIndex(SpiderDataList spiderDataList, int index) {
		SpiderDataRecord spiderData1 = (SpiderDataRecord) spiderDataList.getDataList().get(index);
		SpiderDataGroup spiderDataGroup1 = spiderData1.getSpiderDataGroup();
		SpiderDataGroup recordInfo = spiderDataGroup1.extractGroup("recordInfo");
		SpiderDataGroup typeGroup = recordInfo.extractGroup("type");
		return typeGroup.extractAtomicValue("linkedRecordId");
	}

	@Test
	public void testReadListAbstractRecordTypeNoDataForOneRecordType() {
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();

		SpiderDataList spiderDataList = recordListReader.readRecordList(SOME_USER_TOKEN,
				"abstract2", emptyFilter);
		assertEquals(spiderDataList.getTotalNumberOfTypeInStorage(), "199");

		String type1 = extractTypeFromChildInListUsingIndex(spiderDataList, 0);
		assertEquals(type1, "implementing2");

	}

	@Test(expectedExceptions = AuthorizationException.class)
	public void testReadListUnauthorized() {
		authorizator = new NeverAuthorisedStub();
		setUpDependencyProvider();
		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, emptyFilter);
	}

	@Test(expectedExceptions = DataException.class)
	public void testReadListAuthenticatedAndAuthorizedInvalidData() {
		dataValidator = new DataValidatorAlwaysInvalidSpy();
		setUpDependencyProvider();
		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, createNonEmptyFilter());
	}

	@Test
	public void testReadListCorrectFilterMetadataIsRead() {
		recordListReader.readRecordList(SOME_USER_TOKEN, SOME_RECORD_TYPE, createNonEmptyFilter());

		DataValidatorAlwaysValidSpy dataValidatorSpy = (DataValidatorAlwaysValidSpy) dataValidator;
		assertEquals(dataValidatorSpy.metadataId, "placeFilterGroup");
	}

	@Test(expectedExceptions = DataException.class, expectedExceptionsMessageRegExp = ""
			+ "No filter exists for recordType: image")
	public void testReadListAuthenticatedAndAuthorizedNoFilterMetadataNonEmptyFilter() {
		setUpDependencyProvider();
		SpiderDataGroup filter = createNonEmptyFilter();
		recordListReader.readRecordList(SOME_USER_TOKEN, "image", filter);
	}

	private SpiderDataGroup createNonEmptyFilter() {
		SpiderDataGroup filter = SpiderDataGroup.withNameInData("filter");
		SpiderDataGroup part = SpiderDataGroup.withNameInData("part");
		filter.addChild(part);
		return filter;
	}

	@Test
	public void testReadListAuthenticatedAndAuthorizedNoFilterMetadataEmptyFilter() {
		setUpDependencyProvider();
		SpiderDataList readRecordList = recordListReader.readRecordList(SOME_USER_TOKEN, "image",
				emptyFilter);
		assertEquals(readRecordList.getFromNo(), "1");
		assertEquals(readRecordList.getToNo(), "3");
		assertEquals(readRecordList.getTotalNumberOfTypeInStorage(), "177");
	}

	@Test
	public void testReadListNotAuthorizedButPublicRecordType() {
		recordStorage = new RecordStorageSpy();
		authorizator = new AlwaysAuthorisedExceptStub();
		AlwaysAuthorisedExceptStub authorisedExceptStub = (AlwaysAuthorisedExceptStub) authorizator;
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.add("list");

		authorisedExceptStub.notAuthorizedForRecordTypeAndActions.put("publicReadType", hashSet);
		setUpDependencyProvider();

		recordListReader.readRecordList("unauthorizedUserId", "publicReadType", emptyFilter);
		assertTrue(((RecordStorageSpy) recordStorage).readListWasCalled);
	}

	private void assertDataGroupEquality(DataGroup actual, DataGroup expected) {
		assertEquals(actual.getNameInData(), expected.getNameInData());
		var actualAtomicChildren = actual.getChildren().stream()
				.filter(elem -> elem instanceof DataAtomic).map(elem -> (DataAtomic) elem)
				.collect(Collectors.toList());
		var expectedAtomicChildren = expected.getChildren().stream()
				.filter(elem -> elem instanceof DataAtomic).map(elem -> (DataAtomic) elem)
				.collect(Collectors.toList());
		if (actualAtomicChildren.size() == expectedAtomicChildren.size()) {
			if (!actualAtomicChildren.isEmpty()) {
				for (int idx = 0; idx < actualAtomicChildren.size(); idx++) {
					assertDataAtomicEquality(actualAtomicChildren.get(idx),
							expectedAtomicChildren.get(idx));
				}
			}
		} else {
			fail();
		}

		var actualGroupChildren = actual.getChildren().stream()
				.filter(elem -> elem instanceof DataGroup).map(elem -> (DataGroup) elem)
				.collect(Collectors.toList());
		var expectedGroupChildren = expected.getChildren().stream()
				.filter(elem -> elem instanceof DataGroup).map(elem -> (DataGroup) elem)
				.collect(Collectors.toList());

		if (actualGroupChildren.size() == expectedGroupChildren.size()) {
			if (!actualGroupChildren.isEmpty()) {
				for (int idx = 0; idx < actualAtomicChildren.size(); idx++) {
					assertDataGroupEquality(actualGroupChildren.get(idx),
							expectedGroupChildren.get(idx));
				}
			}
		} else {
			fail();
		}

		assertEquals(actual.getRepeatId(), expected.getRepeatId());
		assertEquals(actual.getAttributes(), expected.getAttributes());
	}

	private void assertDataAtomicEquality(DataAtomic actual, DataAtomic expected) {
		assertEquals(actual.getNameInData(), expected.getNameInData());
		assertEquals(actual.getValue(), expected.getValue());
		assertEquals(actual.getRepeatId(), expected.getRepeatId());
		assertEquals(actual.getAttributes(), expected.getAttributes());
	}
}
