package cop5556fa17.AST;

import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.*;

public abstract class Statement extends ASTNode {

	public Type of_type;
	
	public Statement(Token firstToken) {
		super(firstToken);
	}

}
