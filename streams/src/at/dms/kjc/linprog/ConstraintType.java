package at.dms.kjc.linprog;

import java.io.Serializable;

/**
 * Representation of a constraint type for linprog package.
 */

class ConstraintType implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5877401730468488287L;
	// for objective function only
    public static final ConstraintType OBJ = new ConstraintType("OBJ");
    // for GE
    public static final ConstraintType GE = new ConstraintType("GE");
    // for EQ
    public static final ConstraintType EQ = new ConstraintType("EQ");

    private final String name;
    private ConstraintType(String name) {
        this.name = name;
    }

    @Override
	public String toString() {
        return "Constraint type: " + name;
    }

    @Override
	public boolean equals(Object o) {
        return (o instanceof ConstraintType &&
                ((ConstraintType)o).name.equals(this.name));
    }
}
