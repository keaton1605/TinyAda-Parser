// Parsing shell partially completed

// Note that EBNF rules are provided in comments
// Just add new methods below rules without them

// p1: Tests multiple typeDefinitions, AddingOperators, multiplyingOperators, enumerationTypeDefinition
// p2: Tests exitStatement, while loops, identifierList
// p3: Tests nested if-else statements, simpleExpressions, multiple expressions, multiple relationalOperators
// p4: Tests nested while loops, subprogramBody (this was our initial test)
// p5: Tests multiple typeDefinitions, range, arrayTypeDefinition

// f1: Tests for missing semicolon, undeclared identifier (scope)
// f2: Tests for missing end loop
// f3: Tests having an elsif after an else
// f4: Tests for wrong placement of procedure
// f5: Tests for wrong array syntax

import java.io.FileInputStream;
import java.io.IOException;
import java.io.*;
import java.util.*;

public class Parser extends Object{

   private Chario chario;
   private Scanner scanner;
   private Token token;
   private SymbolTable table;
   public int flag;					
   
   //table: table holding all of the symbol entries 
   //flag: int to determine whether scope or role analysis is applied.
   
   private Set<Integer> addingOperator,
                        multiplyingOperator,
                        relationalOperator,
                        basicDeclarationHandles,
                        statementHandles;

   public Parser(Chario c, Scanner s){
      chario = c;
      scanner = s;
      initHandles();
      initTable();
      token = scanner.nextToken();
   }

   public void reset(){
      scanner.reset();
      initTable();
      token = scanner.nextToken();
   }

   private void initHandles(){
      addingOperator = new HashSet<Integer>();
      addingOperator.add(Token.PLUS);
      addingOperator.add(Token.MINUS);
      multiplyingOperator = new HashSet<Integer>();
      multiplyingOperator.add(Token.MUL);
      multiplyingOperator.add(Token.DIV);
      multiplyingOperator.add(Token.MOD);
      relationalOperator = new HashSet<Integer>();
      relationalOperator.add(Token.EQ);
      relationalOperator.add(Token.NE);
      relationalOperator.add(Token.LE);
      relationalOperator.add(Token.GE);
      relationalOperator.add(Token.LT);
      relationalOperator.add(Token.GT);
      basicDeclarationHandles = new HashSet<Integer>();
      basicDeclarationHandles.add(Token.TYPE);
      basicDeclarationHandles.add(Token.ID);
      basicDeclarationHandles.add(Token.PROC);
      statementHandles = new HashSet<Integer>();
      statementHandles.add(Token.EXIT);
      statementHandles.add(Token.ID);
      statementHandles.add(Token.IF);
      statementHandles.add(Token.LOOP);
      statementHandles.add(Token.NULL);
      statementHandles.add(Token.WHILE);
   }

   private void acceptRole(SymbolEntry s, int expected, String errorMessage){
      if (s.role != SymbolEntry.NONE && s.role != expected) {
    	  if (flag == 1)
    		  chario.putError(errorMessage);
      }
   } //Performs role analysis on input identifier, only prints if toggled

   private void acceptRole(SymbolEntry s, Set<Integer> expected, String errorMessage){
      if (s.role != SymbolEntry.NONE && ! (expected.contains(s.role)))
    	  if (flag == 1)
    		  chario.putError(errorMessage);
   } //Performs role analysis on input identifier, only prints if toggled
   
   private void accept(int expected, String errorMessage){
      if (token.code != expected)
         fatalError(errorMessage);
      token = scanner.nextToken();
   }

   private void fatalError(String errorMessage){
      chario.putError(errorMessage);
      throw new RuntimeException("Fatal error");
   }
   
   /*
   Three new routines for scope analysis.
   */
   
   private void initTable(){
      table = new SymbolTable(chario);
      table.enterScope();
      SymbolEntry entry = table.enterSymbol("BOOLEAN");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("CHAR");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("INTEGER");
      entry.setRole(SymbolEntry.TYPE);
      entry = table.enterSymbol("TRUE");
      entry.setRole(SymbolEntry.CONST);
      entry = table.enterSymbol("FALSE");
      entry.setRole(SymbolEntry.CONST);
   }      

