package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		
	/** **************************************************************************************************************************
	 * SYMBOL TABLE : HASPMAP
	 * */
	HashMap<String, Declaration> symbolTable = new HashMap<String, Declaration>();
	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	/** ********************************************* visit Declaration_Variable ****************************************************
	 * Declaration_Variable ::=  Type name (Expression | epsilon )
	 * 
	 * REQUIRE:  symbolTable.lookupType(name) = null
	 * symbolTable.insert(name, Declaration_Variable)
	 * Declaration_Variable.Type <= Type
	 * REQUIRE if (Expression !=  epsilon) Declaration_Variable.Type == Expression.Type
	 */
	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(!symbolTable.containsKey(declaration_Variable.name)) 				//Check name exsists in symbolTable
		{
			if(declaration_Variable.e != null)
			{	
				declaration_Variable.e.visit(this, arg); 							//Visit expression if not null
				
			}
			symbolTable.put(declaration_Variable.name, declaration_Variable);	//Insert it into the symbolTable
			if(declaration_Variable.type.kind == Kind.KW_int)
			{
				declaration_Variable.of_type = Type.INTEGER;
			}
			else if(declaration_Variable.type.kind == Kind.KW_boolean)
			{
				declaration_Variable.of_type = Type.BOOLEAN;
			}
			else
			{
				String msg ="In declaration_Variable: Type "+declaration_Variable.of_type+" does not match type expected for "+declaration_Variable.name;
				throw new SemanticException(declaration_Variable.firstToken, msg);
			}
			if(declaration_Variable.e != null)
			{	
				if(declaration_Variable.of_type != declaration_Variable.e.of_type)
				{
					String msg ="In declaration_Variable Type "+declaration_Variable.of_type+" does not match its Expression Type "+declaration_Variable.e.of_type;
					throw new SemanticException(declaration_Variable.firstToken, msg);
				}
			}
		}
		else
		{
			String msg ="In declaration_Variable: Variable "+declaration_Variable.name+" already exsists!";
			throw new SemanticException(declaration_Variable.firstToken, msg);
		}
		return declaration_Variable;
	//	throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Expression_Binary ****************************************************
	 * Expression_Binary ::= Expression0 op Expression1
	 * 
	 * REQUIRE:  Expression0.Type == Expression1.Type  && Expression_Binary.Type != null
	 * Expression_Binary.type <=   if op == {EQ, NEQ} then BOOLEAN
	 * 							   else if (op == {GE, GT, LT, LE} && Expression0.Type == INTEGER) then BOOLEAN
	 * 							   else if (op == {AND, OR}) && (Expression0.Type == INTEGER || Expression0.Type ==BOOLEAN) then Expression0.Type
	 *                             else if op == {DIV, MINUS, MOD, PLUS, POWER, TIMES} && Expression0.Type == INTEGER	then INTEGER
	 *                   		   else null
	 */
	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		if(expression_Binary.e0.of_type != expression_Binary.e1.of_type) //should not this be nesting the rest?
		{
			String msg ="In expression_Binary: Type "+ expression_Binary.e0.of_type +" of e0 does not match type "+expression_Binary.e1.of_type+" of e1.";
			throw new SemanticException(expression_Binary.firstToken, msg);
		}
		else if(expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ)
		{		
			expression_Binary.of_type = Type.BOOLEAN;
		}
		else if(expression_Binary.op == Kind.OP_GE || expression_Binary.op == Kind.OP_GT 
				|| expression_Binary.op == Kind.OP_LT || expression_Binary.op == Kind.OP_LE)
		{		
			expression_Binary.of_type = Type.BOOLEAN;
		}
		else if((expression_Binary.e0.of_type == Type.BOOLEAN || expression_Binary.e0.of_type == Type.INTEGER )
				&& (expression_Binary.op == Kind.OP_AND || expression_Binary.op == Kind.OP_OR))
		{
			expression_Binary.of_type = expression_Binary.e0.of_type;
		}
		else if(expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS 
				|| expression_Binary.op == Kind.OP_MOD || expression_Binary.op == Kind.OP_PLUS 
				|| expression_Binary.op == Kind.OP_POWER || expression_Binary.op == Kind.OP_TIMES
				&& expression_Binary.e0.of_type == Type.INTEGER)
		{
			expression_Binary.of_type = Type.INTEGER;
		}
		else
		{
			String msg ="In expression_Binary: Type does not match. Got type "+expression_Binary.of_type;
			throw new SemanticException(expression_Binary.firstToken, msg);
		}
		return expression_Binary;
		//throw new UnsupportedOperationException();
	}

	
	/** ********************************************* visit Expression_Unary ****************************************************
	 * 	Expression_Unary ::= op Expression
	 * 
	 *	Expression_Unary.Type <=
	 *	let t = Expression.Type in 
     *           	if op belongs {EXCL} && (t == BOOLEAN || t == INTEGER) then t
     *               else if op {PLUS, MINUS} && t == INTEGER then INTEGER
	 *	    		else null
     *               REQUIRE:  Expression_ Unary.Type not equal null 
	 */
	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(expression_Unary.e != null)
		{
			expression_Unary.e.visit(this, arg);
		}
		Type t = expression_Unary.e.of_type;
		if(expression_Unary.op == Kind.OP_EXCL && (t == Type.INTEGER || t == Type.BOOLEAN))
			expression_Unary.of_type = t;
		else if((expression_Unary.op == Kind.OP_MINUS || expression_Unary.op == Kind.OP_PLUS) && t == Type.INTEGER)
			expression_Unary.of_type = Type.INTEGER;
		else
			expression_Unary.of_type = null;
		if(expression_Unary.of_type == null)
		{
			String msg ="In expression_Unary: Type can not be NULL but got: "+expression_Unary.of_type;
			throw new SemanticException(expression_Unary.firstToken, msg);
		}
		return expression_Unary;
