package mdt.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MDTInstancePayload {
	@JsonProperty("id") private String id;
	@JsonProperty("aasId") private String aasId;
	@JsonProperty("aasIdShort") private String aasIdShort;
}
