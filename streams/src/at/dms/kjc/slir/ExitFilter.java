package at.dms.kjc.slir;

import at.dms.kjc.CType;
import at.dms.kjc.sir.SIRDummySink;
import at.dms.kjc.sir.SIRPhasedFilter;

public class ExitFilter extends Filter {

	public ExitFilter(CType type) {
		super(new SIRDummySink(type));
		// TODO Auto-generated constructor stub
	}
	
}
