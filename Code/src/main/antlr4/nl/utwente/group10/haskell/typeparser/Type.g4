grammar Type;

CT : [A-Z][a-z]+ ;
VT : [a-z]+ ;
WS : [ \t\r\n]+ -> skip ;

type : functionType | compoundType; // function type
functionType : compoundType ('->' type)+ ;
compoundType : concreteType | tupleType | listType | parenType ;

tupleType : '(' type (',' type)+ ')' ; // tuple type, k>=2
listType : '[' type ']' ;              // list type
parenType : '(' type ')' ;             // parenthesised constructor

concreteType : constantType | variableType ;

constantType : CT ;
variableType : VT ;
