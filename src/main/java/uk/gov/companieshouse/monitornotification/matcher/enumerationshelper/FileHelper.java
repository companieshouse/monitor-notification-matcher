package uk.gov.companieshouse.monitornotification.matcher.enumerationshelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.logging.Logger;

@Component
public class FileHelper {
    private final Logger logger;

    /**
     * Constructor.
     */
    public FileHelper(final Logger logger) {
        this.logger = logger;
    }

	/**
	 * Load the contents of a file into an inputstream
	 * 
	 * @param path
	 * @return inputstream
	 * @throws FileNotFoundException 
	 */
    public InputStream loadFile(String path) throws FileNotFoundException {
        logger.trace("loadFile(path=%s) method called.".formatted(path));
        File fileDescriptionsFile = new File(path);

        if(fileDescriptionsFile.exists()) {
            return new FileInputStream(fileDescriptionsFile);
        }

        return null;
    }
}
