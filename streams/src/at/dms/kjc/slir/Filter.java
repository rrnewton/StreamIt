package at.dms.kjc.slir;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import at.dms.kjc.CType;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.backendSupport.FilterInfo;
import at.dms.kjc.sir.SIRPhasedFilter;


public class Filter {

	/* Input distribution Pattern Fields */
		
	/** The incoming round robin weights for this filter for the steady and for init 
     * if the initWeights are null. */
    protected int[] inputWeights;
    /** if this filter requires a different joiner pattern for init, this will encode the weights */
    protected int[] initInputWeights;
    /** The sources that correspond to the weights for the steady and for init if initWeights/initSources
     * are null
     */
    protected Channel[] inputSources;
    /** if this filter requires a different joiner patter for init, this will encode the sources */
    protected Channel[] initInputSources;
    
    /* Output distribution pattern fields */
    
    /** the (round-robin) weight for each channel used for the steady and for init if this
     * node does not have a separate init pattern.
     */
    protected int[] outputWeights;
    /** Ordered array of sets of channels
     * The order in the outer array corresponds to the order of weights.
     * The inner array is just a set: the elements correspond to 
     * elements of duplicate splitters fused with the top-level
     * round-robin splitter (by synch removal).
     * A round-robin splitter of size n would be an Channel[n][1]
     * A duplicate splitter of size n would be an Channel[1][n]
     */
    protected Channel[][] outputDests;
    /** the weights for init if this node requires a different init splitting pattern */
    protected int[] initOutputWeights;
    /** the dest array for init if this node requires a different init splitting pattern */
    protected Channel[][] initOutputDests; 

    /* computation fields */
    /** Static unique id used in new name if one FilterContent created from another. */
    protected static int unique_ID = 0; 
    /** The unique id given to this FilterContent for use in constructing names */
    protected int my_unique_ID;
    /** Filter name */
    protected String name; 
    /** PreWork and Work method declarations */
    protected JMethodDeclaration[] prework, steady; 
    /** Input and output types */
    protected CType inputType,outputType; 
    /** Multiplicities from scheduler */
    protected int initMult, steadyMult; 
    /** Other method declarations */
    protected JMethodDeclaration[] methods;
    /** Init function for filter */
    protected JMethodDeclaration initFunction; 
    /** Is true when two-stage filter */
    protected boolean is2stage; 
    /** Field declarations */  
    protected JFieldDeclaration[] fields; 
    /** For linear filters, the pop count **/
    protected int popCount;
    /** For linear filters, the peek count **/
    protected int peek;
    

    public Filter(SIRPhasedFilter filter) {
    	my_unique_ID = unique_ID++;
    	name = filter.getName();
    	prework = filter.getInitPhases();
    	steady = filter.getPhases();
    	setInputType(filter.getInputType());
    	setOutputType(filter.getOutputType());
    	methods = filter.getMethods();
    	fields  =  filter.getFields();
    	//paramList = filter.getParams();
    	initFunction  =  filter.getInit();
    	assert prework.length < 2 && steady.length == 1;
    	//if this filter is two stage, then it has the 
    	//init work function as the only member of the init phases
    	is2stage = prework.length == 1;
    	//is2stage = steady.length > 1;
    }
    
    public void setInputType(CType type) {
            inputType = type; 
    }
    
    
    public void setOutputType(CType type) {
            outputType = type;
    }
    
    /**
     * Returns the pop count of this filter.
     */
    public int getPopCount() {
        return popCount;
    }

    /**
     * Returns the peek amount of this filter.
     */
    public int getPeek() {
        return peek;
    }

    /**
     * Returns if this filter is two-stage or not.
     */
    public boolean isTwoStage() 
    {
        return is2stage;
    }
    
    /**
     * Returns string representation of this FilterContent.
     */
    public String toString() {    
    	return name;
    }

    /**
     * Returns filter name of this FilterContent.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns input type of this FilterContent.
     */
    public CType getInputType() {
        return inputType;
    }

    /**
     * Returns output type of this FilterContent.
     */
    public CType getOutputType () {
        return outputType;
    }

    /**
     * Returns list of steady state method declarations.
     */
    public JMethodDeclaration[] getSteadyList() {
        return steady;
    }
    
