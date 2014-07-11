package io.core9.plugin.features;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.admin.AdminPlugin;
import io.core9.plugin.server.VirtualHost;

public interface FeaturesRepositoryPlugin extends Core9Plugin, AdminPlugin {

	/**
	 * Pull a feature repository
	 * @param vhost
	 * @param repoID
	 */
	void pullRepository(VirtualHost vhost, String repoID);

	/**
	 * Push a feature repository
	 * @param virtualHost
	 * @param repoID
	 */
	void pushRepository(VirtualHost virtualHost, String repoID);
	
}
