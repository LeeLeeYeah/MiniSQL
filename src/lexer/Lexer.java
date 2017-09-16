package lexer;
import java.io.*;  
import java.util.*;  
  
//import symbols.*;  
  
public class Lexer {      
    char peek = ' ';        /* 下一个读入字符 */  
    Hashtable<String, Word> words =   
            new Hashtable<String, Word>();  
    
    BufferedReader reader = null;   
    /* 保存当前是否读取到了文件的结尾  */  
    private Boolean isReaderEnd = false;   
      
    /* 是否读取到文件的结尾 */  
    public Boolean getReaderState() {  
        return this.isReaderEnd;  
    }  

      
    private void reserve(Word w) {  
        words.put(w.lexme, w);  
    }  
      
    /* 
     * 构造函数中将关键字和类型添加到hashtable words中 
     */  
    public Lexer(BufferedReader reader) {  
    	this.reader=reader;
        /* 初始化读取文件变量   
         
        try {  
            reader = new BufferedReader(new FileReader("输入.txt"));  
        }  
        catch(IOException e) {  
            System.out.print(e);  
        }  */
          
          
        /* 关键字 */  
        this.reserve(new Word("create",Tag.CREATE));
        this.reserve(new Word("drop",Tag.DROP));
        this.reserve(new Word("table",Tag.TABLE));
        this.reserve(new Word("index",Tag.INDEX));
        this.reserve(new Word("select",Tag.SELECT));
        this.reserve(new Word("insert", Tag.INSERT));  
        this.reserve(new Word("delete", Tag.DELETE));  
        this.reserve(new Word("quit", Tag.QUIT));  
        this.reserve(new Word("execfile", Tag.EXECFILE));  
        this.reserve(new Word("show",Tag.SHOW));
        this.reserve(new Word("from",Tag.FROM));
        this.reserve(new Word("into",Tag.INTO));
        this.reserve(new Word("where",Tag.WHERE));
        this.reserve(new Word("on",Tag.ON));
        this.reserve(new Word("and",Tag.AND));
        this.reserve(new Word("or",Tag.OR));
        this.reserve(new Word("unique",Tag.UNIQUE));
        this.reserve(new Word("primary",Tag.PRIMARY));
        this.reserve(new Word("key",Tag.KEY));
        this.reserve(new Word("values",Tag.VALUES));
        this.reserve(new Word("order",Tag.ORDER));
        this.reserve(new Word("by",Tag.BY));
        this.reserve(new Word("asc",Tag.ASC));
        this.reserve(new Word("desc",Tag.DESC));
        this.reserve(new Word("join",Tag.JOIN));
        this.reserve(new Word("int", Tag.TYPE));  
        this.reserve(new Word("float", Tag.TYPE)); 
        this.reserve(new Word("char", Tag.TYPE)); 
        /* 类型 */  

    }  
      
    private void readch() throws IOException  {  
        /* 这里应该是使用的是 */  
			peek = (char)reader.read();
        if((int)peek == 0xffff){  
	         this.isReaderEnd = true;  
	     }  
    } 

      
    private Boolean readch(char ch) throws IOException {  
        readch();  
        if (this.peek != ch) {  
            return false;  
        }  
          
        this.peek = ' ';  
        return true;  
    }  
    
    public Token scan() throws IOException {  
        /* 消除空白 */   
        for( ; ; readch() ) {  
            if(peek == ' ' || peek == '\t'||peek=='\r')  
                continue;  
            else if (peek == '\n')   ;
            
            else  
                break;  
        }  
          
        /* 下面开始分割关键字，标识符等信息  */  
        switch (peek) {  
        /* 对于 ==, >=, <=, !=的区分使用状态机实现 */  
        case '=' :  
            if (readch('=')) {  
                return Comparison.eq;   
            }  
            else {  
                return Comparison.eq;  
            }  
        case '>' :  
            if (readch('=')) {   
                return Comparison.ge;  
            }  
            else {  
                return Comparison.gt;  
            }  
        case '<' :  
            if (readch('=')) {   
                return Comparison.le;  
            }  
            else if(this.peek=='>'){
            	this.peek = ' ';  
                 return Comparison.ne;  
            }
            else {  
                return Comparison.lt;  
            }  
        case '!' :  
            if (readch('=')) {    
                return Comparison.ne;  
            }  
            else {  
                return new Token('!');  
            }     
        }  
        
        /* 下面是对数字的识别，根据文法的规定的话，这里的 
         * 数字能够识别整数和小数. 
         */  
        if(Character.isDigit(peek)) {  
            double value = 0;  Num n;
            do {  
            	
                value = 10 * value + Character.digit(peek, 10);  
                readch();  
                
            } while (Character.isDigit(peek));  
            if(peek=='.'){
            	readch();int i=1;
            	do {  
                	
                    value = value + Character.digit(peek, 10)*Math.pow(0.1, i++);  
                    readch();  
                    
                } while (Character.isDigit(peek)); 
            	n = new Num((float)value); 
            }               
            else{
            	n = new Num((int)value);  
            }
             
            //table.put(n, "Num");  
            return n;  
        }  
        /*
         * 对字符串进行识别
         */
        if(peek=='\''){
        	StringBuffer sb = new StringBuffer();
        	sb.append("");
        //	sb.append(peek);
        	readch(); 
        	
        	while (peek!='\''&&peek!=';'){
        		 sb.append(peek);  
                 readch(); 
        	}
        	
        //	sb.append(peek);
        	Token w;
        	if(peek==';') {
        		 w  = new Token(peek);  
        	}
        	else
        	 	 w = new Word(sb.toString(), Tag.STR);  
        	readch();       
            return w; 
        }
        /* 
         * 关键字或者是标识符的识别 
         */  
        if(Character.isLetter(peek)) {  
        	
            StringBuffer sb = new StringBuffer();  
              
            /* 首先得到整个的一个分割 */  
            do {  
                sb.append(peek);  
                readch();  
            } while (Character.isLetterOrDigit(peek)||peek=='_'||peek=='.'||peek=='&');  
              
            /* 判断是关键字还是标识符 */  
            String s = sb.toString();  
            Word w = (Word)words.get(s);  
           
            /* 如果是关键字或者是类型的话，w不应该是空的 */  
            if(w != null) {  
                return w; /* 说明是关键字 或者是类型名 */  
            }  
              
            /* 否则就是一个标识符id */ 
            
            w = new Word(s, Tag.ID);  
            
            return w;  
        }  
          
        /* peek中的任意字符都被认为是词法单元返回 */  
        
        Token tok  = new Token(peek);  
        peek = ' ';  
          
        return tok;  
    }  
}  
