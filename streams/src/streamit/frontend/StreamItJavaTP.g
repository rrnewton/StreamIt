/*
 * StreamItJavaTP.g: ANTLR TreeParser for StreamIt->Java conversion
 * David Maze <dmaze@cag.lcs.mit.edu>
 * $Id: StreamItJavaTP.g,v 1.20 2002-07-20 17:38:59 dmaze Exp $
 */

header {
	package streamit.frontend;
	import streamit.frontend.tojava.*;
	import streamit.frontend.nodes.*;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.Iterator;
	import java.util.List;
	import java.util.Map;
}

options {
	mangleLiteralPrefix = "TK_";
	//language="Cpp";
}

class StreamItJavaTP extends TreeParser;
options {
  importVocab=StreamItLex;	// use vocab generated by lexer
  k=1;
}
{
	int indent = 0;
	StreamType cur_type = null;
	List cur_class_params = null;
	String cur_class_name = null;
	Map structs = new HashMap();
	NodesToJava n2j = new NodesToJava(null);
	ComplexProp cplx_prop = new ComplexProp();
	TempVarGen varGen;
	SymbolTable symTab = null;
	String getIndent ()
	{
		String result = "";
		int x = indent;
		while (x-- > 0) result = result + "    ";
		return result;
	}

	// For debugging:
	public void reportError(RecognitionException ex)
	{
		ex.printStackTrace(System.err);
	}
}

program
{
	String t;
	System.out.println ("import streamit.*;");
	System.out.println ("import streamit.io.*;\n");
	System.out.println ("class Complex extends Structure {");
	System.out.println ("  public double real, imag;");
	System.out.println ("}\n");
}
	: (t=stream_decl 
			{ System.out.println (t != null ? t : "no match"); } 
		)*
	;

/*
 * Top-level stream declarations.
 */

stream_decl returns [String t] {t = null;}
	:	t=filter_decl
	|	t=struct_stream_decl
	;

filter_decl returns [String t]
{
	t = "";
	StreamType type;
	StreamType last_type = cur_type;
	NodesToJava last_n2j = n2j;
	String body = "";
	List params = null;
	symTab = new SymbolTable(symTab);
}
	: #(
			TK_filter 
			type=stream_type { cur_type = type; n2j = new NodesToJava(cur_type); }
			name:ID
			{
				t += getIndent() + "class " + name.getText() +
					" extends Filter\n";
				t += getIndent() + "{\n";
				indent++;
				cur_class_name = name.getText();
			}
			(params=stream_param_list
				{
					Iterator iter = params.iterator();
					while (iter.hasNext())
					{
						VariableDeclaration param =
							(VariableDeclaration)iter.next();
						t += param.getField(n2j, getIndent());
					}
					cur_class_params = params;
				}
			)?
			LCURLY
			body=filter_body
			{
				indent--;
				cur_type = last_type;
				n2j = last_n2j;
				cur_class_params = null;
				cur_class_name = null;
				symTab = symTab.getParent();
				t += body + getIndent() + "}\n";
			}
		) 
	;

filter_body returns [String t]
{
	t = "";
	InitFunction init = new InitFunction();
	WorkFunction work = null;
	String dcl;
}
	: ( init=init_func_decl
		| work=work_func_decl
		| (variable_decl) => dcl=variable_decl
			{ t += (getIndent () + dcl + ";" + "\n") ; }
		| (function_decl) => dcl=function_decl
			{ t += (getIndent () + dcl); }
		)*
		{
			t += getIndent() + "public void work() " + work.body + "\n";
			t += init.getText(indent, cur_class_params, cur_type, work, n2j);
			t += init.getConstructor(indent, cur_class_params, cur_class_name, n2j);
		}
	;

struct_stream_decl returns [String t] { t = ""; }
	: #(TK_splitjoin t=struct_stream_decl2["SplitJoin"])
	| #(TK_pipeline t=struct_stream_decl2["Pipeline"])
	| #(TK_feedbackloop t=struct_stream_decl2["FeedbackLoop"])
	;

