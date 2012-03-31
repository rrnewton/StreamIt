package at.dms.kjc.smp;

import java.util.List;
import java.util.Map;
import at.dms.kjc.CType;
import at.dms.kjc.JEmittedTextExpression;
import at.dms.kjc.JExpression;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodCallExpression;
import at.dms.kjc.KjcOptions;
import at.dms.kjc.SLIRReplacingVisitor;
import at.dms.kjc.backendSupport.Channel;
import at.dms.kjc.backendSupport.InterSSGChannel;
import at.dms.kjc.sir.SIRPeekExpression;
import at.dms.kjc.sir.SIRPopExpression;
import at.dms.kjc.sir.SIRPushExpression;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InputPort;
import at.dms.kjc.slir.InterSSGEdge;
import at.dms.kjc.slir.OutputPort;
import at.dms.kjc.slir.StaticSubGraph;
import at.dms.kjc.slir.WorkNode;

/**
 * Private class for setting up dynamic push and pop calls
 * 
 * @author soule
 * 
 */
class PushPopReplacingVisitor extends SLIRReplacingVisitor {

    String                    peekName;
    String                    popManyName;
    String                    popName;
    String                    pushName;
    @SuppressWarnings("rawtypes")
    Channel                   inputChannel;
    @SuppressWarnings("rawtypes")
    Channel                   outputChannel;
    Map<Filter, Integer>      filterToThreadId;
    boolean                   isDynamicPop;
    boolean                   isDynamicPush;
    WorkNode                  workNode;
    Map<String, List<String>> dominators;

    @SuppressWarnings("rawtypes")
    public void init(WorkNode filter, Channel inputChannel,
            Channel outputChannel, String peekName, String popManyName,
            String popName, String pushName,
            Map<Filter, Integer> filterToThreadId,
            Map<String, List<String>> dominators, boolean isDynamicPop,
            boolean isDynamicPush) {
        this.workNode = filter;
        this.peekName = peekName;
        this.popManyName = popManyName;
        this.popName = popName;
        this.pushName = pushName;
        this.inputChannel = inputChannel;
        this.outputChannel = outputChannel;
        this.filterToThreadId = filterToThreadId;
        this.dominators = dominators;
        this.isDynamicPop = isDynamicPop;
        this.isDynamicPush = isDynamicPush;
    }

    @Override
    public Object visitPeekExpression(SIRPeekExpression self,
            CType tapeType, JExpression arg) {
        if (isDynamicPop && !isSource()) {
            InputPort inputPort = ((InterSSGChannel) inputChannel)
                    .getEdge().getDest();
            String threadId = filterToThreadId.get(
                    inputPort.getSSG().getTopFilters()[0]).toString();
            String buffer = "dyn_buf_" + threadId;

            int next = -1;
            if (KjcOptions.threadopt) {                                                            
                int channelId = ((InterSSGChannel) inputChannel).getId();
                Filter nextFilter = ProcessFilterUtils.getNextFilterOnCoreDifferentThread(workNode);                                                 
                                                                              
                // If there is no next filter on this core
                // then we want to return to the main.
                if (nextFilter == null) {                            
                    next = ThreadMapper.coreToThread(getCoreID(inputPort));
                } else {
                    next = ProcessFilterUtils.getFilterThread(nextFilter);                 
                }
                
                
                buffer = "dyn_buf_" + channelId;
            }

            JExpression dyn_queue = new JEmittedTextExpression(buffer);
            JExpression index = new JEmittedTextExpression(threadId);
            JExpression newArg = (JExpression) arg.accept(this);
            JExpression dominated = new JEmittedTextExpression(
                    workNode.toString() + "_multipliers");
            int num_multipliers = (dominators.get(workNode.toString()) == null) ? 0
                    : dominators.get(workNode.toString()).size();
            
            int num_tkns = (ThreadMapper.getMapper().getDominatorToTokens().get(workNode) == null) ? 0
                    : ThreadMapper.getMapper().getDominatorToTokens().get(workNode).size();            
            JExpression tokens = new JEmittedTextExpression(
                    workNode.toString() + "_tokens");            
            JExpression num_tokens = new JIntLiteral(num_tkns);
            
            JExpression num_dominated = new JIntLiteral(num_multipliers);
            if (KjcOptions.threadopt) {
                JExpression nextThread = new JIntLiteral(next);
                JExpression methodCall = new JMethodCallExpression(
                        peekName, new JExpression[] { dyn_queue, index,
                                nextThread, num_dominated, dominated,
                                newArg, num_tokens, tokens });
                methodCall.setType(tapeType);
                return methodCall;
            }
            JExpression methodCall = new JMethodCallExpression(peekName,
                    new JExpression[] { dyn_queue, index, num_dominated,
                            dominated, newArg });
            methodCall.setType(tapeType);
            return methodCall;

        } else {
            JExpression newArg = (JExpression) arg.accept(this);
            JExpression methodCall = new JMethodCallExpression(peekName,
                    new JExpression[] { newArg });
            methodCall.setType(tapeType);
            return methodCall;
        }
    }

    @Override
    public Object visitPopExpression(SIRPopExpression self, CType tapeType) {

        // We have to special case when there is a dynamic pop rate
        // that follows a FileReader. First we check if this is the
        // first filter in the ssg.
        if (isSource()) {
            return staticPop(
                    self,
                    tapeType);
        }
        if (isDynamicPop) {
            return dynamicPop(
                    self,
                    tapeType);
        } else {
            return staticPop(
                    self,
                    tapeType);
        }
    }

