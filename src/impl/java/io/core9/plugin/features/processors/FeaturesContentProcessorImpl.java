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
public class FeaturesContentProcessorImpl implements FeaturesContentProcessor {
	
	private CrudRepository<ContentEntity> contentRepository;
	
	@PluginLoaded
	public void onRepositoryFactory(RepositoryFactory factory) throws NoCollectionNamePresentException {
		this.contentRepository = factory.getRepository(ContentEntity.class, true);
	}
	
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
			ContentEntity entity = contentRepository.read(vhost, id[0], id[1]);
			if(entity != null) {
				feature.put("entry", DataUtils.toMap(entity));
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		@SuppressWarnings("unchecked")
		ContentEntity entity = DataUtils.toObject((Map<String,Object>) item.get("entry"), ContentEntity.class);
		if(entity != null) {
			entity.setCollectionName((String) entity.get("contenttype"));
			contentRepository.upsert(vhost, entity);
		}
	}

	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		@SuppressWarnings("unchecked")
		ContentEntity entity = DataUtils.toObject((Map<String, Object>) item.get("entry"), ContentEntity.class);
		if(entity != null) {
			entity.setCollectionName((String) entity.get("contenttype"));
			contentRepository.delete(vhost, entity);
		}
	}
	
}