struct_stream_decl2[String superclass] returns [String t]
{
	t = "";
	StreamType type = null;
	String body = null;
	List params = null;
	InitFunction init = new InitFunction();
	symTab = new SymbolTable(symTab);
}
	:
		type=stream_type name:ID
		{
			if (type.getIn() instanceof TypePrimitive &&
				((TypePrimitive)type.getIn()).getType() ==
					TypePrimitive.TYPE_VOID &&
				type.getOut() instanceof TypePrimitive &&
				((TypePrimitive)type.getOut()).getType() ==
					TypePrimitive.TYPE_VOID)
				superclass = "StreamIt";
			t += getIndent() + "class " + name.getText () +
				" extends " + superclass + "\n" + "{\n";
			indent++;
		}
		(params=stream_param_list
			{
				Iterator iter = params.iterator();
				while (iter.hasNext())
				{
					VariableDeclaration param =
						(VariableDeclaration)iter.next();
					t += param.getField(n2j, getIndent());
				}
				cur_class_params = params;
			}
		)?
		{ indent++; }
		body=block { init.body = body; }
		{ indent -= 2; }
		{
			if (superclass.equals("StreamIt"))
			{
				indent++;
				t = t + 
				getIndent () + "static public void main (String [] args)\n" +
				getIndent () + "{\n";
				indent++;
				t = t +
				getIndent () + "new " + name.getText () + "().run (args);\n";
				indent--;
				t = t + getIndent () + "}\n";
				indent--;
			}
			t += init.getText(indent+1, cur_class_params, null, null, n2j);
			t += init.getConstructor(indent+1, cur_class_params,
				name.getText(), n2j);
			t += getIndent() + "}\n";
			symTab = symTab.getParent();
		}
	;

stream_type returns [StreamType type]
{
	type = null;
	Type ft, tt;
}
	: #(ARROW ft=data_type tt=data_type)
		{ type = new StreamType(ft, tt); }
	;

stream_param_list returns [List lst]
{
	lst = new ArrayList();
	VariableDeclaration param;
}
	: #(LPAREN (param=stream_param { lst.add(param); })*)
	;

stream_param returns [VariableDeclaration var]
{
	var = new VariableDeclaration();
	Type type;
}
	: #(name:ID type=data_type)
		{ var.type = type; var.name = name.getText();
			symTab.register(name.getText(), type); }
	;

data_type returns [Type t] {t = null; Expression x;}
	: #(LSQUARE t=primitive_type (x=expression { t = new TypeArray(t, x); })+)
	| TK_void { t = new TypePrimitive(TypePrimitive.TYPE_VOID); }
	| t=primitive_type
	;

primitive_type returns [Type t] {t = null;}
	:	TK_int { t = new TypePrimitive(TypePrimitive.TYPE_INT); }
	|	TK_float { t = new TypePrimitive(TypePrimitive.TYPE_FLOAT); }
	|	TK_double { t = new TypePrimitive(TypePrimitive.TYPE_DOUBLE); }
	// |	TK_char { t = "char"; } -- do we actually want this?  --dzm
	|	TK_complex { t = new TypePrimitive(TypePrimitive.TYPE_COMPLEX); }
	|	cust_name:ID { t = (Type)structs.get(cust_name.getText()); }
	;

/*
 * Inline stream declarations.
 */

inline_stream returns [String t] {t=null;}
	: t=inline_filter
	| t=inline_struct_stream
	;

inline_filter returns [String t]
{
	t=null;
	String body = "";
	StreamType type = null;
	StreamType last_type = cur_type;
	NodesToJava last_n2j = n2j;
}
	:
		#(	TK_filter
			type=stream_type
			LCURLY
			{ indent++; cur_type = type; n2j = new NodesToJava(cur_type); }
			body=filter_body
			{ indent--; cur_type = last_type; n2j = last_n2j; }
		)
		{
			t = "new Filter () {\n" +
			body +
			getIndent () + "}";
		}
	;

inline_struct_stream returns [String t] {t=null;}
	: #(TK_pipeline t=inline_struct_stream2["Pipeline"])
	| #(TK_splitjoin t=inline_struct_stream2["SplitJoin"])
	| #(TK_feedbackloop t=inline_struct_stream2["FeedbackLoop"])
	;

inline_struct_stream2[String superclass] returns [String t]
{
	t = null;
	String body;
}
	:
		{ indent += 2; }
		body=block
		{ indent -= 2; }
		{	t = "new " + superclass + "() {\n";
			indent++;
			t += getIndent() + "public void init() " + body + "\n";
			indent--;
			t += getIndent() + "}";
		}
	;

