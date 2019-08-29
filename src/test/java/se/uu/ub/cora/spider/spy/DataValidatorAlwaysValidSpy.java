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

import se.uu.ub.cora.bookkeeper.validator.DataValidator;
import se.uu.ub.cora.bookkeeper.validator.ValidationAnswer;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;

public class DataValidatorAlwaysValidSpy implements DataValidator {
	public boolean validateDataWasCalled = false;
	public int numOfCallsToValidate = 0;
	public String metadataId;
	public DataGroup dataGroup;

	@Override
	public ValidationAnswer validateData(String metadataId, DataElement dataGroup) {
		this.metadataId = metadataId;
		this.dataGroup = (DataGroup) dataGroup;
		validateDataWasCalled = true;
		numOfCallsToValidate++;
		return new ValidationAnswer();
	}

}
