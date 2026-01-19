GO := go
BIN_DIR := bin

ROVER_BIN := $(BIN_DIR)/rover
MOTHERSHIP_BIN := $(BIN_DIR)/mothership
GROUND_CONTROL_BIN := $(BIN_DIR)/ground-control

# Path to the binary
PKGSITE_CMD := $(shell go env GOPATH)/bin/pkgsite

# Added markdoc and markdocclean to phony targets
.PHONY: all rover mothership ground-control clean docs help markdoc markdocclean

all: rover mothership ground-control

rover:
	@echo "🚀 Building Rover..."
	@mkdir -p $(BIN_DIR)
	$(GO) build -o $(ROVER_BIN) ./cmd/rover

mothership:
	@echo "🛸 Building Mothership..."
	@mkdir -p $(BIN_DIR)
	$(GO) build -o $(MOTHERSHIP_BIN) ./cmd/mothership

ground-control:
	@echo "🌍 Building Ground Control..."
	@mkdir -p $(BIN_DIR)
	$(GO) build -o $(GROUND_CONTROL_BIN) ./cmd/ground-control

clean:
	@echo "🧹 Cleaning binary artifacts..."
	rm -rf $(BIN_DIR)
	$(GO) clean

# --- New Documentation Commands (Markdown) ---

markdoc:
	@echo "📝 Generating Markdown documentation for all packages..."
	@echo "⚠️  Note: Ensure 'gomarkdoc' is installed (see README)."
	@# Generate a README.md file in each directory containing Go code
	gomarkdoc --output '{{.Dir}}/README.md' ./...
	@echo "✅ Markdown documentation generated in 'cmd/' and 'internal/' folders."

markdocclean:
	@echo "🧹 Cleaning generated Markdown documentation..."
	@# Find and remove README.md files inside cmd/ and internal/
	find cmd internal -name "README.md" -type f -delete
	@echo "✅ README.md files removed from subdirectories."

# -------------------------------------------------

doc:
	@echo "🌐 Starting documentation server..."
	@echo "👉 Open in your browser: http://localhost:8080/cc.2526"
	@echo "🔴 Press Ctrl+C to stop."
	@$(PKGSITE_CMD) -http=:8080

help:
	@echo "Available Commands:"
	@echo ""
	@echo "  make all            - Compile all components"
	@echo "  make rover          - Compile only the Rover"
	@echo "  make mothership     - Compile only the Mothership"
	@echo "  make ground-control - Compile only Ground Control"
	@echo "  make clean          - Remove the bin/ folder"
	@echo "  make markdoc        - Generate Markdown docs (README.md) in folders"
	@echo "  make markdocclean   - Remove generated Markdown docs"
	@echo "  make doc            - Start the documentation server"
	@echo ""