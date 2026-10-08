CC=gcc
CFLAGS=-Wall -Wextra -Werror -std=c11 -g
OBJ=lista_estoque.o fila_requisicoes.o pilha_historico.o testes.o
BIN=testes

.PHONY: all test valgrind clean

all: $(BIN)

$(BIN): $(OBJ)
	$(CC) $(CFLAGS) -o $(BIN) $(OBJ)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

test: all
	./$(BIN)

valgrind: all
	valgrind --leak-check=full --error-exitcode=1 ./$(BIN)

clean:
	rm -f $(OBJ) $(BIN)
