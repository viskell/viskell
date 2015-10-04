grammar Type;

CT : [A-Z][A-Za-z]+ ;
VT : [a-z]+ ;
WS : [ \t\r\n]+ -> skip ;

LIST_CT : '[]';
TUPLE_CT : '(,)';

type : (typeClasses '=>')? innerType ;
innerType : functionType | compoundType ; // function type
functionType : compoundType '->' innerType ;
compoundType : constantType | variableType | appliedType | tupleType | listType | parenType ;

appliedType : variableType (innerType)* ;
tupleType : '(' innerType (',' innerType)+ ')' ; // tuple type, k>=2
listType : '[' innerType ']' ;              // list type
parenType : '(' innerType ')' ;             // type with parentheses

constantType : typeConstructor (innerType)* ;
typeConstructor : CT | LIST_CT | TUPLE_CT ;
variableType : VT ;

typeClasses : '(' typeWithClass (',' typeWithClass)* ')' | typeWithClass ;
typeWithClass : typeClass classedType ;
classedType : VT ;
typeClass : CT ;
