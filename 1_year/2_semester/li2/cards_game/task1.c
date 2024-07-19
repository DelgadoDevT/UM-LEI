#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>                                                                              
#include <locale.h>
#include <assert.h>

/*
	GUIÃO 1 - PROJETO "JOGO DE CARTAS"

	O objetivo deste guião será, dado um conjunto especifico de cartas (que, por exemplo, representará no jogo final a mão de um jogador), imprimir o tipo de comhbinaçõa que este conjunto representará (de entre as permitidas), juntamente com a carta de valor mais alto dessa combinação.
	
	De entre as combinações permitidas/possíveis, temos:
		1. conjunto com N cartas iguais (com o mesmo valor);
		2. sequência com N cartas consecutivas (não necessáriamente todas com o mesmo naipe);
		3. dupla sequência com N cartas consecutivas (não necessáriamente todas com o mesmo naipe);
		4. Nenhuma das anteriores.

	INPUT ESPERADO -> ler uma linha com o número de testes "T" a serem feitos, seguida de uma linha por teste (onde cada uma dessas linhas terá um conjunto de cartas)
	OUTPUT ESPERADO -> imprimir "T" linhas, com um resultado diferente dependendo da combinação encontrada no conjunto de cartas de cada uma:
		1. conjunto com N cartas onde a carta mais alta é C
		2. sequência com N cartas onde a carta mais alta é C
		3. dupla sequência com N cartas onde a carta mais alta é C
		4. Nada!
*/

#define MaxInput 32

/*

Definimos dois "enums", que representam, respetivamente:
	1. Os naipes das cartas (ordenados por ordem de importância);
	2. Os valores das cartas (ordenados do menor para o maior).

*/

typedef enum suit {spades = 1, hearts, diamonds, clubs} suit;
typedef enum value {ace = 1, two, three, four, five, six, seven, eight, nine, ten, jack, knight, queen, king} value;

/*

Definimos um "type struct" para a carta, que incluirá:
	1. o seu naipe;
	2. o seu valor;
	3. o seu "Unicode" (código de imagem).
*/

typedef struct card {
    suit tp;
    value nu;       
    wchar_t code;
} card;

/*

Definimos um "type combiunation", que será um tipo semelhante ao "card" referido anteriormente, porém apenas com o valor e a imagem da carta, que será usado para fazer comparações em
algumas funções definidas posteriormente.

*/

typedef struct combination {
    int num;
    wchar_t code;
} combination;

/*

	Utilizamos a função "naipe" para devolver o naipe da carta em questão, representados por ordem de importância (já que usamos o "enum suit" definido anteriormente), representados
por números de 1 a 4.

	Para conseguirmos que devolva este resultado corretamente, utilizamos dos valores que definem os códigos "Unicode" das cartas, dividindo-os pelo número total de cartas desse naipe
(não o número de cartas do naipe no baralho do jogo, mas sim o número de cartas do naipe no UNICODE).
*/

suit naipe(wchar_t card) {
    return (card - 0x1F0A0) / 16 + 1;
}

/*

	Utilizamos a função "valor" para devolver o valor da carta em questão, utilizando os valores ordenados no "enum value".
	Para fazer isto, dividimos o valor que representa o código "Unicode" da carta, e calculamos o seu resto pela divisão por 16.

*/

value valor(wchar_t card) {
    return (card - 0x1F0A0) % 16;
}

/*

	Utilizamos a função "checkcode" como critério de ordenação do quicksort usado posteriormente neste guião.
	Nesta função, que recebe dois apontadores de memória para representarem os códigos das cartas a serem comparadas, utiliza a função "valor" para calcular os valores representados
    em cada carta.
	Em seguida, ao subtrair esses valores, conseguimos (dependendo do valor obtido) deduzir o seguinte:
		1. Se a subtração der como resultado um valor negativo, a primeira carta tem um valor menor que a segunda
		2. Se a subtração der um valor positivo, a primeira carta terá um valor mais que a segunda.
		3. Se o resultado for 0, as cartas têm o mesmo valor.

*/

int checkCode(const void *a, const void *b) {
    const wchar_t *cA = (const wchar_t *)a;
    const wchar_t *cB = (const wchar_t *)b;

    value vA = valor(*cA);
    value vB = valor(*cB);

    return vA - vB;
}

