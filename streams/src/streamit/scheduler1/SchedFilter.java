package streamit.scheduler1;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class SchedFilter extends SchedStream
{
    final List srcMsgs = new LinkedList ();
    final List dstMsgs = new LinkedList ();

    protected SchedFilter (Object stream, int push, int pop, int peek)
    {
        super (stream);
        setProduction (push);
        setConsumption (pop);
        setPeekConsumption (peek);
    }

    public void computeSteadySchedule ()
    {
        // initialize self
        setNumExecutions (BigInteger.ONE);
    }

    void printDot (PrintStream outputStream)
    {
        print(getUniqueStreamName () + " [ label=\"" + getStreamName () + "\" ]\n", outputStream);
    }

    SchedObject getLastChild () { return this; }
    SchedObject getFirstChild () { return this; }
}
