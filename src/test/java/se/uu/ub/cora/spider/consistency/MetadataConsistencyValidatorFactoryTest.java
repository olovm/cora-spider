/*
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

package se.uu.ub.cora.spider.consistency;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import se.uu.ub.cora.spider.consistency.MetadataConsistencyGroupAndCollectionValidatorImp;
import se.uu.ub.cora.spider.consistency.MetadataConsistencyValidator;
import se.uu.ub.cora.spider.consistency.MetadataConsistencyValidatorFactory;
import se.uu.ub.cora.spider.record.storage.RecordStorage;
import se.uu.ub.cora.spider.spy.RecordStorageSpy;

public class MetadataConsistencyValidatorFactoryTest {
	@Test
	public void test() {
		RecordStorage recordStorage = new RecordStorageSpy();
		MetadataConsistencyValidatorFactory m = MetadataConsistencyValidatorFactory
				.usingRecordStorage(recordStorage);
		String recordType = "metadataGroup";
		MetadataConsistencyValidator metadataConsistencyValidator = m.factor(recordType);
		assertTrue(metadataConsistencyValidator instanceof MetadataConsistencyGroupAndCollectionValidatorImp);
	}
}
