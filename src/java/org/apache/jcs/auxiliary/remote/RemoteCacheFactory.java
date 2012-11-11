package org.apache.jcs.auxiliary.remote;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.auxiliary.remote.server.behavior.RemoteType;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * The RemoteCacheFactory creates remote caches for the cache hub. It returns a no wait facade which
 * is a wrapper around a no wait. The no wait object is either an active connection to a remote
 * cache or a balking zombie if the remote cache is not accessible. It should be transparent to the
 * clients.
 */
public class RemoteCacheFactory
    implements AuxiliaryCacheFactory
{
    /** The logger. */
    private final static Log log = LogFactory.getLog( RemoteCacheFactory.class );

    /** The name of this auxiliary */
    private String name;

    /** store reference of facades to initiate failover */
    private final static HashMap<String, RemoteCacheNoWaitFacade<? extends Serializable, ? extends Serializable>> facades =
        new HashMap<String, RemoteCacheNoWaitFacade<? extends Serializable, ? extends Serializable>>();

    /**
     * For LOCAL clients we get a handle to all the failovers, but we do not register a listener
     * with them. We create the RemoteCacheManager, but we do not get a cache.
     * <p>
     * The failover runner will get a cache from the manager. When the primary is restored it will
     * tell the manager for the failover to deregister the listener.
     * <p>
     * @param iaca
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @return AuxiliaryCache
     */
    public <K extends Serializable, V extends Serializable> AuxiliaryCache<K, V> createCache(
            AuxiliaryCacheAttributes iaca, ICompositeCacheManager cacheMgr,
           ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
    {
        RemoteCacheAttributes rca = (RemoteCacheAttributes) iaca;

        ArrayList<ICache<K, V>> noWaits = new ArrayList<ICache<K, V>>();

        // if LOCAL
        if ( rca.getRemoteType() == RemoteType.LOCAL )
        {
            // a list to be turned into an array of failover server information
            ArrayList<String> failovers = new ArrayList<String>();

            // not necessary if a failover list is defined
            // REGISTER PRIMARY LISTENER
            // if it is a primary
            boolean primayDefined = false;
            if ( rca.getRemoteHost() != null )
            {
                primayDefined = true;

                failovers.add( rca.getRemoteHost() + ":" + rca.getRemotePort() );

                RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca, cacheMgr, cacheEventLogger,
                                                                         elementSerializer );
                ICache<K, V> ic = rcm.getCache( rca );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    log.info( "noWait is null" );
                }
            }

            // GET HANDLE BUT DONT REGISTER A LISTENER FOR FAILOVERS
            String failoverList = rca.getFailoverServers();
            if ( failoverList != null )
            {
                StringTokenizer fit = new StringTokenizer( failoverList, "," );
                int fCnt = 0;
                while ( fit.hasMoreElements() )
                {
                    fCnt++;

                    String server = (String) fit.nextElement();
                    failovers.add( server );

                    rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
                    rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
                    RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca, cacheMgr, cacheEventLogger,
                                                                             elementSerializer );
                    // add a listener if there are none, need to tell rca what
                    // number it is at
                    if ( ( !primayDefined && fCnt == 1 ) || noWaits.size() <= 0 )
                    {
                        ICache<K, V> ic = rcm.getCache( rca );
                        if ( ic != null )
                        {
                            noWaits.add( ic );
                        }
                        else
                        {
                            log.info( "noWait is null" );
                        }
                    }
                }
                // end while
            }
            // end if failoverList != null

            rca.setFailovers( failovers.toArray( new String[0] ) );

            // if CLUSTER
        }
        else if ( rca.getRemoteType() == RemoteType.CLUSTER )
        {
            // REGISTER LISTENERS FOR EACH SYSTEM CLUSTERED CACHEs
            StringTokenizer it = new StringTokenizer( rca.getClusterServers(), "," );
            while ( it.hasMoreElements() )
            {
                // String server = (String)it.next();
                String server = (String) it.nextElement();
                // p( "tcp server = " + server );
                rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
                rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
                RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca, cacheMgr, cacheEventLogger,
                                                                         elementSerializer );
                rca.setRemoteType( RemoteType.CLUSTER );
                ICache<K, V> ic = rcm.getCache( rca );
                if ( ic != null )
                {
                    noWaits.add( ic );
                }
                else
                {
                    log.info( "noWait is null" );
                }
            }

        }
        // end if CLUSTER

        @SuppressWarnings("unchecked") // No generic arrays in java
        RemoteCacheNoWait<K, V>[] rcnwArray = noWaits.toArray( new RemoteCacheNoWait[0] );
        RemoteCacheNoWaitFacade<K, V> rcnwf =
            new RemoteCacheNoWaitFacade<K, V>(rcnwArray, rca, cacheMgr, cacheEventLogger, elementSerializer );

        getFacades().put( rca.getCacheName(), rcnwf );

        return rcnwf;
    }

    // end createCache

    /**
     * Gets the name attribute of the RemoteCacheFactory object
     * @return The name value
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the RemoteCacheFactory object
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * The facades are what the cache hub talks to.
     * @return Returns the facades.
     */
    public static HashMap<String, RemoteCacheNoWaitFacade<? extends Serializable, ? extends Serializable>> getFacades()
    {
        return facades;
    }
}
