/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	
	public static enum State {
		START, IN_DIGIT, IN_IDENTIFIER, IN_STRING_LITERAL, COMMENTS;
	}

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}
	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  

	HashMap<String, Kind> map = new HashMap<String, Kind>();		//For Keywords and Boolean Literals
	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}

	public static boolean isParsable(String input){
	    boolean parsable = true;
	    try{
	        Integer.parseInt(input);
	    }catch(NumberFormatException e){
	        parsable = false;
	    }
	    return parsable;
	}

	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
	   /* TODO  Replace this with a correct and complete implementation!!! */	

	   map.put("x", Kind.KW_x);
	   map.put("X", Kind.KW_X);
	   map.put("y", Kind.KW_y);
	   map.put("Y", Kind.KW_Y);
	   map.put("r", Kind.KW_r);
	   map.put("R", Kind.KW_R);
	   map.put("a", Kind.KW_a);
	   map.put("A", Kind.KW_A);
	   map.put("Z", Kind.KW_Z);
	   map.put("DEF_X", Kind.KW_DEF_X);
	   map.put("DEF_Y", Kind.KW_DEF_Y);
	   map.put("SCREEN", Kind.KW_SCREEN);
	   map.put("cart_x", Kind.KW_cart_x);
	   map.put("cart_y", Kind.KW_cart_y);
	   map.put("polar_a", Kind.KW_polar_a);
	   map.put("polar_r", Kind.KW_polar_r);
	   map.put("abs", Kind.KW_abs);
	   map.put("sin", Kind.KW_sin);
	   map.put("cos", Kind.KW_cos);
	   map.put("atan", Kind.KW_atan);
	   map.put("log", Kind.KW_log);
	   map.put("image", Kind.KW_image);
	   map.put("int", Kind.KW_int);
	   map.put("boolean", Kind.KW_boolean);
	   map.put("url", Kind.KW_url);
	   map.put("file", Kind.KW_file);
	   map.put("true", Kind.BOOLEAN_LITERAL);
	   map.put("false", Kind.BOOLEAN_LITERAL);
	   
	   int pos = 0;
	   int line = 1;
	   int posInLine = 1;
	   State state = State.START;
	   StringBuilder curr_identifier = new StringBuilder();
	   StringBuilder string_literal = new StringBuilder();
	   StringBuilder integer_literal = new StringBuilder();
		
	   int i=0;
	   while(i<chars.length)
	   {
			switch(state) {
			
			case START :
				pos=i;
				switch(chars[i]) 
				{
				
					//Handling End of Input Character
					case EOFchar:
						tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
						break;
				
					//Handling 0 
					case '0':
						tokens.add(new Token(Kind.INTEGER_LITERAL, pos, 1, line, posInLine));
						posInLine++;
						break;
						
					//Handling SEPARATORS
					case ';' : 
						tokens.add(new Token(Kind.SEMI, pos, 1, line, posInLine));
						posInLine++;
						break;
						
					case ',' :
						tokens.add(new Token(Kind.COMMA, pos, 1, line, posInLine));
						posInLine++;
						break;
						
					case '[' :
						tokens.add(new Token(Kind.LSQUARE, pos, 1, line, posInLine));
						posInLine++;
						break;
						
					case ']' :
						tokens.add(new Token(Kind.RSQUARE, pos, 1, line, posInLine));
						posInLine++;
						break;
						
					case '(' :
						 tokens.add(new Token(Kind.LPAREN, pos, 1, line, posInLine));
						 posInLine++;
						 break;
						 
					case ')' :
						 tokens.add(new Token(Kind.RPAREN, pos, 1, line, posInLine));
						 posInLine++;
						 break;
					
					//Handling OPERATORS
					/* =  |  >  | <  |  !  |  ?  |   :   |  ==  |  !=  |   <=  | >= |  &  |   |  |  +  |  -  |  * |  /  |  %  |  **  | ->  | <-  | @ */
					  
					case '=' :
						if(chars[i+1]=='=') {
							 tokens.add(new Token(Kind.OP_EQ, pos, 2, line, posInLine));
							 posInLine+=2;
							 i++;
						}
						else {
							 tokens.add(new Token(Kind.OP_ASSIGN, pos, 1, line, posInLine));
							 posInLine+=1;
						}
						break;
						
					case '>' :
						if(chars[i+1]=='=') {
							tokens.add(new Token(Kind.OP_GE, pos, 2, line, posInLine));
							posInLine+=2;
							i++;
						}
						else
						{
							tokens.add(new Token(Kind.OP_GT, pos, 1, line, posInLine));
							posInLine+=1;
						}
						break;
						
					case '<' :
						switch(chars[i+1]) {
							case '-' : 
								tokens.add(new Token(Kind.OP_LARROW, pos, 2, line, posInLine));
								posInLine+=2;
								i++;
								break;
							case '=' : 
								tokens.add(new Token(Kind.OP_LE, pos, 2, line, posInLine));
								posInLine+=2;
								i++;
								break;
							default : 
								tokens.add(new Token(Kind.OP_LT, pos, 1, line, posInLine));
								posInLine+=1;
							break;	
						}
						break;
						
					case '!' :
						switch(chars[i+1])
						{
							case '=' :
								tokens.add(new Token(Kind.OP_NEQ, pos, 2, line, posInLine));
								posInLine+=2;
								i++;
								break;
							default :
								tokens.add(new Token(Kind.OP_EXCL, pos, 1, line, posInLine));
								posInLine+=1;
								break;
						}
						break;
						
					case '-' :
						if(chars[i+1] == '>') {
							tokens.add(new Token(Kind.OP_RARROW, pos, 2, line, posInLine));
							i++;
							posInLine+=2;
						}
						else
						{
							tokens.add(new Token(Kind.OP_MINUS, pos, 1, line, posInLine));
							posInLine+=1;
						}
						break;
						
					case '*' :
						if(chars[i+1] == '*') {
							tokens.add(new Token(Kind.OP_POWER, pos, 2, line, posInLine));
							i++;
							posInLine+=2;
						}
						else {
							tokens.add(new Token(Kind.OP_TIMES, pos, 1, line, posInLine));
							posInLine+=1;
						}
						break;
						
					case '+' :
						tokens.add(new Token(Kind.OP_PLUS, pos, 1, line, posInLine));
						posInLine+=1;
						break;
					
					case '/' :
						if(chars[i+1]=='/')
						{
							state = State.COMMENTS;
							posInLine+=1;
						}
						else
						{
							tokens.add(new Token(Kind.OP_DIV, pos, 1, line, posInLine));
							posInLine+=1;
						}
						break;
					
					case '%' :
						tokens.add(new Token(Kind.OP_MOD, pos, 1, line, posInLine));
						posInLine+=1;
						break;
						
					case '@' :
						tokens.add(new Token(Kind.OP_AT, pos, 1, line, posInLine));
						posInLine+=1;
						break;
						
					case '?' :
						tokens.add(new Token(Kind.OP_Q, pos, 1, line, posInLine));
						posInLine+=1;
						break;
						
					case ':' :
						tokens.add(new Token(Kind.OP_COLON, pos, 1, line, posInLine));
						posInLine+=1;
						break;
					
					case '&' :
						tokens.add(new Token(Kind.OP_AND, pos, 1, line, posInLine));
						posInLine+=1;
						break;
						
					case '|' :
						tokens.add(new Token(Kind.OP_OR, pos, 1, line, posInLine));
						posInLine+=1;
						break;
					
					case '\n' : 
						line++;
						posInLine = 1;
						break;
						
					case '\r' :
						if(chars[i+1]=='\n'){
							i+=1;
							pos+=1; //corrected
						}
						else {	pos+=1; }
						line++;
						posInLine = 1;
						break;
						
					case '\"' :
						state = State.IN_STRING_LITERAL;
						posInLine++;
						break;

					default :
						
						if(Character.isDigit(chars[i])){
							state = State.IN_DIGIT;
						//	if(chars[i+1]==EOFchar){
								i--;
						//	}
						//	else {integer_literal.append(chars[i]);}
						}
						else if (Character.isJavaIdentifierStart(chars[i])) {
			                 state = State.IN_IDENTIFIER;			                 
			                 if(chars[i+1]==EOFchar){
									i--;
			                 }
			                 else {curr_identifier.append(chars[i]);}
			             } 
			             else if (Character.isWhitespace(chars[i])){
			            	 posInLine++;
			             }
			             else //corrected - added
			             {
			            	String msg ="Illegal Character at "+line+" pos "+pos;
			 				throw new LexicalException(msg,pos);
			             }
						break;
					}
				break;
				
			case IN_DIGIT :
				while(Character.isDigit(chars[i])){
					integer_literal.append(chars[i]);
					i++;				}
				i--;
				if(isParsable(integer_literal.toString()))
				{
					int length_of_integer = i-pos+1;
					tokens.add(new Token(Kind.INTEGER_LITERAL, pos, length_of_integer, line, posInLine));
					posInLine+=length_of_integer;
					
					
				}
				else 
				{
					String msg ="Value too Large for INT at "+line;
					throw new LexicalException(msg,pos);
				}
				state = State.START;
				integer_literal = new StringBuilder();
				break;
				
			case IN_IDENTIFIER :				
				while(Character.isJavaIdentifierPart(chars[i])&&chars[i]!=EOFchar){
					curr_identifier.append(chars[i]);
					i++;
				}i--;
				int length_of_identifier = i-pos+1;
				Kind value = map.get(curr_identifier.toString());
				if(value!= null){
					tokens.add(new Token(value, pos, length_of_identifier, line, posInLine));
					curr_identifier = new StringBuilder();
				}
				else {
					tokens.add(new Token(Kind.IDENTIFIER, pos, length_of_identifier, line, posInLine));
					curr_identifier = new StringBuilder();
				}
				posInLine+=length_of_identifier;
				state = State.START;
				break;
				
			case IN_STRING_LITERAL :
				while(chars[i]!= '\"'){
					if(chars[i]==EOFchar)
					{
						 String msg ="String not terminated. EOF reached at line "+line;
						 throw new LexicalException(msg,i);
					}
					if(chars[i] == '\\')
					{

						if(chars[i+1] == 'b' || chars[i+1] == 't' || chars[i+1] == 'n' || chars[i+1] == 'f' || chars[i+1] == 'r' 
								|| chars[i+1] == '\'' || chars[i+1] =='\"' || chars[i+1] == '\\')
						{
							i+=1;
						}
						else
						{
							String msg ="Illegal Escape Sequence in String at line "+line+" position "+pos;
							throw new LexicalException(msg,i);
						}
						/*
						if(chars[i+1]!= 'b' && chars[i+1]!= 't' && chars[i+1]!= 'n' && chars[i+1]!= 'f' && chars[i+1]!= 'r' 
								&& chars[i+1]!= '\'' && chars[i+1]!='\"' && chars[i+1] != '\\')
						{
							String msg ="Illegal Escape Sequence in String at line "+line+" position "+pos;
							throw new LexicalException(msg,i);
						}
						else
						{
							//i+=1;
						}
						*/
					}
					if(chars[i]=='\n' ||chars[i]=='\r')
					{
						String msg ="Illegal Escape Sequence in String at line "+line;
						throw new LexicalException(msg,i);
					}
					string_literal.append(chars[i]);
					i++;
				}
				int len = i-pos;
				tokens.add(new Token(Kind.STRING_LITERAL, pos, len+1 /* string_literal.length()+2*/, line, posInLine-1)); // corrected string_literal.length()+2 to len+1
				posInLine+=len; //string_literal.length()+1;
				state = State.START;
				break;
				
			case COMMENTS : 
					while(chars[i]!= '\n' && chars[i] != '\r' && chars[i] != EOFchar )
					{
						i++;
						System.out.print(chars[i]);
					}

					if(chars[i]=='\r' && chars[i+1] == '\n')
					{
						i++;
					}
					
					if(chars[i] == EOFchar ) i--;
					posInLine=1;
					line++;
					state=State.START;
				break;
				
			default:
				break;
				
			} i++;
		}
		return this;
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
