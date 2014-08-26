package org.safehaus.subutai.product.common.test.unit.mock;


import com.datastax.driver.core.ResultSet;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.dbmanager.DbManager;

import java.util.List;


public class DbManagerMock implements DbManager {

	private boolean deleteInfoResult;

	@Override
	public ResultSet executeQuery(String cql, Object... values) {
		return null;
	}


	@Override
	public boolean executeUpdate(String cql, Object... values) {
		return false;
	}


	@Override
	public boolean saveInfo(String source, String key, Object info) {
		return true;
	}


	@Override
	public boolean saveEnvironmentInfo(final String source, final String key, final Object info) {
		return false;
	}


	@Override
	public <T> T getInfo(String source, String key, Class<T> clazz) {
		return null;
	}


	@Override
	public <T> T getEnvironmentInfo(final String source, final String key, final Class<T> clazz) {
		return null;
	}


	@Override
	public <T> List<T> getInfo(String source, Class<T> clazz) {
		return null;
	}


	@Override
	public <T> List<T> getEnvironmentInfo(final String source, final Class<T> clazz) {
		return null;
	}


	@Override
	public boolean deleteInfo(String source, String key) {
		return deleteInfoResult;
	}


	@Override
	public ResultSet executeQuery2(final String cql, final Object... values) throws DBException {
		return null;
	}


	@Override
	public void executeUpdate2(final String cql, final Object... values) throws DBException {

	}


	@Override
	public void saveInfo2(final String source, final String key, final Object info) throws DBException {

	}


	@Override
	public void deleteInfo2(final String source, final String key) throws DBException {

	}


	public DbManagerMock setDeleteInfoResult(boolean result) {
		deleteInfoResult = result;
		return this;
	}
}
