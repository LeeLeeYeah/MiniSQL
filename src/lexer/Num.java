package lexer;

public class Num extends Token{  
    public final double value;  
      
    public Num(float v) {  
        super(Tag.FLOATNUM);  
        this.value = v;  
    }  
    public Num(int v) {  
        super(Tag.INTNUM);  
        this.value = v;  
    }    
    public String toString() {
    	if(this.tag==Tag.INTNUM)
    		return  "" + (int)value;  
    	else return "" + (float)value;
    }  
}  
