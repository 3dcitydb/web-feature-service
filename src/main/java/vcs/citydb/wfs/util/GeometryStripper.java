package vcs.citydb.wfs.util;

import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryArrayProperty;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.util.walker.GMLWalker;

public class GeometryStripper extends GMLWalker {
	
	@Override
	public <T extends AbstractGeometry> void visit(GeometryArrayProperty<T> arrayProperty) {
		arrayProperty.unsetGeometry();
	}

	@Override
	public <T extends AbstractGeometry> void visit(GeometryProperty<T> geometryProperty) {
		if (geometryProperty.isSetGeometry()) {
			String gmlId = geometryProperty.getGeometry().getId();
			if (gmlId != null)
				geometryProperty.setHref("#" + gmlId);
			
			geometryProperty.unsetGeometry();
		}
	}
	
}
