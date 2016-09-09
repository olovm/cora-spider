/*
 * Copyright 2016 Olov McKie
 * Copyright 2016 Uppsala University Library
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.beefeater.Authorizator;
import se.uu.ub.cora.beefeater.AuthorizatorImp;
import se.uu.ub.cora.spider.data.DataMissingException;
import se.uu.ub.cora.spider.dependency.SpiderDependencyProviderSpy;
import se.uu.ub.cora.spider.dependency.SpiderInstanceProvider;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.spider.record.storage.RecordStorage;
import se.uu.ub.cora.spider.spy.AuthorizatorAlwaysAuthorizedSpy;
import se.uu.ub.cora.spider.spy.RecordPermissionKeyCalculatorStub;
import se.uu.ub.cora.spider.spy.RecordStorageSpy;
import se.uu.ub.cora.spider.testdata.TestDataRecordInMemoryStorage;

public class SpiderDownloaderTest {
	private RecordStorage recordStorage;
	private StreamStorageSpy streamStorage;
	private Authorizator authorizator;
	private RecordPermissionKeyCalculatorStub keyCalculator;
	private SpiderDownloader downloader;
	private SpiderDependencyProviderSpy dependencyProvider;

	@BeforeMethod
	public void beforeMethod() {
		authorizator = new AuthorizatorImp();
		keyCalculator = new RecordPermissionKeyCalculatorStub();
		recordStorage = TestDataRecordInMemoryStorage.createRecordStorageInMemoryWithTestData();
		streamStorage = new StreamStorageSpy();

		setUpDependencyProvider();

	}

	private void setUpDependencyProvider() {
		dependencyProvider = new SpiderDependencyProviderSpy();
		dependencyProvider.authorizator = authorizator;
		dependencyProvider.keyCalculator = keyCalculator;
		dependencyProvider.recordStorage = recordStorage;
		dependencyProvider.streamStorage = streamStorage;
		SpiderInstanceProvider.setSpiderDependencyProvider(dependencyProvider);
		downloader = SpiderDownloaderImp.usingDependencyProvider(dependencyProvider);
	}

	@Test
	public void testInit() {
		assertNotNull(downloader);
	}

	// @Test
	public void testExternalDependenciesAreCalled() {
		authorizator = new AuthorizatorAlwaysAuthorizedSpy();
		recordStorage = new RecordStorageSpy();
		setUpDependencyProvider();

		downloader.download("userId", "image", "image:123456789", "master");

		assertTrue(((RecordStorageSpy) recordStorage).readWasCalled);

		assertTrue(((AuthorizatorAlwaysAuthorizedSpy) authorizator).authorizedWasCalled);
	}

	// @Test
	public void testDownloadStream() {
		InputStream stream = new ByteArrayInputStream("a string".getBytes(StandardCharsets.UTF_8));
		streamStorage.stream = stream;

		OutputStream outputStream = downloader.download("userId", "image", "image:123456789",
				"master");

		assertEquals(outputStream, stream);

		// SpiderDataGroup groupUpdated = recordUpdated.getSpiderDataGroup();
		// SpiderDataGroup resourceInfo = groupUpdated.extractGroup("resourceInfo");
		// SpiderDataGroup master = resourceInfo.extractGroup("master");
		//
		// String streamId = master.extractAtomicValue("streamId");
		// assertEquals(streamId, streamStorage.streamId);
		//
		// String size = master.extractAtomicValue("size");
		// assertEquals(size, String.valueOf(streamStorage.size));
		//
		// String fileName = master.extractAtomicValue("fileName");
		// assertEquals(fileName, "someFileName");

	}

	@Test(expectedExceptions = DataException.class)
	public void testDownloadStreamNotChildOfBinary() {
		downloader.download("userId", "place", "place:0002", "master");
	}

	@Test(expectedExceptions = DataException.class)
	public void testDownloadStreamNotChildOfBinary2() {

		downloader.download("userId", "recordTypeAutoGeneratedId", "someId", "master");
	}

	@Test(expectedExceptions = RecordNotFoundException.class)
	public void testDownloadNotFound() {
		downloader.download("userId", "image", "NOT_FOUND", "master");
	}

	@Test(expectedExceptions = DataMissingException.class)
	public void testDownloadResourceIsMissing() {
		downloader.download("userId", "image", "image:123456789", null);

	}

	@Test(expectedExceptions = DataMissingException.class)
	public void testDownloadResourceIsEmpty() {
		downloader.download("userId", "image", "image:123456789", "");
	}

	@Test(expectedExceptions = RecordNotFoundException.class)
	public void testDownloadResourceDoesNotExistInRecord() {
		// RecordStorageCreateUpdateSpy recordStorageSpy = new RecordStorageCreateUpdateSpy();
		// recordStorage = recordStorageSpy;
		// setUpDependencyProvider();
		downloader.download("userId", "image", "image:123456789", "NonExistingResource");

	}
	//
	// @Test(expectedExceptions = RecordNotFoundException.class)
	// public void testNonExistingRecordType() {
	// InputStream stream = new ByteArrayInputStream("a string".getBytes(StandardCharsets.UTF_8));
	// downloader.download("userId", "recordType_NOT_EXISTING", "id", stream, "someFileName");
	// }
	//
	// @Test(expectedExceptions = AuthorizationException.class)
	// public void testUpdateRecordUserNotAuthorisedToUpdateData() {
	//
	// SpiderDownloader downloader = setupWithUserNotAuthorized();
	// InputStream stream = new ByteArrayInputStream("a string".getBytes(StandardCharsets.UTF_8));
	// downloader.download("userId", "image", "image:123456789", stream, "someFileName");
	// }
	//
	// private SpiderDownloader setupWithUserNotAuthorized() {
	// authorizator = new NeverAuthorisedStub();
	// setUpDependencyProvider();
	//
	// SpiderDownloader downloader =
	// SpiderDownloaderImp.usingDependencyProvider(dependencyProvider);
	// return downloader;
	// }

}
