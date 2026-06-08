"""Parser module for the compiler using PLY.

Provides grammatical rules and parser functions."""

from __future__ import annotations

from typing import Any

import ply.yacc as yacc

from src.ast_nodes import (
    BinOp,
    CallNode,
    ContinueNode,
    DoNode,
    FunctionNode,
    GotoNode,
    IfNode,
    Literal,
    PrintNode,
    ProgramNode,
    ReadNode,
    ReturnNode,
    SubroutineNode,
    UnOp,
    VarDecl,
    Variable,
)
from src.lexer import tokenize
from src.lexer import tokens


precedence = (
    ("left", "EQV", "NEQV"),
    ("left", "OR"),
    ("left", "AND"),
    ("right", "NOT"),
    ("left", "EQ", "NE", "LT", "LE", "GT", "GE"),
    ("left", "CONCAT"),
    ("left", "PLUS", "MINUS"),
    ("left", "ASTERISK", "DIVIDE"),
    ("right", "POWER"), 
)


def p_program_with_main(p):
    """program : main_program subprograms_opt"""
    p[0] = [p[1]] + p[2]


def p_program_subprograms_only(p):
    """program : subprograms"""
    p[0] = p[1]


def p_subprograms_opt(p):
    """subprograms_opt : subprograms
                       | empty"""
    p[0] = p[1] if p[1] is not None else []


def p_subprograms_single(p):
    """subprograms : subprogram"""
    p[0] = [p[1]]


def p_subprograms_multiple(p):
    """subprograms : subprograms subprogram"""
    p[0] = p[1] + [p[2]]


def p_subprogram(p):
    """subprogram : function_subprogram
                  | subroutine_subprogram"""
    p[0] = p[1]


def p_program_header(p):
    """program_header : PROGRAM IDENTIFIER"""
    p[0] = p[2]


def p_main_program(p):
    """main_program : program_header statements END
                    | program_header END"""
    name = p[1]
    body = p[2] if len(p) == 4 else []
    p[0] = ProgramNode(name=name, body=body)


def p_function_header_typed(p):
    """function_header : type FUNCTION IDENTIFIER LPAREN parameters RPAREN"""
    return_type = p[1]
    name = p[3]
    params = p[5]
    p[0] = (name, return_type, params)


def p_function_header_untyped(p):
    """function_header : FUNCTION IDENTIFIER LPAREN parameters RPAREN"""
    name = p[2]
    params = p[4]
    p[0] = (name, None, params)


def p_function_subprogram(p):
    """function_subprogram : function_header statements END
                           | function_header END"""
    name, return_type, params = p[1]
    body = p[2] if len(p) == 4 else []
    p[0] = FunctionNode(name=name, return_type=return_type, params=params, body=body)


def p_subroutine_header_with_params(p):
    """subroutine_header : SUBROUTINE IDENTIFIER LPAREN parameters RPAREN"""
    name = p[2]
    params = p[4]
    p[0] = (name, params)


def p_subroutine_header_without_params(p):
    """subroutine_header : SUBROUTINE IDENTIFIER"""
    name = p[2]
    params: list[str] = []
    p[0] = (name, params)


def p_subroutine_subprogram(p):
    """subroutine_subprogram : subroutine_header statements END
                             | subroutine_header END"""
    name, params = p[1]
    body = p[2] if len(p) == 4 else []
    p[0] = SubroutineNode(name=name, params=params, body=body)


def p_parameters(p):
    """parameters : parameter_list
                  | empty"""
    p[0] = p[1] if p[1] is not None else []


def p_parameter_list_single(p):
    """parameter_list : IDENTIFIER"""
    p[0] = [p[1]]


def p_parameter_list_multiple(p):
    """parameter_list : parameter_list COMMA IDENTIFIER"""
    p[0] = p[1] + [p[3]]


def p_type(p):
    """type : INTEGER
            | REAL
            | LOGICAL"""
    p[0] = p[1].upper() 


def p_statements_single(p):
    """statements : statement"""
    p[0] = [p[1]]


def p_statements_multiple(p):
    """statements : statements statement"""
    p[0] = p[1] + [p[2]]


def p_statement(p):
    """statement : label_statement
                 | unlabeled_statement"""
    p[0] = p[1]


def p_label_statement(p):
    """label_statement : INTEGER_CONST unlabeled_statement"""
    label = int(p[1])
    p[0] = resolve_labeled_statement(label, p[2])


