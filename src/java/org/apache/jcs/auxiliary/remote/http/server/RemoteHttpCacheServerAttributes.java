package org.apache.jcs.auxiliary.remote.http.server;

import org.apache.jcs.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Configuration for the RemoteHttpCacheServer. Most of these properties are used only by the
 * service.
 */
public class RemoteHttpCacheServerAttributes
    extends AbstractAuxiliaryCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = -3987239306108780496L;

    /** Can a cluster remote put to other remotes */
    private boolean localClusterConsistency = true;

    /** Can a cluster remote get from other remotes */
    private boolean allowClusterGet = true;

    /**
     * clones
     * <p>
     * @return AuxiliaryCacheAttributes clone
     */
    public AuxiliaryCacheAttributes copy()
    {
        try
        {
            return (AuxiliaryCacheAttributes) this.clone();
        }
        catch ( Exception e )
        {
            // swallow
        }
        return this;
    }

    /**
     * Should cluster updates be propagated to the locals
     * <p>
     * @return The localClusterConsistency value
     */
    public boolean isLocalClusterConsistency()
    {
        return localClusterConsistency;
    }

    /**
     * Should cluster updates be propagated to the locals
     * <p>
     * @param r The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( boolean r )
    {
        this.localClusterConsistency = r;
    }

    /**
     * Should gets from non-cluster clients be allowed to get from other remote auxiliaries.
     * <p>
     * @return The localClusterConsistency value
     */
    public boolean isAllowClusterGet()
    {
        return allowClusterGet;
    }

    /**
     * Should we try to get from other cluster servers if we don't find the items locally.
     * <p>
     * @param r The new localClusterConsistency value
     */
    public void setAllowClusterGet( boolean r )
    {
        allowClusterGet = r;
    }

    /**
     * @return String details
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nRemoteHttpCacheServiceAttributes" );
        buf.append( "\n cacheName = [" + this.getCacheName() + "]" );
        buf.append( "\n allowClusterGet = [" + this.isAllowClusterGet() + "]" );
        buf.append( "\n localClusterConsistency = [" + this.isLocalClusterConsistency() + "]" );
        buf.append( "\n eventQueueType = [" + this.getEventQueueType() + "]" );
        buf.append( "\n eventQueuePoolName = [" + this.getEventQueuePoolName() + "]" );
        return buf.toString();
    }
}