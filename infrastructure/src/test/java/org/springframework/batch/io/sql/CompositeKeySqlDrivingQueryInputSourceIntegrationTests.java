/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.io.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.batch.io.InputSource;
import org.springframework.batch.restart.GenericRestartData;
import org.springframework.batch.restart.RestartData;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Lucas Ward
 *
 */
public class CompositeKeySqlDrivingQueryInputSourceIntegrationTests extends
		AbstractSqlInputSourceIntegrationTests {

	protected InputSource createInputSource() throws Exception {

		CompositeKeySqlDrivingQueryInputSource inputSource =
			new CompositeKeySqlDrivingQueryInputSource(getJdbcTemplate(),
					"SELECT ID, VALUE from T_FOOS order by ID, VALUE",
					new FooCompositeKeyMapper());

		inputSource.setRestartQuery("SELECT ID from T_FOOS where ID > ? and VALUE > ? order by ID");
		inputSource.setRestartDataConverter(new FooRestartDataConverter());
		FooInputSource fooInputSource = new FooInputSource(inputSource, getJdbcTemplate());
		fooInputSource.setFooDao(new CompositeKeyFooDao(getJdbcTemplate()));
		return fooInputSource;
	}

	private static class FooRestartDataConverter implements CompositeKeyRestartDataConverter{

		private static final String ID_RESTART_KEY = "FooRestartDataConverter.id";
		private static final String VALUE_RESTART_KEY = "FooRestartDataConverter.value";

		public RestartData createRestartData(Object compositeKey) {

			List values = (List)compositeKey;
			Properties data = new Properties();
			data.setProperty(ID_RESTART_KEY, values.get(0).toString());
			data.setProperty(VALUE_RESTART_KEY, values.get(1).toString());
			return new GenericRestartData(data);
		}

		public Object[] createArguments(RestartData restartData) {
			Object[] args = new Object[2];
			args[0] = restartData.getProperties().get(ID_RESTART_KEY);
			args[1] = restartData.getProperties().getProperty(VALUE_RESTART_KEY);
			return args;
		}
	}

	private static class FooCompositeKeyMapper implements RowMapper{
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			List key = new ArrayList();
			key.add(new Long(rs.getLong(1)));
			key.add(new Long(rs.getLong(2)));
			return key;
		}
	}
}
