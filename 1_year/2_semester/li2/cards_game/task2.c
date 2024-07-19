#include <stdio.h>
#include <stdlib.h>
#include <wchar.h>                                                                              
#include <locale.h>
#include <assert.h>

/*
                                                                  **GUIÃO 2**
Este programa tem como objetivo comparar e ordenar combinações de cartas do mesmo tipo. É importante notar que todas as combinações devem ter o mesmo tipo e número de cartas.

Input esperado:
O programa lê uma linha com o número de testes T e, para cada teste, o número de sequências seguido de uma linha para cada sequência.

Output esperado:
O programa imprime o resultado dos T testes. Para cada teste é impressa uma linha com "Teste" seguida do número do teste.
Se todas as sequências forem do mesmo tipo e número de cartas, o programa imprime uma linha para cada sequência, em ordem crescente.
Cada sequência é também ordenada em ordem crescente e as cartas são separadas por espaços. Se as sequências não forem todas iguais, o programa imprime a frase "Combinações não iguais!".
*/

#define MaxInput 40

typedef enum suit {spades = 1, hearts, diamonds, clubs} suit;
typedef enum value {ace = 1, two, three, four, five, six, seven, eight, nine, ten, jack, knight, queen, king} value;

typedef struct card {
    suit tp;
    value nu;       
    wchar_t code;
} card;

typedef struct combination {
    int num;
    wchar_t code;
} combination;

typedef struct order_cards {
    suit tp;
    value nu;
    wchar_t code;
    int ind;
} order_cards;

suit naipe(wchar_t card) {
    return (card - 0x1F0A0) / 16 + 1;
}

value valor(wchar_t card) {
    return (card - 0x1F0A0) % 16;
}

