package bms.tool.mdprocessor;

import java.io.FileNotFoundException;

/**
 * Defines a http download source
 *
 * @author Catizard
 * @implSpec A HttpDownloadSource instance should separate the meta fields to break the cyclic dependency
 * @since Tue, 10 Jun 2025 09:19 PM
 */
public interface HttpDownloadSource {
    /**
     * Construct download url based on md5 <br>
     *
     * @param md5 missing sabun's md5
     * @return download url, based on download source
     */
    String getDownloadURLBasedOnMd5(String md5) throws FileNotFoundException, RuntimeException;

    /**
     * Name is an unique symbol, also the option from 'otherTab'
     */
    String getName();

    // For further implementations

    boolean isAllowDownloadThroughMd5();

    boolean isAllowDownloadThroughSha256();

    boolean isAllowMetaQuery();
}
