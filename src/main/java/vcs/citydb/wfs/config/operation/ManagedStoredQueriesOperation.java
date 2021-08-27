package vcs.citydb.wfs.config.operation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ManagedStoredQueriesOperationType")
public class ManagedStoredQueriesOperation {
	@XmlAttribute(required=true)
	private boolean isEnabled = false;

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
