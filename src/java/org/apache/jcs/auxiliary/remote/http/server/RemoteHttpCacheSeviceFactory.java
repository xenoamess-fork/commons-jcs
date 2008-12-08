package org.apache.jcs.auxiliary.remote.http.server;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.jcs.auxiliary.remote.http.behavior.IRemoteHttpCacheConstants;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.config.PropertySetter;

/** Creates the server. */
public class RemoteHttpCacheSeviceFactory
{
    /** The logger */
    private final static Log log = LogFactory.getLog( RemoteHttpCacheSeviceFactory.class );

    /**
     * Configures the attributes and the event logger and constructs a service.
     * <p>
     * @param cacheManager
     * @return RemoteHttpCacheService
     */
    public static RemoteHttpCacheService createRemoteHttpCacheService( ICompositeCacheManager cacheManager )
    {
        Properties props = cacheManager.getConfigurationProperties();
        ICacheEventLogger cacheEventLogger = configureCacheEventLogger( props );
        RemoteHttpCacheServerAttributes attributes = configureRemoteHttpCacheServerAttributes( props );

        RemoteHttpCacheService service = new RemoteHttpCacheService( cacheManager, attributes, cacheEventLogger );
        if ( log.isInfoEnabled() )
        {
            log.info( "Created new RemoteHttpCacheService " + service );
        }
        return service;
    }

    /**
     * Tries to get the event logger.
     * <p>
     * @param props
     * @return ICacheEventLogger
     */
    protected static ICacheEventLogger configureCacheEventLogger( Properties props )
    {
        ICacheEventLogger cacheEventLogger = AuxiliaryCacheConfigurator
            .parseCacheEventLogger( props, IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_PREFIX );

        return cacheEventLogger;
    }

    /**
     * Configure.
     * <p>
     * jcs.remotehttpcache.serverattributes.ATTRIBUTENAME=ATTRIBUTEVALUE
     * <p>
     * @param prop
     * @return RemoteCacheServerAttributesconfigureRemoteCacheServerAttributes
     */
    protected static RemoteHttpCacheServerAttributes configureRemoteHttpCacheServerAttributes( Properties prop )
    {
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();

        // configure automatically
        PropertySetter.setProperties( rcsa, prop,
                                      IRemoteHttpCacheConstants.HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + "." );

        return rcsa;
    }
}