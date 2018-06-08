package vcs.citydb.wfs.config.feature;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import net.opengis.ows._1.KeywordsType;
import net.opengis.ows._1.WGS84BoundingBoxType;
import net.opengis.wfs._2.Abstract;
import net.opengis.wfs._2.ExtendedDescriptionType;
import net.opengis.wfs._2.MetadataURLType;
import net.opengis.wfs._2.Title;
import vcs.citydb.wfs.config.Constants;

@XmlTransient
public class FeatureType {
	@XmlTransient
	private QName name;
	@XmlElement(name="Title", namespace=Constants.WFS_NAMESPACE_URI)	
	protected List<Title> titles;	
	@XmlElement(name="Abstract", namespace=Constants.WFS_NAMESPACE_URI)	
	protected List<Abstract> abstracts;
	@XmlElement(name="Keywords", namespace=Constants.OWS_NAMESPACE_URI)
	protected List<KeywordsType> keywords;
	@XmlElement(name="WGS84BoundingBox", namespace=Constants.OWS_NAMESPACE_URI)
	protected List<WGS84BoundingBoxType> wgs84BoundingBoxes;
	@XmlElement(name="MetadataURL", namespace=Constants.WFS_NAMESPACE_URI)		
	protected List<MetadataURLType> metadataURLs;
	@XmlElement(name="ExtendedDescription", namespace=Constants.WFS_NAMESPACE_URI)			
	protected ExtendedDescriptionType extendedDescription;
	
	protected FeatureType() {
		
	}
	
	protected FeatureType(QName name, FeatureType other) {
		this.name = name;
		titles = other.titles;
		abstracts = other.abstracts;
		keywords = other.keywords;
		wgs84BoundingBoxes = other.wgs84BoundingBoxes;
		metadataURLs = other.metadataURLs;
		extendedDescription = other.extendedDescription;
	}
	
	public QName getName() {
		return name;
	}
	
	public void setName(QName name) {
		this.name = name;
	}

	public List<Title> getTitles() {
		return titles;
	}
	
	public void addTitle(Title title) {
		if (titles == null)
			titles = new ArrayList<Title>();
		
		titles.add(title);
	}
	
	public boolean isSetTitles() {
		return titles != null;
	}

	public void setTitles(List<Title> titles) {
		this.titles = titles;
	}
	
	public List<Abstract> getAbstracts() {
		return abstracts;
	}
	
	public void addAbstract(Abstract _abstract) {
		if (abstracts == null)
			abstracts = new ArrayList<Abstract>();
		
		abstracts.add(_abstract);
	}
	
	public boolean isSetAbstracts() {
		return abstracts != null;
	}

	public void setAbstracts(List<Abstract> abstracts) {
		this.abstracts = abstracts;
	}
	
	public List<KeywordsType> getKeywords() {
		return keywords;
	}
	
	public void addKeyword(KeywordsType keyword) {
		if (keywords == null)
			keywords = new ArrayList<KeywordsType>();
		
		keywords.add(keyword);
	}
	
	public boolean isSetKeywords() {
		return keywords != null;
	}

	public void setKeywords(List<KeywordsType> keywords) {
		this.keywords = keywords;
	}

	public List<WGS84BoundingBoxType> getWGS84BoundingBoxes() {
		return wgs84BoundingBoxes;
	}
	
	public void addWGS84BoundingBox(WGS84BoundingBoxType wgs84BoundingBox) {
		if (wgs84BoundingBoxes == null)
			wgs84BoundingBoxes = new ArrayList<WGS84BoundingBoxType>();
		
		wgs84BoundingBoxes.add(wgs84BoundingBox);
	}
	
	public boolean isSetWGS84BoundingBoxes() {
		return wgs84BoundingBoxes != null;
	}
	
	public void setWGS84BoundingBoxes(List<WGS84BoundingBoxType> wgs84BoundingBoxes) {
		this.wgs84BoundingBoxes = wgs84BoundingBoxes;
	}
	
	public List<MetadataURLType> getMetadataURLs() {
		return metadataURLs;
	}
	
	public void addMetadataURL(MetadataURLType metadataURL) {
		if (metadataURLs == null)
			metadataURLs = new ArrayList<MetadataURLType>();
		
		metadataURLs.add(metadataURL);
	}
	
	public boolean isSetMetadataURLs() {
		return metadataURLs != null;
	}

	public void setMetadataURLs(List<MetadataURLType> metadataURLs) {
		this.metadataURLs = metadataURLs;
	}

	public ExtendedDescriptionType getExtendedDescription() {
		return extendedDescription;
	}

	public boolean isSetExtendedDescription() {
		return extendedDescription != null;
	}
	
	public void setExtendedDescription(ExtendedDescriptionType extendedDescription) {
		this.extendedDescription = extendedDescription;
	}
}
