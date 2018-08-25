package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import src.cop5556fa17.ImageFrame;
import cop5556fa17.ImageFrame;
import cop5556fa17.ImageSupport;
import java.awt.image.BufferedImage;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */
	
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		//cw = new ClassWriter(0);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		
		/** ************** PREDEFINED VARIABLES ************** */
		Integer intval = new Integer(0);
		
		//x:  the locations in the x direction, used as a loop index in assignment statements involving images
		cw.visitField(ACC_STATIC, "x", "I", null, intval);
		
		//y:  the location in the y direction, used as a loop index in assignment statements involving images
		cw.visitField(ACC_STATIC, "y", "I", null, intval);
		
		//X:  the upper bound on the value of loop index x.  It is also the width of the image. Obtain by invoking the ImageSupport.getX method
		cw.visitField(ACC_STATIC, "X", "I", null, intval);
		
		//Y:  the upper bound on the value of the loop index y.  It is also the height of the image.  Obtain by invoking the ImageSupport.getY method.
		cw.visitField(ACC_STATIC, "Y", "I", null, intval);
		
		//r:  the radius in the polar representation of cartesian location x and y.  Obtain from x and y with RuntimeFunctions.polar_r.
		cw.visitField(ACC_STATIC, "r", "I", null, intval);
		
		//a:  the angle, in degrees, in the polar representation of cartesian location x and y.  Obtain from x and y with RuntimeFunctions.polar_a.
		cw.visitField(ACC_STATIC, "a", "I", null, intval);
		
		//R:  the upper bound on r, obtain from polar_r(X,Y)
		cw.visitField(ACC_STATIC, "R", "I", null, intval);
		
		//A:  the upper bound on a, obtain from polar_a(0,Y)
		cw.visitField(ACC_STATIC, "A", "I", null, intval);
		
		//DEF_X:  the default image width.  For simplicity, let this be 256.
		cw.visitField(ACC_STATIC, "DEF_X", "I", null, 256);
		
		//DEF_Y:  the default image height.  For simplicity, let this be 256.
		cw.visitField(ACC_STATIC, "DEF_Y", "I", null, 256);
		
		//Z:  the value of a white pixel.  This is 0xFFFFFF or  16777215.
		cw.visitField(ACC_STATIC, "Z", "I", null, 0xFFFFFF); 
		
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	/** ************************************************** Declaration_Variable ********************************************** */
	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		String fieldName = declaration_Variable.name;
		String fieldType = null;
		FieldVisitor fv;
		Integer initValue = new Integer(0);
		if(declaration_Variable.of_type == Type.INTEGER)
			fieldType = "I";
		else if(declaration_Variable.of_type == Type.BOOLEAN)
			fieldType = "Z";
		fv = cw.visitField(ACC_STATIC, 	fieldName, fieldType, null, initValue);
		fv.visitEnd();
		if (declaration_Variable.e != null) 
		{	
			declaration_Variable.e.visit(this, arg);
			if(declaration_Variable.e.of_type == Type.INTEGER)
				fieldType = "I";
			else if(declaration_Variable.e.of_type == Type.BOOLEAN)
				fieldType = "Z";
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, fieldType);
		}
		return null;
	}

	/** ***************************************************** Expression_Binary ********************************************** */
	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		Label stackElementsMatch = new Label();
		Label endLabelsForIfElse = new Label();
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		Kind operator =	expression_Binary.op;
		if(operator == Kind.OP_PLUS)
		{
			mv.visitInsn(IADD);
		}
		else if(operator == Kind.OP_MINUS)
		{
			mv.visitInsn(ISUB);
		}
		else if(operator == Kind.OP_DIV)
		{
			mv.visitInsn(IDIV);
		}
		else if(operator == Kind.OP_TIMES)
		{
			mv.visitInsn(IMUL);
		}
		else if(operator == Kind.OP_MOD)
		{
			mv.visitInsn(IREM);
		}
		else if(operator == Kind.OP_AND)
		{
			mv.visitInsn(IAND);
		}
		else if(operator == Kind.OP_OR)
			mv.visitInsn(IOR);
		else if(operator == Kind.OP_EQ)
		{
			mv.visitJumpInsn(IF_ICMPEQ, stackElementsMatch);
			mv.visitLdcInsn(false);
		}
		else if(operator == Kind.OP_NEQ)
		{
			mv.visitJumpInsn(IF_ICMPNE, stackElementsMatch);
			mv.visitLdcInsn(false);
		}
		else if(operator == Kind.OP_GE)
		{
			mv.visitJumpInsn(IF_ICMPGE, stackElementsMatch);
			mv.visitLdcInsn(false);
		}
		else if(operator == Kind.OP_GT)
		{
			mv.visitJumpInsn(IF_ICMPGT, stackElementsMatch);
			mv.visitLdcInsn(false);
		}
		else if(operator == Kind.OP_LE)
		{
			mv.visitJumpInsn(IF_ICMPLE, stackElementsMatch);
			mv.visitLdcInsn(false);
		}
		else if(operator == Kind.OP_LT)
		{
			mv.visitJumpInsn(IF_ICMPLT, stackElementsMatch);
			mv.visitLdcInsn(false);
		}
		mv.visitJumpInsn(GOTO, endLabelsForIfElse);
		mv.visitLabel(stackElementsMatch);
		mv.visitLdcInsn(true);
		mv.visitLabel(endLabelsForIfElse);
		return null;
	}


	/** ************************************************* Expression_Unary ******************************************* */
	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
		if(expression_Unary.op == Kind.OP_PLUS)
			expression_Unary.e.visit(this, arg);
		else if(expression_Unary.op == Kind.OP_MINUS)
		{
			expression_Unary.e.visit(this, arg);
			mv.visitInsn(INEG);
		}
		else if(expression_Unary.op == Kind.OP_EXCL && expression_Unary.of_type == Type.INTEGER)
		{
			expression_Unary.e.visit(this, arg);
			mv.visitLdcInsn(Integer.MAX_VALUE);
			mv.visitInsn(IXOR);
		}
		else if(expression_Unary.op == Kind.OP_EXCL && expression_Unary.of_type == Type.BOOLEAN)
		{
			expression_Unary.e.visit(this, arg);
			Label itsTrue = new Label();
			Label itsFalse = new Label();
			mv.visitLdcInsn(new Boolean(true));
			mv.visitJumpInsn(IF_ICMPEQ, itsTrue);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitJumpInsn(GOTO, itsFalse);
			mv.visitLabel(itsTrue);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitLabel(itsFalse);
		}
		return null;
	}

	/** *****************************************************  Index  ***************************************************** */
	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(!index.isCartesian())
		{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		return null;
	}

	/** ********************************************* Expression_PixelSelector ******************************************** */
	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)	throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel",ImageSupport.getPixelSig, false);
		return null;
	}

	/** ********************************************** Expression_Conditional ********************************************* */
	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg) throws Exception {
		// TODO 
		Label conditionIsTrue = new Label();
		Label endConditions = new Label();
		expression_Conditional.condition.visit(this, arg);
		mv.visitLdcInsn(new Boolean(true));
		mv.visitJumpInsn(IF_ICMPEQ, conditionIsTrue);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, endConditions);
		mv.visitLabel(conditionIsTrue);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitLabel(endConditions);
		return null;
	}

	/** ************************************************ Declaration_Image ******************************************** */
	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		String fieldName = declaration_Image.name;
		String fieldType = ImageSupport.ImageDesc;
		FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();
		if(declaration_Image.source != null)
		{
			declaration_Image.source.visit(this, arg); //puts image path on stack
			if(declaration_Image.xSize != null && declaration_Image.ySize != null)
			{
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;" , false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer", "valueOf","(I)Ljava/lang/Integer;" , false);
			}
			else
			{
				//If no index is given pass null for the xSize and ySize parameters
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			//Use the cop5556fa17.ImageSupport.readImage method to read the image loaded on stack
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig ,false);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		else
		{
			if(declaration_Image.xSize != null && declaration_Image.ySize != null)
			{
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			}
			else//use values of the predefined constants def_X=256 and def_Y=256
			{
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
			}
			//use makeImage method to create an image
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig ,false);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		return null;
	}
	
	/** *********************************************** Source_StringLiteral ********************************************* */
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	/** ********************************************* Source_CommandLineParam ******************************************** */
	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		mv.visitVarInsn(ALOAD,0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	/** ************************************************** Source_Ident ************************************************* */
	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, ImageSupport.StringDesc);
		return null;
	}

	/** ************************************************ Declaration_SourceSink ***************************************** */
	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		//Add field
		String fieldName = declaration_SourceSink.name;
		String fieldType = ImageSupport.StringDesc;
		FieldVisitor fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();
		if(declaration_SourceSink.source != null)
		{
			declaration_SourceSink.source.visit(this, arg); //leaves value on stack
		}
		mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		return null;
	}
	
	/** ************************************************ Expression_IntLit ********************************************** */
	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		Integer inVal = new Integer(expression_IntLit.value);
		mv.visitLdcInsn(inVal);
		return null;
	}

	/** ***************************************** Expression_FunctionAppWithExprArg ************************************* */
	@Override
	public Object visitExpression_FunctionAppWithExprArg(Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.function == Kind.KW_abs)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		else if(expression_FunctionAppWithExprArg.function == Kind.KW_log)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		return null;
	}

	/** ***************************************** Expression_FunctionAppWithIndexArg ************************************ */
	@Override
	public Object visitExpression_FunctionAppWithIndexArg(Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_x)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_y)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_r)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_a)
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		return null;
	}

	/** ******************************************** Expression_PredefinedName ***************************************** */
	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg) throws Exception {
		// TODO HW6
		if(expression_PredefinedName.kind == Kind.KW_x)
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		else if(expression_PredefinedName.kind == Kind.KW_y)
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
		else if(expression_PredefinedName.kind == Kind.KW_X)
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
		else if(expression_PredefinedName.kind == Kind.KW_Y)
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
		else if(expression_PredefinedName.kind == Kind.KW_r)
			mv.visitFieldInsn(GETSTATIC, className, "r", "I");
		else if(expression_PredefinedName.kind == Kind.KW_a)
			mv.visitFieldInsn(GETSTATIC, className, "a", "I");
		else if(expression_PredefinedName.kind == Kind.KW_R)
			mv.visitFieldInsn(GETSTATIC, className, "R", "I");
		else if(expression_PredefinedName.kind == Kind.KW_A)
			mv.visitFieldInsn(GETSTATIC, className, "A", "I");
		else if(expression_PredefinedName.kind == Kind.KW_DEF_X)
			mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
		else if(expression_PredefinedName.kind == Kind.KW_DEF_Y)
			mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
		else if(expression_PredefinedName.kind == Kind.KW_Z)
			mv.visitFieldInsn(GETSTATIC, className, "Z", "I");
		return null;
	}

	/** ************************************************* Statement_Out ************************************************ */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5 only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		if(statement_Out.getDec().of_type == Type.INTEGER)
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER); 
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);	
		}	
		else if(statement_Out.getDec().of_type ==  Type.BOOLEAN)
		{
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);	
		}
		else if(statement_Out.getDec().of_type ==  Type.IMAGE)
		{	
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, Type.IMAGE);
			statement_Out.sink.visit(this, arg);
		}
		return null;
	}

	/** ************************************************** Statement_In ************************************************* */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		String fieldName = statement_In.name;
		String fieldType = null;
		statement_In.source.visit(this, arg);
		if(statement_In.getDec().of_type == Type.INTEGER)
		{
			fieldType = "I";
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
		}
		else if(statement_In.getDec().of_type == Type.BOOLEAN)
		{
			fieldType = "Z";
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",false);
		}
		else if(statement_In.getDec().of_type == Type.IMAGE)
		{
			fieldType = ImageSupport.ImageDesc;
			Declaration_Image img = (Declaration_Image)statement_In.getDec(); //cast to Declaration_Image
		    if(img.xSize == null && img.ySize == null)
		    {
		    	mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
		    }
		    else
		    {
		    	//getX
		    	mv.visitFieldInsn(GETSTATIC, className, fieldName, fieldType);
		    	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false); //puts x_size
		    	mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;" , false); //makes Integer Object of x_size
		    	
		    	//getY
		    	mv.visitFieldInsn(GETSTATIC, className, fieldName, fieldType);
		    	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false); //puts y_size
		    	mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;" , false); //makes Integer Object of y_size
		    }
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
		}
		mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		return null;
	}
	
	/** ****************************************************  LHS  ****************************************************** */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		String fieldName = lhs.name;
		String fieldType = null;
		if(lhs.of_type == Type.INTEGER)
		{
			fieldType = "I";
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		else if(lhs.of_type == Type.BOOLEAN)
		{
			fieldType = "Z";
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		else if(lhs.of_type == Type.IMAGE)
		{
			fieldType = "Ljava/awt/image/BufferedImage;";
	    	mv.visitFieldInsn(GETSTATIC, className, fieldName, fieldType);
	    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
	    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
	    	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		}
		return null;
	}
	
	/** ************************************************** Sink_SCREEN ************************************************* */
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	/** ************************************************** Sink_Ident ************************************************** */
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	/** *********************************************** Expression_BooleanLit ****************************************** */
	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		Boolean boolVal = new Boolean(expression_BooleanLit.value);
		mv.visitLdcInsn(boolVal);
		return null;	
	}

	/** ************************************************** Expression_Ident ******************************************** */
	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		//TODO
		String fieldName = expression_Ident.name;
		String fieldType = null;
		if(expression_Ident.of_type == Type.INTEGER)
			fieldType = "I";
		else if(expression_Ident.of_type == Type.BOOLEAN)
			fieldType = "Z";
		else if(expression_Ident.of_type == Type.IMAGE)
			fieldType = ImageSupport.ImageDesc;
		else if(expression_Ident.of_type == Type.URL || expression_Ident.of_type == Type.FILE)
			fieldType = "Ljava/lang/String;";
		mv.visitFieldInsn(GETSTATIC, className, fieldName, fieldType);
		return null;
	}

	/** **************************************************** Statement_Assign ****************************************** */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label for_start = new Label();
		Label end1 = new Label();
		Label end2 = new Label();
		
		if(statement_Assign.lhs.index != null && statement_Assign.lhs.of_type == Type.IMAGE)
		{	
			//Set Predefined Variables
			mv.visitLdcInsn(0);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitLdcInsn(0);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
	    	mv.visitMethodInsn(INVOKESTATIC,ImageSupport.className, "getX", ImageSupport.getXSig, false); //puts x_size
	    	mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
	    	
	    	mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
	    	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false); //puts y_size
	    	mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
	    	
	    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
	    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false); 
	    	mv.visitFieldInsn(PUTSTATIC, className, "r", "I");
	    	
	    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
	    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
	    	mv.visitFieldInsn(PUTSTATIC, className, "a", "I");
	    	
	    	mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
	    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
	    	mv.visitFieldInsn(PUTSTATIC, className, "R", "I");

	    	mv.visitLdcInsn(new Integer(0));
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
	    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
	    	mv.visitFieldInsn(PUTSTATIC, className, "A", "I");

	       	mv.visitLabel(for_start);
	 
			    	statement_Assign.e.visit(this, arg); 		//puts rgb value on stack
			    	statement_Assign.lhs.visit(this, arg); 		//sets pixel
			    	
			    	//increment y
			    	mv.visitLdcInsn(1);
			    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			    	mv.visitInsn(IADD);
			    	mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			    	
			    	// Put y and Y to compare
			    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			    	mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			    	
			    	//exit inner loop if y = Y to increment x, else calculate r and a and jump to start
			    	mv.visitJumpInsn(IF_ICMPEQ, end1);
			    	
			    	//calculate r
			    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			    	mv.visitFieldInsn(PUTSTATIC, className, "r", "I");		
			    	
			    	//calculate a
			    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			    	mv.visitFieldInsn(PUTSTATIC, className, "a", "I");
			    	
			    	mv.visitJumpInsn(GOTO, for_start);
			    	
			    //end of inner loop
	    		
			    //Deals with the outer loop
			    mv.visitLabel(end1);
			    
		    	//increment x
		    	mv.visitLdcInsn(1);
		    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		    	mv.visitInsn(IADD);					
		    	mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
		     	
		    	// Put x and X to compare
		    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		    	mv.visitFieldInsn(GETSTATIC, className, "X", "I");
		    	
		    	//exit outer loop aswell if x = X, else calculate r & a, rest y = 0 and jump to start
		    	mv.visitJumpInsn(IF_ICMPEQ, end2); 
		    	
		    	//reset y
		    	mv.visitLdcInsn(0);
		    	mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
		    	
		    	//calculate r with new x
		    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
		    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		    	mv.visitFieldInsn(PUTSTATIC, className, "r", "I");
		    	
		    	//calculate a with new x
		    	mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		    	mv.visitFieldInsn(GETSTATIC, className, "y", "I");
		    	mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		    	mv.visitFieldInsn(PUTSTATIC, className, "a", "I");
		    	
		    	mv.visitJumpInsn(GOTO, for_start);
		    	
		    	//end of outer loop loop
		    	mv.visitLabel(end2);
	    }
		else
		{
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		return null;
	}
}
