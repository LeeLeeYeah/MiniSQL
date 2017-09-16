package lexer;

public class Word extends Token {  
    public String lexme = "";  
      
    public Word (String s, int t) {  
        super(t);  
        this.lexme = s;  
    }  
      
    public String toString() {  
        return this.lexme;  
    }  
      
  //  public static final Word   
   //     and = new Comparison("&&", Tag.AND),  
    //    or = new Comparison("||", Tag.OR),
    //	lt=new Word ("<", Tag.OP),
    //	gt= new Word (">", Tag.OP),
     //   eq = new Word ("==", Tag.OP),  
    //    ne = new Word("!=", Tag.OP),  
     //   le = new Word("<=", Tag.OP),  
     //   ge = new Word(">=", Tag.OP);
     //   minus = new Comparison("minus", Tag.MINUS),  
     //   True = new Comparison("true", Tag.TRUE),  
      //  False = new Comparison("false", Tag.FALSE);  
     //temp = new Comparison("t", Tag.TEMP);  
}  
