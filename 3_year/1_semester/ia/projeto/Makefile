# Makefile for TaxiGreen - IA-2526
# Optimized for Linux/Mac/WSL
# ==========================================

# Variables
PYTHON = python3
PIP = pip3
SRC_DIR = src
DOC_DIR = docs
DOC_BUILD_DIR = $(DOC_DIR)/_build

# Colors (ANSI Escape Codes)
CYAN = \033[1;36m
GREEN = \033[1;32m
YELLOW = \033[1;33m
RESET = \033[0m

.PHONY: help install server client clean doc doc-open docclean generate

# Default Target
help:
	@printf "$(CYAN)🚖 TaxiGreen Management System - Command Menu$(RESET)\n"
	@printf "\n"
	@printf "$(YELLOW)make install$(RESET)   - Install python dependencies\n"
	@printf "$(YELLOW)make generate$(RESET)  - Generate Braga network data (nodes.csv, edges.csv)\n"
	@printf "$(YELLOW)make server$(RESET)    - Start the TaxiGreen Server\n"
	@printf "$(YELLOW)make client$(RESET)    - Start the TaxiGreen Client (new terminal)\n"
	@printf "$(YELLOW)make doc$(RESET)       - Generate HTML documentation (Sphinx)\n"
	@printf "$(YELLOW)make doc-open$(RESET)  - Open documentation in browser\n"
	@printf "$(YELLOW)make docclean$(RESET)  - Clean documentation build only\n"
	@printf "$(YELLOW)make clean$(RESET)     - Remove cache, logs, docs build and temporary maps\n"
	@printf "\n"

install:
	@printf "$(CYAN)📦 Installing Dependencies...$(RESET)\n"
	$(PIP) install -r requirements.txt
	@printf "$(GREEN)✔ Done!$(RESET)\n"

generate:
	@printf "$(CYAN)🌍 Generating Braga Network Data...$(RESET)\n"
	PYTHONPATH=$(PWD) $(PYTHON) src/utils/Graph_Generator.py

server:
	@printf "$(CYAN)🟢 Starting Server...$(RESET)\n"
	$(PYTHON) $(SRC_DIR)/server.py

client:
	@printf "$(CYAN)🔵 Starting Client...$(RESET)\n"
	$(PYTHON) $(SRC_DIR)/client.py

doc:
	@printf "$(CYAN)📚 Generating Documentation (Sphinx)...$(RESET)\n"
	@if [ ! -f "$(DOC_DIR)/conf.py" ]; then \
		printf "$(YELLOW)⚠  Sphinx config not found!$(RESET)\n"; \
		exit 1; \
	fi
	@sphinx-build -M html $(DOC_DIR) $(DOC_BUILD_DIR)
	@printf "$(GREEN)✔ Documentation generated in $(DOC_BUILD_DIR)/html$(RESET)\n"

doc-open:
	@printf "$(CYAN)🌏 Opening Documentation...$(RESET)\n"
	@xdg-open $(DOC_BUILD_DIR)/html/index.html 2>/dev/null || open $(DOC_BUILD_DIR)/html/index.html 2>/dev/null || start $(DOC_BUILD_DIR)/html/index.html 2>/dev/null || echo "Could not open browser automatically. Please open $(DOC_BUILD_DIR)/html/index.html manually."

docclean:
	@printf "$(YELLOW)🧹 Cleaning documentation...$(RESET)\n"
	@rm -rf $(DOC_BUILD_DIR)
	@printf "$(GREEN)✔ Documentation cleaned!$(RESET)\n"

clean:
	@printf "$(YELLOW)🧹 Cleaning up project...$(RESET)\n"
	@rm -rf __pycache__ .pytest_cache
	@find . -type d -name "__pycache__" -exec rm -rf {} +
	@rm -f *.html
	@rm -rf $(DOC_BUILD_DIR)
	@printf "$(GREEN)✔ Workspace clean!$(RESET)\n"