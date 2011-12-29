package at.dms.kjc.sir;

import at.dms.kjc.CStdType;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JIntLiteral;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JStringLiteral;
import at.dms.kjc.sir.lowering.Propagator;

/**
 * A StreaMIT filter that writes a file to a data source. It is more
 * general than the SIRFileWriter in that in can also write to stdout
 * or stderr.
 */
public class SIRWriter extends SIRPredefinedFilter implements Cloneable {

	private static final long serialVersionUID = 1L;

	/**
	 * The filename of the data source.
	 */
	private JExpression fileName;

	public SIRWriter() {
		super(null,
				"Writer",
				/* fields */ JFieldDeclaration.EMPTY(),
				/* methods */ JMethodDeclaration.EMPTY(),
				new JIntLiteral(null, 1),
				new JIntLiteral(null, 1),
				new JIntLiteral(null, 0),
				null, CStdType.Void);
		this.fileName = new JStringLiteral("");
	}

	public void setFileName(JExpression fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		if (!(fileName instanceof JStringLiteral)) {
			System.err.println("Error:  have not yet resolved filename for filereader.\n" +
					"        the filename expression is " + fileName);
			new RuntimeException().printStackTrace();
			System.exit(1);
		}
		return ((JStringLiteral)fileName).stringValue();
	}

	@Override
	public void propagatePredefinedFields(Propagator propagator) {
		JExpression newFilename = (JExpression)fileName.accept(propagator);
		if (newFilename!=null && newFilename!=fileName) {
			fileName = newFilename;
		}
	}

}
