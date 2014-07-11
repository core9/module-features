package io.core9.plugin.features;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.admin.AdminPlugin;
import io.core9.plugin.server.VirtualHost;

import java.io.IOException;
import java.util.Map;

public interface FeaturesPlugin extends Core9Plugin, AdminPlugin {
	
	/**
	 * Add a FeatureProcessor to the list of processors
	 */
	FeaturesPlugin addFeatureProcessor(FeaturesProcessor processor);
	
	/**
	 * Return the feature processors
	 * @return
	 */
	Map<String,FeaturesProcessor> getFeatureProcessors();

	/**
	 * Bootstrap a feature
	 * @param virtualHost
	 * @param repo
	 * @param featurename
	 * @param version
	 * @throws IOException 
	 */
	void bootstrapFeatureVersion(VirtualHost virtualHost, String repo, String featurename, String version) throws IOException;

	/**
	 * Return the current feature version
	 * @param vhost
	 * @param repo
	 * @param featurename
	 * @return
	 */
	String getFeatureVersion(VirtualHost vhost, String repo, String featurename);

	/**
	 * Bootstrap a feature, check if the current feature is enabled with the check
	 * @param vhost
	 * @param repo
	 * @param featurename
	 * @param version
	 * @param check
	 * @throws IOException
	 */
	void bootstrapFeatureVersion(VirtualHost vhost, String repo, String featurename, String version, boolean check)	throws IOException;
}
