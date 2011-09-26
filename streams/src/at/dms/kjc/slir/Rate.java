package at.dms.kjc.slir;

public class Rate {
	private int min, max, avg;
	private boolean hasMin, hasMax, hasAvg;
	private boolean isDynamic;
	
	public int getStaticRate() {
		assert ! isDynamic;
		return avg;
	}
	

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.slir.Rate other = new at.dms.kjc.slir.Rate();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.slir.Rate other) {
        other.min = this.min;
        other.max = this.max;
        other.avg = this.avg;
        other.hasMin = this.hasMin;
        other.hasMax = this.hasMax;
        other.hasAvg = this.hasAvg;
        other.isDynamic = this.isDynamic;
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}
