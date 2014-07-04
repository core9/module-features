package io.core9.plugin.features.entity;

import io.core9.plugin.database.repository.AbstractCrudEntity;
import io.core9.plugin.database.repository.Collection;
import io.core9.plugin.database.repository.CrudEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Collection("configuration")
public class FeatureRepository extends AbstractCrudEntity implements CrudEntity {
	
	private String configtype;
	private String user;
	private String password;
	private String path;
	private Map<String, String> current;

	public String getConfigtype() {
		return configtype;
	}

	public void setConfigtype(String configtype) {
		this.configtype = configtype;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getCurrent() {
		return current;
	}

	public void setCurrent(Map<String, String> current) {
		this.current = current;
	}
	
	@Override
	public Map<String,Object> retrieveDefaultQuery() {
		Map<String,Object> query = new HashMap<String,Object>();
		List<Map<String,Object>> or = new ArrayList<Map<String,Object>>();
		Map<String,Object> _private = new HashMap<String,Object>();
		_private.put("configtype", "featuresrepo_private");
		Map<String,Object> _public = new HashMap<String,Object>();
		_public.put("configtype", "featuresrepo_public");
		or.add(_private);
		or.add(_public);
		query.put("$or", or);
		return query;
	}

}
