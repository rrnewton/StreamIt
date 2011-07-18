package at.dms.kjc.slir;

public class Rate {
	private int min, max, avg;
	private boolean hasMin, hasMax, hasAvg;
	private boolean isDynamic;
	
	public int getStaticRate() {
		assert ! isDynamic;
		return avg;
	}
	
}
