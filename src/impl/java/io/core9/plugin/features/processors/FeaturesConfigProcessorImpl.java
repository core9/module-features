package io.core9.plugin.features.processors;

import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.server.VirtualHost;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

@PluginImplementation
public class FeaturesConfigProcessorImpl implements FeaturesConfigProcessor {
	
	private static final String CONFIGURATION_COLLECTION = "configuration";
	
	@InjectPlugin
	private MongoDatabase database;

	@Override
	public String getFeatureNamespace() {
		return "configuration";
	}

	@Override
	public String getProcessorAdminTemplateName() {
		return "features/processor/config.tpl.html";
	}

	@Override
	public boolean updateFeatureContent(VirtualHost vhost, File repository,	Map<String, Object> item) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", item.get("id"));
		Map<String,Object> entry = database.getSingleResult(vhost.getContext("database"), vhost.getContext("prefix") + CONFIGURATION_COLLECTION, query);
		if(entry != null) {
			item.put("entry", entry);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		Map<String,Object> entry = (Map<String, Object>) item.get("entry");
		if(entry != null) {
			Map<String,Object> query = entry;
			if(entry.get("_id") != null) {
				query = new HashMap<String, Object>();
				query.put("_id", entry.get("_id"));
			}
			database.upsert(vhost.getContext("database"), vhost.getContext("prefix") + CONFIGURATION_COLLECTION, entry, query);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		Map<String,Object> entry = (Map<String, Object>) item.get("entry");
		if(entry != null) {
			Map<String,Object> query = new HashMap<String,Object>();
			query.put("_id", item.get("id"));
			database.delete(vhost.getContext("database"), vhost.getContext("prefix") + CONFIGURATION_COLLECTION, query);
		}
	}

}
