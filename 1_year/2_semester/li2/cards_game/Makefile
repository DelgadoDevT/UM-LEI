CFLAGS=-Wall -Wextra -pedantic -O2
OBJS=$(patsubst %c, %o, $(wildcard *.c))

cards: CardsGame.c
	$(CC) $(CFLAGS) -o $@ $<

# Procura funções demasiado complexas 
complexidade:
	@echo Only printing when Modified McCabe Cyclomatic Complexity is above 5
	@echo | pmccabe -v
	@pmccabe *.c| sort -nr | awk '{if($$1>5)print}'

# Procura problemas no código 
check:
	cppcheck --enable=all --suppress=missingIncludeSystem .

# Esta parte foi gerada usando o comando gcc -MM *.c
cards.o: cards.c cards.h
main.o: main.c cards.h

codigo.zip: $(wildcard *.h) $(wildcard *.c)
	zip -9r $@ $^
  
clean:
	rm $(OBJS) a.out cards