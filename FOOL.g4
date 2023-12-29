grammar FOOL;

@header {
	import java.util.ArrayList;
	import java.util.HashMap;
	import ast.*;
	import lib.FOOLlib;
	import java.util.HashSet;
}

@parser::members {
	private int nestingLevel = 0;
	private ArrayList<HashMap<String,STentry>> symTable = new ArrayList<>();
	// livello ambiente con dichiarazioni più esterno è 0 (prima posizione ArrayList) invece che 1 (slides)
	// il "fronte" della lista di tabelle è symTable.get(nestingLevel)
	private int classOffset = -2;
	private HashMap<String, HashMap<String,STentry>> classTable = new HashMap<>();
}

@lexer::members {
	int lexicalErrors=0;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

prog returns [Node ast]: 
	{
		// String corrisponde all'ID da salvare nella symTable e STentry sono i suoi valori associati
		HashMap<String,STentry> hm = new HashMap<String,STentry>();
		// lista di mappe (configurazione scelta per la symTable)
   		symTable.add(hm);
   		boolean withClasses = false;
   	}
   	( 	
   		e=exp {$ast = new ProgNode($e.ast);} |
   		LET (c=cllist {withClasses = true;} (d=declist)? | d=declist {withClasses = false;})
   		IN e=exp {
        	if (withClasses) {
        		$ast = new ProgLetInNode($c.classList, $d.astlist, $e.ast);
        	} else {
				$ast = new ProgLetInNode($d.astlist, $e.ast);
        	}
        }      
	) 
	// quando sono qui sono sicuro che il nesting level è 0
	{symTable.remove(nestingLevel);}
    SEMIC 
    ;
    
    
cllist returns [ArrayList<Node> classList]:
	{
		$classList = new ArrayList<Node>();
		boolean withEstension = false;
	}
	(CLASS classId=ID {withEstension = false;} (EXTENDS extended=ID {withEstension = true;})?
		{
			ClassNode classNode = new ClassNode($classId.text);	
			// utilizzato per ottimizzazione					
			HashSet<String> fieldsAndMethods = new HashSet<>();
			// se non si eredita il tipo è un nuovo oggetto ClassTypeNode con una lista vuota in allFields e allMethods
			ClassTypeNode classTypeNode = new ClassTypeNode();	
			// aggiungo nella symbol table il nome della classe mappato ad una nuova STentry e decremento il classOffset
			STentry classEntry = new STentry(nestingLevel, classOffset--);
            if (symTable.get(nestingLevel).put($classId.text, classEntry) != null) {
            	System.out.println("Class id " + $classId.text + " at line " + $classId.line + " already declared");
            	System.exit(0);
            }
			// se si eredita, inizializzo classType con la copia di quello della classe padre
			if (withEstension) {
				// prendo la entry della classe da cui eredito
				STentry superEntry = symTable.get(0).get($extended.text);				
				if (superEntry != null) {
					// copio il tipo della classe da cui eredito (tutto il contenuto)
					classTypeNode = ((ClassTypeNode)superEntry.getType()).copy();
					classNode.setSuperEntry(superEntry);
					FOOLlib.addSupertype($classId.text,$extended.text);
				} else {
					System.out.println("Class id " + $extended.text + " at line " + $extended.line + " does not exist");
					System.exit(0);
				}	
			}
			$classList.add(classNode);
			
            // creo la Virtual Table per la classe corrente
			HashMap<String,STentry> virtualTable;
			// se estendo creo la Virtual Table copiando la Virtual Table della classe da cui si eredita (tutto il contenuto)
            if (withEstension) {
            	virtualTable = new HashMap<>(classTable.get($extended.text));
            } else {
            	// vuota se non eredito
            	virtualTable = new HashMap<>();
            }
            // aggiungo alla Class Table il nome della classe mappato ad una nuova Virtual Table
			classTable.put($classId.text, virtualTable);
			// creo un nuovo livello per la Symbol Table (la nuova virtual table creata)
			symTable.add(virtualTable);
			// entro nei campi della classe (per questo aumento il nestingLevel dello scope)                     
			nestingLevel++;					
		}
		// gestione dei campi della classe
		LPAR {
			// se estendo parto dall'offset dell'ultimo campo della classe padre (primo offset libero in base a lunghezza di allFields), altrimenti da -1
			int fieldOffset = withEstension ? -classTypeNode.getFields().size() - 1  : -1;
		} ((fieldId=ID COLON fieldType=type) {
			// controllo che il campo non sia già stato dichiarato nella classe corrente
			if (!fieldsAndMethods.add($fieldId.text)) {
				System.out.println("Field id " + $fieldId.text + " at line " + $fieldId.line + " already declared in this class");
	            System.exit(0);
			}
			// gestisco il caso del NON Override (ovvero nessuna estensione oppure estensione ma non override dei campi)
			if (!withEstension || !classTable.get($extended.text).containsKey($fieldId.text)) {
				FieldNode fieldNode = new FieldNode($fieldId.text, $fieldType.ast, fieldOffset);
				// aggiungo il campo alla classe
				classNode.addField(fieldNode);
				// aggiornamento ClassTypeNode
				classTypeNode.addField(fieldNode, -fieldOffset - 1);	
				// creo la entry per il campo												
				STentry fieldEntry = new STentry(nestingLevel, $fieldType.ast, fieldOffset--);				
				// aggiungo il campo in Virtual Table
				if (virtualTable.put($fieldId.text, fieldEntry) != null) {
		            System.out.println("Field id " + $fieldId.text + " at line " + $fieldId.line + " already declared");
		            System.exit(0);
		        }		        								
			} 
			// gestisco il caso dell'estensione con Override
			else {
				// prendo la entry del campo della classe da cui estendo
				STentry superEntry = classTable.get($extended.text).get($fieldId.text);
				// non consento l'override di un campo con un metodo
				if (superEntry.getType() instanceof ArrowTypeNode) {
					System.out.println("Can't override a method with field");
					System.exit(0);
				}			
				FieldNode fieldNode = new FieldNode($fieldId.text, $fieldType.ast, superEntry.getOffset());
				// aggiornamento di allFields in ClassTypeNode 
				classTypeNode.replaceField(fieldNode, -superEntry.getOffset() - 1); 
				// sostituisco la nuova STentry alla vecchia preservando l'offset che era nella vecchia STentry
				STentry fieldEntry = new STentry(nestingLevel, $fieldType.ast, superEntry.getOffset());
				// aggiungo il campo in Virtual Table (aggiornamento Virtual Table)
				virtualTable.put($fieldId.text, fieldEntry);
			}
		}
		// Gestisco altri eventuali campi 
		(COMMA fieldId=ID COLON fieldType=type {
			if (!fieldsAndMethods.add($fieldId.text)) {
				System.out.println("Field id " + $fieldId.text + " at line " + $fieldId.line + " already declared in this class");
	            System.exit(0);
			}
			// gestisco il caso del NON Override
			if (!withEstension || !classTable.get($extended.text).containsKey($fieldId.text)){
				FieldNode fieldNode = new FieldNode($fieldId.text, $fieldType.ast, fieldOffset);
				classNode.addField(fieldNode);
				classTypeNode.addField(fieldNode, -fieldOffset - 1);																
				STentry fieldEntry = new STentry(nestingLevel, $fieldType.ast, fieldOffset--);				
				if (virtualTable.put($fieldId.text, fieldEntry) != null) {
		            System.out.println("Field id " + $fieldId.text + " at line " + $fieldId.line + " already declared");
		            System.exit(0);
		        }		        								
			} 
			// gestisco il caso dell'Override
			else {
				STentry superEntry = classTable.get($extended.text).get($fieldId.text);
				if (superEntry.getType() instanceof ArrowTypeNode) {
					System.out.println("Can't override a method with field");
					System.exit(0);
				}			
				FieldNode fieldNode = new FieldNode($fieldId.text, $fieldType.ast, superEntry.getOffset());
				classTypeNode.replaceField(fieldNode, -superEntry.getOffset() - 1); 
				STentry stEntry = new STentry(nestingLevel, $fieldType.ast, superEntry.getOffset());
				virtualTable.put($fieldId.text, stEntry);
		    }
		})*)? RPAR
		// Gestione dei metodi della classe
		CLPAR {
			// se estendo parto dall'offset dell'ultimo campo della classe padre (primo offset libero in base a lunghezza di allMethods), altrimenti da 0
			int methodOffset = withEstension ? classTypeNode.getMethods().size() : 0;
		} (FUN methodId=ID COLON methodType=type {
			int parOffset = 0;
     		MethodNode methodNode = new MethodNode($methodId.text,$methodType.ast);                		
     		STentry methodEntry;
     		// controllo che il metodo non sia già stato dichiarato nella classe corrente
			if(!fieldsAndMethods.add($methodId.text)) {
				System.out.println("Method id " + $methodId.text + " at line " + $methodId.line + " already declared in this class");
		        System.exit(0);
			}
			// gestisco il caso del NON Override (ovvero nessuna estensione oppure estensione ma non override dei metodi)
			if (!withEstension || !classTable.get($extended.text).containsKey($methodId.text)) {
     			methodNode.setOffset(methodOffset);
     			classNode.addMethod(methodNode);
     			classTypeNode.addMethod(methodNode, methodOffset);
     			methodEntry = new STentry(nestingLevel, methodOffset++, true);
     			if (virtualTable.put($methodId.text, methodEntry) != null) { 
     				System.out.println("Method id " + $methodId.text + " at line " + $methodId.line + " already declared");
     				System.exit(0);
     			}
            }
           // gestisco il caso dell'estensione con Override
            else {
           		// prendo la entry del metodo della classe da cui estendo
	    		STentry superEntry = classTable.get($extended.text).get($methodId.text);
	    		// non consento l'override di un campo con un metodo
	    		if (!(superEntry.getType() instanceof ArrowTypeNode)) {
	    			System.out.println("Can't override a field with method");
	    			System.exit(0);
	    		}
	    		methodNode.setOffset(superEntry.getOffset());
	    		// aggiornamento di allMethods in ClassTypeNode 
		    	classTypeNode.replaceMethod(methodNode,superEntry.getOffset());
		    	classNode.addMethod(methodNode);	    	
		    	// creo una nuova STentry con lo stesso offset
		    	methodEntry = new STentry(nestingLevel,superEntry.getOffset(), true);
		    	// aggiungo il metodo in Virtual Table (aggiornamento Virtual Table)
		    	virtualTable.put($methodId.text, methodEntry);
            }
            // entro nello scope dei parametri del metodo, per cui aumento il nestingLevel dello scope
	        nestingLevel++;
	        // e aggiungo quindi un nuovo livello alla symbol table
	        symTable.add(new HashMap<String,STentry>());         
		}
		// dichiarazione dei parametri del metodo
		LPAR {
			ArrayList<Node> params = new ArrayList<Node>();
        } (parId=ID COLON parType=hotype {
        	ParNode parNode = new ParNode($parId.text,$parType.ast);
 			params.add($parType.ast);
 		 	methodNode.addPar(parNode);
 		 	// se la variabile è di tipo funzionale (ArrowTypeNode) l'incremento vale doppio
 		 	if ($parType.ast instanceof ArrowTypeNode) {
 		 		parOffset+=2;
 		 	} else {
 		 		parOffset++;
 		 	}
 		 	if (virtualTable.put($parId.text, new STentry(nestingLevel, $parType.ast, parOffset)) != null) {
 		 		System.out.println("Parameter id " + $parId.text + " at line " + $parId.line + " already declared");
   				System.exit(0);
   			}
 		}
 		// Gestisco altri eventuali parametri del metodo
 		(COMMA parId=ID COLON parType=hotype {
 			ParNode par = new ParNode($parId.text,$parType.ast);
 			params.add($parType.ast);
			methodNode.addPar(par);
			if ($parType.ast instanceof ArrowTypeNode) {
				parOffset+=2;
			} else {
				parOffset++;
			}
			if (virtualTable.put($parId.text, new STentry(nestingLevel, $parType.ast, parOffset)) != null) {
				System.out.println("Parameter id " + $parId.text + " at line " + $parId.line + " already declared");
				System.exit(0);
			}	
		})*
     	)? 
     	{
     		ArrowTypeNode arrowTypeNode = new ArrowTypeNode(params,$methodType.ast);
     		methodNode.setSymType(arrowTypeNode);
     		methodEntry.addType(arrowTypeNode);                 		
     	}
     	RPAR 
     	// Dichiarazione delle variabili dentro al metodo
        (LET {
         	ArrayList<Node> varlist = new ArrayList();
         	int varOffset = -2;
        }
        (VAR varId=ID COLON varType=type ASS e=exp SEMIC {
     		VarNode v = new VarNode($varId.text,$varType.ast,$e.ast);
         	varlist.add(v);
         	HashMap<String,STentry> varHm = symTable.get(nestingLevel);
         	if (varHm.put($varId.text, new STentry(nestingLevel,$varType.ast,varOffset--)) != null) {
         		System.out.println("Var id " + $varId.text + " at line " + $varId.line + " already declared");
				System.exit(0);
			}
        }
        {	
        	methodNode.addDec(varlist);
        })+ IN)? 
     	e=exp {
     		methodNode.addBody($e.ast);	                 	
          	symTable.remove(nestingLevel--);	                      	
        } SEMIC)*                
        CRPAR {
          	classNode.setSymType(classTypeNode);
          	classEntry.addType(classTypeNode);
          	// rimuovo la hashmap corrente poichè esco dallo scope (rimosso livello corrente symbol table)
          	symTable.remove(nestingLevel--);
        }
	)+
; 
        
declist	returns [ArrayList<Node> astlist]: 
	{
		$astlist = new ArrayList<Node>();
		// se sono a nesting level 0 devo continuare ad usare il classOffset, altrimenti riparto da -2
	   	int offset = nestingLevel == 0 ? classOffset : -2;
	}      
	(
		(   // Dichiarazione variabile 
			VAR i=ID COLON h=hotype ASS e=exp { 
				VarNode v = new VarNode($i.text,$h.ast,$e.ast);  
             	$astlist.add(v);
             	// creo una nuova mappa per lo scoping corrente                                 
             	HashMap<String,STentry> hm = symTable.get(nestingLevel);
             	// controllo se la variabile è già stata dichiarata
            	if (hm.put($i.text,new STentry(nestingLevel,$h.ast,offset)) != null) {
             		System.out.println("Var id " + $i.text + " at line " + $i.line + " already declared");
              		System.exit(0);
              	}
              	// se la variabile è di tipo funzionale (ArrowTypeNode) il decremento vale doppio
              	if (v.getSymType() instanceof ArrowTypeNode) {
              		offset-=2;
              	} else {
              		offset--;
              	}  
            } |
            // Dichiarazione funzione   
            FUN i=ID COLON t=type { //inserimento di ID nella symtable
            	FunNode f = new FunNode($i.text,$t.ast);      
	            $astlist.add(f);                              
	            HashMap<String,STentry> hm = symTable.get(nestingLevel);
	            // creo una entry per la funzione
	            STentry entry = new STentry(nestingLevel, offset);
	            // controllo se la funzione è già stata dichiarata
	            if (hm.put($i.text,entry) != null) {
	            	System.out.println("Fun id " + $i.text + " at line " + $i.line + " already declared");
               	   	System.exit(0);
               	}
               	// decremento l'offset di 2 dato che è una funzione
               	offset-=2;
               	// creare una nuova hashmap per la symTable
                nestingLevel++;
                HashMap<String,STentry> hmn = new HashMap<String,STentry>();
                symTable.add(hmn);
            }
			LPAR {
				// Creo una lista di tutti i tipi presenti come parametri della funzione
				ArrayList<Node> parTypes = new ArrayList<Node>();
				// offset dei parametri della funzione
              	int parOffset=0;
            }
			(
				// Function ID e Function Type
				fid=ID COLON fty=hotype {
					parTypes.add($fty.ast);
					//creo nodo ParNode
                  	ParNode fpar = new ParNode($fid.text,$fty.ast); 
                  	// lo attacco al FunNode con addPar
                  	f.addPar(fpar); 
                  	// se il parametro è di tipo ArrowTypeNode deve avere offset 2                         
                  	if ($fty.ast instanceof ArrowTypeNode) {
                  		parOffset+=2;
                  	} else {
                  		parOffset++;
                  	}
                  	// aggiungo dichiarazione a hmn e controllo se il parametro è già stato dichiarato                             
                  	if (hmn.put($fid.text, new STentry(nestingLevel,$fty.ast, parOffset)) != null ) { //aggiungo dich a hmn 
                  		System.out.println("Parameter id " + $fid.text + " at line " + $fid.line + " already declared");
                   		System.exit(0);
                   	}
                }
                (COMMA id=ID COLON ty=hotype {
                	parTypes.add($ty.ast);
                	ParNode par = new ParNode($id.text,$ty.ast);
                	// lo aggiungo al FunNode con addPar
                	f.addPar(par);
                	// se il parametro è di tipo ArrowTypeNode deve avere offset 2   
                	if ($ty.ast instanceof ArrowTypeNode) {
                		parOffset+=2;
                	} else {
          	    		parOffset++;
          	    	}
                	if (hmn.put($id.text, new STentry(nestingLevel,$ty.ast,parOffset)) != null) {
                		System.out.println("Parameter id " + $id.text + " at line " + $id.line + " already declared");
                 		System.exit(0);
                	}
                 })*
          	)? 
            RPAR {           	
            	// Creo il tipo ArrowType della funzione e lo metto nella STentry e nel FunNode
              	ArrowTypeNode type = new ArrowTypeNode(parTypes,$t.ast);
              	f.setSymType(type);
              	entry.addType(type);
            }
            (
            	LET d=declist IN {f.addDec($d.astlist);}
            )? 
            e=exp {
            	f.addBody($e.ast);
               	// rimuovo la hashmap corrente poichè esco dallo scope              
               	symTable.remove(nestingLevel--);    
            }
      	) SEMIC
    )+          
;		


exp	returns [Node ast]: 
	f=term {$ast= $f.ast;} (
		(PLUS l=term {$ast = new PlusNode($ast,$l.ast);}) |
		(MINUS l=term {$ast = new MinusNode($ast,$l.ast);}) | 
		(OR l=term {$ast = new OrNode($ast,$l.ast);}) 
 	)*
;
 	
term returns [Node ast]: 
	f=factor {$ast= $f.ast;} (
		(TIMES l=factor {$ast = new MultNode($ast,	$l.ast);}) |
		(DIV l=factor {$ast = new DivNode($ast,	$l.ast);}) |
		(AND l=factor {$ast = new AndNode($ast, $l.ast);})
	)*
;
	
 	
factor returns [Node ast]: 
	f=value {$ast= $f.ast;} (
		(EQ eq=value {$ast = new EqualNode($ast,$eq.ast);}) | 
		(GE ge=value {$ast = new GreaterEqualNode($ast, $ge.ast);}) |
		(LE le=value {$ast = new LessEqualNode($ast, $le.ast);})
	)*
;	 	
 
value returns [Node ast]: 
	n = INTEGER {$ast = new IntNode(Integer.parseInt($n.text));} | 
		TRUE {$ast = new BoolNode(true);} | 
		FALSE {$ast = new BoolNode(false);} | 
		NULL {$ast= new EmptyNode();} |
		NEW classId=ID {
	 		// controllo che la classe sia in classTable
	 		if (!classTable.containsKey($classId.text)) {
	 			System.out.println("Class " + $classId.text + " at line " + $classId.line + " not declared");
            	System.exit(0);
	 		}
	 	} LPAR {
	 		// salvo i parametri del costruttore (campi) in una lista e considero la possibilità di avere più parametri
	 		ArrayList<Node> fieldList = new ArrayList();
	 	} (field=exp {fieldList.add($field.ast);} (COMMA field=exp {fieldList.add($field.ast);})*)? 
	 	{
	 		// creo il NewNode passando come STentry quella della classe in symTable
	 		$ast = new NewNode($classId.text, symTable.get(0).get($classId.text), fieldList);
	 	} RPAR | 	
		IF x=exp THEN CLPAR y=exp CRPAR ELSE CLPAR z=exp CRPAR {$ast = new IfNode($x.ast,$y.ast,$z.ast);} | 
		NOT LPAR e=exp RPAR	{$ast = new NotNode($e.ast);} | 
		PRINT LPAR e=exp RPAR {$ast = new PrintNode($e.ast);} | 
		LPAR e=exp RPAR {$ast= $e.ast;} | 
	i = ID {//cercare la dichiarazione
		int j=nestingLevel;
        STentry entry = null; 
        while (j>=0 && entry==null) {
        	entry=(symTable.get(j--)).get($i.text);
        } 
        if (entry==null) {
        	System.out.println("Id " + $i.text + " at line " + $i.line + " not declared");
            System.exit(0);
        }               
	   	$ast = new IdNode($i.text,entry,nestingLevel);} 
	   	(
	   		LPAR {ArrayList<Node> arglist = new ArrayList<Node>();}
	   			(a=exp {arglist.add($a.ast);} 
	   	 			(COMMA a=exp {arglist.add($a.ast);})*
	   	 		)? 
	   		RPAR {$ast = new CallNode($i.text,entry,arglist,nestingLevel);} | 
	   		DOT id2=ID 
		   	LPAR {
		   		// salvo i parametri del metodo una lista
	   	 		ArrayList<Node> parlist = new ArrayList<Node>();
	   	 	} (firstPar=exp {parlist.add($firstPar.ast);} (COMMA otherPars=exp {parlist.add($otherPars.ast);})*)? 
	   	 	{
   	 			// ID1 deve essere l'id di un oggetto 
   	 			if (!(entry.getType() instanceof RefTypeNode)) {
	   	 			System.out.println("Method invocation of a non-class id at line " + $i.line);
		            System.exit(0);
	   	 		}
	   	 		// cerco la definizione del metodo tramite la classTable
	   	 		String classId = ((RefTypeNode)entry.getType()).getClassId();
	   	 		STentry methodEntry = classTable.get(classId).get($id2.text);
	   	 		if (methodEntry==null) {
	   	 		    System.out.println("Method " + $id2.text + " at line " + $id2.line + " not declared");
		            System.exit(0);
		        } 
  		 	} 
	   	 	RPAR {
	   	 		$ast= new ClassCallNode($i.text, $id2.text, entry, methodEntry, parlist, nestingLevel);
	   	 	}
	   	)?
;
 			
hotype returns [Node ast]: 
	t=type {$ast = $t.ast;} | 
	a=arrow {$ast = $a.ast;}
;

type returns [Node ast]: 
	INT  {$ast=new IntTypeNode();} | 
	BOOL {$ast=new BoolTypeNode();} | 
	i=ID {$ast = new RefTypeNode($i.text);}
;	
 	  
arrow returns [Node ast]: 
	{ArrayList<Node> parTypes = new ArrayList<Node>();}
	LPAR (
		ht=hotype {parTypes.add($ht.ast);} 
		(COMMA ht=hotype {{parTypes.add($ht.ast);} })*
	)? RPAR ARROW ret=type {$ast = new ArrowTypeNode(parTypes, $ret.ast);}
; 
  		
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

PLUS  	: '+' ;
MINUS   : '-' ;
TIMES   : '*' ;
DIV 	: '/' ;
LPAR	: '(' ;
RPAR	: ')' ;
CLPAR	: '{' ;
CRPAR	: '}' ;
SEMIC 	: ';' ;
COLON   : ':' ; 
COMMA	: ',' ;
DOT	    : '.' ;
OR	    : '||';
AND	    : '&&';
NOT	    : '!' ;
GE	    : '>=' ;
LE	    : '<=' ;
EQ	    : '==' ;	
ASS	    : '=' ;
TRUE	: 'true' ;
FALSE	: 'false' ;
IF	    : 'if' ;
THEN	: 'then';
ELSE	: 'else' ;
PRINT	: 'print' ;
LET     : 'let' ;	
IN      : 'in' ;	
VAR     : 'var' ;
FUN	    : 'fun' ; 
CLASS	: 'class' ; 
EXTENDS : 'extends' ;	
NEW 	: 'new' ;	
NULL    : 'null' ;	  
INT	    : 'int' ;
BOOL	: 'bool' ;
ARROW   : '->' ; 	
INTEGER : '0' | ('-')?(('1'..'9')('0'..'9')*) ; 
ID  	: ('a'..'z'|'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')* ;
WHITESP  : ( '\t' | ' ' | '\r' | '\n' )+    -> channel(HIDDEN) ;
COMMENT : '/*' (.)*? '*/' -> channel(HIDDEN) ;
ERR   	 : . { System.out.println("Invalid char: "+ getText()); lexicalErrors++; } -> channel(HIDDEN);