package io.core9.plugin.features.entity;

import java.util.HashMap;
import java.util.Map;

import io.core9.plugin.database.repository.Collection;

@Collection("configuration")
public class PrivateFeatureRepository extends FeatureRepository {

	@Override
	public Map<String,Object> retrieveDefaultQuery() {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("configtype", "featuresrepo_private");
		return query;
	}
}
