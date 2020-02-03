/*
You can test Chario, Scanner, or Parser separately by adding and removing comments
in the last few lines of the TerminalApp constructor.
*/

import java.io.*;

public class TerminalApp{

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;

   public TerminalApp(String[] filename){

      FileInputStream stream;
      try{
         stream = new FileInputStream(filename[0]);
      }catch(IOException e){
         System.out.println("Error opening file.");
         return;
      }      
      chario = new Chario(stream);
      scanner = new Scanner(chario);
      parser = new Parser(chario, scanner);
      if (filename.length == 1)
    	  testParser("-a");
      else
    	  testParser(filename[1]);
   }

   private void testChario(){
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(String flag){
      try{
         parser.parse(flag);
      }
      catch(Exception e){}
      chario.reportErrors();
   }

   public static void main(String args[]){
	  if (args.length < 1)
		  System.out.println("Error opening file.");
	  else
		  new TerminalApp(args);
   }
}