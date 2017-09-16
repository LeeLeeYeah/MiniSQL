package lexer;

public class Tag {  
    public final static int  
    	CREATE=201,
    	DROP=202,
    	TABLE=203,
    	INDEX=204,
    	SELECT =205,
    	INSERT =206,
    	DELETE=207,
    	QUIT=208,
    	EXECFILE=209,	
    	SHOW=200,
    	FROM=211,
    	INTO=212,
    	WHERE=213,  
    	OR = 271,
    	ON=214,
        AND= 215,  
        UNIQUE=216,
        PRIMARY=217,
        KEY=218,
        VALUES=219,
	    JOIN=223,
        ORDER=233,
        BY=244,
        ASC=245,		
        DESC=246,		//关键字
        STR=222,		//字符窜
        INTNUM  = 220,	//整数  
        FLOATNUM =221,	//浮点数
        TYPE  = 257,  	//字段类型
        OP=222,			//操作符
        ID= 264;		//表名、索引名或字段名

}  