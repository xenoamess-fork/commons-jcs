package org.apache.jcs.auxiliary.lateral.socket.tcp;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheAbstractFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.jcs.auxiliary.lateral.socket.tcp.discovery.UDPDiscoveryManager;
import org.apache.jcs.auxiliary.lateral.socket.tcp.discovery.UDPDiscoveryService;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each
 * lateral service / local relationship is managed by one manager. This manager
 * canl have multiple caches. The remote relationships are consolidated and
 * restored via these managers. The facade provides a front to the composite
 * cache so the implmenetation is transparent.
 *  
 */
public class LateralTCPCacheFactory
    extends LateralCacheAbstractFactory
{
    private final static Log log = LogFactory.getLog( LateralTCPCacheFactory.class );

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheFactory#createCache(org.apache.jcs.auxiliary.AuxiliaryCacheAttributes,
     *      org.apache.jcs.engine.behavior.ICompositeCacheManager)
     */
    public AuxiliaryCache createCache( AuxiliaryCacheAttributes iaca, ICompositeCacheManager cacheMgr )
    {

        LateralCacheAttributes lac = (LateralCacheAttributes) iaca;
        ArrayList noWaits = new ArrayList();

        //pars up the tcp servers and set the tcpServer value and
        // get the manager and then get the cache
        // no servers are required.
        if ( lac.getTcpServers() != null )
        {
            StringTokenizer it = new StringTokenizer( lac.getTcpServers(), "," );
            if ( log.isDebugEnabled() )
            {
                log.debug( "Configured for " + it.countTokens() + "  servers." );
            }
            while ( it.hasMoreElements() )
            {
                String server = (String) it.nextElement();
                if ( log.isDebugEnabled() )
                {
                    log.debug( "tcp server = " + server );
                }
                LateralCacheAttributes lacC = (LateralCacheAttributes) lac.copy();
                lacC.setTcpServer( server );
                LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lacC, cacheMgr );
                ICache ic = lcm.getCache( lacC.getCacheName() );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    log.debug( "noWait is null, no lateral connection made" );
                }
            }
        }

        createListener( lac, cacheMgr );

        // create the no wait facade.
        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( (LateralCacheNoWait[]) noWaits
            .toArray( new LateralCacheNoWait[0] ), iaca.getCacheName() );

        // create udp discovery if available.
        createDiscoveryService( lac, lcnwf, cacheMgr );

        return lcnwf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.lateral.LateralCacheAbstractFactory#createListener(org.apache.jcs.auxiliary.lateral.LateralCacheAttributes,
     *      org.apache.jcs.engine.behavior.ICompositeCacheManager)
     */
    public void createListener( LateralCacheAttributes lac, ICompositeCacheManager cacheMgr )
    {
        // don't create a listener if we are not receiving.
        if ( lac.isReceive() )
        {

            if ( log.isInfoEnabled() )
            {
                log.info( "Creating listener for " + lac );
            }

            try
            {

                // make a listener. if one doesn't exist
                LateralTCPListener.getInstance( lac, cacheMgr );

            }
            catch ( Exception e )
            {
                log.error( "Problem creating lateral listener", e );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Not creating a listener since we are not receiving." );
            }
        }
    }

    /**
     * Creates the discovery service. Only creates this for tcp laterals right
     * now.
     * 
     * @param lac
     * @param lcnwf
     * @param cacheMgr
     * @return null if none is created.
     */
    private UDPDiscoveryService createDiscoveryService( LateralCacheAttributes lac, LateralCacheNoWaitFacade lcnwf,
                                                       ICompositeCacheManager cacheMgr )
    {
        UDPDiscoveryService discovery = null;

        //      create the UDP discovery for the TCP lateral
        if ( lac.isUdpDiscoveryEnabled() )
        {

            // need a factory for this so it doesn't
            // get dereferenced, also we don't want one for every region.
            discovery = UDPDiscoveryManager.getInstance().getService( lac, cacheMgr );

            discovery.addNoWaitFacade( lcnwf, lac.getCacheName() );

            if ( log.isInfoEnabled() )
            {
                log.info( "Created UDPDiscoveryService for TCP lateral cache." );
            }

        }
        return discovery;
    }

}