combination setOfCards(card cards[], int size) {
    // Inicializa uma estrutura chamada 'result' e algumas variáveis.
    combination result;
    int count = 1;
    suit naipe = cards[0].tp;
    wchar_t control = cards[0].code;
                                                          
    // Percorre o array de cartas
    for (int i = 0; i < size - 1; i++){
        // Para cada par de cartas consecutivas com o mesmo número...
        if (cards[i].nu == cards[i + 1].nu) {
            // Incrementa 'count'.
            count++;    
            // Atualiza aa variáveis 'naipe' e 'control' se o naipe da carta atual ou da próxima carta for maior que o 'naipe' atual.
            if (naipe < cards[i].tp) {
                naipe = cards[i].tp;
                control = cards[i].code;                
            } else if (naipe < cards[i + 1].tp) {
                naipe = cards[i + 1].tp;
                control = cards[i + 1].code;
            }  
        } 

        // Se encontrar um par de cartas com números diferentes...
        if (cards[i].nu != cards[i + 1].nu) {
            // Reinicia 'count' e interrompe o ciclo.
            count = 1;
            break;
        }
    } 

    // Atribui 'control' ao campo 'code' da "combination" 'result'
    result.code = control;
    // Atribui 'count' ao campo 'num' do 'result' se 'count' for maior que 1 e o tamanho do conjunto de cartas for maior que 1. Caso contrário, atribui 0 ao campo 'num'.
    result.num = (count <= 1 && size > 1) ? 0 : count;
    // Retorna 'result'.
    return result;  
}

combination sequence(card cards[], int size) {
    combination result;
    int count = 1;
    int maxCount = 1;
    wchar_t control = cards[0].code;

    // Percorre um array de last_cards.
    for (int i = 0; i < size - 1; i++) {
        // Se o número da carta atual é um a menos que o número da próxima carta
        if (cards[i].nu == cards[i + 1].nu - 1) {
            // Incrementa 'count'.
            count++;
            // Atualiza 'control' com o código da próxima carta.
            control = cards[i + 1].code;
        } else if (cards[i].nu != cards[i + 1].nu) {
            // Se houver uma lacuna na sequência, retorna 0.
            result.code = 0;
            result.num = 0;
            return result;
        } else {
            // Se não é uma sequência, atualiza o máximo de contagem e reinicia 'count' para 1.
            maxCount = (count > maxCount) ? count : maxCount;
            count = 1;
        }
    }

    // Verifica se o último conjunto de cartas forma a maior sequência.
    maxCount = (count > maxCount) ? count : maxCount;

    // Se a maior sequência é de pelo menos 3 cartas, atualiza 'result'.
    if (maxCount >= 3) {
        result.code = control;
        result.num = maxCount;
    } else {
        result.code = 0; // Indica que não há sequência válida.
        result.num = 0;
    }

    // Retorna 'result'.
    return result;
}

combination doubleSequence(card cards[], int size) {
    combination result;
    int count = 0;
    value savenum = 0;
    int i = 0;
    wchar_t control = cards[0].code;

    // Enquanto 'i' for menor ou igual a 'size - 2'...
    while (i <= size - 2) {
        // Se o número da carta atual é igual ao número da carta seguinte...
        if ((cards[i].nu == cards[i + 1].nu)) { 
            // Incrementa 'count' em 1.
            count++;  

            // Se 'savenum' é menor que o número da carta acual e o naipe dessa carta é maior que o naipe da próxima...
            if ((savenum < cards[i].nu) && (cards[i].tp > cards[i + 1].tp)) {
                // Atualiza o 'control' com o código da carta atual e o 'savenum' com o número dessa mesma carta.
                control = cards[i].code;
                savenum = cards[i].nu;
            } else {
                // Atualiza 'control' com o código da próxima carta e 'savenum' com o número da próxima carta.
                control = cards[i + 1].code;
                savenum = cards[i + 1].nu;
            }
        }

        // Se o número da carta atual não é menor uma unidade que o número da segunda próxima carta...
        if (cards[i].nu != cards[i + 2].nu - 1) break;                                                                  
        // Incrementa 'i' por 2.
        i += 2;
    }

    // Se 'count' é maior ou igual a 3, atribui 'count' ao campo 'num' de 'result'. Caso contrário, atribui 0 ao campo 'num'.
    result.num = (count >= 3) ? count : 0;
    // Atribui 'control' ao campo 'code' de 'result'.
    result.code = control;
    // Retorna 'result'.
    return result;
}

