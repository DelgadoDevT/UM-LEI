SHELL := /bin/sh

POETRY := $(shell command -v poetry 2>/dev/null || echo $(HOME)/.local/bin/poetry)
PROJECT_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
CYAN := \033[1;36m
YELLOW := \033[1;33m
GREEN := \033[1;32m
RESET := \033[0m

.PHONY: help check-poetry install test test-v docs open-docs clean-docs server client gui client1 client2 clean-data

help:
	@printf "$(CYAN)🔐 Secure Chat E2EE - Command Menu$(RESET)\n"
	@printf "\n"
	@printf "$(YELLOW)make install$(RESET)     - Install dependencies with Poetry\n"
	@printf "$(YELLOW)make test$(RESET)        - Run test suite (quiet)\n"
	@printf "$(YELLOW)make test-v$(RESET)      - Run test suite (verbose)\n"
	@printf "$(YELLOW)make docs$(RESET)        - Build Sphinx HTML documentation\n"
	@printf "$(YELLOW)make open-docs$(RESET)   - Open generated docs in default browser\n"
	@printf "$(YELLOW)make clean-docs$(RESET)  - Remove generated documentation files\n"
	@printf "$(YELLOW)make server$(RESET)      - Start chat server\n"
	@printf "$(YELLOW)make client$(RESET)      - Start one chat client\n"
	@printf "$(YELLOW)make gui$(RESET)         - Start desktop GUI client\n"
	@printf "$(YELLOW)make client1$(RESET)     - Alias for client (run in terminal 1)\n"
	@printf "$(YELLOW)make client2$(RESET)     - Alias for client (run in terminal 2)\n"
	@printf "$(YELLOW)make clean-data$(RESET)  - Remove local DB/client data for manual tests\n"
	@printf "\n"
	@printf "$(GREEN)Tip: run 'make <target>' to execute a command (use separate terminals for multi-client tests).$(RESET)\n"

check-poetry:
	@if [ ! -x "$(POETRY)" ]; then \
		echo "Poetry not found. Install it first or add it to PATH."; \
		exit 1; \
	fi

install: check-poetry
	$(POETRY) install

test: check-poetry
	$(POETRY) run pytest -q

test-v: check-poetry
	$(POETRY) run pytest -vv

docs: check-poetry
	$(POETRY) run python -m sphinx.ext.apidoc -f -o docs_src/_api src
	$(POETRY) run sphinx-build -b html docs_src docs
	@echo "Documentation built at docs/index.html"

open-docs: docs
	@xdg-open docs/index.html 2>/dev/null || \
	 open docs/index.html 2>/dev/null || \
	 start docs/index.html 2>/dev/null || \
	 echo "Could not open browser automatically. Open docs/index.html manually."

clean-docs:
	@rm -rf docs/ docs_src/_api
	@echo "Generated documentation cleaned."

server: check-poetry
	$(POETRY) run python -m src.server.main

client: check-poetry
	$(POETRY) run python -m src.client.main

gui: check-poetry
	$(POETRY) run python -m src.client.gui_main

client1: client

client2: client

clean-data:
	rm -f server_data.db
	rm -rf client_data/*
	rm -rf ca/*
	@echo "Local test data cleaned."