    @Override
    public Object visitPushExpression(SIRPushExpression self,
            CType tapeType, JExpression arg) {
        JExpression newArg = (JExpression) arg.accept(this);
        if (isDynamicPush) {

            InterSSGEdge edge = ((InterSSGChannel) outputChannel).getEdge();
            InputPort inputPort = edge.getDest();

            int threadIndex = filterToThreadId.get(inputPort.getSSG()
                    .getTopFilters()[0]);
            String threadId = Integer.toString(threadIndex);

            String buffer = "dyn_buf_" + threadId;

            if (KjcOptions.threadopt) {
                int channelId = ((InterSSGChannel) outputChannel).getId();
                buffer = "dyn_buf_" + channelId;
            }

            JExpression dyn_queue = new JEmittedTextExpression(buffer);
            return new JMethodCallExpression(pushName, new JExpression[] {
                    dyn_queue, newArg });
        } else {
            return new JMethodCallExpression(pushName,
                    new JExpression[] { newArg });

        }
    }

    private JExpression dynamicPop(SIRPopExpression self, CType tapeType) {
        InputPort inputPort = ((InterSSGChannel) inputChannel).getEdge()
                .getDest();

        int threadIndex = filterToThreadId.get(inputPort.getSSG()
                .getTopFilters()[0]);
        String threadId = Integer.toString(threadIndex);
        String buffer = "dyn_buf_" + threadId;
        int next = -1;
        if (KjcOptions.threadopt) {
            int channelId = ((InterSSGChannel) inputChannel).getId();

            
            //Filter nextFilter = ProcessFilterWorkNode.getNextFilterOnCore(workNode);     
            
            Filter nextFilter = ProcessFilterUtils.getNextFilterOnCoreDifferentThread(workNode);    
                        
                            
            // If there is no next filter on this core
            // then we want to return to the main.
            if (nextFilter == null) {                            
                next = ThreadMapper.coreToThread(getCoreID(inputPort));
            } else {
                next = ProcessFilterUtils.getFilterThread(nextFilter);                 
            }
           
            
            buffer = "dyn_buf_" + channelId;      

        }

        JExpression dyn_queue = new JEmittedTextExpression(buffer);
        JExpression index = new JEmittedTextExpression(threadId);

        int num_multipliers = (dominators.get(workNode.toString()) == null) ? 0
                : dominators.get(
                        workNode.toString()).size();

        JExpression num_dominated = new JIntLiteral(num_multipliers);
        JExpression dominated = new JEmittedTextExpression(workNode.toString() + "_multipliers");
        
        int num_tkns = (ThreadMapper.getMapper().getDominatorToTokens().get(workNode) == null) ? 0
                : ThreadMapper.getMapper().getDominatorToTokens().get(workNode).size();            
        JExpression tokens = new JEmittedTextExpression(
                workNode.toString() + "_tokens");            
        JExpression num_tokens = new JIntLiteral(num_tkns);

        
        if (self.getNumPop() > 1) {
            if (KjcOptions.threadopt) {

                JExpression nextThread = new JIntLiteral(next);
                return new JMethodCallExpression(popManyName,
                        new JExpression[] { dyn_queue, index, nextThread,
                                num_dominated, dominated,
                                new JIntLiteral(self.getNumPop()), num_tokens, tokens });
            } else {
                return new JMethodCallExpression(popManyName,
                        new JExpression[] { dyn_queue, index,
                                num_dominated, dominated,
                                new JIntLiteral(self.getNumPop()) });
            }
        } else {
            if (KjcOptions.threadopt) {
                JExpression nextThread = new JIntLiteral(next);
                JExpression methodCall = new JMethodCallExpression(popName,
                        new JExpression[] { dyn_queue, index, nextThread,
                                num_dominated, dominated, num_tokens, tokens });
                methodCall.setType(tapeType);
                return methodCall;
            } else {
                JExpression methodCall = new JMethodCallExpression(popName,
                        new JExpression[] { dyn_queue, index,
                                num_dominated, dominated});
                methodCall.setType(tapeType);
                return methodCall;
            }
        }
    }

    private int getCoreID(InputPort inputPort) {
        Core core = SMPBackend.getComputeNode(inputPort.getSSG()
                .getTopFilters()[0].getWorkNode());
        return core.coreID;
    }

    private boolean isSource() {
        StaticSubGraph ssg = workNode.getParent().getStaticSubGraph();
        if (ssg.getFilterGraph()[0].equals(workNode.getParent())) {
            // If it is, then check what SSG it is connected to.
            // If that SSG has only 1 filter, and that filter is a
            // FileReader, then we should have a static pop instead
            // of a dynamic pop
            List<InterSSGEdge> edges = ssg.getInputPort().getLinks();
            for (InterSSGEdge edge : edges) {
                OutputPort outputPort = edge.getSrc();
                StaticSubGraph outputSSG = outputPort.getSSG();
                Filter[] filterGraph = outputSSG.getFilterGraph();
                if (filterGraph.length == 1) {
                    if (filterGraph[0].getWorkNode().isFileInput()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private JExpression staticPop(SIRPopExpression self, CType tapeType) {

        if (self.getNumPop() > 1) {
            return new JMethodCallExpression(popManyName,
                    new JExpression[] { new JIntLiteral(self.getNumPop()) });
        } else {
            JExpression methodCall = new JMethodCallExpression(popName,
                    new JExpression[0]);
            methodCall.setType(tapeType);
            return methodCall;
        }
    }

}