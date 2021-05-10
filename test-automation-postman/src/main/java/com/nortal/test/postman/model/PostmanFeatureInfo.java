package com.nortal.test.postman.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO replace me
 */
@EqualsAndHashCode
@Data
@Builder
public class PostmanFeatureInfo {
	private String featureId;
	private String featureTitle;
	private String featureDescription;

	private Map<String, String> scenarioDescription;

	public String getFormattedFeatureDescription(){
		if(featureDescription !=null){
			return featureDescription.replace("\n","\\\n");
		}
		return null;
	}
}