combination setOfCards(card cards[], int size) {
    // Inicializa uma estrutura chamada 'result' e algumas variáveis.
    combination result;
    int count = 1;
    suit naipe = cards[0].tp;
    wchar_t control = cards[0].code;
                                                          
    // Percorre o array de last_cards
    for (int i = 0; i < size - 1; i++){
        // Para cada par de last_cards consecutivas com o mesmo número...
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

        // Se encontrar um par de last_cards com números diferentes...
        if (cards[i].nu != cards[i + 1].nu) {
            // Reinicia 'count' e interrompe o ciclo.
            count = 1;
            break;
        }
    } 

    // Atribui 'control' ao campo 'code' da "combination" 'result'
    result.code = control;
    // Atribui 'count' ao campo 'num' do 'result' se 'count' for maior que 1 e o tamanho do conjunto de last_cards for maior que 1. Caso contrário, atribui 0 ao campo 'num'.
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

int checkCode(const void *a, const void *b) {
    // Converte os ponteiros 'void' para ponteiros 'card'
    const card *cardA = (const card *)a;
    const card *cardB = (const card *)b;

    // Se o número da carta A é diferente do número da carta B...
    if (cardA->nu != cardB->nu) {
        // Retorna a diferença entre o número da carta A e o número da carta B
        return cardA->nu - cardB->nu;
    } 
    else {
        // Se o número da carta A é igual ao número da carta B, retorna a diferença entre o naipe da carta A e o naipe da carta B
        return cardA->tp - cardB->tp;
    }
}

int check_combination(card temp[], int size) {
    // Chama as funções 'setOfCards', 'sequence' e 'doubleSequence' para o conjunto de cartas
    combination c1 = setOfCards(temp, size);
    combination c2 = sequence(temp, size);
    combination c3 = doubleSequence(temp, size);

    // Inicializa 'max' com o número de cartas na maior combinação de 'c1' e 'maxtype' com 1
    int max = c1.num;
    int maxtype = 1;

    // Se o número de cartas na maior sequência de 'c2' é maior que 'max', atualiza 'max' e 'maxtype'
    if (c2.num > max) {
        max = c2.num;
        maxtype = 2; 
    }

    // Se o número de cartas na maior sequência dupla de 'c3' é maior que 'max', atualiza 'max' e 'maxtype'
    if (c3.num * 2 > max) {
        max = c3.num;
        maxtype = 3; 
    }

    // Se 'max' é maior que 0, retorna 'maxtype'
    if (max > 0) {
        if (maxtype == 1) {
            return 1;
        } else if (maxtype == 2) {
            return 2;
        } else if (maxtype == 3) {
            return 3;
        }
    }
    
    // Se 'max' é 0, retorna 0
    return 0;
}

void copy_array(card cards[], wchar_t input[], int copy_size, int size) {
    // Inicializa 'position' com 'copy_size'.
    int position = copy_size;

    // Para cada caractere em 'input'...
    for (int h = 0; h < size; h++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'cards' na posição 'position'
        cards[position].nu = valor(input[h]);
        cards[position].tp = naipe(input[h]);
        cards[position].code = input[h];
        // Incrementa 'position'.
        position++;
    }
}

int all_equal(int control[], int n) {
    // Inicializa 'compare' com o primeiro elemento do array 'control'.
    int compare = control[0];

    // Percorre o array 'control' a partir do segundo elemento.
    for (int i = 1; i < n; i++) {
        // Se 'compare' é diferente do elemento atual do array 'control', retorna 0.
        if (compare != control[i]) return 0;
    }

    // Se todos os elementos do array 'control' são iguais a 'compare', retorna 1.
    return 1;
}

void create_last(card cards[], order_cards last_cards[], int tests, int skip) {
    // Declara uma variável 'end'.
    int end;

    // Para cada teste...
    for (int h = 0; h < tests; h++) {
        // Calcula o índice da última carta do teste atual.
        end = (skip * (h + 1)) - 1;
        // Atribui o índice do teste a 'ind' de 'last_cards'.
        last_cards[h].ind = h;
        // Copia o código, o valor numérico e o naipe da última carta do teste atual para 'last_cards'.
        last_cards[h].code = cards[end].code;
        last_cards[h].nu = cards[end].nu;
        last_cards[h].tp = cards[end].tp;
    }
}


void show_sequence(card cards[], int copy_size, int tests) {
    // Inicializa algumas variáveis e arrays.
    int skip = copy_size / tests;
    int arr[tests];
    order_cards last_cards[tests];
    int print_order;
    int print_pos;

    // Para cada teste, ordena o subconjunto de cartas correspondente.
    for (int i = 0; i < tests; i++) {
        qsort(&cards[i * skip], skip, sizeof(card), checkCode);
    }

    // Cria um array de estruturas 'order_cards' que contém a última carta de cada teste.
    create_last(cards, last_cards, tests, skip);

    // Ordena o array 'last_cards'.
    qsort(last_cards, tests, sizeof(order_cards), checkCode);      

    // Cria um array 'arr' que contém os índices dos testes em ordem de acordo com 'last_cards'.
    for (int k = 0; k < tests; k++) { 
        arr[k] = last_cards[k].ind;
    }

    // Para cada teste, imprime as cartas do teste na ordem especificada por 'arr'.
    for (int i = 0; i < tests; i++) {
        print_order = arr[i];
        print_pos = print_order * skip;

        for (int h = 0; h < skip; h++) {
            if (h == (skip - 1)) {
                printf("%lc\n", cards[print_pos].code);
            }
            else {
                printf("%lc ", cards[print_pos].code);
            }
            print_pos++;
        }
    }
}


void process(int test_number, int tests, card* cards, int* line_counter, int* control) {
    // Inicializa algumas variáveis e ponteiros.
    wchar_t* input = NULL;
    card* temp = NULL;
    int copy_size = 0;

    // Para cada teste...
    for (int h = 0; h < tests; h++) {
        // Aloca memória para 'input'.
        input = malloc(MaxInput * sizeof(wchar_t));
        // Verifica se a alocação de memória para 'input' foi bem-sucedida.
        assert(input != NULL);

        // Lê a linha de entrada para 'input'.
        if (scanf("%ls", input) == 1) {
            // Calcula o tamanho de 'input'.
            int size = wcslen(input);

            // Aloca memória para 'temp'.
            temp = malloc(sizeof(card) * size); 
            // Verifica se a alocação de memória para 'temp' foi bem-sucedida.
            assert(temp != NULL);

            // Para cada caractere em 'input'.
            for (int j = 0; j < size; j++) {
                // Converte o caractere para um valor numérico e um naipe e armazena-os em 'temp'.
                temp[j].nu = valor(input[j]);
                temp[j].tp = naipe(input[j]);
                temp[j].code = input[j];
            }
            // Ordena 'temp'.
            qsort(temp, size, sizeof(card), checkCode);
            // Verifica a combinação de 'temp' e armazena o resultado em 'control'.
            control[h] = check_combination(temp, size); 
            // Liberta a memória alocada para 'temp'.
            free(temp);
                        
            // Copia 'input' para 'cards'.
            copy_array(cards, input, copy_size, size);
            // Armazena o tamanho de 'input' em 'line_counter'.
            line_counter[h] = size;
            // Atualiza 'copy_size'.
            copy_size += size;
        }

        // Liberta a memória alocada para 'input'.
        free(input); 
    }    

    // Se todas as combinações são iguais e todos os tamanhos de linha são iguais...
    if (all_equal(control, tests) == 1 && all_equal(line_counter, tests) == 1) {
        // Imprime o número do teste e a sequência de cartas.
        printf("Teste %d\n", test_number);                  
        show_sequence(cards, copy_size, tests);                    
    } else {
        // Se as combinações não são iguais ou os tamanhos de linha não são iguais, imprime uma mensagem de erro.
        printf("Teste %d\n", test_number);
        printf("Combinações não iguais!\n");
    }
}


void process_input(int lines) {
    // Inicializa alguns ponteiros.
    card* cards = NULL;
    int* control = NULL;
    int* line_counter = NULL;

    // Lê o número de linhas de entrada.
    if (scanf("%d", &lines) == 1) {
        // Verifica se 'lines' é maior ou igual a 0.
        assert(lines >= 0);

        // Aloca memória para 'cards'.
        cards = malloc(sizeof(card) * MaxInput * lines);
        // Verifica se a alocação de memória para 'cards' foi bem-sucedida.
        assert(cards != NULL);

        // Para cada linha de entrada...
        for (int i = 0; i < lines; i++) {
            int tests;
            // Lê o número de testes.
            if (scanf("%d", &tests) == 1) {
                // Aloca memória para 'control' e 'line_counter'.
                control = malloc(tests * sizeof(int));
                line_counter = malloc(tests * sizeof(int));
                // Verifica se a alocação de memória para 'control' foi bem-sucedida.
                assert(control != NULL);

                // Processa os testes.
                process(i + 1, tests, cards, line_counter, control);

                // Liberta a memória alocada para 'line_counter' e 'control'.
                free(line_counter);
                free(control);
            }
        }

        // Liberta a memória alocada para 'cards'.
        free(cards);
    }
}

int main(void) {
    // Inicializa 'lines' com 0.
    int lines = 0;
    // Define o locale do programa para "C.UTF-8".
    setlocale(LC_CTYPE, "C.UTF-8");

    // Chama a função 'process_input' para processar as linhas de entrada.
    process_input(lines);

    // Retorna 0.
    return 0;
}