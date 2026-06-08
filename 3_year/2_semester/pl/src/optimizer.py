"""Optimizer module for the compiler's Abstract Syntax Tree.

Provides functions and the ASTOptimizer class to optimize an AST."""

from __future__ import annotations

from copy import deepcopy
from typing import Any

from src.ast_nodes import (
    BinOp,
    CallNode,
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


def optimize_ast(ast_tree: Any) -> tuple[Any, dict[str, Any]]:
    """Public optimizer entrypoint."""
    optimizer = ASTOptimizer()
    optimized = optimizer.optimize(ast_tree)
    return optimized, optimizer.report


def optimize(ast_tree: Any) -> tuple[Any, dict[str, Any]]:
    """Backward-compatible alias for optimize_ast."""
    return optimize_ast(ast_tree)


class ASTOptimizer:
    """Optimizes an AST through constant folding, algebraic simplification, dead code elimination, and jump optimization.

    Attributes:
        max_iterations (int): The maximum number of optimization iterations to run.
        report (dict): A dictionary reporting the optimization passes and metrics."""
    def __init__(self, max_iterations: int = 8) -> None:
        """Initializes the AST optimizer.
        
        Args:
            max_iterations (int): Maximum optimization passes to run."""
        self.max_iterations = max_iterations
        self.report: dict[str, Any] = {
            "entrypoint": "optimize_ast",
            "iteration_cap": self.max_iterations,
            "iterations": 0,
            "reached_iteration_cap": False,
            "passes": {
                "expr_simplification": {
                    "constant_folds": 0,
                    "algebraic_simplifications": 0,
                },
                "dead_code": {"constant_if_pruned": 0, "unreachable_pruned": 0},
                "jump_optimization": {
                    "goto_chain_collapses": 0,
                    "fallthrough_gotos_removed": 0,
                },
            },
            "total_changes": 0,
        }

    def optimize(self, ast_tree: Any) -> Any:
        """Performs optimization on a deep copy of the given AST tree.
        
        Args:
            ast_tree (Any): The AST to optimize.
            
        Returns:
            Any: The optimized AST."""
        tree = deepcopy(ast_tree)
        optimized = self._optimize_root(tree)
        self.report["total_changes"] = self._total_changes()
        return optimized

    def _optimize_root(self, node: Any) -> Any:
        """Optimizes the root node of an AST structure.
        
        Args:
            node (Any): The root AST node.
            
        Returns:
            Any: The optimized root node."""
        if node is None:
            return None
        if isinstance(node, list):
            return [self._optimize_root(item) for item in node]
        if isinstance(node, ProgramNode):
            node.body = self._optimize_scope_body(node.body)
            return node
        if isinstance(node, FunctionNode):
            node.body = self._optimize_scope_body(node.body)
            return node
        if isinstance(node, SubroutineNode):
            node.body = self._optimize_scope_body(node.body)
            return node
        return node

    def _optimize_scope_body(self, body: Any) -> list[Any]:
        """Optimizes a block of statements iteratively until stable or limit is reached.
        
        Args:
            body (Any): The list of statements to optimize.
            
        Returns:
            list[Any]: The optimized list of statements."""
        statements = _flatten_statements(body)
        optimized = statements

        for iteration in range(1, self.max_iterations + 1):
            before = _snapshot(optimized)
            optimized = self._optimize_statement_list(optimized)
            optimized = self._jump_optimize_scope(optimized)
            self.report["iterations"] = max(self.report["iterations"], iteration)

            if _snapshot(optimized) == before:
                return optimized

        self.report["reached_iteration_cap"] = True
        return optimized

    def _optimize_statement_list(self, statements: list[Any]) -> list[Any]:
        """Optimizes a sequence of statements and prunes unreachable code.
        
        Args:
            statements (list[Any]): A list of statements.
            
        Returns:
            list[Any]: The optimized and flattened list of statements."""
        flat = _flatten_statements(statements)
        optimized: list[Any] = []

        for idx, stmt in enumerate(flat):
            result_stmt = self._optimize_statement(stmt)
            if result_stmt is None:
                continue

            if isinstance(result_stmt, list):
                optimized.extend(result_stmt)
            else:
                optimized.append(result_stmt)

            if optimized and _is_terminator(optimized[-1]):
                removed = len(flat) - idx - 1
                if removed > 0:
                    self._inc("dead_code", "unreachable_pruned", removed)
                break

        return optimized

    def _optimize_statement(self, stmt: Any) -> Any:
        """Applies optimizations to a single AST statement.
        
        Args:
            stmt (Any): The statement to optimize.
            
        Returns:
            Any: The optimized statement, or None if pruned."""
        if stmt is None:
            return None

        if isinstance(stmt, list):
            return self._optimize_statement_list(stmt)

        if isinstance(stmt, BinOp) and str(stmt.op) == "=":
            stmt.left = self._optimize_assignment_target(stmt.left)
            stmt.right = self._simplify_expr(stmt.right)
            return stmt

        if isinstance(stmt, IfNode):
            stmt.cond = self._simplify_expr(stmt.cond)
            then_block = self._optimize_statement_list(_flatten_statements(stmt.then_block))
            else_block = self._optimize_statement_list(_flatten_statements(stmt.else_block))

            cond_value = _logical_literal_value(stmt.cond)
            if cond_value is not None:
                self._inc("dead_code", "constant_if_pruned", 1)
                return then_block if cond_value else else_block

            stmt.then_block = then_block
            stmt.else_block = else_block
            return stmt

        if isinstance(stmt, DoNode):
            stmt.start = self._simplify_expr(stmt.start)
            stmt.end = self._simplify_expr(stmt.end)
            if stmt.step is not None:
                stmt.step = self._simplify_expr(stmt.step)
            stmt.block = self._optimize_statement_list(_flatten_statements(stmt.block))
            return stmt

        if isinstance(stmt, PrintNode):
            stmt.expressions = [self._simplify_expr(expr) for expr in stmt.expressions]
            return stmt

        if isinstance(stmt, ReadNode):
            stmt.variables = [self._optimize_read_ref(var) for var in stmt.variables]
            return stmt

        if isinstance(stmt, CallNode):
            stmt.args = [self._simplify_expr(arg) for arg in stmt.args]
            return stmt

        if isinstance(stmt, VarDecl):
            if stmt.dimensions:
                stmt.dimensions = [self._optimize_dimension(dim) for dim in stmt.dimensions]
            return stmt

        return stmt

    def _optimize_assignment_target(self, target: Any) -> Any:
        """Optimizes the target expression of an assignment.
        
        Args:
            target (Any): The assignment target.
            
        Returns:
            Any: The optimized target."""
        if isinstance(target, dict) and target.get("node_type") == "array_ref":
            target["indices"] = [self._simplify_expr(idx) for idx in target.get("indices", [])]
        elif isinstance(target, tuple) and target and target[0] == "var_ref_array":
            indices = [self._simplify_expr(idx) for idx in target[2]]
            target = ("var_ref_array", target[1], indices)
        return target

    def _optimize_dimension(self, dim: Any) -> Any:
        """Optimizes an array dimension declaration.
        
        Args:
            dim (Any): The dimension to optimize.
            
        Returns:
            Any: The optimized dimension."""
        if isinstance(dim, tuple) and len(dim) == 2:
            return (self._simplify_expr(dim[0]), self._simplify_expr(dim[1]))
        return self._simplify_expr(dim)

    def _optimize_read_ref(self, var: Any) -> Any:
        """Optimizes variables targeted by read operations.
        
        Args:
            var (Any): The variable reference.
            
        Returns:
            Any: The optimized variable reference."""
        if isinstance(var, tuple) and var:
            if var[0] == "var_ref_array":
                indices = [self._simplify_expr(idx) for idx in var[2]]
                return ("var_ref_array", var[1], indices)
            return var

        if isinstance(var, dict) and var.get("node_type") == "array_ref":
            var["indices"] = [self._simplify_expr(idx) for idx in var.get("indices", [])]
            return var
        return var

    def _simplify_expr(self, expr: Any) -> Any:
        """Simplifies an expression recursively.
        
        Args:
            expr (Any): The expression to simplify.
            
        Returns:
            Any: The simplified expression."""
        if expr is None:
            return None

        if isinstance(expr, (Literal, Variable)):
            return expr

        if isinstance(expr, UnOp):
            operand = self._simplify_expr(_get_unary_operand(expr))
            _set_unary_operand(expr, operand)
            folded = _fold_unary(_normalize_operator(expr.op), operand)
            if folded is not None:
                self._inc("expr_simplification", "constant_folds", 1)
                return folded
            return expr

        if isinstance(expr, BinOp):
            expr.left = self._simplify_expr(expr.left)
            expr.right = self._simplify_expr(expr.right)

            op = _normalize_operator(expr.op)
            folded = _fold_binary(op, expr.left, expr.right)
            if folded is not None:
                self._inc("expr_simplification", "constant_folds", 1)
                return folded

            simplified = _algebraic_simplify(op, expr.left, expr.right)
            if simplified is not None:
                self._inc("expr_simplification", "algebraic_simplifications", 1)
                return self._simplify_expr(simplified)

            return expr

        if isinstance(expr, dict):
            node_type = expr.get("node_type")
            if node_type == "array_ref":
                expr["indices"] = [self._simplify_expr(idx) for idx in expr.get("indices", [])]
                return expr
            if node_type == "function call":
                expr["args"] = [self._simplify_expr(arg) for arg in expr.get("args", [])]
                return expr
            return expr

        if isinstance(expr, tuple) and expr:
            if expr[0] == "var_ref_array":
                return (
                    "var_ref_array",
                    expr[1],
                    [self._simplify_expr(idx) for idx in expr[2]],
                )
            return expr

        if isinstance(expr, list):
            return [self._simplify_expr(item) for item in expr]

        return expr

    def _jump_optimize_scope(self, statements: list[Any]) -> list[Any]:
        """Performs jump optimization on a statement block, respecting DO loop bounds.
        
        Args:
            statements (list[Any]): The statements to optimize.
            
        Returns:
            list[Any]: Statements with optimized jumps."""
        protected_labels = self._collect_do_labels(statements)
        return self._jump_optimize_statement_list(statements, protected_labels)

    def _jump_optimize_statement_list(
        self,
        statements: list[Any],
        protected_labels: set[int],
    ) -> list[Any]:
        """Collapses GOTO chains and removes fallthrough jumps.
        
        Args:
            statements (list[Any]): The list of statements.
            protected_labels (set[int]): Set of labels belonging to DO loops that should not be targeted by optimizations.
            
        Returns:
            list[Any]: The optimized statement list."""
        for stmt in statements:
            if isinstance(stmt, IfNode):
                stmt.then_block = self._jump_optimize_statement_list(stmt.then_block, protected_labels)
                stmt.else_block = self._jump_optimize_statement_list(stmt.else_block, protected_labels)
            elif isinstance(stmt, DoNode):
                stmt.block = self._jump_optimize_statement_list(stmt.block, protected_labels)

        optimized = list(statements)
        changed = True
        while changed:
            changed = False

            label_to_stmt: dict[int, Any] = {}
            for stmt in optimized:
                label = _get_statement_label(stmt)
                if label is not None:
                    label_to_stmt[label] = stmt

            for stmt in optimized:
                if not isinstance(stmt, GotoNode):
                    continue
                target = _get_goto_target(stmt)
                if target is None or target in protected_labels:
                    continue
                resolved = self._resolve_goto_chain(target, label_to_stmt, protected_labels)
                if resolved != target:
                    _set_goto_target(stmt, resolved)
                    self._inc("jump_optimization", "goto_chain_collapses", 1)
                    changed = True

            new_statements: list[Any] = []
            for idx, stmt in enumerate(optimized):
                if isinstance(stmt, GotoNode) and _get_statement_label(stmt) is None:
                    target = _get_goto_target(stmt)
                    next_label = _next_labeled_statement(optimized, idx)
                    if target is not None and target not in protected_labels and target == next_label:
                        self._inc("jump_optimization", "fallthrough_gotos_removed", 1)
                        changed = True
                        continue
                new_statements.append(stmt)
            optimized = new_statements

        return optimized

    def _resolve_goto_chain(
        self,
        target: int,
        label_to_stmt: dict[int, Any],
        protected_labels: set[int],
    ) -> int:
        """Resolves a chain of GOTOs to its ultimate destination.
        
        Args:
            target (int): The initial target label.
            label_to_stmt (dict[int, Any]): Mapping of labels to statements.
            protected_labels (set[int]): Labels excluded from collapsing.
            
        Returns:
            int: The resolved target label."""
        current = target
        visited: set[int] = set()
        while True:
            if current in visited or current in protected_labels:
                return current
            visited.add(current)
            target_stmt = label_to_stmt.get(current)
            if not isinstance(target_stmt, GotoNode):
                return current
            next_target = _get_goto_target(target_stmt)
            if next_target is None or next_target in visited:
                return current
            current = next_target

    def _collect_do_labels(self, statements: list[Any]) -> set[int]:
        """Collects all labels associated with DO loops recursively.
        
        Args:
            statements (list[Any]): The statement list to scan.
            
        Returns:
            set[int]: A set of DO loop labels."""
        labels: set[int] = set()
        for stmt in statements:
            if isinstance(stmt, DoNode):
                if isinstance(stmt.label, int):
                    labels.add(stmt.label)
                labels.update(self._collect_do_labels(stmt.block))
            elif isinstance(stmt, IfNode):
                labels.update(self._collect_do_labels(stmt.then_block))
                labels.update(self._collect_do_labels(stmt.else_block))
        return labels

    def _inc(self, pass_name: str, metric_name: str, amount: int) -> None:
        """Increments a specific optimization metric in the report.
        
        Args:
            pass_name (str): The optimization pass name.
            metric_name (str): The metric name.
            amount (int): Amount to increment by."""
        self.report["passes"][pass_name][metric_name] += amount

    def _total_changes(self) -> int:
        """Calculates total changes across all optimization passes.
        
        Returns:
            int: Total number of optimizations applied."""
        total = 0
        for pass_info in self.report["passes"].values():
            total += sum(pass_info.values())
        return total


def _flatten_statements(nodes: Any) -> list[Any]:
    """Flattens nested lists of statements into a single flat list.
        
        Args:
            nodes (Any): A nested list or node structure.
            
        Returns:
            list[Any]: The flattened list."""
    result: list[Any] = []

    def _visit(item: Any) -> None:
        if item is None:
            return
        if isinstance(item, list):
            for sub in item:
                _visit(sub)
            return
        result.append(item)

    _visit(nodes)
    return result


def _snapshot(node: Any) -> Any:
    """Creates an immutable snapshot of an AST structure to check for convergence.
        
        Args:
            node (Any): The structure to snapshot.
            
        Returns:
            Any: The immutable representation."""
    if node is None or isinstance(node, (str, int, float, bool)):
        return node
    if isinstance(node, list):
        return ("list", tuple(_snapshot(item) for item in node))
    if isinstance(node, tuple):
        return ("tuple", tuple(_snapshot(item) for item in node))
    if isinstance(node, dict):
        return ("dict", tuple((key, _snapshot(value)) for key, value in sorted(node.items())))
    if hasattr(node, "__dict__"):
        values = tuple((key, _snapshot(value)) for key, value in sorted(node.__dict__.items()))
        return (node.__class__.__name__, values)
    return repr(node)


def _normalize_operator(op: Any) -> str:
    """Normalizes operator strings for comparison and matching.
        
        Args:
            op (Any): The operator token.
            
        Returns:
            str: The normalized uppercase operator string."""
    operator = str(op).strip().upper()
    if operator in {"+", "-", "*", "/", "**", "//", "="}:
        return operator
    if operator.startswith(".") and operator.endswith("."):
        operator = operator[1:-1]
    return operator


def _logical_literal_value(expr: Any) -> bool | None:
    """Extracts the boolean value from a logical literal if applicable.
        
        Args:
            expr (Any): The expression.
            
        Returns:
            bool | None: The boolean value or None."""
    if not isinstance(expr, Literal) or expr.type_name != "LOGICAL":
        return None
    if isinstance(expr.value, bool):
        return expr.value
    if isinstance(expr.value, str):
        token = expr.value.strip().upper()
        if token in {".TRUE.", "TRUE"}:
            return True
        if token in {".FALSE.", "FALSE"}:
            return False
    return None


def _numeric_literal_value(expr: Any) -> float | int | None:
    """Extracts the numeric value from a numeric literal.
        
        Args:
            expr (Any): The expression.
            
        Returns:
            float | int | None: The numeric value or None."""
    if not isinstance(expr, Literal):
        return None
    if expr.type_name == "INTEGER":
        try:
            return int(expr.value)
        except Exception:
            return None
    if expr.type_name == "REAL":
        try:
            return float(expr.value)
        except Exception:
            return None
    return None


def _literal_compare_value(expr: Literal) -> Any:
    """Extracts a literal value suitable for comparison.
        
        Args:
            expr (Literal): The literal expression.
            
        Returns:
            Any: The literal's raw Python value."""
    if expr.type_name == "LOGICAL":
        return _logical_literal_value(expr)
    if expr.type_name in {"INTEGER", "REAL"}:
        return _numeric_literal_value(expr)
    return expr.value


def _make_logical_literal(value: bool) -> Literal:
    """Creates a logical literal node from a boolean.
        
        Args:
            value (bool): The boolean value.
            
        Returns:
            Literal: A LOGICAL literal node."""
    return Literal(value=".TRUE." if value else ".FALSE.", type_name="LOGICAL")


def _make_numeric_literal(value: Any, type_name: str) -> Literal:
    """Creates a numeric literal node.
        
        Args:
            value (Any): The numeric value.
            type_name (str): The numeric type (INTEGER or REAL).
            
        Returns:
            Literal: A numeric literal node."""
    if type_name == "INTEGER":
        return Literal(value=int(value), type_name="INTEGER")
    return Literal(value=float(value), type_name="REAL")


def _fold_unary(op: str, operand: Any) -> Literal | None:
    """Folds unary operations involving literals.
        
        Args:
            op (str): The unary operator.
            operand (Any): The literal operand.
            
        Returns:
            Literal | None: The folded literal, or None."""
    if not isinstance(operand, Literal):
        return None
    if op in {"+", "-"}:
        number = _numeric_literal_value(operand)
        if number is None:
            return None
        return _make_numeric_literal(+number if op == "+" else -number, operand.type_name)
    if op == "NOT":
        value = _logical_literal_value(operand)
        if value is None:
            return None
        return _make_logical_literal(not value)
    return None


def _fold_binary(op: str, left: Any, right: Any) -> Literal | None:
    """Folds binary operations involving literals.
        
        Args:
            op (str): The binary operator.
            left (Any): The left literal operand.
            right (Any): The right literal operand.
            
        Returns:
            Literal | None: The folded literal, or None."""
    if not isinstance(left, Literal) or not isinstance(right, Literal):
        return None

    if op in {"+", "-", "*", "/", "**"}:
        lv = _numeric_literal_value(left)
        rv = _numeric_literal_value(right)
        if lv is None or rv is None:
            return None

        prefer_real = left.type_name == "REAL" or right.type_name == "REAL"
        try:
            if op == "+":
                result = lv + rv
            elif op == "-":
                result = lv - rv
            elif op == "*":
                result = lv * rv
            elif op == "/":
                if rv == 0:
                    return None
                if prefer_real:
                    return _make_numeric_literal(lv / rv, "REAL")
                return _make_numeric_literal(int(lv / rv), "INTEGER")
            else:
                result = lv**rv
        except Exception:
            return None

        if prefer_real or (isinstance(result, float) and not float(result).is_integer()):
            return _make_numeric_literal(result, "REAL")
        return _make_numeric_literal(result, "INTEGER")

    if op == "//" and left.type_name == "CHARACTER" and right.type_name == "CHARACTER":
        return Literal(value=f"{left.value}{right.value}", type_name="CHARACTER")

    if op in {"AND", "OR", "EQV", "NEQV"}:
        lv = _logical_literal_value(left)
        rv = _logical_literal_value(right)
        if lv is None or rv is None:
            return None
        if op == "AND":
            return _make_logical_literal(lv and rv)
        if op == "OR":
            return _make_logical_literal(lv or rv)
        if op == "EQV":
            return _make_logical_literal(lv == rv)
        return _make_logical_literal(lv != rv)

    if op in {"EQ", "NE", "LT", "LE", "GT", "GE"}:
        lv = _literal_compare_value(left)
        rv = _literal_compare_value(right)
        if lv is None or rv is None:
            return None
        if op == "EQ":
            return _make_logical_literal(lv == rv)
        if op == "NE":
            return _make_logical_literal(lv != rv)
        if op == "LT":
            return _make_logical_literal(lv < rv)
        if op == "LE":
            return _make_logical_literal(lv <= rv)
        if op == "GT":
            return _make_logical_literal(lv > rv)
        return _make_logical_literal(lv >= rv)

    return None


def _is_zero(expr: Any) -> bool:
    """Checks if an expression evaluates to literal zero.
        
        Args:
            expr (Any): The expression.
            
        Returns:
            bool: True if zero."""
    value = _numeric_literal_value(expr)
    return value == 0 if value is not None else False


def _is_one(expr: Any) -> bool:
    """Checks if an expression evaluates to literal one.
        
        Args:
            expr (Any): The expression.
            
        Returns:
            bool: True if one."""
    value = _numeric_literal_value(expr)
    return value == 1 if value is not None else False


def _logical_is(expr: Any, value: bool) -> bool:
    """Checks if an expression evaluates to a specific boolean value.
        
        Args:
            expr (Any): The expression.
            value (bool): The boolean target value.
            
        Returns:
            bool: True if it matches the boolean."""
    literal = _logical_literal_value(expr)
    return literal is value


def _logical_false() -> Literal:
    """Returns a FALSE logical literal.
        
        Returns:
            Literal: A .FALSE. literal."""
    return _make_logical_literal(False)


def _logical_true() -> Literal:
    """Returns a TRUE logical literal.
        
        Returns:
            Literal: A .TRUE. literal."""
    return _make_logical_literal(True)


def _is_real_operand(left: Any, right: Any) -> bool:
    """Checks if either operand is of REAL type.
        
        Args:
            left (Any): The left operand.
            right (Any): The right operand.
            
        Returns:
            bool: True if either operand is REAL."""
    if isinstance(left, Literal) and left.type_name == "REAL":
        return True
    if isinstance(right, Literal) and right.type_name == "REAL":
        return True
    return False


def _algebraic_simplify(op: str, left: Any, right: Any) -> Any | None:
    """Simplifies algebraic and logical expressions involving identities.
        
        Args:
            op (str): The operator.
            left (Any): The left expression.
            right (Any): The right expression.
            
        Returns:
            Any | None: The simplified expression, or None."""
    if op == "+":
        if _is_zero(right):
            return left
        if _is_zero(left):
            return right
    elif op == "-":
        if _is_zero(right):
            return left
    elif op == "*":
        if _is_one(right):
            return left
        if _is_one(left):
            return right
        if _is_zero(right) or _is_zero(left):
            return _make_numeric_literal(0, "REAL" if _is_real_operand(left, right) else "INTEGER")
    elif op == "/":
        if _is_one(right):
            return left
    elif op == "AND":
        if _logical_is(right, True):
            return left
        if _logical_is(left, True):
            return right
        if _logical_is(right, False) or _logical_is(left, False):
            return _logical_false()
    elif op == "OR":
        if _logical_is(right, False):
            return left
        if _logical_is(left, False):
            return right
        if _logical_is(right, True) or _logical_is(left, True):
            return _logical_true()
    return None


def _is_terminator(stmt: Any) -> bool:
    """Checks if a statement terminates a control flow (e.g. RETURN, STOP).
        
        Args:
            stmt (Any): The statement.
            
        Returns:
            bool: True if a terminator."""
    return isinstance(stmt, ReturnNode) or (isinstance(stmt, tuple) and stmt == ("stop",))


def _get_unary_operand(node: UnOp) -> Any:
    """Extracts the operand from a unary operation node.
        
        Args:
            node (UnOp): The UnOp node.
            
        Returns:
            Any: The operand."""
    if hasattr(node, "operand"):
        return node.operand
    return getattr(node, "expr", None)


def _set_unary_operand(node: UnOp, value: Any) -> None:
    """Sets the operand of a unary operation node.
        
        Args:
            node (UnOp): The UnOp node.
            value (Any): The new operand."""
    node.operand = value
    if hasattr(node, "expr"):
        node.expr = value


def _get_statement_label(stmt: Any) -> int | None:
    """Retrieves the label of a statement if it possesses one.
        
        Args:
            stmt (Any): The statement.
            
        Returns:
            int | None: The statement label or None."""
    label = getattr(stmt, "statement_label", None)
    if isinstance(label, int):
        return label
    if isinstance(stmt, (GotoNode, DoNode)):
        return None
    label = getattr(stmt, "label", None)
    if isinstance(label, int):
        return label
    return None


def _get_goto_target(stmt: GotoNode) -> int | None:
    """Retrieves the target label of a GotoNode.
        
        Args:
            stmt (GotoNode): The GOTO node.
            
        Returns:
            int | None: The target label or None."""
    target_label = getattr(stmt, "target_label", None)
    if isinstance(target_label, int):
        return target_label
    label = getattr(stmt, "label", None)
    if isinstance(label, int):
        return label
    return None


def _set_goto_target(stmt: GotoNode, target: int) -> None:
    """Sets the target label for a GotoNode.
        
        Args:
            stmt (GotoNode): The GOTO node.
            target (int): The new target label."""
    if hasattr(stmt, "target_label"):
        stmt.target_label = target
    stmt.label = target


def _next_labeled_statement(statements: list[Any], start_idx: int) -> int | None:
    """Finds the next labeled statement from a given index in a list.
        
        Args:
            statements (list[Any]): The list of statements.
            start_idx (int): The starting index to scan from.
            
        Returns:
            int | None: The label of the next statement, or None."""
    for idx in range(start_idx + 1, len(statements)):
        label = _get_statement_label(statements[idx])
        if label is not None:
            return label
    return None
