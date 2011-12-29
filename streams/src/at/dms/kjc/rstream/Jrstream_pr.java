package at.dms.kjc.rstream;

import at.dms.compiler.JavaStyleComment;
import at.dms.compiler.TokenReference;
import at.dms.kjc.JBlock;
import at.dms.kjc.JStatement;

/**
 * This class represents a rstream_pr block in the parse tree of 
 * the application.  Note that this class was not added to the visitors,
 * it will be visited as a JBlock, one must explicitly check if the JBlock is
 * a Jrstream_pr.
 * 
 * @author Michael Gordon
 * 
 */
public class Jrstream_pr extends JBlock
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4379306873807742522L;

	public Jrstream_pr(TokenReference where,
                       JStatement[] body,
                       JavaStyleComment[] comments)
    {
        super(where, body, comments);
    }
    

}