    /**
     * Returns array of initialization state methods.
     */
    public JMethodDeclaration[] getPrework() {
        return prework;
    }

    /**
     * Returns work function.
     */
    public JMethodDeclaration getWork() {
        if(steady!=null)
            return steady[0];
        else
            return null;
    }

    /**
     * Returns init function.
     */
    public JMethodDeclaration getInit() {
        return initFunction;
    }

    /**
     * Returns multiplicity of init schedule.
     */
    public int getInitMult() {
        return initMult;
    }
    
    /**
     * Multiplies the steady state schedule by mult amount.
     * @param mult The number of times to multiply the steady state schedule by.
     */
    public void multSteadyMult(int mult) 
    {
        steadyMult *= mult;
    }
    
    public int getMult(SchedulingPhase phase) {
        if (SchedulingPhase.INIT == phase) 
            return initMult;
        return steadyMult;
    }
    
    /**
     * Returns the multiplicity of steady state schedule.
     */
    public int getSteadyMult() {
        return steadyMult;
    }

    /**
     * Set the name of this filter to n;
     *
     * @param n The new name of this filter
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Set the init multiplicity of this fitler to im;
     * 
     * @param im The new init multiplicity.
     */
    public void setInitMult(int im) {
        initMult = im;
    }
   
    
    /** 
     * return the number of items produced in the init stage.
     * 
     * @return the number of items produced in the init stage.
     */
    public int initItemsPushed() {
    	System.out.println(name);
        int items = steady[0].getPushInt() * initMult;
        if (isTwoStage()) {
            items -= steady[0].getPushInt();
            items += getPreworkPush();
        }
        return items;
    }
    
    /**
     * Return the number of items needed for this filter to fire 
     * in the initialization schedule.
     * 
     * @return the number of items needed for this filter to fire 
     * in the initialization schedule.
     */
    public int initItemsNeeded() {
        if (getInitMult() < 1)
            return 0;
        //the number of items needed after the prework function
        //executes and before the work function executes
        int bottomPeek = 0;
        //the init mult assuming everything is a two stage
        int myInitMult = getInitMult();
        int initPeek = 0;
        
        if (isTwoStage()) { 
            bottomPeek = Math.max(0, peek - (getPreworkPeek() - getPreworkPop()));
            //can't call init peek on non-twostages
            initPeek = getPreworkPeek();
        }
        else //if it is not a two stage, fake it for the following calculation
            myInitMult++;
            
        //(prePeek + bottomPeek + Math.max((initFire - 2), 0) * pop);
        return 
            initPeek + bottomPeek + Math.max((myInitMult - 2), 0) * 
                 getPopInt(); 
    }
    
    /**
     * Set the steady multiplicity of this filter to sm.
     * 
     * @param sm The new steady multiplicity.
     */
    public void setSteadyMult(int sm) {
        steadyMult = sm;
    }
    
    /**
     * Returns push amount.
     */
    public int getPushInt() {
        return steady[0].getPushInt();
    }

    /**
     * Returns pop amount.
     */
    public int getPopInt() {
        return steady[0].getPopInt();
    }

    /**
     * Returns peek amount.
     */
    public int getPeekInt() {
        return steady[0].getPeekInt();
    }

    /**
     * Returns push amount of init stage.
     * result may be garbage or error if !isTwoStage()
     */
    public int getPreworkPush() {
        return prework[0].getPushInt();
    }

    /**
     * Returns pop amount of init stage.
     * result may be garbage or error if !isTwoStage()
     */
    public int getPreworkPop() {
        return prework[0].getPopInt();
    }

    /**
     * Returns peek amount of init stage.
     * result may be garbage or error if !isTwoStage()
     */
    public int getPreworkPeek() {
        return prework[0].getPeekInt();
    }

    /**
     * Returns method declarations.
     */
    public JMethodDeclaration[] getMethods() {
        return methods;
    }
    
    /**
     * Returns field declarations.
     */
    public JFieldDeclaration[] getFields() 
    {
        return fields;
    }
    
    /**
     * Returns init-work method declaration.
     * result may be garbage or error if !isTwoStage()
     */
    public JMethodDeclaration getInitWork() {
        return prework[0];
    }
    