void bridge(card cards[], int size) {
    // Chama as funções 'setOfCards', 'sequence' e 'doubleSequence' para o conjunto de cartas.
    combination c1 = setOfCards(cards, size);
    combination c2 = sequence(cards, size);
    combination c3 = doubleSequence(cards, size);

    // Inicializa 'max' com o número de cartas na maior combinação de 'c1', 'maxtype' com 1 e 'maxcode' com o código da carta mais alta em 'c1'.
    int max = c1.num;
    int maxtype = 1;
    wchar_t maxcode = c1.code;

    // Se o número de cartas na maior sequência de 'c2' é maior que 'max', atualiza 'max', 'maxtype' e 'maxcode'.
    if (c2.num > max) {
        max = c2.num;
        maxtype = 2; 
        maxcode = c2.code;
    }

    // Se o número de cartas na maior sequência dupla de 'c3' é maior que 'max', atualiza 'max', 'maxtype' e 'maxcode'.
    if (c3.num * 2 > max) {
        max = c3.num;
        maxtype = 3; 
        maxcode = c3.code;
    }

    // Se 'max' é maior que 0, imprime a maior combinação, sequência ou sequência dupla encontrada.
    if (max > 0) {
        if (maxtype == 1) {
            wprintf(L"conjunto com %d cartas onde a carta mais alta é %lc\n", max, maxcode);
        } else if (maxtype == 2) {
            wprintf(L"sequência com %d cartas onde a carta mais alta é %lc\n", max, maxcode);
        } else if (maxtype == 3) {
            wprintf(L"dupla sequência com %d cartas onde a carta mais alta é %lc\n", max, maxcode);
        }
    } else {
        // Se 'max' é 0, imprime "Nada!".
        wprintf(L"Nada!\n");
    }
}

int main(void) {
    // Inicializa um apontador para wchar_t chamado 'input' e outro para 'card' chamado 'cards'.
    wchar_t* input = NULL;
    card* cards = NULL;
    int lines;

    // Define o locale do programa para "C.UTF-8" (para conseguirmos usar as imagens das cartas).
    setlocale(LC_CTYPE, "C.UTF-8");

    // Lê o número de linhas de entrada.
    if (scanf("%d", &lines) == 1) {
        // Verifica se 'lines' é maior ou igual a 0.
        assert(lines >= 0);

        // Aloca memória para 'cards'.
        cards = malloc(sizeof(card) * MaxInput * lines);
        // Verifica se a alocação de memória para 'cards' foi bem-sucedida.
        assert(cards != NULL);

        // Para cada linha de entrada....
        for (int i = 0; i < lines; i++) {
            // Aloca memória para o 'input'.
            input = malloc(MaxInput * sizeof(wchar_t));
            // Verifica se a alocação de memória para o 'input' foi bem-sucedida.
            assert(input != NULL);

            // Lê a linha de entrada para 'input'.
            if (scanf("%ls", input) == 1) {

                // Calcula o tamanho do 'input'.
                int size = wcslen(input);

                // Verifica se 'size' é menor ou igual ao 'MaxInput'.
                assert(size <= MaxInput);

                // Ordena o 'input'.
                qsort(input, size, sizeof(wchar_t), checkCode);

                // Para cada caracter no 'input'...
                for (int h = 0; h < size; h++) {
                    // Converte o caracter para um valor numérico e um naipe e armazena-os no struct 'cards'.
                    cards[h].nu = valor(input[h]);
                    cards[h].tp = naipe(input[h]);
                    cards[h].code = input[h];
                }

                // Chama a função 'bridge' para 'cards'.
                bridge(cards, size);
            }

            // Liberta a memória alocada para o 'input'.
            free(input);
        }
    }

    // Liberta a memória alocada para 'cards'.
    free(cards);

    // Retorna 0.
    return 0;
}