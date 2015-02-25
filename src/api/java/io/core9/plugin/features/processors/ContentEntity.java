package io.core9.plugin.features.processors;

import io.core9.plugin.database.repository.CrudEntity;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContentEntity extends HashMap<String,Object> implements CrudEntity, Map<String,Object> {

	private static final long serialVersionUID = -4731587048082225446L;
	private String collectionName;

	@Override
	public String getId() {
		String id = (String) this.get("_id");
		if(id  == null) {
			UUID uuid = UUID.randomUUID();
			long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
			this.put("_id", Long.toString(l, Character.MAX_RADIX).toUpperCase());
		}
		return (String) this.get("_id");
	}

	@Override
	public void setId(String id) {
		this.put("_id", id);
	}

	@Override
	public Map<String, Object> retrieveDefaultQuery() {
		return null;
	}

	@Override
	public String retrieveCollectionOverride() {
		return this.collectionName;
	}
	
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

}