    /**
     * Set the init work of this filter to meth.
     * result may be garbage or error if !isTwoStage()
     * 
     * @param meth The new init work method.
     */
    public void setPrework(JMethodDeclaration meth) {
	if(meth == null) {
	    is2stage = false;
	    prework = new JMethodDeclaration[0];
	}
	else {
	    if (prework == null || prework.length == 0) {
		prework = new JMethodDeclaration[1];
	    }

	    is2stage = true;
	    prework[0] = meth;
	    addMethod(meth);
	}
    }

    /**
     * Method exists to allow SIRCodeUnit interface but should not be called.
     */
    public void addField(JFieldDeclaration field) {
        throw new AssertionError("should not call");
    }

    /** but subclasses can add fields */
    protected void addAField(JFieldDeclaration field) {
        JFieldDeclaration[] newFields = 
            new JFieldDeclaration[fields.length + 1];
        for (int i=0; i<fields.length; i++) {
            newFields[i] = fields[i];
        }
        newFields[fields.length] = field;
        this.fields = newFields;
    }
    
    /**
     * Method exists to allow SIRCodeUnit interface but should not be called.
     */
    public void addFields(JFieldDeclaration[] fields) {
        throw new AssertionError("should not call");
    }

    /**
     * Method exists to allow SIRCodeUnit interface but should not be called.
     */
    public void addMethod(JMethodDeclaration method) {
        JMethodDeclaration[] newMethods = new JMethodDeclaration[methods.length + 1];
        for (int i = 0; i < methods.length; i++)
            newMethods[i] = methods[i];
        newMethods[newMethods.length - 1] = method;
        methods = newMethods;
    }

    /** but subclasses can add methods */
    protected void addAMethod(JMethodDeclaration method) {
        JMethodDeclaration[] newMethods = 
            new JMethodDeclaration[methods.length + 1];
        for (int i=0; i<methods.length; i++) {
            newMethods[i] = methods[i];
        }
        newMethods[methods.length] = method;
        this.methods = newMethods;
    }
    
    /**
     * Method exists to allow SIRCodeUnit interface but should not be called.
     */
    public void addMethods(JMethodDeclaration[] methods) {
        throw new AssertionError("should not call");
    }

    /**
     * Method exists to allow SIRCodeUnit interface but should not be called.
     */
    public void setFields(JFieldDeclaration[] fields) {
        this.fields = fields;
        //throw new AssertionError("should not call");
    }

    /**
     * Method exists to allow SIRCodeUnit interface but should not be called.
     */
    public void setMethods(JMethodDeclaration[] methods) {
        throw new AssertionError("should not call");
    }
    
    /**
     * Allow subclasses to replace methods array
     */
    protected void setTheMethods(JMethodDeclaration[] methods) {
        this.methods = methods;
    }
    
    /* output distribution pattern methods */
    
    
    /** Set the weights for the steady state (and for init if this
     * node does not require a different pattern for init) */
    public void setOutputWeights(int[] newW) {
        this.outputWeights = newW;
    }

    /** Set the weights for the init stage, this means that init will 
     * have a splitting pattern that is different from steady 
     * */
    public void setInitOutputWeights(int[] newW) {
        this.initOutputWeights = newW;
    }
    
    /**
     * Set the steady weights and dests of the output
     * weights and dests.
     * 
     * @param weights List of integer weights.
     * @param dests List of Lists of Channel for splitting pattern.
     */
    public void setOutput(LinkedList<Integer> weights, 
            LinkedList<LinkedList<Channel>> dests, SchedulingPhase phase) {
        int[] newWeights;
        Channel[][] newDests;

        if (weights.size() == 1) 
            newWeights = new int[]{1};
        else {
            newWeights = new int[weights.size()];
            for (int i = 0; i < weights.size(); i++)
                newWeights[i] = weights.get(i).intValue();
        }

        //convert the dests list
        int i = 0;
        newDests = new Channel[dests.size()][];
        for(LinkedList<Channel> dest : dests)
            newDests[i++] = dest.toArray(new Channel[0]);

        if (SchedulingPhase.INIT == phase) {
            setInitOutputWeights(newWeights);
            setInitOutputDests(newDests);
        } else {
            setOutputWeights(newWeights);
            setOutputDests(newDests);
        }
    }
    
