package com.gu.management.manifest;

import org.apache.log4j.Logger;

import java.util.List;

public class Manifest {

	private static final Logger LOGGER = Logger.getLogger(Manifest.class);

	private String revisionNumber;
	private String absolutePath;
	private String manifestString;
	private String manifestFilePath = "META-INF/MANIFEST.MF";
	private ApplicationFileProvider fileProvider;

    public Manifest(ApplicationFileProvider fileProvider) {
        this.fileProvider=fileProvider;
    }

    public void setManifestFilePath(String manifestFilePath) {
        this.manifestFilePath = manifestFilePath;
        reload();
    }

    public String getRevisionNumber() {
        return revisionNumber;
    }


    public String getManifestInformation() {
        if(absolutePath != null) {
            return "Absolute-Path: " + absolutePath + "\n" + manifestString;
        }
        return manifestString;
    }

    private String getValue(String line) {
		String[] splits = line.split(":");
		return splits[1].trim();
	}

    private void parseRevisionNumber(String line) {
			revisionNumber = getValue(line);
	}

    @Override
	public String toString() {
		return "Manifest: " + getManifestInformation();
	}

    public void reload() {
		LOGGER.info("Reloading manifest: "+manifestFilePath);
		List<String> file = fileProvider.getFileContents(manifestFilePath);
		if(file != null) {
			parseManifest(file);
		} else {
			manifestString = String.format("Manifest file not found: '%s", fileProvider.getAbsolutePath(manifestFilePath)) + "'";
			revisionNumber = String.valueOf(System.currentTimeMillis());
			LOGGER.debug("Manfest not found generating random revision number : "+revisionNumber);
		}
	}

	private void parseManifest(List<String> file) {
		absolutePath = fileProvider.getAbsolutePath(manifestFilePath);
		manifestString = "";

		for (String line : file) {
			manifestString += line + "\n";
			if(line.startsWith("Revision") ) {
				parseRevisionNumber(line);
				LOGGER.info("Manifest Revision: "+revisionNumber);
			}
		}
	}
}