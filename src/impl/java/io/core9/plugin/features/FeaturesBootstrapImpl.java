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
	private PrivateFeaturesProvider[] privateProviders;
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
		List<PrivateFeatureRepository> repoList = privateRepo.getAll(vhost);
		Map<String, String> pathsToRepositories = getPathsToRepositories(privateProviders, repoList);
		Map<FeaturesProvider, String> providersToRepositories = new HashMap<FeaturesProvider, String>();
		for(FeaturesProvider provider : this.privateProviders) {
			PrivateFeaturesProvider privateProvider = (PrivateFeaturesProvider) provider;
			if(pathsToRepositories.get(provider.getRepositoryPath()) == null) {
				PrivateFeatureRepository newConf = new PrivateFeatureRepository();
				newConf.setPath(provider.getRepositoryPath());
				newConf.setUser(privateProvider.getUsername());
				newConf.setPassword(privateProvider.getPassword());
				privateRepo.create(vhost, newConf);
				repoList.add(newConf);
				pathsToRepositories.put(provider.getRepositoryPath(), newConf.getId());
			}
			providersToRepositories.put(provider, pathsToRepositories.get(provider.getRepositoryPath()));
		}
		for(PrivateFeatureRepository repo : privateRepo.getAll(vhost)) {
			initializeRepository(repo);
		}
		handleFeatureProviders(vhost, providersToRepositories);
	}
	
	/**
	 * Create and initialize a public repository
	 * @param vhost
	 */
	private void handlePublicRepositories(VirtualHost vhost) {
		List<PublicFeatureRepository> repoList = publicRepo.getAll(vhost);
		Map<String, String> pathsToRepositories = getPathsToRepositories(providers, repoList);
		Map<FeaturesProvider, String> providersToRepositories = new HashMap<FeaturesProvider, String>();
		for(FeaturesProvider provider : this.providers) {
			if(pathsToRepositories.get(provider.getRepositoryPath()) == null) {
				PublicFeatureRepository newConf = new PublicFeatureRepository();
				newConf.setPath(provider.getRepositoryPath());
				newConf.setUser(null);
				newConf.setPassword(null);
				publicRepo.create(vhost, newConf);
				repoList.add(newConf);
				pathsToRepositories.put(provider.getRepositoryPath(), newConf.getId());
			}
			providersToRepositories.put(provider, pathsToRepositories.get(provider.getRepositoryPath()));
		}
		for(PublicFeatureRepository repoConf : repoList) {
			initializeRepository(repoConf);
		}
		handleFeatureProviders(vhost, providersToRepositories);
	}
	
	private Map<String, String> getPathsToRepositories(FeaturesProvider[] providers, List<? extends FeatureRepository> repos) {
		Map<String, String> providersToRepositories = new HashMap<String, String>();
		for(FeaturesProvider provider : providers) {
			boolean available = false;
			for(FeatureRepository repoConf : repos) {
				available = repoConf.getPath().equals(provider.getRepositoryPath());
				if(available) {
					providersToRepositories.put(provider.getRepositoryPath(), repoConf.getId());
				}
			}
		}
		return providersToRepositories;
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
		List<PrivateFeaturesProvider> privateProviders = new ArrayList<PrivateFeaturesProvider>();
		for(Plugin plugin: this.registry.getPlugins()) {
			List<Class<?>> interfaces = ClassUtils.getAllInterfaces(plugin.getClass());
			if (interfaces.contains(FeaturesProvider.class)) {
				if(interfaces.contains(PrivateFeaturesProvider.class)) {
					privateProviders.add((PrivateFeaturesProvider) plugin);
				} else {
					providers.add((FeaturesProvider) plugin);
				}
			}
			if (interfaces.contains(FeaturesProcessor.class)) {
				features.addFeatureProcessor((FeaturesProcessor) plugin);
			}
		}
		this.providers = providers.toArray(new FeaturesProvider[providers.size()]);
		this.privateProviders = privateProviders.toArray(new PrivateFeaturesProvider[privateProviders.size()]);
		for(VirtualHost vhost : this.vhosts) {
			handlePrivateRepositories(vhost);
			handlePublicRepositories(vhost);
		}
		this.vhosts = null;
	}

	@Override
	public void addVirtualHost(VirtualHost vhost) {
		// Run featurerepo bootstrap if providers are available, else build cache to run on bootstrap (processPlugins())
		if(this.providers != null || this.privateProviders != null) {
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
