package vcs.citydb.wfs.config.operation;

import vcs.citydb.wfs.config.operation.OutputFormat.OutputFormatList;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

@XmlType(name="GetFeatureOperationType", propOrder={
		"outputFormats"
})
public class GetFeatureOperation {
	@XmlJavaTypeAdapter(OutputFormatAdapter.class)
	private LinkedHashMap<String, OutputFormat> outputFormats;

	public GetFeatureOperation() {
		outputFormats = new LinkedHashMap<>();
		outputFormats.put(GetFeatureOutputFormat.GML3_1.value(), new OutputFormat(GetFeatureOutputFormat.GML3_1.value()));
	}

	public Collection<OutputFormat> getOutputFormats() {
		return outputFormats.values();
	}

	public Set<String> getOutputFormatsAsString() {
		return outputFormats.keySet();
	}

	public boolean supportsOutputFormat(GetFeatureOutputFormat outputFormat) {
		return outputFormats.containsKey(outputFormat.value());
	}

	public boolean supportsOutputFormat(String outputFormat) {
		GetFeatureOutputFormat candidate = GetFeatureOutputFormat.fromValue(outputFormat);
		return candidate != null && supportsOutputFormat(candidate);
	}

	public OutputFormat getOutputFormat(String outputFormat) {
		return outputFormats.get(outputFormat);
	}

	private final static class OutputFormatAdapter extends XmlAdapter<OutputFormatList, LinkedHashMap<String, OutputFormat>> {

		@Override
		public LinkedHashMap<String, OutputFormat> unmarshal(OutputFormatList v) throws Exception {
			LinkedHashMap<String, OutputFormat> outputFormats = new LinkedHashMap<>();
			for (OutputFormat outputFormat : v.outputFormats) {
				GetFeatureOutputFormat candidate = GetFeatureOutputFormat.fromValue(outputFormat.getName()); 
				if (candidate != null)
					outputFormats.put(outputFormat.getName(), outputFormat);
			}
			
			if (!outputFormats.containsKey(GetFeatureOutputFormat.GML3_1.value()))
				outputFormats.put(GetFeatureOutputFormat.GML3_1.value(), new OutputFormat(GetFeatureOutputFormat.GML3_1.value()));
			
			return outputFormats;
		}

		@Override
		public OutputFormatList marshal(LinkedHashMap<String, OutputFormat> v) throws Exception {
			OutputFormatList list = new OutputFormatList();
			list.outputFormats = new ArrayList<>(v.values());
			return list;
		}

	}

}
