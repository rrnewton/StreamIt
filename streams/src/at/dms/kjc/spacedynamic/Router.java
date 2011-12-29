package at.dms.kjc.spacedynamic;

import java.util.LinkedList;

public interface Router 
{
    public LinkedList<ComputeNode> getRoute(SpdStaticStreamGraph ssg, ComputeNode src, ComputeNode dst);

    
}