   private SymbolEntry enterId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
   		  entry = table.enterSymbol(token.string);
   	  else {
   	      if (flag != 0)
   	    	  fatalError("identifier expected");
      }	//Performs the analysis no matter the flag, only prints results with certain flags
      
      token = scanner.nextToken();
      return entry;
   }

   private SymbolEntry findId(){
      SymbolEntry entry = null;
   	  if (token.code == Token.ID)
   	  	  entry = table.findSymbol(token.string);
   	  else {
   	      if (flag != 0) 
   	    	  fatalError("identifier expected");
      } //Performs the analysis no matter the flag, only prints results with certain flags
   	 
      token = scanner.nextToken();
      return entry;
   }

   public void parse(String flags){
	   
	  /*
	   * Initializes the flag value 
	   */
	   
	  if (flags.contentEquals("-s"))
		  flag = 2;
	  else if (flags.contentEquals("-r"))
		  flag = 1;
	  else
		  flag = 0;
      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
      if (flag != 0)
    	  table.exitScope(flag);
   }

   /*
   subprogramBody =
         subprogramSpecification "is"
         declarativePart
         "begin" sequenceOfStatements
         "end" [ <procedure>identifier ] ";"
   */
    private void subprogramBody() {
      subprogramSpecification();
      accept(Token.IS, "'is' expected");
      declarativePart();
      accept(Token.BEGIN, "'begin' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      if (flag != 0)
    	  table.exitScope(flag);
      //Prints errors if flag toggled
      
      if (token.code == Token.ID) {
         SymbolEntry entry = findId();
         acceptRole(entry, SymbolEntry.PROC, "must be a procedure name");
      } //Does role analysis on the identifier
      
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
    private void subprogramSpecification() {
    	accept(Token.PROC, "'procedure' expected");
    	SymbolEntry entry = enterId();
    	entry.setRole(SymbolEntry.PROC);
    	table.enterScope();
    	//Whenever a new procedure is called, enter a new scope
    	
    	if (token.code == Token.L_PAR)
    		formalPart();
    }
    
   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
    private void formalPart() {
    	accept(Token.L_PAR, "'(' expected");
    	parameterSpecification();
    	while (token.code == Token.SEMI) {
    		token = scanner.nextToken();
    		parameterSpecification();
    	}
    	accept(Token.R_PAR, "'}' expected");
    }
    
   /*
   parameterSpecification = identifierList ":" mode <type>identifier
   */
    private void parameterSpecification() {
    	SymbolEntry list = identifierList();
    	list.setRole(SymbolEntry.PARAM);
    	accept(Token.COLON, "':' expected");
    	mode();
    	SymbolEntry entry = findId();
    	acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
    }
    
   /*
   mode = [ "in" ] | "in" "out" | "out"
   */
    private void mode() {
    	if(token.code == Token.IN)
    		accept(Token.IN, "'in' expected");
    	if(token.code == Token.OUT)
    		accept(Token.OUT, "'out' expected");
    }
    
   /*
   declarativePart = { basicDeclaration }
   */
   private void declarativePart() {
      while (basicDeclarationHandles.contains(token.code))
         basicDeclaration();
   }

   /*
   basicDeclaration = objectDeclaration | numberDeclaration
                    | typeDeclaration | subprogramBody   
   */
   private void basicDeclaration() {
      switch (token.code){
         case Token.ID:
            numberOrObjectDeclaration();
            break;
         case Token.TYPE:
            typeDeclaration();
            break;
         case Token.PROC:
            subprogramBody();
            break;
         default: fatalError("error in declaration part");
      }
   }

   /*
   objectDeclaration =
         identifierList ":" typeDefinition ";"

   numberDeclaration =
         identifierList ":" "constant" ":=" <static>expression ";"
   */
   private void numberOrObjectDeclaration() {
      SymbolEntry list = identifierList();
      accept(Token.COLON, "':' expected");
      if (token.code == Token.CONST){
         list.setRole(SymbolEntry.CONST);
         token = scanner.nextToken();
         accept(Token.GETS, "':=' expected");
         expression();
      }
      else {
         list.setRole(SymbolEntry.VAR);
         typeDefinition();
      } //Sets the role of each entry in the identifierList
      
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   private void typeDeclaration() {
	   accept(Token.TYPE, "'type' expected");
	   SymbolEntry entry = enterId();
	   entry.setRole(SymbolEntry.TYPE);
	   accept(Token.IS, "'is' expected");
	   typeDefinition();
	   accept(Token.SEMI, "semicolon expected");
   }
   
   /*
   typeDefinition = enumerationTypeDefinition | arrayTypeDefinition
                  | range | <type>identifier
   */
   private void typeDefinition() {
	   switch (token.code) {
	       case Token.L_PAR:
	           enumerationTypeDefinition();
	           break;
	        case Token.ARRAY:
	           arrayTypeDefinition();
	           break;
	        case Token.RANGE:
	           range();
	           break;
	        case Token.ID:
	        	SymbolEntry entry = findId();
	        	acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
	        	break;
	        default: fatalError("error in definition part");
	   }
   }

   /*
   enumerationTypeDefinition = "(" identifierList ")"
   */
   private void enumerationTypeDefinition() {
	   accept(Token.L_PAR, "'(' expected");
	   SymbolEntry list = identifierList();
	   list.setRole(SymbolEntry.CONST);
	   accept(Token.R_PAR, "')' expected");
   }
   
   /*
   arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>identifier
   */
   private void arrayTypeDefinition() {
	   accept(Token.ARRAY, "'array' expected");
	   accept(Token.L_PAR, "'(' expected");
	   index();
	   while(token.code == Token.COMMA) {
		   token = scanner.nextToken();
		   index();
	   }
	   accept(Token.R_PAR, "')' expected");
	   accept(Token.OF, "'of' expected");
	   SymbolEntry entry = findId();
	   acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
   }

   /*
   index = range | <type>identifier
   */
   private void index() {
	   switch(token.code) {
	   		case Token.RANGE:
	   			range();
	   			break;
	   		case Token.ID:
	   			SymbolEntry entry = findId();
	   			acceptRole(entry, SymbolEntry.TYPE, "must be a type name");
	   			break;
	   		default: fatalError("error in index type");
	   }
   }

   /*
   range = "range" simpleExpression ".." simpleExpression
   */
   private void range() {
	   accept(Token.RANGE, "'array' expected");
	   simpleExpression();
	   accept(Token.THRU, "'..' expected");
	   simpleExpression();
   }

   /*
   identifierList = identifier { "," identifier }
   */
   private SymbolEntry identifierList() {
	   SymbolEntry list = enterId();
	   while(token.code == Token.COMMA) {
		   token = scanner.nextToken();
		   list.append(enterId());
	   } //Appends each identifier to the first SymbolEntry
	   
	   return list;
   }

   /*
   sequenceOfStatements = statement { statement }
   */
   private void sequenceOfStatements(){
      statement();
      while (statementHandles.contains(token.code))
         statement();
   }

   /*
   statement = simpleStatement | compoundStatement

   simpleStatement = nullStatement | assignmentStatement
                   | procedureCallStatement | exitStatement

   compoundStatement = ifStatement | loopStatement
   */
   private void statement(){
      switch (token.code){
         case Token.ID:
            assignmentOrCallStatement();
            break;
         case Token.EXIT:
            exitStatement();
            break;
         case Token.IF:
            ifStatement();
            break;
         case Token.NULL:
            nullStatement();
            break;
         case Token.WHILE:
         case Token.LOOP:
            loopStatement();
            break;
         default: fatalError("error in statement");
      }
   }

   /*
   nullStatement = "null" ";"
   */
   private void nullStatement() {
	   accept(Token.NULL, "'null' expected");
	   accept(Token.SEMI, "semicolon expected");
   }

   /*
   loopStatement =
         [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"
   */
   private void loopStatement() {
	   if(token.code == Token.WHILE)
		   iterationScheme();
	   accept(Token.LOOP, "'loop' expected");
	   sequenceOfStatements();
	   accept(Token.END, "'end' expected");
	   accept(Token.LOOP, "'loop' expected");
	   accept(Token.SEMI, "semicolon expected");
   }
   
   /*
   iterationScheme = "while" condition
   */
   private void iterationScheme() {
	   accept(Token.WHILE, "'while' expected");
	   condition();
   }
   
   /*
   ifStatement =
         "if" condition "then" sequenceOfStatements
         { "elsif" condition "then" sequenceOfStatements }
         [ "else" sequenceOfStatements ]
         "end" "if" ";"
   */
   private void ifStatement() {
	   accept(Token.IF, "'if' expected");
	   condition();
	   accept(Token.THEN, "'then' expected");
	   sequenceOfStatements();
	   if (token.code == Token.ELSIF) {
		   token = scanner.nextToken();
		   condition();
		   accept(Token.THEN, "'then' expected");
		   sequenceOfStatements();
	   }
	   if (token.code == Token.ELSE) {
		   token = scanner.nextToken();
		   sequenceOfStatements();
	   }
	   accept(Token.END, "'end' expected");
	   accept(Token.IF, "'if' expected");
	   accept(Token.SEMI, "semicolon expected");
   }

   /*
   exitStatement = "exit" [ "when" condition ] ";"
   */
   private void exitStatement() {
	   accept(Token.EXIT, "'exit' expected");
	   if (token.code == Token.WHEN) {
		   token = scanner.nextToken();
		   condition();
	   }
	   accept(Token.SEMI, "semicolon expected");
   }

   /*
   assignmentStatement = <variable>name ":=" expression ";"

   procedureCallStatement = <procedure>name [ actualParameterPart ] ";"
   */
   private void assignmentOrCallStatement(){
      SymbolEntry entry = name();
      if (token.code == Token.GETS){
         token = scanner.nextToken();
         expression();
      }
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   condition = <boolean>expression
   */
   private void condition(){
      expression();
   }

   /*
   expression = relation { "and" relation } | { "or" relation }
   */
   private void expression(){
      relation();
      if (token.code == Token.AND)
         while (token.code == Token.AND){
            token = scanner.nextToken();
            relation();
         }
      else if (token.code == Token.OR)
         while (token.code == Token.OR){
            token = scanner.nextToken();
            relation();
         }
   }

   /*
   relation = simpleExpression [ relationalOperator simpleExpression ]
   */
   private void relation() {
	   simpleExpression();
	   if (relationalOperator.contains(token.code)) {
		   token = scanner.nextToken();
		   simpleExpression();
	   }	   
   }

   /*
  simpleExpression =
         [ unaryAddingOperator ] term { binaryAddingOperator term }
   */
   private void simpleExpression(){
      if (addingOperator.contains(token.code))
         token = scanner.nextToken();
      term();
      while (addingOperator.contains(token.code)){
         token = scanner.nextToken();
         term();
      }
   }

   /*
   term = factor { multiplyingOperator factor }
   */
   private void term() {
	   factor();
	   while(multiplyingOperator.contains(token.code)) {
		   token = scanner.nextToken();
		   factor();
	   }
   }

   /*
   factor = primary [ "**" primary ] | "not" primary
   */
   private void factor() {
	   if(token.code != Token.NOT) {
		   primary();
		   if(token.code == Token.EXPO)
			   primary();
	   }
	   else {
		   accept(Token.NOT, "'not' expected");
		   primary();
	   }
   }

   /*
   primary = numericLiteral | name | "(" expression ")"
   */
   void primary(){
      switch (token.code){
         case Token.INT:
         case Token.CHAR:
        	token = scanner.nextToken();
        	break;
         case Token.ID:
            SymbolEntry entry = name();
            break;
         case Token.L_PAR:
            token = scanner.nextToken();
            expression();
            accept(Token.R_PAR, "')' expected");
            break;
         default: fatalError("error in primary");
      }
   }

   /*
   name = identifier [ indexedComponent ]
   */
   private SymbolEntry name(){
	  SymbolEntry entry = findId();
      if (token.code == Token.L_PAR) 
         indexedComponent();
      return entry;
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   private void indexedComponent() {
	   accept(Token.L_PAR, "'(' expected");
	   expression();
	   while(token.code == Token.COMMA) {
		   token = scanner.nextToken();
		   expression();
	   }
	   accept(Token.R_PAR, "')' expected");
   }
   
   public static void main(String[] args)
   {
   }
}
