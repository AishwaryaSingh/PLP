/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;

import static cop5556fa17.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}

	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = "\" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}

	/* MY TEST CASES */
	
	@Test
	public void myStringLiteral() throws LexicalException{
		String input = "h \"hi greetings\" k";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 1, 1, 1);
		checkNext(scanner, STRING_LITERAL, 2, 14, 1, 3);
		checkNext(scanner, IDENTIFIER, 17, 1, 1, 18);
	}

	@Test
	public void mySeparators() throws LexicalException {
		String input = ";;\r\n(==)";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, LPAREN, 4, 1, 2, 1);
		checkNext(scanner, OP_EQ, 5, 2, 2, 2);
		checkNext(scanner, RPAREN, 7, 1, 2, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myInteger() throws LexicalException {
		String input = ";;\r(12)\n234!";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, LPAREN, 3, 1, 2, 1);
		checkNext(scanner, INTEGER_LITERAL, 4, 2, 2, 2);
		checkNext(scanner, RPAREN, 6, 1, 2, 4);
		checkNext(scanner,INTEGER_LITERAL, 8, 3, 3, 1);
		checkNext(scanner,OP_EXCL, 11, 1, 3, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myIdentifiers() throws LexicalException {
		String input = "true\r(us)\n12 Y 22";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, LPAREN, 5, 1, 2, 1);
		checkNext(scanner, IDENTIFIER, 6, 2, 2, 2);
		checkNext(scanner, RPAREN, 8, 1, 2, 4);
		checkNext(scanner, INTEGER_LITERAL, 10, 2, 3, 1);
		checkNext(scanner, KW_Y, 13, 1, 3, 4);
		checkNext(scanner, INTEGER_LITERAL, 15, 2, 3, 6);
		checkNextIsEOF(scanner);
	}
		
	@Test
	public void myRandomTestWhichShouldBeLegal() throws LexicalException {
		String input = "(us)\n12 true";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, IDENTIFIER, 1, 2, 1, 2);
		checkNext(scanner, RPAREN, 3, 1, 1, 4);
		checkNext(scanner, INTEGER_LITERAL, 5, 2, 2, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 8, 4, 2, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myOtherRandomTestCalledBob() throws LexicalException {
		String input = "12 Y 22";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, KW_Y, 3, 1, 1, 4);
		checkNext(scanner, INTEGER_LITERAL, 5, 2, 1, 6);
	
	}

	@Test
	public void checkForLegalComments() throws LexicalException {
		String input = "12//twelve\n12//hellllo";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 11, 2, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	
	
	@Test
	public void myFailedTest_1() throws LexicalException {
		String input = "/ /// Hoping this is /// still in comment. \r\n / //";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,OP_DIV,0,1,1,1);
		
		checkNext(scanner,OP_DIV,46,1,2,2);
		
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myFailedTest_2() throws LexicalException {
		String input =  "\" \\\\ \"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,STRING_LITERAL,0,6,1,1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myFailedTest_3() throws LexicalException {
		String input = "\" \\b \\t \\n \\f \\r \\\" \\' \"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,STRING_LITERAL,0,24,1,1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myFailedTest_4() throws LexicalException {
		String input = "<= ^";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(3,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void myFailedTest_5() throws LexicalException {
		String input = "01 12 3\n23 4567890";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,INTEGER_LITERAL,0,1,1,1);
		checkNext(scanner,INTEGER_LITERAL,1,1,1,2);
		checkNext(scanner,INTEGER_LITERAL,3,2,1,4);
		checkNext(scanner,INTEGER_LITERAL,6,1,1,7);
		checkNext(scanner,INTEGER_LITERAL,8,2,2,1);
		checkNext(scanner,INTEGER_LITERAL,11,7,2,4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void myFailedTest_6() throws LexicalException {
		String input = "#";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(0,e.getPos());
			throw e;
		}
	}

	@Test
	public void myFailedTest_7() throws LexicalException {
		String input = "\" hello\"123\"456\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner,STRING_LITERAL,0,8,1,1);
		checkNext(scanner,INTEGER_LITERAL,8,3,1,9);
		checkNext(scanner,STRING_LITERAL,11,5,1,12);
		checkNextIsEOF(scanner);
	}
}