//		throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Index ****************************************************
	 * Index ::= Expression0 Expression1
	 * 			 REQUIRE: Expression0.Type == INTEGER &&  Expression1.Type == INTEGER
	 *         	 Index.isCartesian <= !(Expression0 == KW_r && Expression1 == KW_a)
	 */
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(index.e0.of_type == Type.INTEGER && index.e1.of_type == Type.INTEGER)
		{
			if(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_a)
				index.setCartesian(false);
			else
				index.setCartesian(true);
		}
		else
		{
			String msg ="In index: Type "+index.e0.of_type+" of e0 does not match type "+index.e1.of_type+" of e1.";
			throw new SemanticException(index.firstToken, msg);
		}
		return index;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Expression_PixelSelector ****************************************************
	 * Expression_PixelSelector ::=   name Index
	 * 
	 * name.Type <= SymbolTable.lookupType(name)
	 * Expression_PixelSelector.Type <=  if name.Type == IMAGE then INTEGER 
	 * 									 else if Index == null then name.Type
	 * 									 else  null
	 * REQUIRE:  Expression_PixelSelector.Type != null
	 */
	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(expression_PixelSelector.index != null)
			expression_PixelSelector.index.visit(this, arg);
		if(!symbolTable.containsKey(expression_PixelSelector.name))
		{
			String msg = "In expression_PixelSelector : "+expression_PixelSelector.name+" does not exist in symbolTable!";
			throw new SemanticException(expression_PixelSelector.firstToken,msg);
		}
		Type name =	symbolTable.get(expression_PixelSelector.name).of_type ;
		if(name == Type.IMAGE)
		{
			expression_PixelSelector.of_type =  Type.INTEGER;
		}
		else if(expression_PixelSelector.index == null)
		{
			expression_PixelSelector.of_type = name;
		}
		else
		{
			expression_PixelSelector.of_type = null;
		}
        if(expression_PixelSelector.of_type == null)
        {
			String msg ="In expression_PixelSelector : Type can be INTEGER/IMAGEnot be "+expression_PixelSelector.of_type;
			throw new SemanticException(expression_PixelSelector.firstToken, msg);
        }
		return expression_PixelSelector;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Expression_Conditional ****************************************************
	 * Expression_Conditional ::=  Expressioncondition Expressiontrue Expressionfalse
	 * 
	 * REQUIRE:  Expressioncondition.Type == BOOLEAN && Expressiontrue.Type ==Expressionfalse.Type
	 * Expression_Conditional.Type <= Expressiontrue.Type
	 */
	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_Conditional.falseExpression.visit(this, arg);
		expression_Conditional.trueExpression.visit(this, arg);
		expression_Conditional.condition.visit(this, arg);
		if(expression_Conditional.trueExpression.of_type == expression_Conditional.falseExpression.of_type
				&& expression_Conditional.condition.of_type == Type.BOOLEAN)
		{
			expression_Conditional.of_type = expression_Conditional.trueExpression.of_type;
		}
		else
		{
			String msg;
			if(expression_Conditional.trueExpression.of_type != expression_Conditional.falseExpression.of_type)
				msg = "Type "+expression_Conditional.trueExpression.of_type+" of true condition does not match type "+expression_Conditional.falseExpression.of_type+" of false condition.";
			else	
				msg = "In expression_Conditional : Expression CONDITION must be boolean not : "+expression_Conditional.condition.of_type;
			throw new SemanticException(expression_Conditional.firstToken,msg);
		}
		return expression_Conditional;
		//throw new UnsupportedOperationException();
	}
	
	/** ********************************************* visit Declaration_Image ****************************************************
	 * Declaration_Image  ::= name (  xSize ySize | epsilon) Source
	 * 
	 * REQUIRE:  symbolTable.lookupType(name) = null
	 *        	 symbolTable.insert(name, Declaration_Image)
	 *         	 Declaration_Image.Type <= IMAGE   
	 * 
	 * REQUIRE if xSize != null then ySize != null && xSize.Type == INTEGER && ySize.type == INTEGER
	 */
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(!symbolTable.containsKey(declaration_Image.name)) //check name
		{
			symbolTable.put(declaration_Image.name, declaration_Image);
			declaration_Image.of_type = Type.IMAGE;
			if(declaration_Image.xSize != null && declaration_Image.ySize != null)
			{
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
				if(declaration_Image.xSize.of_type != Type.INTEGER || declaration_Image.ySize.of_type != Type.INTEGER)
				{
					String msg ="In declaration_Image: Type of xSize/ysize type is INTEGER. xSize : "
								+declaration_Image.xSize.of_type+" ySize : "+declaration_Image.ySize.of_type;
					throw new SemanticException(declaration_Image.firstToken, msg);
				}
			}
			else if(declaration_Image.xSize == null && declaration_Image.ySize != null)
			{
				String msg ="In declaration_Image: Type of xSize is null but ySize is"+declaration_Image.ySize.of_type;
				throw new SemanticException(declaration_Image.firstToken, msg);
			}
			else if(declaration_Image.xSize != null && declaration_Image.ySize == null)
			{
				String msg ="In declaration_Image: Type of ysize is null but type of xSize is "+declaration_Image.xSize.of_type;
				throw new SemanticException(declaration_Image.firstToken, msg);
			}
		}
		else
		{
			String msg ="In declaration_Image: "+declaration_Image.name+" already exsists in symbolTable!";
			throw new SemanticException(declaration_Image.firstToken, msg);
		}
		return declaration_Image;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Source_StringLiteral ****************************************************
	 * Source_StringLiteral ::=  fileOrURL
	 * 
	 * Source_StringLIteral.Type <= if isValidURL(fileOrURL) then URL else FILE
	 */
	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(source_StringLiteral.isValidURL(source_StringLiteral.fileOrUrl))
		{
			source_StringLiteral.of_type = Type.URL;
		}
		else
			source_StringLiteral.of_type = Type.FILE;
		return source_StringLiteral;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Source_CommandLineParam ****************************************************
	 * Source_CommandLineParam  ::= ExpressionparamNum
	 * 
	 * Source_CommandLineParam .Type <= ExpressionparamNum.Type
	 * 
	 * REQUIRE:  Source_CommandLineParam .Type == INTEGER
	 */
	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		source_CommandLineParam.paramNum.visit(this, arg);
		source_CommandLineParam.of_type = null;
		if(source_CommandLineParam.paramNum.of_type != Type.INTEGER)
		{
			String msg ="In source_CommandLineParam: Expected Type was INTEGER but got "+source_CommandLineParam.of_type;
			throw new SemanticException(source_CommandLineParam.firstToken, msg);
		}
		return source_CommandLineParam;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Source_Ident ****************************************************
	 * Source_Ident ::= name
	 * 
	 * Source_Ident.Type <= symbolTable.lookupType(name)
	 * 
	 * REQUIRE:  Source_Ident.Type == FILE || Source_Ident.Type == URL
	 */
	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(symbolTable.containsKey(source_Ident.name))
		{
			source_Ident.of_type = symbolTable.get(source_Ident.name).of_type;
			if(source_Ident.of_type != Type.FILE && source_Ident.of_type != Type.URL)
			{
				String msg ="In source_CommandLineParam: Expected Type was FILE/URL but got "+source_Ident.of_type;
				throw new SemanticException(source_Ident.firstToken, msg);
			}
		}
		else
		{
			String msg ="In source_CommandLineParam: "+source_Ident.name+" not found in symbolTable!!!";
			throw new SemanticException(source_Ident.firstToken, msg);
		}
		return source_Ident;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Declaration_SourceSink ****************************************************
	 * Declaration_SourceSink  ::= Type name  Source
	 * 
	 * REQUIRE:  symbolTable.lookupType(name) = null
	 * 			 symbolTable.insert(name, Declaration_SourceSink)
	 * 			 Declaration_SourceSink.Type <= Type
	 * 
	 * REQUIRE Source.Type == Declaration_SourceSink.Type
	 */
	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(!symbolTable.containsKey(declaration_SourceSink.name)) //check name
		{
			symbolTable.put(declaration_SourceSink.name, declaration_SourceSink);
			
			if(declaration_SourceSink.type == Kind.KW_url)
			{
				declaration_SourceSink.of_type = Type.URL;
			}
			else if(declaration_SourceSink.type == Kind.KW_file)
			{
				declaration_SourceSink.of_type = Type.FILE;
			}
			declaration_SourceSink.source.visit(this, arg);
			if(declaration_SourceSink.source.of_type == null || declaration_SourceSink.of_type == declaration_SourceSink.source.of_type)
			{
				//if(declaration_SourceSink.of_type == Type.FILE && declaration_SourceSink.source.of_type == Type.INTEGER ||declaration_SourceSink.of_type == Type.URL && declaration_SourceSink.source.of_type == Type.INTEGER){}
				//else
//				{
//				String msg ="In declaration_SourceSink: Type of declaration_SourceSink : "+declaration_SourceSink.of_type+
//						" does not match type of source : "+declaration_SourceSink.source.of_type;
//				throw new SemanticException(declaration_SourceSink.firstToken, msg);
//				}
			}
			else
			{
			String msg ="In declaration_SourceSink: Type of declaration_SourceSink : "+declaration_SourceSink.of_type+
					" does not match type of source : "+declaration_SourceSink.source.of_type;
			throw new SemanticException(declaration_SourceSink.firstToken, msg);
			}
		}
		else
		{
			String msg ="In declaration_SourceSink: Entry name "+declaration_SourceSink.name+" already exists in symbolTable!";
			throw new SemanticException(declaration_SourceSink.firstToken, msg);
		}
		return declaration_SourceSink;
		//throw new UnsupportedOperationException();
	}

	
	/** ********************************************* visit Expression_IntLit ****************************************************
	 * 	Expression_IntLit ::=  value
	 * 
	 * 	Expression_IntLIt.Type <= INTEGER
	 */
	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.of_type = Type.INTEGER;
		return expression_IntLit;
	}

	/** ******************************************* visit Expression_FunctionAppWithExprArg ***************************************
	 * Expression_FunctionAppWithExprArg ::=  function Expression
	 * 
	 * REQUIRE:  Expression.Type == INTEGER
	 * 
	 * Expression_FunctionAppWithExprArg.Type <= INTEGER
	 */
	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.arg.of_type == Type.INTEGER)
		{
			 expression_FunctionAppWithExprArg.of_type = Type.INTEGER;
		}
		return expression_FunctionAppWithExprArg;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Expression_FunctionAppWithIndexArg ****************************************************
	 * Expression_FunctionAppWithIndexArg ::=   function Index
	 * 
	 * Expression_FunctionAppWithIndexArg.Type <= INTEGER
	 */
	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithIndexArg.of_type = Type.INTEGER;
		return expression_FunctionAppWithIndexArg;
		//throw new UnsupportedOperationException();
	}

	/** ********************************************* visit Expression_PredefinedName ****************************************************
	 * Expression_PredefinedName ::=  predefNameKind
	 * 
	 * Expression_PredefinedName.TYPE <= INTEGER
	 */
	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.of_type = Type.INTEGER;
		return expression_PredefinedName;
		//throw new UnsupportedOperationException();
	}

	/** *********************************************** visit Statement_Out *************************************************************
	 * Statement_Out ::= name Sink
	 * 
	 * Statement_Out.Declaration <= name.Declaration
	 * 
	 * REQUIRE:  (name.Declaration != null)
	 * REQUIRE:   ((name.Type == INTEGER || name.Type == BOOLEAN) && Sink.Type == SCREEN)
	 *              ||  (name.Type == IMAGE && (Sink.Type ==FILE || Sink.Type == SCREEN))
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)                       // I THINK THIS DOES NOT WORK
			throws Exception {
		// TODO Auto-generated method stub
		statement_Out.sink.visit(this, arg);
		if(!symbolTable.containsKey(statement_Out.name))
		{
			String msg = "In statement_Out : symbolTable does not contain : "+statement_Out.name;
			throw new SemanticException(statement_Out.firstToken,msg);
		}
		Type type = symbolTable.get(statement_Out.name).of_type;
		statement_Out.setDec(symbolTable.get(statement_Out.name));
		if(((type == Type.INTEGER || type == Type.BOOLEAN) && statement_Out.sink.of_type == Type.SCREEN) 
		  ||(type == Type.IMAGE && (statement_Out.sink.of_type == Type.FILE || statement_Out.sink.of_type == Type.SCREEN))){}
		else
		{
			String msg = "In statement_Out : Declaration NULL! : "+statement_Out.getDec();
			throw new SemanticException(statement_Out.firstToken,msg);
		}
		return statement_Out;
		//throw new UnsupportedOperationException();
	}

	/** *********************************************** visit Statement_In *************************************************************
	 * Statement_In ::= name Source
	 * 
	 * Statement_In.Declaration <= name.Declaration
     * 
     * REQUIRE:  if (name.Declaration != null) & (name.type == Source.type) //THIS IS NOT SUPPOSED TO BE CHECKED ANYMORE
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statement_In.source.visit(this, arg);
		if(!symbolTable.containsKey(statement_In.name))
		{
			String msg = "In statement_In: "+statement_In.name+" not in symbolTable!!";
			throw new SemanticException(statement_In.firstToken,msg);
		}
		statement_In.setDec(symbolTable.get(statement_In.name));
		/*//Corrected for the 5th assignment 
		if(statement_In.getDec()!= null && statement_In.source.of_type == symbolTable.get(statement_In.name).of_type )
		{
			statement_In.setDec(symbolTable.get(statement_In.name));		
		}
		*/
		return statement_In;
		//throw new UnsupportedOperationException();
	}

	/** *********************************************** visit Statement_Assign *************************************************************
	 * Statement_Assign ::=  LHS  Expression
	 * 
	 * REQUIRE:  LHS.Type == Expression.Type
	 * 
	 * StatementAssign.isCartesian <= LHS.isCartesian
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		statement_Assign.e.visit(this, arg);
		statement_Assign.lhs.visit(this, arg);
		if(statement_Assign.lhs.of_type == statement_Assign.e.of_type)
		{
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
			//statement_Assign.of_type = statement_Assign.lhs.of_type; //added for 6
		}
		else if(statement_Assign.lhs.of_type == Type.IMAGE && statement_Assign.e.of_type == Type.INTEGER)
		{
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
			//statement_Assign.of_type = statement_Assign.lhs.of_type; //adde for 6
		}
		else
		{
			String msg = "In statement_Assign: Type "+statement_Assign.lhs.of_type
					+" of LHS does not match Type "+statement_Assign.e.of_type+" of its Expression";
			throw new SemanticException(statement_Assign.firstToken,msg);
		}
		return statement_Assign;
		//throw new UnsupportedOperationException();
	}

	/** *********************************************** visit LHS *************************************************************
	 * LHS ::= name Index
	 * 
	 * LHS.Declaration <= symbolTable.lookupDec(name)
	 * LHS.Type <= LHS.Declaration.Type
	 * LHS.isCarteisan <= Index.isCartesian
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if(symbolTable.containsKey(lhs.name))
		{
			lhs.of_type = symbolTable.get(lhs.name).of_type;
			if(lhs.index != null)
			{
				lhs.index.visit(this, arg);
				lhs.setCartesian(lhs.index.isCartesian());
			}
			//WHY NOT SET LHS AS CARTESIAN IF INDEX == NULL????????????????????????????????????????????????????????????????????
		}
		else
		{
			String msg ="In lhs: "+lhs.name+" not found in symblTable!!";
			throw new SemanticException(lhs.firstToken, msg);
		}
		return lhs;
		//throw new UnsupportedOperationException();
	}

	/** *********************************************** visit Sink_SCREEN *************************************************************
	 * Sink_SCREEN ::= SCREEN
	 * 
	 * Sink_SCREEN.Type <= SCREEN
	 */
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.of_type = Type.SCREEN;
		return sink_SCREEN;
		//throw new UnsupportedOperationException();
	}

	/** *********************************************** visit Sink_Ident *************************************************************
	 * Sink_Ident ::= name
	 * 
	 * Sink_Ident.Type <= symbolTable.lookupType(name)
	 * 
	 * REQUIRE:  Sink_Ident.Type  == FILE
	 */
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(!symbolTable.containsKey(sink_Ident.name))
		{
			String msg ="In sink_Ident: Entry "+sink_Ident.name+" does not exist in the symbolTable!";
			throw new SemanticException(sink_Ident.firstToken, msg);
		}
		sink_Ident.of_type = symbolTable.get(sink_Ident.name).of_type;
		if(sink_Ident.of_type != Type.FILE)
		{
			String msg ="In sink_Ident: Expected Type FILE but got "+sink_Ident.of_type;
			throw new SemanticException(sink_Ident.firstToken, msg);
		}
		return sink_Ident;
		//throw new UnsupportedOperationException();
	}

	/** ******************************************* visit Expression_BooleanLit *************************************************************
	 * Expression_BooleanLit ::=  value
	 * 
	 * Expression_BooleanLit.Type <= BOOLEAN
	 */
	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_BooleanLit.of_type = Type.BOOLEAN;
		return expression_BooleanLit;
		//throw new UnsupportedOperationException();
	}

	/** ******************************************* visit Expression_Ident *****************************************************************
	 * Expression_Ident  ::=   name
	 * 
	 * Expression_Ident.Type <= symbolTable.lookupType(name)
	 */
	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, 
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(symbolTable.containsKey(expression_Ident.name))
		{
			expression_Ident.of_type = symbolTable.get(expression_Ident.name).of_type;
		}
		else
		{
			String msg = "In expression_Ident : "+expression_Ident.name+" not found in symbolTable!!";
			throw new SemanticException(expression_Ident.firstToken,msg);
		}
		return expression_Ident;
		//throw new UnsupportedOperationException();
	}
}
