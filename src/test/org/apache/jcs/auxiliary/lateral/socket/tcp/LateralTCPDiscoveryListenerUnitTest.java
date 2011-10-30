package org.apache.jcs.auxiliary.lateral.socket.tcp;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.lateral.LateralCache;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.logging.MockCacheEventLogger;
import org.apache.jcs.utils.discovery.DiscoveredService;
import org.apache.jcs.utils.serialization.StandardSerializer;

/** Test for the listener that observers UDP discovery events. */
public class LateralTCPDiscoveryListenerUnitTest
    extends TestCase
{
    /** the listener */
    private LateralTCPDiscoveryListener listener;

    /** The cache manager. */
    private ICompositeCacheManager cacheMgr;

    /** The event logger. */
    protected MockCacheEventLogger cacheEventLogger;

    /** The serializer. */
    protected IElementSerializer elementSerializer;

    /** Create the listener for testing */
    protected void setUp()
    {
        cacheMgr = CompositeCacheManager.getInstance();
        cacheEventLogger = new MockCacheEventLogger();
        elementSerializer = new StandardSerializer();

        listener = new LateralTCPDiscoveryListener( cacheMgr, cacheEventLogger, elementSerializer );
    }

    /**
     * Add a no wait facade.
     */
    public void testAddNoWaitFacade_NotInList()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_NotInList";
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );

        // DO WORK
        listener.addNoWaitFacade( cacheName, facade );

        // VERIFY
        assertTrue( "Should have the facade.", listener.containsNoWaitFacade( cacheName ) );
    }

    /**
     * Add a no wait to a known facade.
     */
    public void testAddNoWait_FacadeInList()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_FacadeInList";
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );

        // DO WORK
        boolean result = listener.addNoWait( noWait );

        // VERIFY
        assertTrue( "Should have added the no wait.", result );
    }

    /**
     * Add a no wait from an unknown facade.
     */
    public void testAddNoWait_FacadeNotInList()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_FacadeInList";
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );

        // DO WORK
        boolean result = listener.addNoWait( noWait );

        // VERIFY
        assertFalse( "Should not have added the no wait.", result );
    }

    /**
     * Remove a no wait from an unknown facade.
     */
    public void testRemoveNoWait_FacadeNotInList()
    {
        // SETUP
        String cacheName = "testRemoveNoWaitFacade_FacadeNotInList";
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );

        // DO WORK
        boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertFalse( "Should not have removed the no wait.", result );
    }

    /**
     * Remove a no wait from a known facade.
     */
    public void testRemoveNoWait_FacadeInList_NoWaitNot()
    {
        // SETUP
        String cacheName = "testAddNoWaitFacade_FacadeInList";
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );

        // DO WORK
        boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertFalse( "Should not have removed the no wait.", result );
    }

    /**
     * Remove a no wait from a known facade.
     */
    public void testRemoveNoWait_FacadeInList_NoWaitIs()
    {
        // SETUP
        String cacheName = "testRemoveNoWaitFacade_FacadeInListNoWaitIs";
        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );

        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        LateralCache cache = new LateralCache( cattr );
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );
        listener.addNoWait( noWait );

        // DO WORK
        boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertTrue( "Should have removed the no wait.", result );
    }

    /**
     * Add a no wait to a known facade.
     */
    public void testAddDiscoveredService_FacadeInList_NoWaitNot()
    {
        // SETUP
        String cacheName = "testAddDiscoveredService_FacadeInList_NoWaitNot";

        ArrayList<String> cacheNames = new ArrayList<String>();
        cacheNames.add( cacheName );

        DiscoveredService service = new DiscoveredService();
        service.setCacheNames( cacheNames );
        service.setServiceAddress( "localhost" );
        service.setServicePort( 9999 );

        // since the no waits are compared by object equality, I have to do this
        // TODO add an equals method to the noWait.  the problem if is figuring out what to compare.
        ITCPLateralCacheAttributes lca = new TCPLateralCacheAttributes();
        lca.setTransmissionType( LateralCacheAttributes.TCP );
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lca, cacheMgr, cacheEventLogger,
                                                                         elementSerializer );
        LateralCacheNoWait noWait = (LateralCacheNoWait) lcm.getCache( cacheName );

        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );
        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );

        // DO WORK
        listener.addDiscoveredService( service );

        // VERIFY
        assertTrue( "Should have no wait.", listener.containsNoWait( cacheName, noWait ) );
    }

    /**
     * Remove a no wait from a known facade.
     */
    public void testRemoveDiscoveredService_FacadeInList_NoWaitIs()
    {
        // SETUP
        String cacheName = "testRemoveDiscoveredService_FacadeInList_NoWaitIs";

        ArrayList<String> cacheNames = new ArrayList<String>();
        cacheNames.add( cacheName );

        DiscoveredService service = new DiscoveredService();
        service.setCacheNames( cacheNames );
        service.setServiceAddress( "localhost" );
        service.setServicePort( 9999 );

        // since the no waits are compared by object equality, I have to do this
        // TODO add an equals method to the noWait.  the problem if is figuring out what to compare.
        ITCPLateralCacheAttributes lca = new TCPLateralCacheAttributes();
        lca.setTransmissionType( LateralCacheAttributes.TCP );
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lca, cacheMgr, cacheEventLogger,
                                                                         elementSerializer );
        LateralCacheNoWait noWait = (LateralCacheNoWait) lcm.getCache( cacheName );

        LateralCacheNoWait[] noWaits = new LateralCacheNoWait[0];
        ILateralCacheAttributes cattr = new LateralCacheAttributes();
        cattr.setCacheName( cacheName );
        LateralCacheNoWaitFacade facade = new LateralCacheNoWaitFacade( noWaits, cattr );
        listener.addNoWaitFacade( cacheName, facade );
        listener.addDiscoveredService( service );

        // DO WORK
        listener.removeDiscoveredService( service );

        // VERIFY
        assertFalse( "Should not have no wait.", listener.containsNoWait( cacheName, noWait ) );
    }
}