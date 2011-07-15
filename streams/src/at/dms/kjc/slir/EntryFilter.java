package at.dms.kjc.slir;

import at.dms.kjc.CType;
import at.dms.kjc.sir.SIRDummySource;
import at.dms.kjc.sir.SIRPhasedFilter;

public class EntryFilter extends Filter {
	public EntryFilter(CType type) {
		super(new SIRDummySource(type));
		// TODO Auto-generated constructor stub
	}
}
