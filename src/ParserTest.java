package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;

import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Simple test case with an empty program. This test expects an exception
	 * because all legal programs must have at least an identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("k", dec.name);
		assertNull(dec.e);
	}

	/*********************************************** MY TEST CASES ***********************************************/
	
	@Test
	public void test_1() throws LexicalException, SyntaxException {
		String input = "some int num = 2;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "some"); 
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("num", dec.name);
		assertEquals("Expression_IntLit [value=2]", dec.e.toString());
	}
	

	@Test
	public void test_2() throws LexicalException, SyntaxException {
		String input = "uf int student = (3298671);";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "uf"); 
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("student", dec.name);
		assertEquals("Expression_IntLit [value=3298671]", dec.e.toString());
	}
	
	@Test
	public void test_3() throws LexicalException, SyntaxException {
		String input = "p image k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "p"); 
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements
				.get(0);
		assertEquals("k", dec.name);
	}
	
	@Test
	public void test_4() throws LexicalException, SyntaxException {
		String input = "p image k <- @ 5;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "p"); 
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements.get(0);
		assertEquals("k", dec.name);
		assertEquals("Source_CommandLineParam [paramNum=Expression_IntLit [value=5]]", dec.source.toString());
	}
	
	@Test
	public void test_5() throws LexicalException, SyntaxException {
		String input = "uf url rebel= \"hi\" ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "uf"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_SourceSink dec = (Declaration_SourceSink) ast.decsAndStatements.get(0);
		assertEquals("KW_url",dec.type.toString());
		assertEquals("rebel", dec.name);
		assertEquals("hi", dec.source.firstToken.getText());
	}
	
	@Test
	public void test_6() throws LexicalException, SyntaxException {
		String input = "uf student[[x,y]]=-+15;ko=5;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "uf"); 
		Statement_Assign dec = (Statement_Assign) ast.decsAndStatements.get(0);
		assertEquals("student",dec.firstToken.getText());
		assertEquals(KW_x,dec.lhs.index.e0.firstToken.kind);
		assertEquals("OP_MINUS",dec.e.firstToken.kind.toString());
		assertEquals("Expression_Unary [op=OP_MINUS, e=Expression_Unary [op=OP_PLUS, e=Expression_IntLit [value=15]]]",dec.e.toString());
	}
	
	@Test
	public void test_7() throws LexicalException, SyntaxException {
		String input = "uf student=(taylor[true,duck]);";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "uf"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Statement_Assign dec = (Statement_Assign) ast.decsAndStatements.get(0);
		assertEquals("student", dec.firstToken.getText());
		assertEquals("taylor",dec.e.firstToken.getText());
		assertEquals("Expression_PixelSelector [name=taylor, index=Index [e0=Expression_BooleanLit [value=true], e1=Expression_Ident [name=duck]]]",dec.e.toString());
	}
	
	@Test
	public void test_8() throws LexicalException, SyntaxException
	{
		String input = "+-+a?+-x:+-a";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals(ast.firstToken.toString(), "[OP_PLUS,+,0,1,1,1]");
		assertEquals("Expression_Conditional [condition=Expression_Unary [op=OP_PLUS, e=Expression_Unary [op=OP_MINUS, e=Expression_Unary [op=OP_PLUS, e=Expression_PredefinedName [name=KW_a]]]], trueExpression=Expression_Unary [op=OP_PLUS, e=Expression_Unary [op=OP_MINUS, e=Expression_PredefinedName [name=KW_x]]], falseExpression=Expression_Unary [op=OP_PLUS, e=Expression_Unary [op=OP_MINUS, e=Expression_PredefinedName [name=KW_a]]]]", ast.toString());
	}
	

	@Test
	public void test_9() throws LexicalException, SyntaxException
	{
		String input = "-3?A:!student";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals(ast.firstToken.toString(), "[OP_MINUS,-,0,1,1,1]");
		assertEquals("Expression_Conditional [condition=Expression_Unary [op=OP_MINUS, e=Expression_IntLit [value=3]], trueExpression=Expression_PredefinedName [name=KW_A], falseExpression=Expression_Unary [op=OP_EXCL, e=Expression_Ident [name=student]]]", ast.toString());
	}
	
	@Test
	public void test_10() throws LexicalException, SyntaxException {
		String input = "uf image [x,y] student <- @7;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "uf"); 
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements
				.get(0);
		assertEquals("student", dec.name);
		assertEquals("[Declaration_Image [xSize=Expression_PredefinedName [name=KW_x], ySize=Expression_PredefinedName [name=KW_y], name=student, source=Source_CommandLineParam [paramNum=Expression_IntLit [value=7]]]]",ast.decsAndStatements.toString());

	}
	
	@Test
	public void test_11() throws LexicalException, SyntaxException
	{
		String input = "+x&-a";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals(ast.firstToken.toString(), "[OP_PLUS,+,0,1,1,1]");
		assertEquals("Expression_Binary [e0=Expression_Unary [op=OP_PLUS, e=Expression_PredefinedName [name=KW_x]], op=OP_AND, e1=Expression_Unary [op=OP_MINUS, e=Expression_PredefinedName [name=KW_a]]]", ast.toString());
	}
	
	@Test
	public void test_12() throws LexicalException, SyntaxException
	{
		String input = "+x%-a";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals(ast.firstToken.toString(), "[OP_PLUS,+,0,1,1,1]");
		assertEquals("Expression_Binary [e0=Expression_Unary [op=OP_PLUS, e=Expression_PredefinedName [name=KW_x]], op=OP_MOD, e1=Expression_Unary [op=OP_MINUS, e=Expression_PredefinedName [name=KW_a]]]", ast.toString());
	}
	
	@Test
	public void test_13() throws LexicalException, SyntaxException
	{
		String input = "(a-b)";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals(ast.firstToken.getText(), "a");
		assertEquals("Expression_Binary [e0=Expression_PredefinedName [name=KW_a], op=OP_MINUS, e1=Expression_Ident [name=b]]", ast.toString());
	}
	
	@Test
	public void test_14() throws LexicalException, SyntaxException
	{
		String input = "+x>=-a";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals("[OP_PLUS,+,0,1,1,1]",ast.firstToken.toString());
		assertEquals("Expression_Binary [e0=Expression_Unary [op=OP_PLUS, e=Expression_PredefinedName [name=KW_x]], op=OP_GE, e1=Expression_Unary [op=OP_MINUS, e=Expression_PredefinedName [name=KW_a]]]", ast.toString());
	}
	
	@Test
	public void test_15() throws LexicalException, SyntaxException
	{
		String input = "8|9&3!=7";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Expression ast = parser.expression();
		show(ast);
		assertEquals("[INTEGER_LITERAL,8,0,1,1,1]",ast.firstToken.toString());
		assertEquals("Expression_Binary [e0=Expression_IntLit [value=8], op=OP_OR, e1=Expression_Binary [e0=Expression_IntLit [value=9], op=OP_AND, e1=Expression_Binary [e0=Expression_IntLit [value=3], op=OP_NEQ, e1=Expression_IntLit [value=7]]]]", ast.toString());
	}
	
	@Test
	public void failed_test_1() throws LexicalException, SyntaxException
	{
		String input = "p image1 = Z;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "p"); 
		assertEquals("Program [name=p, decsAndStatements=[Statement_Assign [lhs=name [name=image1, index=null], e=Expression_PredefinedName [name=KW_Z]]]]", ast.toString());

	}
	
}
