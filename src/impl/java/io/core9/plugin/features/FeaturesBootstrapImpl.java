package io.core9.plugin.features;

import io.core9.core.boot.CoreBootStrategy;
import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.features.entity.FeatureRepository;
import io.core9.plugin.features.entity.PrivateFeatureRepository;
import io.core9.plugin.features.entity.PublicFeatureRepository;
import io.core9.plugin.git.GitRepository;
import io.core9.plugin.git.GitRepositoryManager;
import io.core9.plugin.server.VirtualHost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.apache.commons.lang3.ClassUtils;

@PluginImplementation
public class FeaturesBootstrapImpl extends CoreBootStrategy implements FeaturesBootstrap {
	
	private CrudRepository<PublicFeatureRepository> publicRepo;
	private CrudRepository<PrivateFeatureRepository> privateRepo;
	private FeaturesProvider[] providers;
	private List<VirtualHost> vhosts = new ArrayList<VirtualHost>();
	
	@InjectPlugin
	private GitRepositoryManager git;
	
	@InjectPlugin
	private FeaturesPlugin features;
	
	@PluginLoaded
	public void onRepositoryFactory(RepositoryFactory factory) throws NoCollectionNamePresentException {
		publicRepo = factory.getRepository(PublicFeatureRepository.class);
		privateRepo = factory.getRepository(PrivateFeatureRepository.class);
	}

	/**
	 * Create and initialize a private repository
	 * @param vhost
	 */
	private void handlePrivateRepositories(VirtualHost vhost) {
		for(PrivateFeatureRepository repo : privateRepo.getAll(vhost)) {
			initializeRepository(repo);
		}
	}
	
	/**
	 * Create and initialize a public repository
	 * @param vhost
	 * @param providers 
	 */
	private void handlePublicRepositories(VirtualHost vhost) {
		List<PublicFeatureRepository> repoList = publicRepo.getAll(vhost);
		Map<FeaturesProvider, String> providersToRepositories = new HashMap<FeaturesProvider, String>();
		for(FeaturesProvider provider : this.providers) {
			boolean available = false;
			for(PublicFeatureRepository repoConf : repoList) {
				available = repoConf.getPath().equals(provider.getRepositoryPath());
				if(available) {
					providersToRepositories.put(provider, repoConf.getId());
					break;
				}
			}
			if(!available) {
				PublicFeatureRepository newConf = new PublicFeatureRepository();
				newConf.setPath(provider.getRepositoryPath());
				newConf.setUser(null);
				newConf.setPassword(null);
				publicRepo.create(vhost, newConf);
				repoList.add(newConf);
				providersToRepositories.put(provider, newConf.getId());
			}
		}
		for(PublicFeatureRepository repoConf : repoList) {
			initializeRepository(repoConf);
		}
		handleFeatureProviders(vhost, providersToRepositories);
	}
	
	/**
	 * Handle the feature providers
	 * @param providersToRepositories
	 */
	private void handleFeatureProviders(VirtualHost vhost, Map<FeaturesProvider, String> providersToRepositories) {
		for(Map.Entry<FeaturesProvider, String> entry: providersToRepositories.entrySet()) {
			try {
				features.bootstrapFeatureVersion(vhost, entry.getValue(), entry.getKey().getFeatureName(), entry.getKey().getFeatureVersion());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initialize a repository
	 * @param repoConf
	 * @return
	 */
	private GitRepository initializeRepository(FeatureRepository featureRepo) {
		GitRepository repo = git.registerRepository(featureRepo.getId());
		repo.setLocalPath(featureRepo.getId());
		repo.setOrigin(featureRepo.getPath());
		repo.setUsername(featureRepo.getUser());
		repo.setPassword(featureRepo.getPassword());
		git.init(repo);
		return repo;
	}

	@Override
	public Integer getPriority() {
		return 1560;
	}

	@Override
	public void processPlugins() {
		List<FeaturesProvider> providers = new ArrayList<FeaturesProvider>();
		for(Plugin plugin: this.registry.getPlugins()) {
			List<Class<?>> interfaces = ClassUtils.getAllInterfaces(plugin.getClass());
			if (interfaces.contains(FeaturesProvider.class)) {
				providers.add((FeaturesProvider) plugin);
			}
			if (interfaces.contains(FeaturesProcessor.class)) {
				features.addFeatureProcessor((FeaturesProcessor) plugin);
			}
		}
		this.providers = providers.toArray(new FeaturesProvider[providers.size()]);
		for(VirtualHost vhost : this.vhosts) {
			handlePrivateRepositories(vhost);
			handlePublicRepositories(vhost);
		}
		this.vhosts = null;
	}

	@Override
	public void addVirtualHost(VirtualHost vhost) {
		// Run featurerepo bootstrap if providers are available, else build cache to run on bootstrap (processPlugins())
		if(this.providers != null) {
			handlePrivateRepositories(vhost);
			handlePublicRepositories(vhost);
		} else {
			vhosts.add(vhost);
		}
	}

	@Override
	public void removeVirtualHost(VirtualHost vhost) {
		//Do nothing
		return;
	}
}