/*
 * Function declarations.
 */

init_func_decl returns [InitFunction init]
{
	init = new InitFunction();
	String args = "", body = "";
}
	: #(TK_init
			body=block { init.body = getIndent() + body; }
		)
	;

work_func_decl returns [WorkFunction wf]
{
	wf = new WorkFunction();
	String body, rate;
}
	: #(TK_work
			(	#(TK_push rate=expr { wf.pushRate = rate; })
			|	#(TK_pop  rate=expr { wf.popRate  = rate; })
			|	#(TK_peek rate=expr { wf.peekRate = rate; })
			)*
			body=block { wf.body = body; })
	;

function_decl returns [String t]
{
  t = null;
  String params, code;
	Type return_type;
}
:
  #(
    name:ID
    return_type=data_type
    params=variable_list
    code=block
  )
  {
    t = return_type + " " + name + " " + params + "\n"
      + getIndent () + code;
  }
;

/*
 * Blocks and statements.
 */

block returns [String t]
{
	t = "";
	String stmts = "";
	String stmt = "";
	TempVarGen lastVarGen = varGen;
	varGen = new TempVarGen();
	symTab = new SymbolTable(symTab);
}
: #(
    LCURLY { indent++; }
    (stmt=statement { stmts = stmts + getIndent () + stmt + "\n"; } )*
  )
  {
    indent--;
    t = "{\n"
	  + varGen.allDecls()
      + stmts
      + getIndent () + "}";
	varGen = lastVarGen;
	symTab = symTab.getParent();
  }
;

statement returns [String t] { t = ""; }
	: t=block
	| t=if_statement
	| t=for_statement
	| t=while_statement
	| t=inline_statement { t += ";"; }
	;

if_statement returns [String t]
{
	t = "";
	String cond, cons, alt = null;
}
	: #(TK_if cond=expr cons=statement (alt=statement)?)
		{ t = "if (" + cond + ") {" + cons + "}";
			if (alt != null) t += " else {" + alt + "}"; }
	;

for_statement returns [String t]
{
	t = "";
	String init, cond, incr, body;
}
	: #(TK_for init=inline_statement cond=expr incr=inline_statement
			body=statement)
		{ t = "for (" + init + "; " + cond + "; " + incr + ") {" + body + "}"; }
	;

while_statement returns [String t]
{
	t = "";
	String cond, body;
}
	: #(TK_while cond=expr body=statement)
		{ t = "while (" + cond + ") {" + body + "}"; }
	;

inline_statement returns [String t] { t = ""; }
	:	t=add_statement
	|	t=body_statement
	|	t=loop_statement
	|	t=split_statement
	|	t=join_statement
	|	t=enqueue_statement
	|	t=push_statement
	|	t=print_statement
	|	t=assign_statement
	|	t=expr_assign_statement
	|	(variable_decl) => t=variable_decl
	|	(expr_statement) => t=expr_statement
	;

add_statement returns [String t] {t=null;}
	: #(TK_add t=substream_statement["add"])
	;

body_statement returns [String t] {t=null;}
	: #(TK_body t=substream_statement["setBody"])
	;

loop_statement returns [String t] {t=null;}
	: #(TK_loop t=substream_statement["setLoop"])
	;

substream_statement[String operation] returns [String t]
{
  t = null;
  String params, inline;
}
	: name:ID params=func_call_params
		{ t = operation + "(new " + name.getText() + params + ")"; }
	| inline=inline_stream
		{ t = operation + "(" + inline + ")"; }
	;

split_statement returns [String t] {t=null; String type = null;}
	: #(TK_split type=split_join_type)
		{ t = "setSplitter(" + type + ")"; }
	;

join_statement returns [String t] {t=null; String type = null;}
	: #(TK_join type=split_join_type)
		{ t = "setJoiner(" + type + ")"; }
	;

