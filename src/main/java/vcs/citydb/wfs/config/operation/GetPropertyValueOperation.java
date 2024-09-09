package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@XmlType(name = "GetPropertyValueOperationType")
public class GetPropertyValueOperation {
    @XmlAttribute(required = true)
    private boolean isEnabled = false;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Collection<OutputFormat> getOutputFormats() {
        return Arrays.stream(GetPropertyValueOutputFormat.values())
                .map(Objects::toString)
                .map(OutputFormat::new)
                .collect(Collectors.toList());
    }

    public Set<String> getOutputFormatsAsString() {
        return getOutputFormats().stream()
                .map(OutputFormat::getName)
                .collect(Collectors.toSet());
    }

    public boolean supportsOutputFormat(String outputFormat) {
        return GetPropertyValueOutputFormat.fromValue(outputFormat) != null;
    }

    public OutputFormat getOutputFormat(String outputFormat) {
        GetPropertyValueOutputFormat format = GetPropertyValueOutputFormat.fromValue(outputFormat);
        return format != null ? new OutputFormat(format.value()) : null;
    }
}
