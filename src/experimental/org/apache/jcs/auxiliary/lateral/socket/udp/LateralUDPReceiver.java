package org.apache.jcs.auxiliary.lateral.socket.udp;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;

/**
 * A highly unreliable UDP receiver. It is easy to outrun. Uncaught message will
 * die.
 */
public class LateralUDPReceiver implements Runnable
{
    private final static Log log =
        LogFactory.getLog( LateralUDPReceiver.class );

    private final byte[] m_buffer = new byte[65536];
    private MulticastSocket m_socket;

    ILateralCacheListener ilcl;


    /**
     * Constructor for the LateralUDPReceiver object
     *
     * @param lca
     * @param ilcl
     * @exception IOException
     */
    public LateralUDPReceiver( ILateralCacheAttributes lca, ILateralCacheListener ilcl )
        throws IOException
    {
        this( lca.getUdpMulticastAddr(), lca.getUdpMulticastPort() );
        this.ilcl = ilcl;
    }


    /**
     * Constructor for the LateralUDPReceiver object
     *
     * @param multicastAddressString
     * @param multicastPort
     * @exception IOException
     */
    protected LateralUDPReceiver( String multicastAddressString, int multicastPort )
        throws IOException
    {

        log.debug( "constructing listener, " + multicastAddressString + ":" + multicastPort );

        try
        {
            m_socket = new MulticastSocket( multicastPort );
            m_socket.joinGroup( InetAddress.getByName( multicastAddressString ) );
        }
        catch ( IOException e )
        {
            log.error( e );
            log.debug( "Could not bind to multicast address " + multicastAddressString + ":" + multicastPort );
            //throw e ;//new CacheException( "Could not bind to multicast address " + multicastAddressString + ":" + multicastPort, e);
        }
    }


    /**
     * Highly unreliable. If it is processing one message while another comes in
     * , the second message is lost. This is for low concurency peppering.
     */
    public Object waitForMessage()
        throws IOException
    {
        final DatagramPacket packet = new DatagramPacket( m_buffer,
            m_buffer.length );

        Object obj = null;
        try
        {
            m_socket.receive( packet );

            final ByteArrayInputStream byteStream = new ByteArrayInputStream( m_buffer, 0, packet.getLength() );

            final ObjectInputStream objectStream = new ObjectInputStream( byteStream );

            obj = objectStream.readObject();

        }
        catch ( Exception e )
        {
            log.error( e );
            //throw new CacheException( "Error receving multicast packet", e);
        }
        return obj;
    }


    /** Main processing method for the LateralUDPReceiver object */
    public void run()
    {

        try
        {
            while ( true )
            {

                Object obj = waitForMessage();

                LateralElementDescriptor led = ( LateralElementDescriptor ) obj;
                if ( led.requesterId == LateralCacheInfo.listenerId )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "from self" );
                    }
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "from another" );
                    }
                    if ( led.command == led.UPDATE )
                    {
                        ilcl.handlePut( led.ce );
                    }
                    else
                        if ( led.command == led.UPDATE )
                    {
                        ilcl.handleRemove( led.ce.getCacheName(), led.ce.getKey() );
                    }
                }
            }
        }
        catch ( Exception e )
        {
        }
    }

    /** Description of the Method */
    public static void main( String args[] )
    {
        try
        {
            LateralUDPReceiver lur = new LateralUDPReceiver( "228.5.6.7", 6789 );
            Thread t = new Thread( lur );
            t.start();
        }
        catch ( Exception e )
        {
            log.error( e.toString() );
        }
    }

}
// end class
