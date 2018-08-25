package cop5556fa17;

import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.AST.*;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;
	
	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * ***************************************** Match ************************************************
	 * match method
	 * @param kind
	 * @throws SyntaxException 
	 */
	 Token match(Kind kind) throws SyntaxException
	 {
       if( t.kind == kind)
       {
		  t = scanner.nextToken();
		  return t;
       }
       else {
    	   String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line + " buy got : " + kind;
    	   throw new SyntaxException(t, message);
       }
	 }
	
	 // ************************************************************************************************
	
	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * @return 
	 * 
	 * @throws SyntaxException
	 * 
	 * 
	 * CONSTRUCTOR
	 * 
	 * public Program(Token firstToken, Token name, ArrayList<ASTNode> decsAndStatements)
	 * 
	 */
	
	Program program() throws SyntaxException {
		//TODO  implement this
		Program p = null;
		Token firstToken = t;
		Token name = t;
		ArrayList<ASTNode> res = new ArrayList<>(); // for decsAndStatements
		if(t.kind == IDENTIFIER)
		{	
			match(IDENTIFIER);
			while(t.kind == KW_url || t.kind == KW_file || t.kind == KW_image || t.kind == KW_int || t.kind == KW_boolean || t.kind == IDENTIFIER)
			{
				if(t.kind == IDENTIFIER) {
					Statement s = statement();
					res.add(s);
				}
				else {
					Declaration d = declaration();
					res.add(d);
				}
				match(SEMI);
			}
			return new Program(firstToken,name,res);			//name == firstToken
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}

	/** ***************************************** Declaration ************************************************
	 * Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration 
	 * 
	 */
	Declaration declaration() throws SyntaxException {
		Declaration dec = null;
		if(t.kind == KW_int || t.kind == KW_boolean)
		{	
			dec = variable_declaration();
		}
		else if(t.kind == KW_image)
		{
			dec= image_declaration();
		}
		else if(t.kind == KW_url || t.kind == KW_file)
		{
			dec = source_sink_declaration();
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return dec;
	}
	
	/** ************************************** VariableDeclaration *******************************************
	 * VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | epsilon )
	 * @return 
	 * 
	 * Declaration_Variable(Token firstToken, Token type, Token name, Expression e)
	 * 
	 */
	Declaration_Variable variable_declaration() throws SyntaxException {
		Token firstToken = t;
		var_type();						//returns void
		Token type = firstToken;		//type of identifier == firstToken
		Token name = t;					//name of indentifier
		Expression e = null;
		if(t.kind == IDENTIFIER)
		{	
			match(IDENTIFIER);
			if(t.kind == OP_ASSIGN)
			{
				match(OP_ASSIGN);
				e = expression();
				return new Declaration_Variable(firstToken, type, name, e);
			}
			else
			{
				return new Declaration_Variable(firstToken, type, name, null);
			}
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}
	
	/** ***************************************** VarType ************************************************
	 * VarType ::= KW_int | KW_boolean
	 * @return 
	 * 
	 * @throws SyntaxException
	 */
	void var_type() throws SyntaxException {
		if(t.kind == KW_int)
		{	
			match(KW_int);
		}
		else if(t.kind == KW_boolean)
		{
			match(KW_boolean);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}
	
	/** ********************************* SourceSinkDeclaration **********************************************
	 * SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
	 * 
	 * SourceSinkType := KW_url | KW_file
	 * 
	 * @return 
	 * 
	 * Declaration_SourceSink(Token firstToken, Token type, Token name, Source source)
	 * 
	 */
	Declaration_SourceSink source_sink_declaration() throws SyntaxException {
		Token firstToken = t;
		Token type = t;
		source_sink_type();
		Token name = t;
		Source s = null;
		if(t.kind == IDENTIFIER)
		{
			match(IDENTIFIER);
			match(OP_ASSIGN);
			s = source();
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return new Declaration_SourceSink(firstToken, type, name, s);
	}
	
	/** ***************************************** Source ************************************************
	 * Source ::= STRING_LITERAL  
	 * Source ::= OP_AT Expression 
	 * Source ::= IDENTIFIER  
	 * 
	 * Source_StringLiteral(Token firstToken, String fileOrUrl)
	 * Source_CommandLineParam(Token firstToken, Expression paramNum)
	 * Source_Ident(Token firstToken, Token name)
	 * 
	 * @return 
	 */
	Source source() throws SyntaxException{
		Source s = null;
		Token firstToken = t;
		if(t.kind == STRING_LITERAL)
		{
			String fileOrUrl = t.getText();
			s = new Source_StringLiteral(firstToken, fileOrUrl);
			match(STRING_LITERAL);
		}
		else if(t.kind == OP_AT)
		{
			match(OP_AT);
			Expression paramNum = expression();
			s = new Source_CommandLineParam(firstToken, paramNum);
		}
		else if(t.kind == IDENTIFIER)
		{
			match(IDENTIFIER);
			s = new Source_Ident(firstToken, /*name*/ firstToken);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return s;
	}
	
	/** **************************************** SourceSinkType **********************************************
	 * SourceSinkType := KW_url | KW_file
	 * @throws SyntaxException
	 */
	void source_sink_type() throws SyntaxException {
		if(t.kind == KW_url)
		{	
			match(KW_url);
		}
		else if(t.kind == KW_file)
		{
			match(KW_file);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
	}
	
	/** ************************************** ImageDeclaration *************************************************
	 * 
	 * ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | epsilon) IDENTIFIER ( OP_LARROW Source | epsilon )   
	 * @return 
	 * 
	 * @throws SyntaxException
	 * 
	 * Declaration_Image(Token firstToken, Expression xSize, Expression ySize, Token name, Source source)
	 */
	Declaration_Image image_declaration() throws SyntaxException {   
		Token firstToken = t;
		Token name;
		Source s = null;
		Expression xSize =null;
		Expression ySize =null;
		if(t.kind == KW_image)
		{
			match(KW_image);
			if(t.kind == LSQUARE)
			{
				match(LSQUARE);
				xSize = expression();
				match(COMMA);
				ySize = expression();
				match(RSQUARE);	
			}
			
		/*	else{return;}*/ // for epsilon //corected by commenting block for assignment 3.
			
			name=t;
			match(IDENTIFIER);
			if(t.kind == OP_LARROW)
			{
				match(OP_LARROW);
				s = source();
			}
		/*		redundant code block ?
		 	else
			{
				return new Declaration_Image(firstToken, xSize, ySize, name, s);										// for epsilon
			}
		*/
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return new Declaration_Image(firstToken, xSize, ySize, name, s);		
	}
	
	/** ***************************************** Statement ************************************************
	 * Statement  ::= AssignmentStatement 
  						| ImageOutStatement    
						| ImageInStatement    
	 * @return 

	 * @throws SyntaxException
	 */
	Statement statement() throws SyntaxException {
		Statement s = null;
		Token firstToken = t;			//for the identifier
		if(t.kind == IDENTIFIER)
		{
			match(IDENTIFIER);
			if(t.kind == OP_RARROW)
			{
				s = image_out_statement(firstToken);
			}
			else if(t.kind == OP_LARROW)
			{
				s = image_in_statement(firstToken);
			}
			else if(t.kind == LSQUARE || t.kind == OP_ASSIGN)	//OP_ASSIGN in case of epsilon in Lhs else LSQUARE
			{
				s = assignment_statement(firstToken);
			}
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
		}
		return s;
	}
	
	/** ***************************************** ImageOutStatement ************************************************
	 * ImageOutStatement ::= IDENTIFIER OP_RARROW Sink 
	 * 
	 * Statement_Out(Token firstToken, Token name, Sink sink)
	 * 
	 */
	Statement_Out image_out_statement(Token firstToken) throws SyntaxException{
		Sink s = null;
		if(t.kind == OP_RARROW)
		{
			match(OP_RARROW);
			s = sink();		//return new Statement_Out(firstToken, /*name*/firstToken, sink()); //will work no??
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
    	}
		return new Statement_Out(firstToken, /*name*/firstToken, s);
	}
	
	/** ***************************************** Sink ************************************************
	 * Sink ::= IDENTIFIER | KW_SCREEN  //ident must be file
	 * @return 
	 * 
	 * Sink_Ident(Token firstToken, Token name)
	 * Sink_SCREEN(Token firstToken)
	 * 
	 * @throws SyntaxException
	 */
	Sink sink() throws SyntaxException{
		Sink s = null;
		if(t.kind == IDENTIFIER)
		{
			s = new Sink_Ident(t,t);		//firstToken == t;
			match(IDENTIFIER);
		}
		else if(t.kind == KW_SCREEN)
		{
			s = new Sink_SCREEN(t);
			match(KW_SCREEN);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
    	}
		return s;
	}
	
	/** ***************************************** ImageInStatement ************************************************
	 * ImageInStatement ::= IDENTIFIER OP_LARROW Source
	 * 
	 * Statement_In(Token firstToken, Token name, Source source)
	 * 
	 */
	Statement_In image_in_statement(Token firstToken) throws SyntaxException {
		Source s= null;
		if(t.kind == OP_LARROW)
		{
			match(OP_LARROW);
			s = source();
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
    	}
		return new Statement_In(firstToken, firstToken, s);
	}
	
	/** ***************************************** AssignmentStatement ************************************************
	 * AssignmentStatement ::= Lhs OP_ASSIGN Expression
	 * 
	 * Statement_Assign(Token firstToken, LHS lhs, Expression e)
	 * 
	 */
	Statement_Assign assignment_statement(Token firstToken) throws SyntaxException{
		LHS lhs = lhs(firstToken);								//returns null in case of epsilon
		Expression e = null;
		if(t.kind == OP_ASSIGN)
		{
			match(OP_ASSIGN);
			e = expression();
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
			throw new SyntaxException(t, message);
    	}	
		return new Statement_Assign(firstToken, lhs, e);
	}
	
	/** ***************************************** Expression ************************************************
	 * 
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * @return 
	 * 
	 * Expression_Conditional(Token firstToken, Expression condition, Expression trueExpression, Expression falseExpression)
	 * 
	 * @throws SyntaxException
	 */
	public Expression expression() throws SyntaxException 
	{	
		Expression e = null;
		Expression trueExpression = null;
		Expression falseExpression = null;
		Token firstToken = t;
		Expression condition = or_expression();
		if(t.kind == OP_Q)
		{
			match(OP_Q);
			trueExpression = expression();
			match(OP_COLON);
			falseExpression = expression();
			e = new Expression_Conditional(firstToken, condition, trueExpression, falseExpression);
		}
		else
		{
			e = condition; //condition = or_expression();
		}	//Handle errors in the called methods
		return e;
	}

	/** ********************************************** OrExpression *****************************************
	 * 
	 * OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
	 * 
	 * @throws SyntaxException
	 * 
	 * Expression_Binary(Token firstToken, Expression e0, Token op, Expression e1)
	 * 
	 */
	Expression or_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 =	and_expression();
		Expression e1 = null;
		Token op = null;
		while(t.kind == OP_OR)
		{
			op = t;
			match(t.kind);
			e1 = and_expression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	/** ********************************************** AndExpression *****************************************
	 * 
	 * AndExpression ::= EqExpression ( OP_AND  EqExpression )
	 * @return *
	 * 
	 *  @throws SyntaxException
	 */
	Expression and_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = eq_expression();
		Expression e1 = null;
		Token op = null;
		while(t.kind == OP_AND)
		{
			op = t;
			match(t.kind);
			e1 = eq_expression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	/** ********************************************** EqExpression ******************************************
	 * 
	 * EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
	 * 
	 * */
	Expression eq_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = rel_expression();
		Expression e1 = null;
		Token op = null;
		while(t.kind == OP_EQ || t.kind == OP_NEQ){
			op = t;
			match(t.kind);
			e1 = rel_expression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	/** ********************************************** RelExpression *****************************************
	 * RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
	 * @throws SyntaxException
	 */
	Expression rel_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 =	add_expression();
		Expression e1 = null;
		Token op = null;
		while(t.kind == OP_LT || t.kind == OP_GT || t.kind == OP_LE || t.kind == OP_GE)
		{
			op = t;
			match(t.kind);
			e1 = add_expression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	/** ********************************************** AddExpression *****************************************
	 * 
	 * AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
	 * 
	 * @throws SyntaxException
	 */
	Expression add_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 =	mult_expression();
		Expression e1 = null;
		Token op = null;
		while(t.kind == OP_PLUS || t.kind == OP_MINUS)
		{
			op = t;
			match(t.kind);
			e1 = mult_expression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	/** ********************************************** MultExpression *****************************************
	 * 
	 * MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
	 * 
	 * @throws SyntaxException
	 */
	Expression mult_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 =	unary_expression();
		Expression e1 = null;
		Token op = null;
		while(t.kind == OP_TIMES || t.kind == OP_DIV || t.kind == OP_MOD)
		{
			op = t;
			match(t.kind);
			e1 = unary_expression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	/** ********************************************** UnaryExpression *****************************************
	 * 
	 * UnaryExpression ::= OP_PLUS UnaryExpression  | OP_MINUS UnaryExpression | UnaryExpressionNotPlusMinus
	 *  
	 * UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary | IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
	 * 
	 * 
	 * @throws SyntaxException
	 */
	Expression unary_expression() throws SyntaxException {
		Token firstToken = t;
		Expression e = null;
		Token op = null;				//Will this be null in case of unary expression not plus minus?
		if(t.kind == OP_PLUS || t.kind == OP_MINUS )
		{
			op = t;
			match(t.kind);
			Expression e1 = unary_expression();
			e = new Expression_Unary(firstToken, op, e1); 
		}
	// ************************************** For UnaryExpressionNotPlusMinus ***********************************
		else if( t.kind == OP_EXCL|| t.kind == KW_x || t.kind == KW_y || t.kind == KW_r || t.kind == KW_a || t.kind == KW_X 
				|| t.kind == KW_Y || t.kind == KW_Z || t.kind == KW_A || t.kind == KW_R || t.kind == KW_DEF_X || t.kind == KW_DEF_Y 
				|| t.kind == INTEGER_LITERAL || t.kind == BOOLEAN_LITERAL || t.kind == LPAREN || t.kind == KW_sin || t.kind == KW_cos || t.kind == KW_atan
				|| t.kind == KW_abs || t.kind == KW_cart_x || t.kind ==KW_cart_y || t.kind == KW_polar_a || t.kind == KW_polar_r || t.kind == IDENTIFIER)
		{
			e = unary_expression_not_plus_minus();
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return e;
	}
	
	/** ********************************************** UnaryExpressionNotPlusMinus *****************************************
	 * 
	 * UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary | IdentOrPixelSelectorExpression 
	  					| KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y				
	 *
	 * Expression_PredefinedName(Token firstToken, Kind kind)
	 *
	 */
	Expression unary_expression_not_plus_minus() throws SyntaxException{
		Token firstToken = t;
		Expression e = null;
		Token op = null;
		if(t.kind == OP_EXCL)
		{
			op = t;
			match(OP_EXCL);
			Expression e1 = unary_expression();
			e = new Expression_Unary(firstToken, op, e1);
		}
		else if(t.kind == KW_x || t.kind == KW_y || t.kind == KW_r || t.kind == KW_a || t.kind == KW_X || t.kind == KW_Y 
				|| t.kind == KW_Z || t.kind == KW_A || t.kind == KW_R || t.kind == KW_DEF_X || t.kind == KW_DEF_Y )
		{
			e = new Expression_PredefinedName(firstToken, t.kind);
			match(t.kind);
		}
		else if(t.kind == INTEGER_LITERAL || t.kind == BOOLEAN_LITERAL || t.kind == LPAREN 
				|| t.kind == KW_sin || t.kind == KW_cos || t.kind == KW_atan || t.kind == KW_abs 
				|| t.kind == KW_cart_x || t.kind ==KW_cart_y || t.kind == KW_polar_a || t.kind == KW_polar_r)
		{
			e = primary();
		}
		else if(t.kind == IDENTIFIER)
		{
			e = ident_or_pixel_selector_expression();
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return e;
	}
	
	/** ***************************************************** Primary ***************************************************
	 * 
	 * Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
	 * @return 
	 */
	Expression primary() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		if(t.kind == INTEGER_LITERAL)
		{
			e = new Expression_IntLit(firstToken, Integer.parseInt(t.getText()));
			match(INTEGER_LITERAL);
		}
		else if(t.kind == LPAREN)
		{
			match(LPAREN);
			e = expression();
			match(RPAREN);
		}
		else if(t.kind == KW_sin || t.kind == KW_cos || t.kind == KW_atan || t.kind == KW_abs || t.kind == KW_cart_x || t.kind ==KW_cart_y || t.kind == KW_polar_a || t.kind == KW_polar_r)
		{
			e = function_application();
		}
		else if(t.kind == BOOLEAN_LITERAL)
		{
			e = new Expression_BooleanLit(firstToken, Boolean.valueOf(t.getText()));
			match(BOOLEAN_LITERAL);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return e;
	}

	/**  *************************************** IdentOrPixelSelectorExpression **************************************************
	 * 
	 * IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER
	 * IdentOrPixelSelectorExpression::=  IDENTIFIER (LSQUARE Selector RSQUARE  | epsilon)
	 * @return 
	 * 
	 */
	Expression ident_or_pixel_selector_expression() throws SyntaxException {
		Index i = null;
		Token firstToken = t;
		Token ident = t;
		if(t.kind == IDENTIFIER)
		{
			match(IDENTIFIER);
			if(t.kind == LSQUARE)
			{
				match(LSQUARE);
				i = selector(firstToken);
				match(RSQUARE);
			}
			else								// for epsilon
			{
				return new Expression_Ident(firstToken, ident);
			}
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return new Expression_PixelSelector(firstToken, ident, i);
	}
	
	/** ******************************************* Lhs *************************************************
	 *  Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | epsilon )
	 *  
	 *  IDENTIFIER has been matched in Statement when looking ahead.
	 *  
	 *  LHS(Token firstToken, Token name, Index index)
	 *  
	 * @return LHS
	 */
	LHS lhs(Token firstToken) throws SyntaxException {
		Index i = null;
		if(t.kind == LSQUARE)
		{
			match(LSQUARE);
			i = lhs_selector();
			match(RSQUARE);
			return new LHS(firstToken, firstToken, i);
		}
		else if(t.kind == OP_ASSIGN )				//in case of epsilon
		{
			return new LHS(firstToken, firstToken, null);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
	}
	
	/** **************************************** FunctionApplication **********************************************
	 * 
	 * FunctionApplication ::= FunctionName LPAREN Expression RPAREN | FunctionName  LSQUARE Selector RSQUARE 
	 * FunctionApplication ::= FunctionName (LPAREN Expression RPAREN | LSQUARE Selector RSQUARE )
	 * 
	 * 
	 * Expression_FunctionAppWithExprArg(Token firstToken, Kind function, Expression arg)
	 * Expression_FunctionAppWithIndexArg(Token firstToken, Kind function, Index arg)
	 * 
	 */
	Expression function_application() throws SyntaxException {
		Token firstToken = t;
		function_name();
		Expression e = null;
		Expression arg_e = null;
		Index arg_i = null;
		if(t.kind == LPAREN)
		{
			match(LPAREN);
			arg_e = expression();
			match(RPAREN);
			e = new Expression_FunctionAppWithExprArg(firstToken,firstToken.kind,arg_e);
		}
		else if(t.kind == LSQUARE)
		{
			match(LSQUARE);
			arg_i = selector(firstToken);
			match(RSQUARE);
			e = new Expression_FunctionAppWithIndexArg(firstToken,firstToken.kind,arg_i);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return e;
	}
	
	/** **************************************** FunctionName **********************************************
	 * 
	 * FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs| KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
	 * 
	 * @throws SyntaxException
	 */
	void function_name() throws SyntaxException {
		if(t.kind == KW_sin)
			match(KW_sin);
		else if(t.kind == KW_cos)
			match(KW_cos);
		else if(t.kind == KW_atan)
			match(KW_atan);
		else if(t.kind == KW_abs)
			match(KW_abs);
		else if(t.kind == KW_cart_x)
			match(KW_cart_x);
		else if(t.kind == KW_cart_y)
			match(KW_cart_y);
		else if(t.kind == KW_polar_a)
			match(KW_polar_a);
		else if(t.kind == KW_polar_r)
			match(KW_polar_r);
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
	}
	
	/** **************************************** LhsSelector **********************************************
	 * 
	 * LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
	 * 
	 * Index(Token firstToken, Expression e0, Expression e1)
	 * 
	 * @throws SyntaxException
	 */
	Index lhs_selector() throws SyntaxException {
		Index i = null;
		if(t.kind == LSQUARE)
		{
			match(LSQUARE);
			if(t.kind == KW_x)
			{
				i = xy_selector();
			}
			else if(t.kind == KW_r)
			{
				i = ra_selector();
			}
			else
			{
				String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
		    	throw new SyntaxException(t, message);
		    }
			match(RSQUARE);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return i;
		
	}
	
	/** **************************************** XySelector **********************************************
	 * 
	 * XySelector ::= KW_x COMMA KW_y 
	 * 
	 * Index(Token firstToken, Expression e0, Expression e1)
	 * 
	 * @throws SyntaxException
	 */
	Index xy_selector() throws SyntaxException {
		Expression e_x = null;
		Expression e_y = null;
		Token firstToken = t;
		if(t.kind == KW_x)
		{
			e_x = new Expression_PredefinedName(t,t.kind);
			match(KW_x);
			match(COMMA);
			e_y = new Expression_PredefinedName(t,t.kind);
			match(KW_y);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return new Index(firstToken,e_x,e_y);
	}
	
	/** **************************************** RaSelector **********************************************
	 * RaSelector ::= KW_r COMMA KW_A
	 * 
	 * @throws SyntaxException
	 */
	Index ra_selector() throws SyntaxException {
		Expression e_r = null;
		Expression e_A = null;
		Token firstToken = t;
		if(t.kind == KW_r )
		{
			e_r = new Expression_PredefinedName(t,t.kind);
			match(KW_r);
			match(COMMA);
			e_A = new Expression_PredefinedName(t,t.kind);
			match(KW_a);
		}
		else
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		return new Index(firstToken, e_r, e_A);
	}
	
	/** **************************************** Selector **********************************************
	 * 
	 * Selector ::=  Expression COMMA Expression
	 * 
	 * Index(Token firstToken, Expression e0, Expression e1)
	 * 
	 * @throws SyntaxException
	 */	
	Index selector(Token firstToken) throws SyntaxException 
	{
		Expression e0 = expression();
		if(t.kind == COMMA )
		{
			match(COMMA);
		}
		else																							
		{
			String message =  "Expected "+t.kind+" at " + t.line + ":" + t.pos_in_line;
	    	throw new SyntaxException(t, message);
	    }
		Expression e1 = expression();
		return new Index(firstToken, e0, e1);
	}
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line + "but got :"+t.kind;
		throw new SyntaxException(t, message);
	}
}
