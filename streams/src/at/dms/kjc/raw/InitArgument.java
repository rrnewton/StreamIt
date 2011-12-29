package at.dms.kjc.raw;

import java.util.List;

import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldAccessExpression;
import at.dms.kjc.JLocalVariableExpression;
import at.dms.kjc.sir.SIRFilter;


/* This class finds the init statement call for a given filter and
   returns a string representing the args of the init call */
public class InitArgument {
    public static String getInitArguments(SIRFilter tar) {
        // get parameters from parent
        List params = tar.getParams();
        StringBuffer buf = new StringBuffer();

        // convert to string
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) instanceof JFieldAccessExpression ||
                params.get(i) instanceof JLocalVariableExpression) {
                if (((JExpression)params.get(i)).getType().isArrayType()) {
                    buf.append("0/*array*/,");
                    continue;
                }
                else
                    System.err.println("Found a non-constant in an init function call");
            }
            FlatIRToC ftoc = new FlatIRToC();
            ((JExpression)params.get(i)).accept(ftoc);
            buf.append(ftoc.getPrinter().getString() + ",");
        }
        if (buf.length() > 0) {
            buf.setCharAt(buf.length() - 1, ' ');
        }

        // return
        return buf.toString();
    }
}
