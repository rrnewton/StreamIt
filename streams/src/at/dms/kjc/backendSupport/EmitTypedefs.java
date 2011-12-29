package at.dms.kjc.backendSupport;

import java.util.HashSet;
import java.util.Set;

import at.dms.kjc.CType;
import at.dms.kjc.CVectorType;
import at.dms.kjc.JExpression;
import at.dms.kjc.JFieldDeclaration;
import at.dms.kjc.JMethodDeclaration;
import at.dms.kjc.JVariableDefinition;
import at.dms.kjc.SLIREmptyVisitor;
import at.dms.kjc.common.CodegenPrintWriter;
import at.dms.kjc.common.CommonUtils;
import at.dms.kjc.sir.SIRStructure;

/**
 * Emit code defining the vector types and structure types used in the program.
 * 
 * @author dimock
 */
public class EmitTypedefs {

    /**
     * Create typedefs and other general header info.
     * @param structs       Structures detected by front end.
     * @param backendbits   BackEndFactory to get access to rest of code.
     * @param p             a CodeGenPrintWriter on which to emit the C code.
     */
    static public void emitTypedefs(SIRStructure[] structs, BackEndFactory backendbits, CodegenPrintWriter p) {

        // mess for getting typedefs for all vector types in program into vectorTypeDefs
        final Set<String> vectorTypeDefs = new HashSet<String>();
        for (int i = 0; i < backendbits.getComputeNodes().size(); i++) {
            ComputeCodeStore c = backendbits.getComputeNodes().getNthComputeNode(i).getComputeCode();
            for (JFieldDeclaration m : c.getFields()) {
                m.accept(new SLIREmptyVisitor(){
                    @Override
                    public void visitVariableDefinition(JVariableDefinition self,
                            int modifiers, CType type, String ident, JExpression expr) {
                        if (type instanceof CVectorType) {
                            vectorTypeDefs.add(((CVectorType)type).typedefString());
                        }
                    }
                });
            }
            for (JMethodDeclaration m : c.getMethods()) {
                m.accept(new SLIREmptyVisitor(){
                    @Override
                    public void visitVariableDefinition(JVariableDefinition self,
                            int modifiers, CType type, String ident, JExpression expr) {
                        if (type instanceof CVectorType) {
                            vectorTypeDefs.add(((CVectorType)type).typedefString());
                        }
                    }
                });
            }
        }
        for (String v : vectorTypeDefs) {
            p.println(v);
        }
        p.println(CVectorType.miscStrings());

        // structs
        for (SIRStructure s : structs) {
            p.println(CommonUtils.structToTypedef(s, false));
        }
    }
}
