grammar Type;

CT : [A-Z][a-z]+ ;
VT : [a-z]+ ;
WS : [ \t\r\n]+ -> skip ;

baseType : typeClasses ? type ;
type : functionType | compoundType ; // function type
functionType : compoundType '->' type ;
compoundType : constantType | variableType | tupleType | listType | parenType ;

tupleType : '(' type (',' type)+ ')' ; // tuple type, k>=2
listType : '[' type ']' ;              // list type
parenType : '(' type ')' ;             // type with parentheses

constantType : typeConstructor (WS* type)* ;
typeConstructor : CT ;
variableType : VT ;

typeClasses : '(' typeWithClass (',' typeWithClass)* ')' '=>' ;
typeWithClass : typeClass WS* classedType ;
classedType : VT ;
typeClass : CT ;
