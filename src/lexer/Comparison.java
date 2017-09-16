package lexer;

public class Comparison extends Word{  
	    public Comparison(String s, int tag) {  
	        super(s, tag);  
	    }  
	    public static Comparison parseCompar(Token a){
	    	String aa=a.toString();
	    	return Comparison.parseCompar(aa);
	    }
	    public static Comparison parseCompar(String a){
	    	if(a.equals("<"))return lt;
	    	else if(a.equals(">"))return gt;
	    	else if(a.equals("<="))return le;
	    	else if(a.equals(">="))return ge;
	    	else if(a.equals("=="))return eq;
	    	else if(a.equals("="))return eq;
	    	else if(a.equals("<>"))return ne;
	    	else return null;
	    }
	    public static final Comparison  
	    lt=new Comparison ("<", Tag.OP),
    	gt= new Comparison (">", Tag.OP),
        eq = new Comparison ("==", Tag.OP),  
        ne = new Comparison("<>", Tag.OP),  
        le = new Comparison("<=", Tag.OP),  
        ge = new Comparison(">=", Tag.OP);
	 
}
