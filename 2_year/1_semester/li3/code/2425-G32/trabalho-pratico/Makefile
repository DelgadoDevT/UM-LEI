# Compiler and flags
CC = gcc
CFLAGS = -Wall -Wextra -Werror=discarded-qualifiers -std=c11 -D_GNU_SOURCE -Iinclude $(shell pkg-config --cflags glib-2.0) -O3 -march=native

# Linker flags for GLib and math lib
LDLIBS = $(shell pkg-config --libs glib-2.0) -lm

# Source files for programa-principal
SRC = src/main.c \
      src/entities/artist.c \
      src/managers/artist_manager.c \
      src/entities/album.c \
      src/managers/album_manager.c \
      src/parsers/generic_parser.c \
      src/parsers/parser_sorter.c \
      src/output/file_error_output.c \
      src/entities/music.c \
      src/managers/music_manager.c \
      src/utils/parse_helper.c \
      src/entities/user.c \
      src/managers/user_manager.c \
      src/entities/history.c \
      src/managers/history_manager.c \
      src/utils/logic_validator.c \
      src/utils/syntax_validator.c \
      src/parsers/parse_query.c \
      src/queries/query_one.c \
      src/queries/query_two.c \
      src/queries/query_three.c \
      src/queries/query_four.c \
      src/queries/query_five.c \
      src/queries/query_six.c \
      src/output/query_output.c \
      src/utils/query_helper.c \
      src/utils/recommender.c 

# Object files for programa-principal (placed in resultados/src/)
OBJ = $(SRC:src/%.c=resultados/src/%.o) obj/recomendador-linux-x86_64.o

# Source files for programa-testes (excluding main.c)
TEST_SRC = $(filter-out src/main.c, $(SRC)) src/test.c 

# Object files for programa-testes (placed in resultados/src/)
TEST_OBJ = $(TEST_SRC:src/%.c=resultados/src/%.o) obj/recomendador-linux-x86_64.o

# Source files for programa-interativo
INTERATIVE_SRC = $(filter-out src/main.c, $(SRC)) src/interative.c 

# Object files for programa-interativo (placed in resultados/src/)
INTERATIVE_OBJ = $(INTERATIVE_SRC:src/%.c=resultados/src/%.o) obj/recomendador-linux-x86_64.o

# Target executables
TARGET = programa-principal
TEST_TARGET = programa-testes
INTERATIVE_TARGET = programa-interativo

# Doxygen configuration
DOXYGEN = doxygen
DOXYFILE = Doxyfile
DOC_DIR = doc

# Default rule to build everything
all: $(TARGET) $(TEST_TARGET) $(INTERATIVE_TARGET)

# Rule to build the target executable for programa-principal
$(TARGET): $(OBJ)
	@mkdir -p $(dir $@)  # Ensure the 'resultados' directory exists
	$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)

# Rule to build the target executable for programa-testes
$(TEST_TARGET): $(TEST_OBJ)
	@mkdir -p $(dir $@)  # Ensure the 'resultados' directory exists
	$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)

# Rule to build the target executable for programa-interativo
$(INTERATIVE_TARGET): $(INTERATIVE_OBJ)
	@mkdir -p $(dir $@)  # Ensure the 'resultados' directory exists
	$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)

# Pattern rule to compile source files into object files for both targets
resultados/src/%.o: src/%.c
	@mkdir -p $(dir $@)  # Ensure subdirectories in 'resultados/src/' exist
	$(CC) $(CFLAGS) -c $< -o $@ && echo "Compiled $< to $@"

# Rule to generate Doxygen documentation
doc:
	@$(DOXYGEN) $(DOXYFILE) && echo "Documentation generated in $(DOC_DIR)"

# Rule to clean Doxygen documentation recursively
docclean:
	rm -rf $(DOC_DIR)/*
	@echo "Documentation cleaned from $(DOC_DIR)"

# Clean rule to remove object files and the target executables
clean:
	rm -rf $(filter-out obj/recomendador-linux-x86_64.o, $(OBJ)  $(TEST_OBJ) $(INTERATIVE_OBJ)) $(TARGET) $(TEST_TARGET) $(INTERATIVE_TARGET)
	rm -rf resultados/*.txt resultados/*.csv
	rm -rf resultados/src  # Removes the 'src' directory inside 'resultados'
	rm -rf resultados/test # Removes the 'test' directory inside 'resultados'
	rm -rf resultados/interativo # Removes the 'interativo' directory inside 'resultados'
	@echo "Cleaned object files, executables, and 'resultados' directories."

.PHONY: all clean doc docclean
