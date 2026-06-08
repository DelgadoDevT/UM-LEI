"""Code generation module.

Provides the CodeGenerator class for transforming AST nodes into EWVM assembly code."""

from src.ast_nodes import *

class CodeGenerator:
    """Generates assembly code from an Abstract Syntax Tree.

    Attributes:
        symtab (SymbolTable): The symbol table containing variable types and declarations.
        code (list[str]): The generated lines of assembly code.
        label_counter (int): Counter for generating unique labels.
        env (dict): Environment mapping for variables to indices.
        current_scope (str): The current scope being compiled.
        indent_level (int): The current indentation level.
        used_labels (set): A set of labels actually jumped to.
        current_local_count (int): Counter for local variables.
        local_types (dict): Mapping of local variable types."""
    def __init__(self, symtab):
        """Initializes the code generator.
        
        Args:
            symtab (SymbolTable): The symbol table for the AST."""
        self.symtab = symtab
        self.code = []
        self.label_counter = 0
        self.env = {}  # var_name -> {"type": "global"|"local"|"arg", "index": int, "is_array": bool}
        self.current_scope = "global"
        self.indent_level = 0
        self.used_labels = set()
        self.current_local_count = 0
        self.local_types = {}

    def new_label(self, prefix="L"):
        """Generates a new unique label.
        
        Args:
            prefix (str): The prefix for the label. Defaults to 'L'.
            
        Returns:
            str: A new unique label string."""
        self.label_counter += 1
        label = f"{prefix}{self.label_counter}"
        self.used_labels.add(label)
        return label

    def emit(self, instruction):
        # Normalize instruction: one space between opcode and arguments, trimmed
        """Emits an assembly instruction, applying appropriate formatting.
        
        Args:
            instruction (str): The raw instruction string."""
        parts = instruction.strip().split()
        if not parts:
            return
        
        normalized_instr = " ".join(parts)
        
        # Rule: START, STOP, and Labels (ending in :) are at Level 0.
        # Everything else (instructions) is at Level 1 (4 spaces).
        if normalized_instr in ("START", "STOP") or (normalized_instr.endswith(":") and " " not in normalized_instr):
            self.code.append(normalized_instr)
        else:
            self.code.append("    " + normalized_instr)

    def generate(self, ast):
        """Generates complete assembly code from the AST.
        
        Args:
            ast (Any): The root node of the AST.
            
        Returns:
            str: The final, formatted assembly code as a string."""
        self.code = []
        self.visit(ast)
        
        # Post-processing: Remove unused labels and cleanup whitespace
        code_lines = self.code
        used_labels = set()

        # Scan for all jump/branch targets
        for line in code_lines:
            parts = line.strip().split()
            if len(parts) >= 2:
                instr = parts[0]
                target = parts[1]
                if instr in ("JUMP", "JZ", "JNZ", "CALL", "PUSHA"):
                    used_labels.add(target)

        final_code = []
        for line in code_lines:
            stripped = line.strip()
            if not stripped:
                final_code.append("")
                continue
                
            if stripped.endswith(":") and " " not in stripped:
                label = stripped[:-1]
                # Keep subprogram entry points even if not called within this file (could be external)
                # But for internal labels (DOSTART, LABEL, etc), we can prune if unused.
                if label.startswith(("DOSTART", "DOEND", "LABEL", "IF", "ELSE", "ENDIF")) and label not in used_labels:
                    continue
            final_code.append(line)
            
        # Ensure only one blank line between blocks and strip trailing/leading blanks
        result = "\n".join(final_code)
        import re
        result = re.sub(r'\n{3,}', '\n\n', result)
        return result.strip() + "\n"
        
    def visit(self, node):
        """Delegates visiting an AST node to its corresponding method.
        
        Args:
            node (Any): The node to visit.
            
        Returns:
            Any: The result of the visit method."""
        if hasattr(node, "stmt_label") and node.stmt_label is not None:
            self.emit(f"LABEL{node.stmt_label}:")
        
        method_name = f"visit_{type(node).__name__}"
        visitor = getattr(self, method_name, self.generic_visit)
        return visitor(node)

    def generic_visit(self, node):
        """Fallback for unhandled node types.
        
        Args:
            node (Any): The unhandled node.
            
        Raises:
            Exception: Always raises due to missing visit method."""
        raise Exception(f"No visit_{type(node).__name__} method")

    def visit_list(self, node_list):
        """Visits a list of nodes.
        
        Args:
            node_list (list): The list of nodes."""
        for node in node_list:
            self.visit(node)

    def _collect_decls(self, stmts, decl_info):
        """Collects variable declarations and their properties recursively.
        
        Args:
            stmts (list): List of statements.
            decl_info (dict): Dictionary to store declaration data."""
        for stmt in stmts:
            if isinstance(stmt, list):
                self._collect_decls(stmt, decl_info)
            elif isinstance(stmt, VarDecl):
                if stmt.name not in decl_info:
                    decl_info[stmt.name] = {"type": stmt.var_type, "dimensions": stmt.dimensions}
                else:
                    if stmt.dimensions:
                        decl_info[stmt.name]["dimensions"] = stmt.dimensions
                    if stmt.var_type != "DIMENSION" and not isinstance(stmt.var_type, list):
                        decl_info[stmt.name]["type"] = stmt.var_type


    def _is_last_instr(self, instr_name):
        # Ignore empty lines at the end when checking
        """Checks if the last emitted instruction matches a given name.
        
        Args:
            instr_name (str): The instruction to check for.
            
        Returns:
            bool: True if it matches the last instruction."""
        for i in range(len(self.code) - 1, -1, -1):
            line = self.code[i].strip()
            if not line:
                continue
            return line == instr_name
        return False

    def visit_ProgramNode(self, node):
        """Generates code for the main program node.
        
        Args:
            node (ProgramNode): The main program node."""
        self.env = {}
        self.local_types = {}
        self.current_local_count = 0
        self.emit("START")
        
        # Coletar declaracoes unicas
        decl_info = {}
        self._collect_decls(node.body, decl_info)
        
        for name, info in decl_info.items():
            if info["type"] != "DIMENSION" and not isinstance(info["type"], list):
                self.local_types[name] = info["type"]

        global_count = len(decl_info)
        if global_count > 0:
            self.emit(f"PUSHN {global_count}")
            
        for i, (name, info) in enumerate(decl_info.items()):
            is_array = info["dimensions"] is not None and len(info["dimensions"]) > 0
            self.env[name] = {"type": "global", "index": i, "is_array": is_array}
            if is_array:
                # Avaliar size
                dim_node = info["dimensions"][0]
                if isinstance(dim_node, Literal):
                    size = dim_node.value
                else:
                    size = 10
                self.emit(f"PUSHI {size}")
                self.emit("ALLOCN")
                self.emit(f"STOREG {i}")
        
        for stmt in node.body:
            if not isinstance(stmt, VarDecl):
                self.visit(stmt)
            
        if not self._is_last_instr("STOP"):
            self.emit("STOP")
        self.code.append("") # Blank line after main program
        
    def visit_SubroutineNode(self, node):
        """Generates code for a subroutine node.
        
        Args:
            node (SubroutineNode): The subroutine node."""
        if self.code and self.code[-1] != "":
            self.code.append("")
            
        self.env = {}
        self.local_types = {}
        self.current_scope = node.name
        self.emit(f"{node.name}:")
        
        decl_info = {}
        self._collect_decls(node.body, decl_info)

        for name, info in decl_info.items():
            if info["type"] != "DIMENSION" and not isinstance(info["type"], list):
                self.local_types[name] = info["type"]

        args_count = len(node.params)
        for i, param in enumerate(node.params):
            is_array = False
            if param in decl_info and decl_info[param]["dimensions"]:
                is_array = True
            # For N args: first arg is at -N, last arg is at -1
            self.env[param] = {"type": "arg", "index": -(args_count - i), "is_array": is_array}
            
        local_vars_names = [name for name in decl_info.keys() if name not in node.params]
        
        local_count = len(local_vars_names)
        self.current_local_count = local_count
        if local_count > 0:
            self.emit(f"PUSHN {local_count}")
            
        for i, name in enumerate(local_vars_names):
            info = decl_info[name]
            is_array = info["dimensions"] is not None and len(info["dimensions"]) > 0
            self.env[name] = {"type": "local", "index": i, "is_array": is_array}
            if is_array:
                dim_node = info["dimensions"][0]
                if isinstance(dim_node, Literal):
                    size = dim_node.value
                else:
                    size = 10
                self.emit(f"PUSHI {size}")
                self.emit("ALLOCN")
                self.emit(f"STOREL {i}")
            
        for stmt in node.body:
            if not isinstance(stmt, VarDecl):
                self.visit(stmt)
            
        if not self._is_last_instr("RETURN"):
            if self.current_local_count > 0:
                self.emit(f"POP {self.current_local_count}")
            self.emit("RETURN")
        self.current_scope = "global"
        self.current_local_count = 0
        self.code.append("") # Blank line after subroutine

    def visit_FunctionNode(self, node):
        """Generates code for a function node.
        
        Args:
            node (FunctionNode): The function node."""
        if self.code and self.code[-1] != "":
            self.code.append("")

        self.env = {}
        self.local_types = {}
        self.current_scope = node.name
        self.emit(f"{node.name}:")
        
        decl_info = {}
        self._collect_decls(node.body, decl_info)

        for name, info in decl_info.items():
            if info["type"] != "DIMENSION" and not isinstance(info["type"], list):
                self.local_types[name] = info["type"]
        self.local_types[node.name] = node.return_type
        
        args_count = len(node.params)
        for i, param in enumerate(node.params):
            is_array = False
            if param in decl_info and decl_info[param]["dimensions"]:
                is_array = True
            # For N args: first arg is at -N, last arg is at -1
            self.env[param] = {"type": "arg", "index": -(args_count - i), "is_array": is_array}
            
        # Return value placeholder is below all arguments: at -(N+1)
        self.env[node.name] = {"type": "arg", "index": -(args_count + 1), "is_array": False}
        
        local_vars_names = [name for name in decl_info.keys() if name not in node.params and name != node.name]
        
        local_count = len(local_vars_names)
        self.current_local_count = local_count
        if local_count > 0:
            self.emit(f"PUSHN {local_count}")
            
        for i, name in enumerate(local_vars_names):
            info = decl_info[name]
            is_array = info["dimensions"] is not None and len(info["dimensions"]) > 0
            self.env[name] = {"type": "local", "index": i, "is_array": is_array}
            if is_array:
                dim_node = info["dimensions"][0]
                if isinstance(dim_node, Literal):
                    size = dim_node.value
                else:
                    size = 10
                self.emit(f"PUSHI {size}")
                self.emit("ALLOCN")
                self.emit(f"STOREL {i}")
            
        for stmt in node.body:
            if not isinstance(stmt, VarDecl):
                self.visit(stmt)
            
        if not self._is_last_instr("RETURN"):
            if self.current_local_count > 0:
                self.emit(f"POP {self.current_local_count}")
            self.emit("RETURN")
        self.current_scope = "global"
        self.current_local_count = 0
        self.code.append("") # Blank line after function

    def visit_VarDecl(self, node):
        """Visits a variable declaration node (usually a no-op as it's handled in subprograms).
        
        Args:
            node (VarDecl): The variable declaration node."""
        pass

    def visit_PrintNode(self, node):
        """Generates code for print statements.
        
        Args:
            node (PrintNode): The print statement node."""
        for expr in node.expressions:
            expr_type = self._get_expr_type(expr)
            self.visit(expr)
            if expr_type == "REAL":
                self.emit("WRITEF")
            elif expr_type == "INTEGER":
                self.emit("WRITEI")
            elif expr_type == "LOGICAL":
                self.emit("WRITEI") # Imprime 0 ou 1
            elif expr_type == "CHARACTER":
                self.emit("WRITES")
            else:
                self.emit("WRITEI")
        self.emit("WRITELN")

    def visit_ReadNode(self, node):
        """Generates code for read statements.
        
        Args:
            node (ReadNode): The read statement node."""
        for var in node.variables:
            name = None
            if isinstance(var, Variable):
                name = var.name
            elif isinstance(var, tuple):
                name = var[1]
            elif isinstance(var, dict):
                name = var.get("name")
                
            var_type = self._get_var_type(name)
            if isinstance(var, tuple) and var[0] == "var_ref_array":
                self._emit_load(name)
                self.visit(var[2][0])
                self.emit("PUSHI 1")
                self.emit("SUB")
                self.emit("READ")
                if var_type == "REAL":
                    self.emit("ATOF")
                elif var_type == "INTEGER":
                    self.emit("ATOI")
                # Stack: addr, idx, val (val is top)
                self.emit("STOREN")
            elif isinstance(var, dict) and var.get("node_type") == "array_ref":
                self._emit_load(name)
                self.visit(var["indices"][0])
                self.emit("PUSHI 1")
                self.emit("SUB")
                self.emit("READ")
                if var_type == "REAL":
                    self.emit("ATOF")
                elif var_type == "INTEGER":
                    self.emit("ATOI")
                self.emit("STOREN")
            else:
                self.emit("READ")
                if var_type == "REAL":
                    self.emit("ATOF")
                elif var_type == "INTEGER":
                    self.emit("ATOI")
                self._emit_store(name)

    def visit_tuple(self, node):
        """Visits tuple nodes (e.g. stop statement).
        
        Args:
            node (tuple): The tuple node.
            
        Raises:
            Exception: If the tuple is not handled."""
        if node[0] == "stop":
            self.emit("STOP")
        else:
            raise Exception(f"Unhandled tuple: {node}")

    def visit_IfNode(self, node):
        """Generates code for IF statements.
        
        Args:
            node (IfNode): The if statement node."""
        if not node.else_block:
            end_label = self.new_label("ENDIF")
            self.visit(node.cond)
            self.emit(f"JZ {end_label}")
            
            for stmt in node.then_block:
                self.visit(stmt)
            self.emit(f"{end_label}:")
        else:
            else_label = self.new_label("ELSE")
            end_label = self.new_label("ENDIF")
            
            self.visit(node.cond)
            self.emit(f"JZ {else_label}")
            
            for stmt in node.then_block:
                self.visit(stmt)
            self.emit(f"JUMP {end_label}")
                
            self.emit(f"{else_label}:")
            
            for stmt in node.else_block:
                self.visit(stmt)
                    
            self.emit(f"{end_label}:")

    def visit_DoNode(self, node):
        """Generates code for DO loops.
        
        Args:
            node (DoNode): The DO loop node."""
        start_label = self.new_label("DOSTART")
        end_label = self.new_label("DOEND")
        
        # Init var: var = start
        self.visit(node.start)
        self._emit_store(node.var)
        
        self.emit(f"{start_label}:")
        
        # Determine operator based on step sign
        is_negative_step = False
        if node.step:
            if isinstance(node.step, Literal) and isinstance(node.step.value, (int, float)):
                if node.step.value < 0: is_negative_step = True
            elif isinstance(node.step, UnOp) and node.step.op == '-' and isinstance(node.step.operand, Literal):
                is_negative_step = True

        var_type = self._get_var_type(node.var)
        limit_type = self._get_expr_type(node.end)
        use_real = (var_type == "REAL" or limit_type == "REAL")

        self._emit_load(node.var)
        if var_type == "INTEGER" and use_real:
            self.emit("ITOF")
            
        self.visit(node.end)
        if limit_type == "INTEGER" and use_real:
            self.emit("ITOF")

        if is_negative_step:
            self.emit("FSUPEQ" if use_real else "SUPEQ")
        else:
            self.emit("FINFEQ" if use_real else "INFEQ")
            
        self.emit(f"JZ {end_label}")
        
        for stmt in node.block:
            self.visit(stmt)
            
        # Step: var = var + step
        self._emit_load(node.var)
        if node.step:
            self.visit(node.step)
            step_type = self._get_expr_type(node.step)
            if var_type == "REAL" and step_type == "INTEGER":
                self.emit("ITOF")
            elif var_type == "INTEGER" and step_type == "REAL":
                # This should probably not happen in well-formed F77 DO loops, but for safety:
                pass
            
            if var_type == "REAL" or (node.step and self._get_expr_type(node.step) == "REAL"):
                self.emit("FADD")
            else:
                self.emit("ADD")
        else:
            self.emit("PUSHI 1")
            if var_type == "REAL":
                self.emit("ITOF")
                self.emit("FADD")
            else:
                self.emit("ADD")
        self._emit_store(node.var)
        
        self.emit(f"JUMP {start_label}")
        
        self.emit(f"{end_label}:")

    def visit_GotoNode(self, node):
        """Generates code for GOTO statements.
        
        Args:
            node (GotoNode): The GOTO node."""
        self.emit(f"JUMP LABEL{node.label}")

    def visit_ContinueNode(self, node):
        """Generates code for CONTINUE statements.
        
        Args:
            node (ContinueNode): The continue node."""
        if node.label and not (hasattr(node, "stmt_label") and node.stmt_label == node.label):
            self.emit(f"LABEL{node.label}:")

    def visit_CallNode(self, node):
        """Generates code for subroutine calls.
        
        Args:
            node (CallNode): The call node."""
        for arg in node.args:
            if isinstance(arg, dict) and arg.get("node_type") == "array_ref":
                self._emit_load(arg["name"]) # address
                self.visit(arg["indices"][0]) # index
                self.emit("PUSHI 1")
                self.emit("SUB")
                self.emit("PADD")
            elif isinstance(arg, Variable):
                # For F77, we should pass variable address.
                self._emit_addr(arg.name)
            else:
                self.visit(arg)
        self.emit(f"PUSHA {node.subroutine_name}")
        self.emit("CALL")
        if len(node.args) > 0:
            self.emit(f"POP {len(node.args)}")

    def visit_ReturnNode(self, node):
        """Generates code for RETURN statements.
        
        Args:
            node (ReturnNode): The return statement node."""
        if self.current_local_count > 0:
            self.emit(f"POP {self.current_local_count}")
        self.emit("RETURN")

    def visit_BinOp(self, node):
        """Generates code for binary operations.
        
        Args:
            node (BinOp): The binary operation node."""
        if node.op == "=":
            # Assignment
            if isinstance(node.left, Variable):
                self.visit(node.right)
                self._emit_store(node.left.name)
            elif isinstance(node.left, dict) and node.left.get("node_type") == "array_ref":
                # Array assignment: [value, address, index]
                self._emit_load(node.left["name"]) # push address
                self.visit(node.left["indices"][0]) # push index
                self.emit("PUSHI 1")
                self.emit("SUB")
                self.visit(node.right) # push value
                self.emit("STOREN")
            else:
                self.visit(node.right) # fallback
            return

        # Expressions
        left_type = self._get_expr_type(node.left)
        right_type = self._get_expr_type(node.right)
        
        self.visit(node.left)
        if left_type == "INTEGER" and right_type == "REAL":
            self.emit("ITOF")
            left_type = "REAL"
            
        self.visit(node.right)
        if right_type == "INTEGER" and left_type == "REAL":
            self.emit("ITOF")
            right_type = "REAL"

        op = node.op.upper()
        if left_type == "REAL" or right_type == "REAL":
            if op in ("+", ".ADD."): self.emit("FADD")
            elif op in ("-", ".SUB."): self.emit("FSUB")
            elif op in ("*", ".MUL."): self.emit("FMUL")
            elif op in ("/", ".DIV."): self.emit("FDIV")
            elif op == ".LT.": self.emit("FINF")
            elif op == ".LE.": self.emit("FINFEQ")
            elif op == ".GT.": self.emit("FSUP")
            elif op == ".GE.": self.emit("FSUPEQ")
            elif op == ".EQ.": self.emit("EQUAL")
        else:
            if op in ("+", ".ADD."): self.emit("ADD")
            elif op in ("-", ".SUB."): self.emit("SUB")
            elif op in ("*", ".MUL."): self.emit("MUL")
            elif op in ("/", ".DIV."): self.emit("DIV")
            elif op == ".LT.": self.emit("INF")
            elif op == ".LE.": self.emit("INFEQ")
            elif op == ".GT.": self.emit("SUP")
            elif op == ".GE.": self.emit("SUPEQ")
            elif op == ".EQ.": self.emit("EQUAL")
            elif op == "**":
                # Dummy para ** (EWVM n tem pow, emitimos erro ou convertemos em loop, vamos simplificar para MUL por agr)
                self.emit("MUL")
                
        # Lógicos
        if op == ".AND.": self.emit("AND")
        elif op == ".OR.": self.emit("OR")
        elif op == ".NE.":
            self.emit("EQUAL")
            self.emit("NOT")

    def visit_UnOp(self, node):
        """Generates code for unary operations.
        
        Args:
            node (UnOp): The unary operation node."""
        self.visit(node.operand)
        op = node.op.upper()
        if op == "-":
            t = self._get_expr_type(node.operand)
            if t == "REAL":
                self.emit("PUSHF -1.0")
                self.emit("FMUL")
            else:
                self.emit("PUSHI -1")
                self.emit("MUL")
        elif op == ".NOT.":
            self.emit("NOT")

    def visit_Literal(self, node):
        """Generates code to push literal values.
        
        Args:
            node (Literal): The literal node."""
        if node.type_name == "INTEGER":
            self.emit(f"PUSHI {node.value}")
        elif node.type_name == "REAL":
            self.emit(f"PUSHF {node.value}")
        elif node.type_name == "LOGICAL":
            val = 1 if ".TRUE." in str(node.value).upper() else 0
            self.emit(f"PUSHI {val}")
        elif node.type_name == "CHARACTER":
            self.emit(f'PUSHS "{node.value}"')

    def visit_Variable(self, node):
        """Generates code to load a variable value.
        
        Args:
            node (Variable): The variable node."""
        self._emit_load(node.name)

    def _emit_addr(self, name):
        """Emits instructions to load the address of a variable.
        
        Args:
            name (str): The variable name."""
        info = self.env.get(name)
        if not info: return
        if info["type"] == "global":
            self.emit(f"PUSHG {info['index']}")
        elif info["type"] == "local":
            self.emit(f"PUSHL {info['index']}")
        elif info["type"] == "arg":
            self.emit(f"PUSHL {info['index']}")

    def visit_dict(self, node):
        # array_ref ou function call in expression
        """Visits dictionary nodes (e.g. array references, function calls).
        
        Args:
            node (dict): The dictionary node."""
        if node.get("node_type") == "array_ref":
            self._emit_load(node["name"]) # address
            self.visit(node["indices"][0]) # offset
            self.emit("PUSHI 1")
            self.emit("SUB")
            self.emit("LOADN")
        elif node.get("node_type") == "function call":
            name_upper = node["name"].upper()
            if name_upper == "MOD":
                # MOD is an intrinsic, evaluate both arguments and emit MOD instruction
                for arg in node["args"]:
                    self.visit(arg)
                self.emit("MOD")
            else:
                self.emit("PUSHI 0") # return space
                # Refactored: Use address of array element if it is an array_ref
                for arg in node["args"]:
                    if isinstance(arg, dict) and arg.get("node_type") == "array_ref":
                         self._emit_load(arg["name"]) # address
                         self.visit(arg["indices"][0]) # index
                         self.emit("PUSHI 1")
                         self.emit("SUB")
                         self.emit("PADD")
                    else:
                        self.visit(arg)
                self.emit(f"PUSHA {node['name']}")
                self.emit("CALL")
                if len(node["args"]) > 0:
                    self.emit(f"POP {len(node['args'])}")

    def _emit_load(self, name):
        """Emits instructions to load a variable's value.
        
        Args:
            name (str): The variable name."""
        info = self.env.get(name)
        if not info: return # Fallback ou warning
        if info["type"] == "global":
            self.emit(f"PUSHG {info['index']}")
        elif info["type"] in ("local", "arg"):
            self.emit(f"PUSHL {info['index']}")

    def _emit_store(self, name):
        """Emits instructions to store a value into a variable.
        
        Args:
            name (str): The variable name."""
        info = self.env.get(name)
        if not info: return
        if info["type"] == "global":
            self.emit(f"STOREG {info['index']}")
        elif info["type"] in ("local", "arg"):
            self.emit(f"STOREL {info['index']}")

    def _get_var_type(self, name):
        """Resolves the type of a variable from the environment or symbol table.
        
        Args:
            name (str): The variable name.
            
        Returns:
            str: The resolved type string."""
        if hasattr(self, 'local_types') and name in self.local_types:
            return self.local_types[name]
        info = self.symtab.lookup(name)
        if info: return info.get("type", "INTEGER")
        
        # Implicit F77 typing
        first_char = name.upper()[0]
        if first_char in "IJKLMN":
            return "INTEGER"
        return "REAL"

    def _get_expr_type(self, node):
        """Evaluates the return type of an expression node.
        
        Args:
            node (Any): The expression node.
            
        Returns:
            str: The evaluated type string."""
        if isinstance(node, Literal):
            return node.type_name
        if isinstance(node, Variable):
            return self._get_var_type(node.name)
        if isinstance(node, dict) and node.get("node_type") == "function call":
            return self._get_var_type(node["name"])
        if isinstance(node, dict) and node.get("node_type") == "array_ref":
            return self._get_var_type(node["name"])
        if isinstance(node, BinOp):
            left_t = self._get_expr_type(node.left)
            right_t = self._get_expr_type(node.right)
            if left_t == "REAL" or right_t == "REAL":
                return "REAL"
            return "INTEGER"
        if isinstance(node, UnOp):
            return self._get_expr_type(node.operand)
        return "INTEGER"

