package io.core9.plugin.features.entity;

import java.util.HashMap;
import java.util.Map;

import io.core9.plugin.database.repository.Collection;

@Collection("configuration")
public class PublicFeatureRepository extends FeatureRepository {
	
	public static final String CONFIGTYPE = "featuresrepo_public";
	
	public String getConfigtype() {
		return CONFIGTYPE;
	}
	
	@Override
	public Map<String,Object> retrieveDefaultQuery() {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("configtype", CONFIGTYPE);
		return query;
	}
}
