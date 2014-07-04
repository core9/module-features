package io.core9.plugin.features.processors;

import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.server.VirtualHost;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

@PluginImplementation
public class FeaturesContentProcessorImpl implements FeaturesContentProcessor {
	
	@InjectPlugin
	private MongoDatabase database;
	
	@Override
	public String getFeatureNamespace() {
		return "content";
	}
	
	@Override
	public String getProcessorAdminTemplateName() {
		return "features/processor/content.tpl.html";
	}

	@Override
	public boolean updateFeatureContent(VirtualHost vhost, File repositoryPath, Map<String, Object> feature) {
		String[] id = ((String) feature.get("id")).split("-");
		if(id.length == 2) {
			Map<String,Object> query = new HashMap<>();
			query.put("_id", id[1]);
			Map<String,Object> entry = database.getSingleResult(vhost.getContext("database"), vhost.getContext("prefix") + id[0], query);
			if(entry != null) {
				feature.put("entry", entry);
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> feature) {
		@SuppressWarnings("unchecked")
		Map<String,Object> entry = (Map<String, Object>) feature.get("entry");
		database.upsert(vhost.getContext("database"), (String) vhost.getContext("prefix") + entry.get("contenttype"), entry, entry);
	}

	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		@SuppressWarnings("unchecked")
		Map<String,Object> entry = (Map<String, Object>) item.get("entry");
		Map<String,Object> query = new HashMap<>();
		query.put("contenttype", entry.get("contenttype"));
		query.put("_id", entry.get("_id"));
		database.delete(vhost.getContext("database"), (String) vhost.getContext("prefix") + entry.get("contenttype"), query);
	}
	
}
