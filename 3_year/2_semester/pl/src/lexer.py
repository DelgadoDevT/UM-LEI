"""
Lexical analyzer (lexer) for the Fortran compiler.

This module uses `ply.lex` to break down Fortran source code into tokens.
It handles fixed-form Fortran 77 conventions, including line continuations,
comments, and 72-character line limits.
"""
import ply.lex as lex

class LexError(Exception):
    """
    Exception raised for lexical errors encountered during tokenization.
    """
    pass

reserved = {
    'continue': 'CONTINUE',
    'do': 'DO',
    'else': 'ELSE',
    'end': 'END',
    'endif': 'ENDIF',
    'function': 'FUNCTION',
    'goto': 'GOTO',
    'if': 'IF',
    'integer': 'INTEGER',
    'logical': 'LOGICAL',
    'print': 'PRINT',
    'program': 'PROGRAM',
    'read': 'READ',
    'return': 'RETURN',
    'then': 'THEN',
    
    # Important extras
    'call': 'CALL',
    'dimension': 'DIMENSION',
    'real': 'REAL',
    'stop': 'STOP',
    'subroutine': 'SUBROUTINE'
}

tokens = [
    'IDENTIFIER', 'INTEGER_CONST', 'REAL_CONST', 'DOUBLE_PRECISION_CONST',
    'LOGICAL_CONST', 'CHARACTER_CONST',
    'PLUS', 'MINUS', 'ASTERISK', 'DIVIDE', 'POWER',
    'EQ', 'NE', 'LT', 'LE', 'GT', 'GE',
    'AND', 'OR', 'NOT', 'EQV', 'NEQV',
    'CONCAT',
    'EQUALS', 'COMMA', 'LPAREN', 'RPAREN', 'COLON'
] + list(reserved.values())

def t_COMMENT(t):
    r'(?:^|\n)[Cc][^\n]*|(?:^|\n)[\*!][^\n]*'
    """
    Match and ignore comments in the source code.

    Comments start with 'C', 'c', '*', or '!' in the first column, or after a newline.

    Args:
        t (lex.LexToken): The current token.
    """
    t.lexer.lineno += t.value.count('\n')
    pass

def t_LOGICAL_CONST(t):
    r'\.(?i:TRUE|FALSE)\.'
    """
    Match a logical constant (e.g., .TRUE., .FALSE.).

    Args:
        t (lex.LexToken): The current token.

    Returns:
        lex.LexToken: The unmodified token.
    """
    return t

def t_CHARACTER_CONST(t):
    r"'(?:[^']|'')*'"
    """
    Match a character constant enclosed in single quotes.

    Handles escaped quotes (two consecutive single quotes).

    Args:
        t (lex.LexToken): The current token.

    Returns:
        lex.LexToken: The token with its value stripped of outer quotes and unescaped.
    """
    t.value = t.value[1:-1].replace("''", "'")
    return t

def t_REAL_CONST(t):
    r'(\d+\.\d*|\.\d+)([EeDd][+-]?\d+)?|\d+[EeDd][+-]?\d+'
    """
    Match a real or double precision constant.

    Converts 'D' exponents to 'E' and parses the value as a float.

    Args:
        t (lex.LexToken): The current token.

    Returns:
        lex.LexToken: The token with its value parsed as a float.
    """
    if 'D' in t.value.upper():
        t.type = 'DOUBLE_PRECISION_CONST'
    t.value = float(t.value.upper().replace('D', 'E'))
    return t

def t_INTEGER_CONST(t):
    r'\d+'
    """
    Match an integer constant.

    Args:
        t (lex.LexToken): The current token.

    Returns:
        lex.LexToken: The token with its value parsed as an int.
    """
    t.value = int(t.value)
    return t

def t_IDENTIFIER(t):
    r'[A-Za-z][A-Za-z0-9]*'
    """
    Match an identifier or reserved keyword.

    Checks the token value against the reserved words dictionary. It also emits
    a warning if an identifier exceeds 6 characters.

    Args:
        t (lex.LexToken): The current token.

    Returns:
        lex.LexToken: The token with its type updated if it is a keyword.
    """
    val_lower = t.value.lower()
    t.type = reserved.get(val_lower, 'IDENTIFIER')
    if t.type == 'IDENTIFIER' and len(t.value) > 6:
        print(f"Warning: Identifier '{t.value}' exceeds 6 characters at line {t.lexer.lineno}.")
    return t

t_POWER     = r'\*\*'
t_ASTERISK  = r'\*'
t_CONCAT    = r'//'
t_DIVIDE    = r'/'
t_PLUS      = r'\+'
t_MINUS     = r'-'
t_EQ        = r'\.(?i:EQ)\.'
t_NE        = r'\.(?i:NE)\.'
t_LT        = r'\.(?i:LT)\.'
t_LE        = r'\.(?i:LE)\.'
t_GT        = r'\.(?i:GT)\.'
t_GE        = r'\.(?i:GE)\.'
t_AND       = r'\.(?i:AND)\.'
t_OR        = r'\.(?i:OR)\.'
t_NOT       = r'\.(?i:NOT)\.'
t_EQV       = r'\.(?i:EQV)\.'
t_NEQV      = r'\.(?i:NEQV)\.'
t_EQUALS    = r'='
t_COMMA     = r','
t_LPAREN    = r'\('
t_RPAREN    = r'\)'
t_COLON     = r':'

def t_newline(t):
    r'\n'
    """
    Match a newline character and increment the line number counter.

    Args:
        t (lex.LexToken): The current token.
    """
    t.lexer.lineno += 1

t_ignore = " \t"

def t_error(t):
    """
    Handle illegal characters.

    Args:
        t (lex.LexToken): The current token containing the illegal character.

    Raises:
        LexError: Indicating the invalid symbol and its line number.
    """
    raise LexError(f"Invalid Symbol: {t.value[0]} at line {t.lexer.lineno}")

lexer = lex.lex()

def tokenize(data):
    """
    Pre-process the input data and initialize the lexer.

    Handles Fortran fixed-form line continuation (character in column 6)
    and line truncation at 72 characters.

    Args:
        data (str): The raw source code.

    Returns:
        lex.Lexer: The initialized PLY lexer instance ready to generate tokens.
    """
    lines = data.split('\n')
    processed_lines = []
    last_code_idx = -1
    
    for raw_line in lines:
        line = raw_line[:72]
        
        # Check if line is a comment or blank
        if not line.strip() or (len(line) > 0 and line[0].upper() in ['C', '*', '!']):
            processed_lines.append(line)
            continue
            
        # Check for continuation in column 6 (index 5)
        if len(line) > 5 and line[5] not in [' ', '0'] and last_code_idx != -1:
            processed_lines[last_code_idx] += line[6:]
            processed_lines.append("")
        else:
            processed_lines.append(line)
            last_code_idx = len(processed_lines) - 1

    data_merged = '\n'.join(processed_lines)
    lexer.input(data_merged)
    lexer.lineno = 1
    return lexer

def next_token():
    """
    Advance the global lookahead token.

    Fetches the next token from the lexer and stores it in the global `_lookahead` variable.
    """
    global _lookahead
    _lookahead = lexer.token()

def lookahead():
    """
    Retrieve information about the current lookahead token.

    Returns:
        tuple: A tuple containing (type, value, lineno, lexpos) of the lookahead token.
               If no token is available, returns ("$", None, None, None).
    """
    return (_lookahead.type,_lookahead.value,_lookahead.lineno,_lookahead.lexpos) if _lookahead else ("$",None,None,None)