    /**
     * Set the steady weights and dests of this output to
     * weights and dests.
     * 
     * @param weights Array of integer weights
     * @param dests Array of Channel arrays for splitting pattern.
     */
    public void setOutput(int[] weights, Channel[][] dests, SchedulingPhase phase) {
        if (SchedulingPhase.INIT == phase) {
            setInitOutputWeights(weights);
            setInitOutputDests(dests);
        } else {
            setOutputWeights(weights);
            setOutputDests(dests);
        }
    }
        
    /** @return the weights */
    public int[] getOutputWeights(SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT && initOutputWeights != null)
            return initOutputWeights;
        return outputWeights;
    }

    /** 
     * @return the weights for the initialization stage, note that this may be null
     * if the splitting pattern for init is the same as steady. 
     */
    public int[] getInitOutputWeights() {
        return initOutputWeights;
    }
    
    /** @return whether previous filter was FileInput */
    public boolean isFileInput() {
        return this instanceof FileReaderFilter;
    }

    /** @return dests */
    public Channel[][] getOutputDests(SchedulingPhase phase) {
        if (phase == SchedulingPhase.INIT && initOutputDests != null)
            return initOutputDests;
        return outputDests;
    }

    /** Set dests */
    public void setOutputDests(Channel[][] dests) {
        this.outputDests = dests;
    }

    /** 
     * Return the initialization pattern for splitting.  Note that this may be null
     * if the pattern is the same as the steady pattern. 
     * @return dests 
     */
    public Channel[][] getInitOuttputDests() {
        return initOutputDests;
    }

    /** 
     * Set the initialization pattern for splitting.
     */
    public void setInitOutputDests(Channel[][] newDests) {
        this.initOutputDests = newDests;
    }

    /** @return total of weights */
    public int totalOutputWeights(SchedulingPhase phase) {
        int sum = 0;
        for (int i = 0; i < getOutputWeights(phase).length; i++)
            sum += getOutputWeights(phase)[i];
        return sum;
    }

    /**
     * Combine the weights of adjacent outputs that have equal 
     * destinations.
     * This operation exists as a cleanup operation for synch removal.
     */
    public void canonicalizeOutput(SchedulingPhase phase) {
        if (getOutputWeights(phase).length == 0)
            return;

        int[] weights = new int[getOutputWeights(phase).length];
        Channel[][] edges = new Channel[getOutputWeights(phase).length][];
        int curPort = 0;

        //add the first port to the new edges and weights
        edges[0] = getOutputDests(phase)[0];
        weights[0] = getOutputWeights(phase)[0];

        for(int i = 1 ; i < getOutputWeights(phase).length ; i++) {
            if(Util.setCompare(edges[curPort], getOutputDests(phase)[i])) {
                weights[curPort] += getOutputWeights(phase)[i];
            }
            else {
                curPort++;
                edges[curPort] = getOutputDests(phase)[i];
                weights[curPort] = getOutputWeights(phase)[i];
            }
        }

        Channel[][] newEdges = new Channel[curPort + 1][];
        int[] newWeights = new int[curPort + 1];

        System.arraycopy(edges, 0, newEdges, 0, curPort + 1);
        System.arraycopy(weights, 0, newWeights, 0, curPort + 1);

        //set the new weights and the dests
        setOutput(newWeights, newEdges, phase);
    }
    
    /**
     * Return the width of this splitter meaning the number
     * of connections it has to downstream filters, including 
     * all the edges of a duplicated item, counting each unique 
     * channel once.
     * 
     * @return The width of this splitter.
     */
    public int getOutputWidth(SchedulingPhase phase) {
        return getOutputDestSet(phase).size();
    }
    
    /**
     * Return true if the weight duplicates to channel during the scheduling phase.
     */
    public boolean outputWeightDuplicatesTo(int weight, Channel channel, SchedulingPhase phase) {
        Channel[][] dests = getOutputDests(phase);
        
        for (int d = 0; d < dests[weight].length; d++) {
            if (dests[weight][d] == channel)
                return true;
        }
        return false;
    }
    
    /**
     * Return a list of the edges with each channel appearing once
     * and ordered by the order in which each channel appears in the
     * split pattern.
     * 
     * @return The list.
     */
    public LinkedList<Channel> getOutputDestSequence(SchedulingPhase phase) {
        
        LinkedList<Channel> list = new LinkedList<Channel>();
        for (int i = 0; i < getOutputDests(phase).length; i++) {
            for (int j = 0; j < getOutputDests(phase)[i].length; j++) 
                if (!list.contains(getOutputDests(phase)[i][j]))
                    list.add(getOutputDests(phase)[i][j]);
        }
        return list;
    }
    
    /**
     * return the number of items sent by the output on all instances of a particular channel.
     */
    public int getOutputWeight(Channel in, SchedulingPhase phase) {
        int sum = 0;

        for (int i = 0; i < getOutputDests(phase).length; i++) {
            for (int j = 0; j < getOutputDests(phase)[i].length; j++) {
                if (getOutputDests(phase)[i][j] == in) {
                    sum += getOutputWeights(phase)[i];
                    break;
                }
            }
        }
        return sum;
    }

  
    /**
     * Return a set of all the destination filters of this filter
     * 
     * @return a set of all the destination filters of this filter
     */
    public Set<Filter> getOutputDestFilters(SchedulingPhase phase) {
        HashSet<Filter> dests = new HashSet<Filter>();
        
        for (Channel channel : getOutputDestSet(phase)) {
            dests.add(channel.getDest());
        }
        
        return dests;
    }
    
    /**
     * Return a list of the dests in round-robin order flattening
     * the duplicates.  
     * 
     * @return A list of the dests in round-robin order flattening
     * the duplicates.  
     */ 
    public Channel[] getOutputDestList(SchedulingPhase phase) {
        
        LinkedList<Channel> edges = new LinkedList<Channel>();
        for (int i = 0; i < getOutputDests(phase).length; i++) {
            for (int j = 0; j < getOutputDests(phase)[i].length; j++)
                edges.add(getOutputDests(phase)[i][j]);
        }
        return edges.toArray(new Channel[edges.size()]);
    }
    
    /**
     * Return the set of the outgoing edges of this filter
     * 
     * @return The set of the outgoing edges of this filter
     */
    public Set<Channel> getOutputDestSet(SchedulingPhase phase) {
        HashSet<Channel> set = new HashSet<Channel>();
        for (int i = 0; i < getOutputDests(phase).length; i++) {
            for (int j = 0; j < getOutputDests(phase)[i].length; j++)
                set.add(getOutputDests(phase)[i][j]);
        }
        return set;
    }

    /**
     * @return true if each output channel appears only once in the schedule of splitting
     * for both init and steady
     */
    public boolean outputSingleAppearance() {
        return outputSingleAppearance(SchedulingPhase.STEADY) && outputSingleAppearance(SchedulingPhase.INIT);
    }
    
    /**
     * @return true if each output channel appears only once in the schedule of splitting
     */
    public boolean outputSingleAppearance(SchedulingPhase phase) {
        return getOutputDestSet(phase).size() == getOutputDestList(phase).length;
    }
    
    /**
     * Return true if this output has one output in the steady state and one or 0 outputs
     * in the init stage.
     */
    public boolean oneOutput(SchedulingPhase phase) {
        return (getOutputWeights(phase).length == 1 && getOutputDests(phase).length == 1 && getOutputDests(phase)[0].length == 1);
    }
    
    public Channel getSingleOutputChannel(SchedulingPhase phase) {
        assert oneOutput(phase) : "Calling getSingleOutputEdge() on Filter with less/more than one output";
        //System.out.println(getParent() + " " + phase);
        return getOutputDests(phase)[0][0];
    }
    
    public boolean noOutputs() {
        return noOutputs(SchedulingPhase.INIT) && noOutputs(SchedulingPhase.STEADY);
    }

    public boolean noOutputs(SchedulingPhase phase) {
                return getOutputWeights(phase).length == 0;
    }
    
    public boolean isDuplicateSplitter(SchedulingPhase phase) {
        return (getOutputWeights(phase).length == 1 && getOutputDests(phase).length == 1 && getOutputDests(phase)[0].length >= 2);
    }
    
    public boolean isRRSplitter(SchedulingPhase phase) {
        return (getOutputWeights(phase).length >=2 && getOutputDests(phase).length >= 2);
    }
    
    public boolean isSplitter(SchedulingPhase phase) {
        return (isDuplicateSplitter(phase) || isRRSplitter(phase));
    }

    /**
     * return an iterator that iterates over the output channels in descending
     * order of the number of items sent to the output
     */
    public List<Channel> getSortedOutputs(SchedulingPhase phase) {
        LinkedList<Channel>sortedOutputs = new LinkedList<Channel>();  
        // if there are no dest just return an empty iterator
        if (outputWeights.length == 0) {
            return sortedOutputs;
        }
        // just do a simple linear insert over the dests
        // only has to be done once
        Vector<Channel> sorted = new Vector<Channel>();
        Iterator<Channel> destsIt = getOutputDestSet(phase).iterator();
        // add one element
        sorted.add(destsIt.next());
        while (destsIt.hasNext()) {
            Channel current = destsIt.next();
            // add to end if it is less then everything
            if (getOutputWeight(current, phase) <= getOutputWeight(sorted.get(sorted
                    .size() - 1), phase))
                sorted.add(current);
            else { // otherwise find the correct place to add it
                for (int i = 0; i < sorted.size(); i++) {
                    // if this is the correct place to insert it,
                    // add it and break
                    if (getOutputWeight(current, phase) > getOutputWeight(sorted.get(i), phase)) {
                        sorted.add(i, current);
                        break;
                    }
                }
            }
        }
        assert sorted.size() == getOutputDestSet(phase).size() : "error "
            + sorted.size() + "!= " + getOutputDestSet(phase).size();
        sortedOutputs = (LinkedList<Channel>)sorted.subList(0, sorted.size());

        return sortedOutputs;
    }
    
    public double outputRatio(Channel channel, SchedulingPhase phase) {
        if (totalOutputWeights(phase) == 0)
            return 0.0;
        return ((double) getOutputWeight(channel, phase) / (double) totalOutputWeights(phase));
    }

    /**
     * Return the sum of the weights before index of the weights array.
     */
    public int outputWeightBefore(int index, SchedulingPhase phase) {
        assert index < outputWeights.length;
        int total = 0;
                
        for (int i = 0; i < index; i++)
            total += outputWeights[i];
        
        return total;
    }
    
    /**
     * Return true if this output has the typical fission peeking pattern which is
     * a bunch to one channel, then some duplicated to that channel and another channel
     * 
     * Also return true if it is a simple pattern, meaning no splitting
     * 
     * @param phase
     * 
     */
    public boolean peekingFissionPattern(SchedulingPhase phase) {
        if (noOutputs(phase) || oneOutput(phase))
            return true;
    
        //these checks might die because of null pointers if there aren't 2 outputs etc.
        //so just catch them and return false.
        try {
            if (getOutputWidth(phase) == 2 && getOutputDests(phase).length == 2 &&
                    getOutputDests(phase)[0].length == 1 &&
                    (getOutputDests(phase)[1][0] == getOutputDests(phase)[0][0] ||
                            getOutputDests(phase)[1][1] == getOutputDests(phase)[0][0])) {
                return true;
            }
        } catch (Exception e) {
            
        }
        return false;
    }   
    
    /**
     * Return the sum of weights for edges before this channel appears in the splitting schedule.
     * The output pattern node must be single appearance.
     * 
     * @param channel The channel in question
     * @return The total weights before channel
     */
    public int outputWeightBefore(Channel channel, SchedulingPhase phase) {
        assert outputSingleAppearance();
        int total = 0;
        
        for (int w = 0; w < getOutputWeights(phase).length; w++ ) {
            boolean found = false;
            //see if the channel is in this dest list
            for (int d = 0; d < getOutputDests(phase)[w].length; d++) {
                if (getOutputDests(phase)[w][d] == channel) {
                    found = true;
                    break;
                }
            }
            if (found) {
                return total;
            }
            total += getOutputWeights(phase)[w];
        }
        assert false;
        return 0;
    }
    
    
}
