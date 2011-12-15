/**
 * 
 */
package at.dms.kjc.smp;

import at.dms.kjc.KjcOptions;
import at.dms.kjc.backendSupport.BackEndFactory;
import at.dms.kjc.backendSupport.BackEndScaffold;
import at.dms.kjc.backendSupport.IntraSSGChannel;
import at.dms.kjc.backendSupport.CodeStoreHelper;
import at.dms.kjc.slir.IntraSSGEdge;
import at.dms.kjc.slir.WorkNode;
import at.dms.kjc.slir.InputNode;
import at.dms.kjc.slir.LevelizeSSG;
import at.dms.kjc.slir.OutputNode;
import at.dms.kjc.slir.SchedulingPhase;
import at.dms.kjc.slir.Filter;
import at.dms.kjc.slir.InternalFilterNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mgordon
 * 
 */
public class SMPBackEndFactory extends
		BackEndFactory<SMPMachine, Core, SMPComputeCodeStore, Integer> {

	/**
	 * Set the scheduler used by the backend factory
	 * 
	 * @param scheduler
	 */
	public static void setScheduler(Scheduler scheduler) {
		SMPBackEndFactory.scheduler = scheduler;
	}

	private SMPMachine chip;

	private SMPBackEndScaffold scaffold;
	/** scheduler used by backend */
	private static Scheduler scheduler;
	/** splits the slicegraph into levels */
	private static LevelizeSSG lsg;

	private Map<String, String> dominators;
	private Map<Filter, Integer> filterToThreadId;
	/**
	 * the number of filters that we have yet to process from a level the init
	 * stage
	 */
	private static HashMap<Integer, Integer> levelLeftToProcessInit;

	/**
	 * the number of filters that we have yet to process from a level the init
	 * stage
	 */
	private static HashMap<Integer, Integer> levelLeftToProcessPP;

	public SMPBackEndFactory(SMPMachine chip, Scheduler scheduler,
			Map<String, String> dominators,
			Map<Filter, Integer> filterToThreadId) {
		this.chip = chip;
		SMPBackEndFactory.scheduler = scheduler;
		this.setLayout(scheduler);
		this.dominators = dominators;
		this.filterToThreadId = filterToThreadId;
		
	
		if (scheduler.isTMD()) {
			// levelize the slicegraph
			lsg = new LevelizeSSG(scheduler.getGraphSchedule().getSSG()
					.getTopFilters());

			// fill the left to process maps with the number of filters in a
			// level
			levelLeftToProcessInit = new HashMap<Integer, Integer>();
			levelLeftToProcessPP = new HashMap<Integer, Integer>();

			Filter[][] levels = lsg.getLevels();
			for (int i = 0; i < levels.length; i++) {
				levelLeftToProcessInit.put(i, levels[i].length);
				levelLeftToProcessPP.put(i, levels[i].length);
			}
		}

		scaffold = new SMPBackEndScaffold(dominators);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.BackEndFactory#getBackEndMain()
	 */

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BackEndScaffold> T getBackEndMain() {
		return (T) scaffold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#getChannel(at.dms.kjc.slicegraph
	 * .SliceNode, at.dms.kjc.slicegraph.SliceNode)
	 */
	@Override
	public IntraSSGChannel getChannel(InternalFilterNode src,
			InternalFilterNode dst) {
		assert false;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#getChannel(at.dms.kjc.slicegraph
	 * .Edge)
	 */
	@Override
	public IntraSSGChannel getChannel(IntraSSGEdge e) {
		assert false;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#getCodeStoreHelper(at.dms.kjc
	 * .slicegraph.SliceNode)
	 */
	@Override
	public CodeStoreHelper getCodeStoreHelper(InternalFilterNode node) {
		if (node instanceof WorkNode) {
			// simply do appropriate wrapping of calls...
			return new SMPCodeStoreHelper((WorkNode) node, this, scheduler
					.getComputeNode(node).getComputeCode());
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#getComputeCodeStore(at.dms.kjc
	 * .backendSupport.ComputeNode)
	 */
	@Override
	public SMPComputeCodeStore getComputeCodeStore(Core parent) {
		return parent.getComputeCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#getComputeNode(java.lang.Object)
	 */
	@Override
	public Core getComputeNode(Integer coreNum) {
		return chip.getNthComputeNode(coreNum.intValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.dms.kjc.backendSupport.BackEndFactory#getComputeNodes()
	 */
	@Override
	public SMPMachine getComputeNodes() {
		return chip;
	}

	/**
	 * Return the scheduler used by the backend factory
	 * 
	 * @return Scheduler
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#processInputSliceNode(at.dms
	 * .kjc.slicegraph.InputSliceNode,
	 * at.dms.kjc.backendSupport.SchedulingPhase,
	 * at.dms.kjc.backendSupport.ComputeNodesI)
	 */
	@Override
	public void processFilterInputNode(InputNode input,
			SchedulingPhase whichPhase, SMPMachine chip) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#processOutputSliceNode(at.dms
	 * .kjc.slicegraph.OutputSliceNode,
	 * at.dms.kjc.backendSupport.SchedulingPhase,
	 * at.dms.kjc.backendSupport.ComputeNodesI)
	 */
	@Override
	public void processFilterOutputNode(OutputNode output,
			SchedulingPhase whichPhase, SMPMachine chip) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#processFilterSlices(at.dms.kjc
	 * .slicegraph.Slice, at.dms.kjc.backendSupport.SchedulingPhase,
	 * at.dms.kjc.backendSupport.ComputeNodesI)
	 */
	@Override
	public void processFilterSlices(Filter slice, SchedulingPhase whichPhase,
			SMPMachine chip) {
		assert false : "The SMP backend does not support slices with multiple filters (processFilterSlices()).";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.dms.kjc.backendSupport.BackEndFactory#processFilterSliceNode(at.dms
	 * .kjc.slicegraph.FilterSliceNode,
	 * at.dms.kjc.backendSupport.SchedulingPhase,
	 * at.dms.kjc.backendSupport.ComputeNodesI)
	 */
	public void processFilterWorkNode(WorkNode filter,
			SchedulingPhase whichPhase, SMPMachine chip) {
		
		// switch (whichPhase) {
		// case PRIMEPUMP:
		// System.out.println("SMPBackEndFactory.processFilterWorkNode() whichPhase = PRIMEPUMP");
		// case INIT:
		// System.out.println("SMPBackEndFactory.processFilterWorkNode() whichPhase = INIT");
		// case STEADY:
		// System.out.println("SMPBackEndFactory.processFilterWorkNode() whichPhase = STEADY");
		// case PREINIT:
		// System.out.println("SMPBackEndFactory.processFilterWorkNode() whichPhase = PREINIT");
		//
		// }

		if (filter.isPredefined()) {
			if (filter.isFileInput()) {
				System.out
						.println("SMPBackEndFactory.processFilterWorkNode filter.isFileInput()=true");
				(new ProcessFileReader(filter, whichPhase, this))
						.processFileReader();
			} else if (filter.isFileOutput()) {
				(new ProcessFileWriter(filter, whichPhase, this))
						.processFileWriter();
			}
		} else {
			if (KjcOptions.sharedbufs
					&& FissionGroupStore.isFizzed(filter.getParent())) {
				for (Filter slice : FissionGroupStore.getFizzedSlices(filter
						.getParent())) {

					new ProcessFilterWorkNode().doit(slice.getWorkNode(),
							whichPhase, this);
				}
			} else {
				new ProcessFilterWorkNode().doit(filter, whichPhase, this);
			}

			if (scheduler.isTMD() && !KjcOptions.noswpipe) {
				// if we are using the tmd scheduler we have to add barriers
				// between each
				// init/primepump call of different levels
				// so we keep a hashmap that will tell us how many more filters
				// needs to be
				// processed in the level so that we only add the barrier after
				// the last to be processed
				// so after the entire level has executed

				if (whichPhase == SchedulingPhase.INIT) {
					int level = lsg.getLevel(filter.getParent());
					int leftToProcess = levelLeftToProcessInit.get(level);
					leftToProcess--;
					levelLeftToProcessInit.put(level, leftToProcess);
					if (leftToProcess == 0)
						SMPComputeCodeStore.addBarrierInit();
				} else if (whichPhase == SchedulingPhase.PRIMEPUMP) {
					int level = lsg.getLevel(filter.getParent());
					int leftToProcess = levelLeftToProcessPP.get(level);
					leftToProcess--;
					levelLeftToProcessPP.put(level, leftToProcess);
					if (leftToProcess == 0) {
						SMPComputeCodeStore.addBarrierInit();
						levelLeftToProcessPP.put(level,
								lsg.getLevels()[level].length);
					}
				}
			}		
		}
	}

	public Map<String, String> getDominators() {
		return dominators;
	}

	public void setDominators(Map<String, String> dominators) {
		this.dominators = dominators;
	}

	public Map<Filter, Integer> getFilterToThreadId() {
		return filterToThreadId;
	}

	public void setFilterToThreadId(Map<Filter, Integer> filterToThreadId) {
		this.filterToThreadId = filterToThreadId;
	}
}