def p_unlabeled_statement(p):
    """unlabeled_statement : declaration
                           | assignment
                           | print_statement
                           | read_statement
                           | do_statement
                           | if_statement
                           | goto_statement
                           | continue_statement
                           | call_statement
                           | return_statement
                           | stop_statement
                           | dimension_statement"""
    p[0] = p[1]


def p_dimension_statement(p):
    """dimension_statement : DIMENSION dimension_list"""
    declarations = []
    var_type = "DIMENSION"
    for var in p[2]:
        declarations.append(VarDecl(name=var['name'], var_type=var_type, dimensions=var.get('dimensions')))
    p[0] = declarations


def p_dimension_list_single(p):
    """dimension_list : array_decl"""
    p[0] = [p[1]]  


def p_dimension_list_multiple(p):
    """dimension_list : dimension_list COMMA array_decl"""
    p[0] = p[1] + [p[3]]


def p_declaration(p):
    """declaration : type var_list"""
    var_type = p[1]
    declarations=[]
    for var in p[2]:
        decl_node = VarDecl(name=var['name'],var_type=var_type,dimensions=var.get('dimensions', []))
        declarations.append(decl_node)
    p[0] =declarations


def p_var_list_single(p):
    """var_list : var_decl"""
    p[0] = [p[1]]


def p_var_list_multiple(p):
    """var_list : var_list COMMA var_decl"""
    p[0] = p[1] + [p[3]]


def p_var_decl_identifier(p):
    """var_decl : IDENTIFIER"""
    p[0] = {"name": p[1], "isArray": False, "dimensions": None}  


def p_var_decl_array(p):
    """var_decl : array_decl"""
    p[0]= p[1]  


def p_array_decl(p):
    """array_decl : IDENTIFIER LPAREN bound_list RPAREN"""
    p[0] = {"name": p[1], "isArray": True, "dimensions": p[3]}


def p_bound_list_single(p):
    """bound_list : bound"""
    p[0] = [p[1]]


def p_bound_list_multiple(p):
    """bound_list : bound_list COMMA bound"""
    p[0] = p[1] + [p[3]]


def p_bound_single(p):
    """bound : expression"""
    p[0] = p[1]  


def p_bound_range(p):
    """bound : expression COLON expression"""
    p[0] = (p[1], p[3])


def p_assignment_scalar(p):
    """assignment : IDENTIFIER EQUALS expression"""
    left=Variable(name=p[1])
    p[0] = BinOp(left=left, op="=", right=p[3])


def p_assignment_array(p):
    """assignment : IDENTIFIER LPAREN expression_list RPAREN EQUALS expression"""
    left ={"node_type": "array_ref", "name": p[1], "indices": p[3]}
    p[0] = BinOp(left=left, op="=", right=p[6])


def p_print_statement(p):
    """print_statement : PRINT ASTERISK COMMA print_list
                       | PRINT ASTERISK"""
    expressions = p[4] if len(p) == 5 else []
    p[0] = PrintNode(expressions=expressions)


def p_print_list_single(p):
    """print_list : expression"""
    p[0] = [p[1]]


def p_print_list_multiple(p):
    """print_list : print_list COMMA expression"""
    p[0] = p[1] + [p[3]]


def p_read_statement(p):
    """read_statement : READ ASTERISK COMMA var_ref_list
                      | READ ASTERISK"""
    variables = p[4] if len(p) == 5 else []
    p[0] = ReadNode(variables=variables)


def p_var_ref_list_single(p):
    """var_ref_list : var_ref"""
    p[0] = [p[1]]


def p_var_ref_list_multiple(p):
    """var_ref_list : var_ref_list COMMA var_ref"""
    p[0] = p[1] + [p[3]]


def p_var_ref_identifier(p):
    """var_ref : IDENTIFIER"""
    p[0] = ("var_ref", p[1])


def p_var_ref_array(p):
    """var_ref : IDENTIFIER LPAREN expression_list RPAREN"""
    p[0] = ("var_ref_array", p[1], p[3])


def p_do_statement_no_step(p):
    """do_statement : DO INTEGER_CONST IDENTIFIER EQUALS expression COMMA expression"""
    label = int(p[2])
    p[0] = DoNode(label=label, var=p[3], start=p[5], end=p[7], step=None, block=[])


