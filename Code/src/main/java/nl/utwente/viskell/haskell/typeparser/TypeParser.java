// Generated from nl/utwente/viskell/haskell/typeparser/Type.g4 by ANTLR 4.5
package nl.utwente.viskell.haskell.typeparser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TypeParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, CT=8, VT=9, WS=10, 
		LIST_CT=11, TUPLE_CT=12;
	public static final int
		RULE_type = 0, RULE_innerType = 1, RULE_functionType = 2, RULE_compoundType = 3, 
		RULE_appliedType = 4, RULE_tupleType = 5, RULE_listType = 6, RULE_parenType = 7, 
		RULE_constantType = 8, RULE_typeConstructor = 9, RULE_variableType = 10, 
		RULE_typeClasses = 11, RULE_typeWithClass = 12, RULE_classedType = 13, 
		RULE_typeClass = 14;
	public static final String[] ruleNames = {
		"type", "innerType", "functionType", "compoundType", "appliedType", "tupleType", 
		"listType", "parenType", "constantType", "typeConstructor", "variableType", 
		"typeClasses", "typeWithClass", "classedType", "typeClass"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'=>'", "'->'", "'('", "','", "')'", "'['", "']'", null, null, null, 
		"'[]'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, "CT", "VT", "WS", "LIST_CT", 
		"TUPLE_CT"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Type.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TypeParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class TypeContext extends ParserRuleContext {
		public InnerTypeContext innerType() {
			return getRuleContext(InnerTypeContext.class,0);
		}
		public TypeClassesContext typeClasses() {
			return getRuleContext(TypeClassesContext.class,0);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitType(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(30);
				typeClasses();
				setState(31);
				match(T__0);
				}
				break;
			}
			setState(35);
			innerType();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InnerTypeContext extends ParserRuleContext {
		public FunctionTypeContext functionType() {
			return getRuleContext(FunctionTypeContext.class,0);
		}
		public CompoundTypeContext compoundType() {
			return getRuleContext(CompoundTypeContext.class,0);
		}
		public InnerTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_innerType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterInnerType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitInnerType(this);
		}
	}

	public final InnerTypeContext innerType() throws RecognitionException {
		InnerTypeContext _localctx = new InnerTypeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_innerType);
		try {
			setState(39);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(37);
				functionType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(38);
				compoundType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionTypeContext extends ParserRuleContext {
		public CompoundTypeContext compoundType() {
			return getRuleContext(CompoundTypeContext.class,0);
		}
		public InnerTypeContext innerType() {
			return getRuleContext(InnerTypeContext.class,0);
		}
		public FunctionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterFunctionType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitFunctionType(this);
		}
	}

	public final FunctionTypeContext functionType() throws RecognitionException {
		FunctionTypeContext _localctx = new FunctionTypeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_functionType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			compoundType();
			setState(42);
			match(T__1);
			setState(43);
			innerType();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CompoundTypeContext extends ParserRuleContext {
		public ConstantTypeContext constantType() {
			return getRuleContext(ConstantTypeContext.class,0);
		}
		public VariableTypeContext variableType() {
			return getRuleContext(VariableTypeContext.class,0);
		}
		public AppliedTypeContext appliedType() {
			return getRuleContext(AppliedTypeContext.class,0);
		}
		public TupleTypeContext tupleType() {
			return getRuleContext(TupleTypeContext.class,0);
		}
		public ListTypeContext listType() {
			return getRuleContext(ListTypeContext.class,0);
		}
		public ParenTypeContext parenType() {
			return getRuleContext(ParenTypeContext.class,0);
		}
		public CompoundTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterCompoundType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitCompoundType(this);
		}
	}

	public final CompoundTypeContext compoundType() throws RecognitionException {
		CompoundTypeContext _localctx = new CompoundTypeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_compoundType);
		try {
			setState(51);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(45);
				constantType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				variableType();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(47);
				appliedType();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(48);
				tupleType();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(49);
				listType();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(50);
				parenType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AppliedTypeContext extends ParserRuleContext {
		public VariableTypeContext variableType() {
			return getRuleContext(VariableTypeContext.class,0);
		}
		public List<InnerTypeContext> innerType() {
			return getRuleContexts(InnerTypeContext.class);
		}
		public InnerTypeContext innerType(int i) {
			return getRuleContext(InnerTypeContext.class,i);
		}
		public AppliedTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_appliedType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterAppliedType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitAppliedType(this);
		}
	}

	public final AppliedTypeContext appliedType() throws RecognitionException {
		AppliedTypeContext _localctx = new AppliedTypeContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_appliedType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			variableType();
			setState(55); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(54);
					innerType();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(57); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TupleTypeContext extends ParserRuleContext {
		public List<InnerTypeContext> innerType() {
			return getRuleContexts(InnerTypeContext.class);
		}
		public InnerTypeContext innerType(int i) {
			return getRuleContext(InnerTypeContext.class,i);
		}
		public TupleTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterTupleType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitTupleType(this);
		}
	}

	public final TupleTypeContext tupleType() throws RecognitionException {
		TupleTypeContext _localctx = new TupleTypeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tupleType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			match(T__2);
			setState(60);
			innerType();
			setState(63); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(61);
				match(T__3);
				setState(62);
				innerType();
				}
				}
				setState(65); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__3 );
			setState(67);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListTypeContext extends ParserRuleContext {
		public InnerTypeContext innerType() {
			return getRuleContext(InnerTypeContext.class,0);
		}
		public ListTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterListType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitListType(this);
		}
	}

	public final ListTypeContext listType() throws RecognitionException {
		ListTypeContext _localctx = new ListTypeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_listType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			match(T__5);
			setState(70);
			innerType();
			setState(71);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParenTypeContext extends ParserRuleContext {
		public InnerTypeContext innerType() {
			return getRuleContext(InnerTypeContext.class,0);
		}
		public ParenTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterParenType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitParenType(this);
		}
	}

	public final ParenTypeContext parenType() throws RecognitionException {
		ParenTypeContext _localctx = new ParenTypeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_parenType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			match(T__2);
			setState(74);
			innerType();
			setState(75);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantTypeContext extends ParserRuleContext {
		public TypeConstructorContext typeConstructor() {
			return getRuleContext(TypeConstructorContext.class,0);
		}
		public List<InnerTypeContext> innerType() {
			return getRuleContexts(InnerTypeContext.class);
		}
		public InnerTypeContext innerType(int i) {
			return getRuleContext(InnerTypeContext.class,i);
		}
		public ConstantTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterConstantType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitConstantType(this);
		}
	}

	public final ConstantTypeContext constantType() throws RecognitionException {
		ConstantTypeContext _localctx = new ConstantTypeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_constantType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			typeConstructor();
			setState(81);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(78);
					innerType();
					}
					} 
				}
				setState(83);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeConstructorContext extends ParserRuleContext {
		public TerminalNode CT() { return getToken(TypeParser.CT, 0); }
		public TerminalNode LIST_CT() { return getToken(TypeParser.LIST_CT, 0); }
		public TerminalNode TUPLE_CT() { return getToken(TypeParser.TUPLE_CT, 0); }
		public TypeConstructorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeConstructor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterTypeConstructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitTypeConstructor(this);
		}
	}

	public final TypeConstructorContext typeConstructor() throws RecognitionException {
		TypeConstructorContext _localctx = new TypeConstructorContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_typeConstructor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CT) | (1L << LIST_CT) | (1L << TUPLE_CT))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableTypeContext extends ParserRuleContext {
		public TerminalNode VT() { return getToken(TypeParser.VT, 0); }
		public VariableTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterVariableType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitVariableType(this);
		}
	}

	public final VariableTypeContext variableType() throws RecognitionException {
		VariableTypeContext _localctx = new VariableTypeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_variableType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			match(VT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeClassesContext extends ParserRuleContext {
		public List<TypeWithClassContext> typeWithClass() {
			return getRuleContexts(TypeWithClassContext.class);
		}
		public TypeWithClassContext typeWithClass(int i) {
			return getRuleContext(TypeWithClassContext.class,i);
		}
		public TypeClassesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeClasses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterTypeClasses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitTypeClasses(this);
		}
	}

	public final TypeClassesContext typeClasses() throws RecognitionException {
		TypeClassesContext _localctx = new TypeClassesContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_typeClasses);
		int _la;
		try {
			setState(100);
			switch (_input.LA(1)) {
			case T__2:
				enterOuterAlt(_localctx, 1);
				{
				setState(88);
				match(T__2);
				setState(89);
				typeWithClass();
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(90);
					match(T__3);
					setState(91);
					typeWithClass();
					}
					}
					setState(96);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(97);
				match(T__4);
				}
				break;
			case CT:
				enterOuterAlt(_localctx, 2);
				{
				setState(99);
				typeWithClass();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeWithClassContext extends ParserRuleContext {
		public TypeClassContext typeClass() {
			return getRuleContext(TypeClassContext.class,0);
		}
		public ClassedTypeContext classedType() {
			return getRuleContext(ClassedTypeContext.class,0);
		}
		public TypeWithClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeWithClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterTypeWithClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitTypeWithClass(this);
		}
	}

	public final TypeWithClassContext typeWithClass() throws RecognitionException {
		TypeWithClassContext _localctx = new TypeWithClassContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_typeWithClass);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			typeClass();
			setState(103);
			classedType();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClassedTypeContext extends ParserRuleContext {
		public TerminalNode VT() { return getToken(TypeParser.VT, 0); }
		public ClassedTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classedType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterClassedType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitClassedType(this);
		}
	}

	public final ClassedTypeContext classedType() throws RecognitionException {
		ClassedTypeContext _localctx = new ClassedTypeContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_classedType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(VT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeClassContext extends ParserRuleContext {
		public TerminalNode CT() { return getToken(TypeParser.CT, 0); }
		public TypeClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).enterTypeClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeListener ) ((TypeListener)listener).exitTypeClass(this);
		}
	}

	public final TypeClassContext typeClass() throws RecognitionException {
		TypeClassContext _localctx = new TypeClassContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_typeClass);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			match(CT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\16p\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\3\2\3\2\3\2\5\2$\n\2\3\2"+
		"\3\2\3\3\3\3\5\3*\n\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\5\5\66\n"+
		"\5\3\6\3\6\6\6:\n\6\r\6\16\6;\3\7\3\7\3\7\3\7\6\7B\n\7\r\7\16\7C\3\7\3"+
		"\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\7\nR\n\n\f\n\16\nU\13\n\3\13"+
		"\3\13\3\f\3\f\3\r\3\r\3\r\3\r\7\r_\n\r\f\r\16\rb\13\r\3\r\3\r\3\r\5\r"+
		"g\n\r\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\2\2\21\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36\2\3\4\2\n\n\r\16l\2#\3\2\2\2\4)\3\2\2\2\6+\3\2\2"+
		"\2\b\65\3\2\2\2\n\67\3\2\2\2\f=\3\2\2\2\16G\3\2\2\2\20K\3\2\2\2\22O\3"+
		"\2\2\2\24V\3\2\2\2\26X\3\2\2\2\30f\3\2\2\2\32h\3\2\2\2\34k\3\2\2\2\36"+
		"m\3\2\2\2 !\5\30\r\2!\"\7\3\2\2\"$\3\2\2\2# \3\2\2\2#$\3\2\2\2$%\3\2\2"+
		"\2%&\5\4\3\2&\3\3\2\2\2\'*\5\6\4\2(*\5\b\5\2)\'\3\2\2\2)(\3\2\2\2*\5\3"+
		"\2\2\2+,\5\b\5\2,-\7\4\2\2-.\5\4\3\2.\7\3\2\2\2/\66\5\22\n\2\60\66\5\26"+
		"\f\2\61\66\5\n\6\2\62\66\5\f\7\2\63\66\5\16\b\2\64\66\5\20\t\2\65/\3\2"+
		"\2\2\65\60\3\2\2\2\65\61\3\2\2\2\65\62\3\2\2\2\65\63\3\2\2\2\65\64\3\2"+
		"\2\2\66\t\3\2\2\2\679\5\26\f\28:\5\4\3\298\3\2\2\2:;\3\2\2\2;9\3\2\2\2"+
		";<\3\2\2\2<\13\3\2\2\2=>\7\5\2\2>A\5\4\3\2?@\7\6\2\2@B\5\4\3\2A?\3\2\2"+
		"\2BC\3\2\2\2CA\3\2\2\2CD\3\2\2\2DE\3\2\2\2EF\7\7\2\2F\r\3\2\2\2GH\7\b"+
		"\2\2HI\5\4\3\2IJ\7\t\2\2J\17\3\2\2\2KL\7\5\2\2LM\5\4\3\2MN\7\7\2\2N\21"+
		"\3\2\2\2OS\5\24\13\2PR\5\4\3\2QP\3\2\2\2RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2"+
		"T\23\3\2\2\2US\3\2\2\2VW\t\2\2\2W\25\3\2\2\2XY\7\13\2\2Y\27\3\2\2\2Z["+
		"\7\5\2\2[`\5\32\16\2\\]\7\6\2\2]_\5\32\16\2^\\\3\2\2\2_b\3\2\2\2`^\3\2"+
		"\2\2`a\3\2\2\2ac\3\2\2\2b`\3\2\2\2cd\7\7\2\2dg\3\2\2\2eg\5\32\16\2fZ\3"+
		"\2\2\2fe\3\2\2\2g\31\3\2\2\2hi\5\36\20\2ij\5\34\17\2j\33\3\2\2\2kl\7\13"+
		"\2\2l\35\3\2\2\2mn\7\n\2\2n\37\3\2\2\2\n#)\65;CS`f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}