split_join_type returns [String t] { t = null; }
	:	TK_duplicate { t = "DUPLICATE ()"; }
	|	TK_roundrobin
		( (#(LPAREN expr expr)) => t=func_call_params
			{ t = "WEIGHTED_ROUND_ROBIN" + t; }
		| (#(LPAREN expr)) => t=func_call_params { t = "ROUND_ROBIN" + t; }
		| LPAREN { t = "ROUND_ROBIN()"; }
		)
	;

enqueue_statement returns [String t] {t=""; Expression x; String v;}
	: #(TK_enqueue x=expression_reduced)
		{
			Decomplexifier.Result result;
			result = Decomplexifier.decomplexify(x, varGen, n2j,
				new GetExprType(symTab, cur_type));
			v = (String)result.exp.accept(n2j);
			t = result.statements;
			t += "enqueue(" + v + ")";
		}
	;

push_statement returns [String t] {t = null; Expression x; String v;}
	: #(TK_push x=expression_reduced)
		{
			Decomplexifier.Result result;
			result = Decomplexifier.decomplexify(x, varGen, n2j,
				new GetExprType(symTab, cur_type));
			v = (String)result.exp.accept(n2j);
			t = result.statements;
			t += n2j.pushFunction(cur_type) + "(" + v + ")";
		}
	;

print_statement returns [String t] {t = null;}
	: #(TK_print t=expr) { t = "System.out.println(" + t + ")"; }
	;

assign_statement returns [String t] {t=null; Expression l, x;}
	: #(ASSIGN l=value_expression x=expression_reduced)
		{
			String lhs = (String)l.accept(n2j);
			// Check to see if the left-hand side is complex.
			Type type = (Type)l.accept(new GetExprType(symTab, cur_type));
			if (type != null && type.isComplex())
			{
				if (x instanceof ExprComplex)
				{
					ExprComplex cplx = (ExprComplex)x;
					t = lhs + ".real = " +
						(String)cplx.getReal().accept(n2j) + ";\n";
					t += lhs + ".imag = " +
						(String)cplx.getImag().accept(n2j);
				}
				else if (((Type)x.accept(new GetExprType(symTab, cur_type))).isComplex())
				{
					t = lhs + " = " + (String)x.accept(n2j);
				}
				else
				{
					t = lhs + ".real = " +
						(String)x.accept(n2j) + ";\n";
					t += lhs + ".imag = 0";
				}
			}
			else
			{
				// Assert that RHS is purely real.
				t = lhs + " = " + (String)x.accept(n2j);
			}
		}
	;

variable_decl returns [String t]
{
	t = "";
	String init_value = "";
	Type type;
	Expression array_dim;
}
	:	 #(name:ID
			type=data_type
			(array_dim=array_modifiers
				{type = new TypeArray(type, array_dim);})*
			(init_value=variable_init)?
		)
		{
			// Possibly overwrite init_value.
			if (type.isComplex() || (type instanceof TypeStruct))
				init_value = "= new " + n2j.convertType(type) + "()";
			if (type instanceof TypeArray)
			{
				TypeArray ta = (TypeArray)type;
				init_value = "= new " + n2j.convertType(ta.getBase()) +
					"[" + ta.getLength().accept(n2j) + "]";
				}
			t = n2j.convertType(type) + " " + name.getText () +
				" " + init_value;
			symTab.register(name.getText(), type);
		}
	;

variable_init returns [String t] { t = null; }
	: #(ASSIGN t=expr)
		{ t = " = " + t; }
	;

expr_assign_statement returns [String t]
{
	t = null;
	String l = null;
	String o = null;
	String r = null;
}
	:	( #(PLUS_EQUALS { o = "+="; } l=expr r=expr)
		| #(MINUS_EQUALS { o = "-="; } l=expr r=expr)
		) { t = l + " " + o + " " + r; }
	;

expr_statement returns [String t] { t = ""; }
	: t=expr
		{
			// Gross hack to strip out leading class casts,
			// since they'll illegal (JLS 14.8).
			if (t.charAt(0) == '(' &&
				Character.isUpperCase(t.charAt(1)))
				t = t.substring(t.indexOf(')') + 1);
		}
	;

/*
 * Expressions.
 */

expr returns [String t] {t=null; Expression x;}
	: x=expression_reduced { t = (String)x.accept(n2j); }
	;

expression_reduced returns [Expression x] {x=null;}
	: x=expression
		{
			x = (Expression)x.accept(new VarToComplex(symTab, cur_type));
			x = (Expression)x.accept(cplx_prop);
		}
	;

expression returns [Expression x] {x=null;}
	: x=minic_expr
	| x=streamit_expr
	;

streamit_expr returns [Expression x] {x=null;}
	: (x=pop_expr | x=peek_expr)
	;

pop_expr returns [Expression x] {x=null;}
	: TK_pop { x = new ExprPop(); }
	;

peek_expr returns [Expression x] {x=null;}
	: #(TK_peek x=expression) { x = new ExprPeek(x); }
	;

variable_list returns [String t]
{
  t = "";
  String params = "";
  String comma = "";
  String p = "";
}
	: #(LPAREN
			(p=variable_decl
				{
					params += comma + p;
					comma = ", ";
				}
			)*
		)
		{
			t = "(" + params + ")";
		}
	;