def p_do_statement_with_step(p):
    """do_statement : DO INTEGER_CONST IDENTIFIER EQUALS expression COMMA expression COMMA expression"""
    label = int(p[2])
    p[0] = DoNode(label=label, var=p[3], start=p[5], end=p[7], step=p[9], block=[])


def p_if_statement_single_line(p):
    """if_statement : IF LPAREN expression RPAREN unlabeled_statement"""
    p[0] = IfNode(cond=p[3], then_block=[p[5]], else_block=[])


def p_if_statement(p):
    """if_statement : IF LPAREN expression RPAREN THEN statements ENDIF
                    | IF LPAREN expression RPAREN THEN ENDIF
                    | IF LPAREN expression RPAREN THEN statements ELSE statements ENDIF
                    | IF LPAREN expression RPAREN THEN statements ELSE ENDIF
                    | IF LPAREN expression RPAREN THEN ELSE statements ENDIF
                    | IF LPAREN expression RPAREN THEN ELSE ENDIF"""
    cond = p[3]
    then_block: list[Any] = []
    else_block: list[Any] = []

    if len(p) == 8 and p.slice[6].type == "statements":
        then_block = p[6]
    elif len(p) == 10:
        then_block = p[6]
        else_block = p[8]
    elif len(p) == 9 and p.slice[6].type == "statements":
        then_block = p[6]
    elif len(p) == 9 and p.slice[6].type == "ELSE":
        else_block = p[7]

    p[0] = IfNode(cond=cond, then_block=then_block, else_block=else_block)


def p_goto_statement(p):
    """goto_statement : GOTO INTEGER_CONST"""
    label = int(p[2])
    p[0] = GotoNode(label=label)


def p_continue_statement(p):
    """continue_statement : CONTINUE"""
    p[0] = ContinueNode()


def p_call(p):
    """call_statement : CALL IDENTIFIER
                      | CALL IDENTIFIER LPAREN RPAREN
                      | CALL IDENTIFIER LPAREN argument_list RPAREN"""
    name = p[2]
    args: list[Any] = []
    if len(p) == 6:
        args = p[4]
    p[0] = CallNode(subroutine_name=name, args=args)


def p_return(p):
    """return_statement : RETURN"""
    p[0] = ReturnNode()


def p_stop_statement(p):
    """stop_statement : STOP"""
    p[0] = ("stop",)


def p_argument_list_single(p):
    """argument_list : expression"""
    p[0] = [p[1]]


def p_argument_list_multiple(p):
    """argument_list : argument_list COMMA expression"""
    p[0] = p[1] + [p[3]]


def p_expression_list_single(p):
    """expression_list : expression"""
    p[0] = [p[1]]


def p_expression_list_multiple(p):
    """expression_list : expression_list COMMA expression"""
    p[0] = p[1] + [p[3]]


def p_expression_binary(p):
    """expression : expression OR expression
                  | expression AND expression
                  | expression EQV expression
                  | expression NEQV expression
                  | expression EQ expression
                  | expression NE expression
                  | expression LT expression
                  | expression LE expression
                  | expression GT expression
                  | expression GE expression
                  | expression PLUS expression
                  | expression MINUS expression
                  | expression ASTERISK expression
                  | expression DIVIDE expression
                  | expression POWER expression
                  | expression CONCAT expression"""
    p[0] = BinOp(left=p[1], op=str(p[2].upper()), right=p[3])


def p_expression_not(p):
    """expression : NOT expression"""
    p[0] = UnOp(op=str(p[1].upper()), operand=p[2])


def p_expression_unary_plus(p):
    """expression : PLUS expression"""
    p[0] = UnOp(op="+", operand=p[2])


def p_expression_unary_minus(p):
    """expression : MINUS expression"""
    p[0] = UnOp(op="-", operand=p[2])


def p_expression_group(p):
    """expression : LPAREN expression RPAREN"""
    p[0] = p[2]  


def p_expression_identifier(p):
    """expression : IDENTIFIER"""
    p[0] = Variable(name=p[1])  
    
def p_expression_function_call(p):
    """expression : IDENTIFIER LPAREN expression_list RPAREN"""
    p[0] = {"node_type":"function call","name":p[1],"args":p[3]} 
    
