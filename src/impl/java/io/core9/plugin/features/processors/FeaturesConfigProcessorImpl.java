package io.core9.plugin.features.processors;

import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.DataUtils;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.server.VirtualHost;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;

@PluginImplementation
public class FeaturesConfigProcessorImpl implements FeaturesConfigProcessor {
	
	private CrudRepository<ConfigEntity> configRepository;
	
	@PluginLoaded
	public void onRepositoryFactory(RepositoryFactory factory) throws NoCollectionNamePresentException {
		this.configRepository = factory.getRepository(ConfigEntity.class);
	}

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
		ConfigEntity result = configRepository.read(vhost, (String) item.get("id"));
		if(result != null) {
			item.put("entry", DataUtils.toMap(result));
			return true;
		}
		return false;
	}

	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		@SuppressWarnings("unchecked")
		ConfigEntity entity = DataUtils.toObject((Map<String,Object>) item.get("entry"), ConfigEntity.class);
		if(entity != null) {
			configRepository.upsert(vhost, entity);
		}
	}

	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		configRepository.delete(vhost, (String) item.get("id"));
	}

}
