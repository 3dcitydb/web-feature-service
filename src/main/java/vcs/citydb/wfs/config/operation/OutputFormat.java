package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@XmlType(name="OutputFormatType", propOrder={
		"options"
})
public class OutputFormat {
	@XmlAttribute(required=true)
	private String name;
	@XmlJavaTypeAdapter(MapAdapter.class)
	@XmlElement(nillable=false)
	LinkedHashMap<String, String> options;

	public OutputFormat() {
		options = new LinkedHashMap<>();
	}
	
	public OutputFormat(String name) {
		this();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean hasOptions() {
		return !options.isEmpty();
	}

	public String getOption(String name) {
		return options.get(name);
	}
	
	public Map<String, String> getOptions() {
		return options;
	}
	
	@XmlType(name="OutputFormatListType")
	protected final static class OutputFormatList {
		@XmlElement(name="outputFormat", nillable=false)
		protected List<OutputFormat> outputFormats;
	}

	@XmlType(name="OutputFormatOptionListType")
	private final static class OutputFormatOptionList {
		@XmlElement(name="option", nillable=false)
		private List<OutputFormatOption> options;
	}
	
	@XmlType(name="OutputFormatOptionType")
	private final static class OutputFormatOption {
		@XmlAttribute(required=true)
		private String name;
		@XmlValue
		private String value;
	}

	private final static class MapAdapter extends XmlAdapter<OutputFormatOptionList, LinkedHashMap<String, String>> {

		@Override
		public LinkedHashMap<String, String> unmarshal(OutputFormatOptionList v) throws Exception {
			LinkedHashMap<String, String> options = new LinkedHashMap<>();	
			for (OutputFormatOption option : v.options) {
				if (option.name != null && !option.name.isEmpty())
					options.put(option.name, option.value);
			}

			return options;
		}

		@Override
		public OutputFormatOptionList marshal(LinkedHashMap<String, String> v) throws Exception {
			OutputFormatOptionList list = new OutputFormatOptionList();
			list.options = new ArrayList<>();
			
			for (Entry<String, String> entry : v.entrySet()) {
				OutputFormatOption option = new OutputFormatOption();
				option.name = entry.getKey();
				option.value = entry.getValue();
				list.options.add(option);
			}
			
			return !list.options.isEmpty() ? list : null;
		}

	}

}
