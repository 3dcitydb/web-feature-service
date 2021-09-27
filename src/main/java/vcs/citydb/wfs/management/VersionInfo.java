package vcs.citydb.wfs.management;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

public class VersionInfo {
	private static VersionInfo instance;

	private String name = "VC Web Feature Service";
	private String version = "not available";
	private String copyright = "2015-" + LocalDate.now().getYear() + ", virtualcitysystems GmbH";
	private final Vendor vendor = new Vendor();

	public static synchronized VersionInfo getInstance() {
		if (instance == null) {
			instance = new VersionInfo();

			try {
				Properties app = new Properties();
				app.load(VersionInfo.class.getResourceAsStream("/vcs/citydb/wfs/application.properties"));

				instance.name = app.getProperty("name");
				instance.version = app.getProperty("version");
				instance.copyright = app.getProperty("vendorCopyright") + ", " + app.getProperty("vendorName");
				instance.vendor.name = app.getProperty("vendorName");
				instance.vendor.websiteUrl = app.getProperty("vendorWebsiteUrl");
				instance.vendor.address = app.getProperty("vendorStreet") + ", " +
						app.getProperty("vendorTown") + ", " +
						app.getProperty("vendorCountry");
			} catch (IOException e) {
				// nothing to do
			}
		}

		return instance;
	}

	private static class Vendor {
		private String name = "virtualcitysystems GmbH";
		private String address = "Tauentzienstraße 7 b/c, 10789 Berlin, Germany";
		private String websiteUrl = "https://www.vc.systems";
	}
}
