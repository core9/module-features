package io.core9.plugin.features.entity;

import java.util.HashMap;
import java.util.Map;

import io.core9.plugin.database.repository.Collection;

@Collection("configuration")
public class PrivateFeatureRepository extends FeatureRepository {
	private static final HashMap<String, Object> defaultQuery = new HashMap<String,Object>() {
		private static final long serialVersionUID = 7894958903345882391L;
		{
			defaultQuery.put("configtype", "featuresrepo_private");
		}
	};

	@Override
	public Map<String,Object> retrieveDefaultQuery() {
		return defaultQuery;
	}
}