def p_expression_literal(p):
    """expression : INTEGER_CONST
                  | REAL_CONST
                  | DOUBLE_PRECISION_CONST
                  | LOGICAL_CONST
                  | CHARACTER_CONST"""
    token_type = p.slice[1].type
    if token_type == "INTEGER_CONST":
        value_type = "INTEGER"
    elif token_type in ("REAL_CONST", "DOUBLE_PRECISION_CONST"):
        value_type = "REAL"
    elif token_type == "LOGICAL_CONST":
        value_type = "LOGICAL"
    else:  
        value_type = "CHARACTER"
    p[0] = Literal(value=p[1], type_name=value_type)


def p_empty(p):
    """empty :"""
    p[0] = None


def p_error(p):
    if p:
        error_msg = f"Syntax error at '{p.value}' (line {p.lineno}, type {p.type})"
        raise Exception(error_msg)
    raise Exception("Syntax error at EOF")


parser = yacc.yacc()


def _find_matching_do_terminator(statements: list[Any], do_index: int) -> int | None:
    """Finds the terminating CONTINUE statement for a DO loop.
    
    Args:
        statements (list[Any]): A list of statement nodes.
        do_index (int): The index of the DO statement.
        
    Returns:
        int | None: The index of the matching terminator, or None."""
    target_do = statements[do_index]
    if not isinstance(target_do, DoNode):
        return None

    target_label = target_do.label
    nested_do_labels: list[int] = []

    for idx in range(do_index + 1, len(statements)):
        stmt = statements[idx]
        if isinstance(stmt, DoNode):
            nested_do_labels.append(stmt.label)
            continue

        if isinstance(stmt, ContinueNode) and stmt.label is not None:
            if nested_do_labels and stmt.label == nested_do_labels[-1]:
                nested_do_labels.pop()
                continue
            if not nested_do_labels and stmt.label == target_label:
                return idx

    return None


def _attach_do_blocks_in_statements(statements: list[Any]) -> list[Any]:
    """Attaches DO block contents iteratively into DO loop nodes.
    
    Args:
        statements (list[Any]): A list of statements.
        
    Returns:
        list[Any]: The restructured list of statements with DO blocks attached."""
    rebuilt: list[Any] = []
    idx = 0

    while idx < len(statements):
        stmt = statements[idx]
        if isinstance(stmt, DoNode):
            match_idx = _find_matching_do_terminator(statements, idx)
            if match_idx is None:
                stmt.block = _attach_do_blocks_in_statements(stmt.block)
                rebuilt.append(stmt)
                idx += 1
                continue

            stmt.block = _attach_do_blocks_in_statements(statements[idx + 1:match_idx])
            rebuilt.append(stmt)
            rebuilt.append(_attach_do_blocks(statements[match_idx]))
            idx = match_idx + 1
            continue

        rebuilt.append(_attach_do_blocks(stmt))
        idx += 1

    return rebuilt


def _attach_do_blocks(node: Any) -> Any:
    """Attaches DO loop blocks recursively through the AST.
    
    Args:
        node (Any): The root node or list of statements to process.
        
    Returns:
        Any: The processed node with blocks properly structured."""
    if isinstance(node, list):
        return _attach_do_blocks_in_statements(node)
    if isinstance(node, ProgramNode):
        node.body = _attach_do_blocks_in_statements(node.body)
        return node
    if isinstance(node, FunctionNode):
        node.body = _attach_do_blocks_in_statements(node.body)
        return node
    if isinstance(node, SubroutineNode):
        node.body = _attach_do_blocks_in_statements(node.body)
        return node
    if isinstance(node, IfNode):
        node.then_block = _attach_do_blocks_in_statements(node.then_block)
        node.else_block = _attach_do_blocks_in_statements(node.else_block)
        return node
    if isinstance(node, DoNode):
        node.block = _attach_do_blocks_in_statements(node.block)
        return node
    return node


def parse(data: str) -> Any:
    """Parses a string of source code into an AST.
    
    Args:
        data (str): The source code string.
        
    Returns:
        Any: The parsed Abstract Syntax Tree."""
    my_lexer = tokenize(data)
    parsed_ast = parser.parse(lexer=my_lexer)
    return _attach_do_blocks(parsed_ast)


def resolve_labeled_statement(label: int, statement: Any) -> Any:
    """Assigns a label to a statement node.
    
    Args:
        label (int): The label value.
        statement (Any): The statement node.
        
    Returns:
        Any: The modified statement node."""
    try:
        statement.stmt_label = label
    except:
        pass
    if hasattr(statement, 'label'):
        statement.label = label
    return statement

