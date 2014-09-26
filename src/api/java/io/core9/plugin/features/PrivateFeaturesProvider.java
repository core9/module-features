package io.core9.plugin.features;

public interface PrivateFeaturesProvider extends FeaturesProvider {

	/**
	 * Return the username of the private repository
	 * @return
	 */
	String getUsername();
	
	/**
	 * Return the password of the private repository
	 * @return
	 */
	String getPassword();
}