array_modifiers returns [Expression e] {e=null;}
// NB: previous versions of this had #(LSQUARE (e=expr)+); check to see
// how multi-dimensional arrays actually come through.
	: #(LSQUARE e=expression)
	;

minic_expr returns [Expression x] { x = null; }
	: x=minic_ternary_expr
	| x=minic_binary_expr
	| x=minic_unary_expr
	| x=value_expression
	| number:NUMBER { x = ExprConstant.createConstant(number.getText()); }
	| char_literal:CHAR_LITERAL
		{ x = new ExprConstChar(char_literal.getText()); }
	| string_literal:STRING_LITERAL
		{ x = new ExprConstStr(string_literal.getText()); }
	;

minic_ternary_expr returns [Expression x]
{
	x = null;
	Expression a, b, c;
}
	: #(QUESTION a=expression b=expression c=expression)
		{ x = new ExprTernary(ExprTernary.TEROP_COND, a, b, c); }
	;

minic_binary_expr returns [Expression x]
{
	x = null;
	Expression l = null, r = null;
	int o = 0;
}
	:	( #(PLUS { o = ExprBinary.BINOP_ADD; } l=expression r=expression)
		| #(MINUS { o = ExprBinary.BINOP_SUB; } l=expression r=expression)
		| #(STAR { o = ExprBinary.BINOP_MUL; } l=expression r=expression)
		| #(DIV { o = ExprBinary.BINOP_DIV; } l=expression r=expression)
		| #(MOD { o = ExprBinary.BINOP_MOD; } l=expression r=expression)
		| #(LOGIC_AND { o = ExprBinary.BINOP_AND; } l=expression r=expression)
		| #(LOGIC_OR { o = ExprBinary.BINOP_OR; } l=expression r=expression)
		| #(EQUAL { o = ExprBinary.BINOP_EQ; } l=expression r=expression)
		| #(NOT_EQUAL { o = ExprBinary.BINOP_NEQ; } l=expression r=expression)
		| #(LESS_THAN { o = ExprBinary.BINOP_LT; } l=expression r=expression)
		| #(LESS_EQUAL { o = ExprBinary.BINOP_LE; } l=expression r=expression)
		| #(MORE_THAN { o = ExprBinary.BINOP_GT; } l=expression r=expression)
		| #(MORE_EQUAL { o = ExprBinary.BINOP_GE; } l=expression r=expression)
		)
		{ x = new ExprBinary(o, l, r); }
	;

minic_unary_expr returns [Expression x] {x=null; Expression y;}
	: #(INCREMENT y=expression)
		{ x = new ExprUnary(ExprUnary.UNOP_PREINC, y); }
	| #(DECREMENT y=expression)
		{ x = new ExprUnary(ExprUnary.UNOP_PREDEC, y); }
	;

value_expression returns [Expression x]
{
	x = null;
	Expression left, right;
	List l;
  String func_params = "", array_mods = "";
}
	: id:ID { x = new ExprVar(id.getText()); }
	| #(DOT left=expression field:ID)
		{ x = new ExprField(left, field.getText()); }
	| #(LSQUARE left=expression #(LSQUARE right=expression))
		{ x = new ExprArray(left, right); }
	| #(LPAREN fn:ID l=func_param_list)
		{ x = new ExprFunCall(fn.getText(), l); }
	;

func_param_list returns [List l] { l = new ArrayList();	Expression x; }
	: #(LPAREN
			( x=expression { l.add(x); } )*
		)
	;

func_call_params returns [String t]
{
  t = null;
  String args = "", arg;
}
	: #(LPAREN
			(args=expr
				(arg=expr { args += (", " + arg); })*
			)?
		)
		{ t = "(" + args + ")"; }
	;
