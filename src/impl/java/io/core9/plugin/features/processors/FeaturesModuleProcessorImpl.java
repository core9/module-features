package io.core9.plugin.features.processors;

import io.core9.plugin.server.VirtualHost;

import java.io.File;
import java.util.Map;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

public class FeaturesModuleProcessorImpl implements FeaturesModuleProcessor {

	@Override
	public String getFeatureNamespace() {
		return "module";
	}

	@Override
	public String getProcessorAdminTemplateName() {
		return null;
	}

	@Override
	public boolean updateFeatureContent(VirtualHost vhost, File repository,	Map<String, Object> feature) {
		return false;
	}

	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> feature) {
		
	}

	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {

	}
	
	public static void main(String[] args) {
		GradleConnector conn = GradleConnector.newConnector();
		ProjectConnection project = conn.forProjectDirectory(new File("")).connect();
		
		ModelBuilder<DependencySet> depBuilder = project.model(DependencySet.class);
		depBuilder.setStandardOutput(System.out);
		DependencySet deps = depBuilder.get();
		deps.add(new Dependency() {
			
			@Override
			public String getVersion() {
				return "+";
			}
			
			@Override
			public String getName() {
				return "module-thumbnails-api";
			}
			
			@Override
			public String getGroup() {
				return "io.core9";
			}
			
			@Override
			public Dependency copy() {
				return null;
			}
			
			@Override
			public boolean contentEquals(Dependency dependency) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		//Tasks
		ModelBuilder<GradleProject> builder = project.model(GradleProject.class);
		builder.setStandardOutput(System.out);
		GradleProject mvn = builder.get();
		System.out.println(mvn.getName());
		for(GradleTask task : mvn.getTasks().getAll()) {
			System.out.println(task.getName());
		}
	}

}
