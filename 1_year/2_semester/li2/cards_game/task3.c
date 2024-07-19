#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <wchar.h> 
#include <string.h>                                                                             
#include <locale.h>
#include <assert.h>

#define MaxInput 40
#define MaxPlays 3

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

suit naipe(wchar_t card) {
    return (card - 0x1F0A0) / 16 + 1;
}

value valor(wchar_t card) {
    return (card - 0x1F0A0) % 16;
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
    
    return 0;
}

int contains_all(const wchar_t *base, const wchar_t *object) {
    // Para cada caractere em 'object'...
    for (const wchar_t *p = object; *p; ++p) {
        // Procura o caractere em 'base'.
        const wchar_t *found = wcschr(base, *p);
        
        // Se o caractere não for encontrado em 'base', retorna 1.
        if (!found) {
            return 1;
        }
    }
    
    // Se todos os caracteres de 'object' forem encontrados em 'base', retorna 0
    return 0;
}

void sort_strings(wchar_t string[], int size) {
    // Aloca memória para um array de estruturas 'card'
    card *cards = NULL;
    cards = malloc(sizeof(card) * size);

    // Para cada caractere em 'string'...
    for (int j = 0; j < size; j++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'cards'.
        cards[j].nu = valor(string[j]);
        cards[j].tp = naipe(string[j]);
        cards[j].code = string[j];
    }

    // Ordena 'cards'.
    qsort(cards, size, sizeof(card), checkCode);

    // Copia os códigos das cartas ordenadas de volta para 'string'.
    for (int i = 0; i < size; i++) {
        string[i] = cards[i].code;
    }
}

void removerSubstring(wchar_t *string, const wchar_t *substring) { 
    // Calcula o tamanho de 'string' e 'substring'
    int size = wcslen(string);
    int sizesub = wcslen(substring);

    int indexString = 0;
    int indexSubString;

    // Enquanto 'string[indexString]' não for o caractere nulo
    while (string[indexString]) {
        int matchFound = 0;
        // Para cada caractere em 'substring'
        for (indexSubString = 0; indexSubString < sizesub; indexSubString++) {
            // Se 'string[indexString]' é igual a 'substring[indexSubString]'
            if (string[indexString] == substring[indexSubString]) {
                // Marca que encontrou uma correspondência e interrompe o loop
                matchFound = 1;
                break;
            }
        }

        // Se encontrou uma correspondência
        if (matchFound) {
            // Move todos os caracteres de 'string' para a esquerda, começando em 'indexString'
            for (int i = indexString; i < size; i++) {
                string[i] = string[i + 1];
            }
            // Decrementa 'size'
            size--;
        } else {
            // Incrementa 'indexString'
            indexString++;
        }
    }
}

int validPlay(wchar_t play_string[], int size) {
    // Aloca memória para um array de cartas.
    card *play = malloc(MaxInput * sizeof(card));
    int control = 0;

    // Loop para preencher o array de cartas com os valores e naipes correspondentes.
    for (int h = 0; h < size; h++) {
        // Atribui o valor da carta.
        play[h].nu = valor(play_string[h]);
        // Atribui o naipe da carta.
        play[h].tp = naipe(play_string[h]);
        // Atribui o código da carta.
        play[h].code = play_string[h];       
    }

    // Verifica a combinação das cartas.
    control = check_combination(play, size);   

    // Se o tamanho da jogada é 1, então a jogada é válida.
    if (size == 1) control = 4;

    // Libera a memória alocada para o array de cartas.
    free(play);

    // Retorna o resultado da verificação.
    return control;
}

void isValid(wchar_t deck_string[], wchar_t play_string[], card deck[], int size) {
    // Calcula o tamanho de 'deck_string' e 'play_string'
    int sizeop = wcslen(deck_string) - wcslen(play_string);
    int size2 = wcslen(play_string);
    int control = 0;

    // Ordena 'deck_string' e 'play_string'
    sort_strings(deck_string, size);
    sort_strings(play_string, size2); 

    // Se 'deck_string' contém todos os caracteres de 'play_string'
    if ((contains_all(deck_string, play_string) == 0) && (validPlay(play_string, size2) != 0)) {
        // Remove 'play_string' de 'deck_string'
        removerSubstring(deck_string, play_string);
        // Copia 'deck_string' para 'deck'
        for (int h = 0; h < sizeop; h++) {
            deck[h].nu = valor(deck_string[h]);
            deck[h].tp = naipe(deck_string[h]);
            deck[h].code = deck_string[h];
        }

        // Imprime 'deck'
        for (int k = 0; k < sizeop; k++) {
            if (k == (sizeop - 1)) {
                printf("%lc\n", deck[k].code);
            } else {
                printf("%lc ", deck[k].code);
            }
        }   

        control = 1;
    } else {
        // Se 'deck_string' não contém todos os caracteres de 'play_string', imprime 'deck'
        for (int i = 0; i < size; i++) {
            if (i == (size - 1)) {
                printf("%lc\n", deck[i].code);
            } else {
                printf("%lc ", deck[i].code);
            }
        }
    }

    // Se 'sizeop' é 0 e 'control' é 1, imprime uma linha em branco
    if (sizeop == 0 && control == 1) printf("\n");
}

int sumCards(card hand1[], card hand2[], int size, int size2) {
    // Calcula os índices das últimas cartas de 'hand1' e 'hand2'
    int last = size - 1;
    int last2 = size2 - 1;

    // Compara as últimas cartas de 'hand1' e 'hand2'
    if (hand1[last].nu > hand2[last2].nu) {
        return 1;
    } else if (hand1[last].nu < hand2[last2].nu) { 
        return 0;
    } else if (hand1[last].tp > hand2[last2].tp) {
        return 1;
    } else if (hand1[last].tp < hand2[last2].tp) {
        return 0;
    }

    // Se as últimas cartas de 'hand1' e 'hand2' são iguais, retorna 2
    return 2;
}

int kingException(card deck[], int size, card play[], int size2, card actual_play[], int size3, wchar_t deck_string[], wchar_t actual_play_string[]) {
    // Se a última carta de 'play' é um rei
    if (play[size2 - 1].nu == king) {
        // Se a penúltima carta de 'play' é um rei
        if (play[size2 - 2].nu == king) {
            // Se a antepenúltima carta de 'play' é um rei
            if (play[size2 - 3].nu == king) {
                // Se 'actual_play' é uma sequência dupla de tamanho 10
                if (check_combination(actual_play, size3) == 3 && size3 == 10) {
                // Verifica se 'actual_play_string' é um subconjunto válido de 'deck_string' e atualiza 'deck' e 'deck_string' se for
                isValid(deck_string, actual_play_string, deck, size);
                return 0;
                }
            } else if (check_combination(actual_play, size3) == 3 && size3 == 8) {
                // Se 'actual_play' é uma sequência dupla de tamanho 8
                isValid(deck_string, actual_play_string, deck, size);
                return 0;
            }

        }

        // Se a penúltima carta de 'play' não é um rei
        if (play[size2 - 2].nu != king) {
            // Se 'actual_play' é uma combinação de tamanho 4
            if (check_combination(actual_play, size3) == 1 && size3 == 4) {
                isValid(deck_string, actual_play_string, deck, size);
                return 0;
            }

            // Se 'actual_play' é uma sequência dupla de tamanho 6
            if (check_combination(actual_play, size3) == 3 && size3 == 6) {
                isValid(deck_string, actual_play_string, deck, size);
                return 0;
            }
        }
    }
    return 1;
}  

void organize(wchar_t deck_string[], int size, wchar_t actual_play_string[], int size3, card actual_play[], card play[], int size2) {
    // Ordena 'deck_string' e 'actual_play_string'
    sort_strings(deck_string, size);
    sort_strings(actual_play_string, size3);
    // Ordena 'actual_play' e 'play'
    qsort(actual_play, size3, sizeof(card), checkCode);
    qsort(play, size2, sizeof(card), checkCode);
}

void processInput(wchar_t actual_play_string[], card actual_play[], wchar_t play_string[], int size2, card play[]) {
    // Lê a linha de entrada para 'actual_play_string'
    assert(scanf("%ls", actual_play_string) == 1);

    // Calcula o tamanho de 'actual_play_string'
    int size3 = wcslen(actual_play_string);

    // Para cada caractere em 'actual_play_string'
    for (int h = 0; h < size3; h++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'actual_play'
        actual_play[h].nu = valor(actual_play_string[h]);
        actual_play[h].tp = naipe(actual_play_string[h]);
        actual_play[h].code = actual_play_string[h];
    }

    // Para cada caractere em 'play_string'
    for (int j = 0; j < size2; j++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'play'
        play[j].nu = valor(play_string[j]);
        play[j].tp = naipe(play_string[j]);
        play[j].code = play_string[j];
    }
}

void process(int size3, card actual_play[], wchar_t actual_play_string[], card play[], wchar_t play_string[], int size2) {
    // Para cada caractere em 'actual_play_string'
    for (int h = 0; h < size3; h++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'actual_play'
        actual_play[h].nu = valor(actual_play_string[h]);
        actual_play[h].tp = naipe(actual_play_string[h]);
        actual_play[h].code = actual_play_string[h];
    }

    // Para cada caractere em 'play_string'
    for (int j = 0; j < size2; j++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'play'
        play[j].nu = valor(play_string[j]);
        play[j].tp = naipe(play_string[j]);
        play[j].code = play_string[j];
    }
}

void printDeckCodes(card deck[], int size) {
    // Para cada carta em 'deck'
    for (int i = 0; i < size; i++) {
        // Imprime o código da carta
        if (i == (size - 1)) {
            printf("%lc\n", deck[i].code);
        } else {
            printf("%lc ", deck[i].code);
        }
    }
}

void space(int sizeop, int control) {
    // Se 'sizeop' é 0 e 'control' é 1, imprime uma linha em branco
    if (sizeop == 0 && control == 1) printf("\n");
}

void package(int size3, card actual_play[], wchar_t actual_play_string[], card play[], wchar_t play_string[], int size, wchar_t deck_string[], int size2) {
    // Processa 'actual_play' e 'play', e organiza 'deck_string' e 'actual_play_string'
    process(size3, actual_play, actual_play_string, play, play_string, size2);
    organize(deck_string, size, actual_play_string, size3, actual_play, play, size2);
}

int comb(card play[], int size2, card actual_play[], int size3) {
    // Verifica se a combinação de 'play' é igual à combinação de 'actual_play' e se 'size2' é igual a 'size3'
    if (check_combination(play, size2) == check_combination(actual_play, size3) && size2 == size3) return 0;
    else return 1;
}

void process_print(int sizeop, card deck[], wchar_t deck_string[]) {
    // Processa 'deck' e imprime 'deck'
    for (int h = 0; h < sizeop; h++) {
        deck[h].nu = valor(deck_string[h]);
        deck[h].tp = naipe(deck_string[h]);
        deck[h].code = deck_string[h];
        
        if (h == (sizeop - 1)) {
            printf("%lc\n", deck[h].code);
        } else {
            printf("%lc ", deck[h].code);
        }
    }
}

int all_King(card play[], int size) {
    // Verifica se todas as cartas em 'play' são reis
    for (int i = 0; i < size; i++) {
        if (play[i].nu != king) return 1;
    }

    return 0;
}

void newDeck(wchar_t deck_string[], int size, wchar_t play_string[], int size2, card deck[]) {
    // Inicializa algumas variáveis e ponteiros
    wchar_t *actual_play_string = NULL;
    card *actual_play = malloc(MaxInput * sizeof(card));
    card *play = malloc(MaxInput * sizeof(card)); 
    int control = 0;

    // Aloca memória para 'actual_play_string' e lê a linha de entrada para 'actual_play_string'
    actual_play_string = malloc(MaxInput * sizeof(wchar_t));
    assert(scanf("%ls", actual_play_string) == 1);
    int size3 = wcslen(actual_play_string);
    int sizeop = wcslen(deck_string) - wcslen(actual_play_string);

    // Processa 'actual_play' e 'play', e organiza 'deck_string' e 'actual_play_string'
    package(size3, actual_play, actual_play_string, play, play_string, size, deck_string, size2);

    // Se todas as cartas em 'play' são reis
    if (all_King(play, size2) == 0) {
        // Se a exceção do rei é verdadeira, retorna
        if (kingException(deck, size, play, size2, actual_play, size3, deck_string, actual_play_string) == 0) return;
    }

    // Se a combinação de 'play' é igual à combinação de 'actual_play' e 'size2' é igual a 'size3'
    if (comb(play, size2, actual_play, size3) == 0) {
        // Se a soma das cartas de 'play' e 'actual_play' é 0 e 'deck_string' contém todos os caracteres de 'actual_play_string'
        int t1 = sumCards(play, actual_play, size2, size3);
        if (t1 == 0 && (contains_all(deck_string, actual_play_string) == 0)) {
            // Remove 'actual_play_string' de 'deck_string', processa 'deck' e imprime 'deck'
            removerSubstring(deck_string, actual_play_string);
            process_print(sizeop, deck, deck_string);
            control = 1;
        } else {
            // Imprime os códigos das cartas de 'deck'
            printDeckCodes(deck, size);
        }
    } else {
        // Imprime os códigos das cartas de 'deck'
        printDeckCodes(deck, size);
    }

    // Imprime uma linha em branco se 'sizeop' é 0 e 'control' é 1
    space(sizeop, control);

    // Liberta a memória alocada para 'actual_play', 'play' e 'actual_play_string'
    free(actual_play);
    free(play);
    free(actual_play_string);
}

int all_equal(int arr[], int size) {
    // Para cada elemento em 'arr'
    for (int i = 0; i < size; i++) {
        // Se o elemento não é igual a 1, retorna 1
        if (arr[i] != 1) {
            return 1;
        }
    }
    // Se todos os elementos em 'arr' são iguais a 1, retorna 0
    return 0;
}

int find_Card(wchar_t *strings[], wchar_t result[]) {
    // Para cada string em 'strings', começando pela última
    int i;
    for (i = 2; i >= 0; i--) {
        // Se a string não é "PASSO"
        if (wcscmp(strings[i], L"PASSO") != 0) {
            // Copia a string para 'result' e retorna o índice da string
            wcscpy(result, strings[i]);
            return i;
        }
    }
    // Se todas as strings em 'strings' são "PASSO", retorna -1
    return -1;
}

void isValid2(wchar_t deck_string[], wchar_t *last_plays[], card deck[], int size) {
    // Inicializa algumas variáveis e ponteiros
    int control[3] = {0};
    int pass;
    wchar_t *result = malloc(MaxInput * sizeof(wchar_t));
    wchar_t *play_string = malloc(MaxInput * sizeof(wchar_t));

    // Encontra a última jogada que não é "PASSO"
    int pos = find_Card(last_plays, result);

    // Para cada jogada em 'last_plays'
    for (int i = 0; i < 3; i++) {
        // Se a jogada é "PASSO", atribui 1 a 'control[i]'. Caso contrário, atribui 0 a 'control[i]'
        if (wcscmp(last_plays[i], L"PASSO") == 0) control[i] = 1;
        else control[i] = 0;
    }

    // Verifica se todas as jogadas em 'last_plays' são "PASSO"
    pass = all_equal(control, 3);
    // Se todas as jogadas em 'last_plays' são "PASSO"
    if (pass == 0) {
        // Lê a próxima jogada para 'play_string' e verifica se 'play_string' é uma jogada válida
        assert(scanf("%ls", play_string) == 1);
        isValid(deck_string, play_string, deck, size);
    } else {
        // Se nem todas as jogadas em 'last_plays' são "PASSO", verifica se 'result' é uma jogada válida
        int size2 = wcslen(last_plays[pos]); 
        card *lastp = malloc(size2 * sizeof(card));
        for (int f = 0; f < size2; f++) {
            lastp[f].nu = valor(result[f]);
            lastp[f].tp = naipe(result[f]);
            lastp[f].code = result[f];
        }
        newDeck(deck_string, size, result, size2, deck); 
        free(lastp); 
    }

    // Liberta a memória alocada para 'play_string' e 'result'
    free(play_string);
    free(result);
}

void readDeck(wchar_t *deck_string, card *deck, int deck_size) {
    // Para cada caractere em 'deck_string'
    for (int h = 0; h < deck_size; h++) {
        // Converte o caractere para um valor numérico e um naipe e armazena-os em 'deck'
        deck[h].nu = valor(deck_string[h]);
        deck[h].tp = naipe(deck_string[h]);
        deck[h].code = deck_string[h];
    }

    // Ordena 'deck'
    qsort(deck, deck_size, sizeof(card), checkCode);
}

void processPlays(wchar_t *deck_string, int plays, card *deck, int deck_size) { 
    // Inicializa algumas variáveis e ponteiros
    wchar_t *control_string = malloc(sizeof(wchar_t) * MaxInput);
    wchar_t *copy_string = malloc(sizeof(wchar_t) * MaxInput);
    int control = 0;

    // Para cada jogada
    for (int f = 0; f < plays; f++) { 
        // Lê a jogada para 'copy_string'
        assert(scanf("%ls", copy_string) == 1);

        // Se a jogada é "PASSO", incrementa 'control'. Caso contrário, copia a jogada para 'control_string'
        if (wcscmp(copy_string, L"PASSO") == 0) { 
            control++;
        } else {
            wcscpy(control_string, copy_string);
        }
    }

    // Se não há jogadas ou todas as jogadas são "PASSO" ou há uma jogada que não é "PASSO"
    if ((plays == 0) || (control == 2 && plays == 0) || (control == 1 && plays == 1)) {
        // Lê a próxima jogada para 'play_string' e verifica se 'play_string' é uma jogada válida
        wchar_t *play_string = malloc(sizeof(wchar_t) * MaxInput);
        assert(scanf("%ls", play_string));
        isValid(deck_string, play_string, deck, deck_size);
        free(play_string);
    } else {
        // Se há mais de uma jogada que não é "PASSO", verifica se 'control_string' é uma jogada válida
        int size2 = wcslen(control_string);
        newDeck(deck_string, deck_size, control_string, size2, deck);
    }

    // Liberta a memória alocada para 'control_string' e 'copy_string'
    free(control_string);
    free(copy_string);
}

void processMaxPlays(wchar_t *deck_string, int plays, wchar_t **last_plays, card *deck, int deck_size) {
    // Aloca memória para 'play_string'
    wchar_t *play_string = malloc(sizeof(wchar_t) * MaxInput);
    int j = 0;

    // Para cada jogada
    for (int h = 0; h < plays; h++) {
        // Lê a jogada para 'play_string'
        assert(scanf("%ls", play_string) == 1);
        // Se a jogada é uma das últimas 'MaxPlays' jogadas, copia a jogada para 'last_plays'
        if (h >= plays - MaxPlays) {
            last_plays[j] = malloc((wcslen(play_string) + 1) * sizeof(wchar_t));
            wcscpy(last_plays[j], play_string);
            j++;
        }
    }

    // Verifica se 'last_plays' é uma jogada válida
    isValid2(deck_string, last_plays, deck, deck_size); 

    // Liberta a memória alocada para 'play_string'
    free(play_string);
}

void processTest(wchar_t *deck_string, int plays, wchar_t **last_plays, int deck_size) {
    // Aloca memória para 'deck' e lê as cartas para 'deck'
    card *deck = malloc(sizeof(card) * deck_size);
    readDeck(deck_string, deck, deck_size);

    // Se o número de jogadas é menor que 'MaxPlays', processa as jogadas. Caso contrário, processa as últimas 'MaxPlays' jogadas
    if (plays < MaxPlays) {
        processPlays(deck_string, plays, deck, deck_size); 
    } else {
        processMaxPlays(deck_string, plays, last_plays, deck, deck_size); 
    }

    // Liberta a memória alocada para 'deck'
    free(deck);
}

int main(void) {
    // Inicializa algumas variáveis e ponteiros
    wchar_t *deck_string = NULL;
    wchar_t **last_plays = malloc(MaxPlays * sizeof(wchar_t*));
    int tests_number, plays;

    // Define o locale do programa para "C.UTF-8"
    setlocale(LC_CTYPE, "C.UTF-8");

    // Lê o número de testes
    if (scanf("%d", &tests_number) == 1) {
        // Verifica se 'tests_number' é maior ou igual a 0
        assert(tests_number >= 0);

        // Aloca memória para 'deck_string'
        deck_string = malloc(sizeof(wchar_t) * MaxInput);
        assert(deck_string != NULL);

        // Para cada teste
        for (int i = 0; i < tests_number; i++) {
            // Imprime o número do teste
            printf("Teste %d\n", i + 1);

            // Lê o número de jogadas
            if (scanf("%d", &plays) == 1) {

                // Lê as cartas para 'deck_string'
                if (scanf("%ls", deck_string) == 1) {
                    int deck_size = wcslen(deck_string);
                    assert(deck_size <= MaxInput);

                    // Processa as jogadas do teste
                    processTest(deck_string, plays, last_plays, deck_size);
                }
            }
        }

        // Liberta a memória alocada para 'deck_string'
        free(deck_string);
    }

    // Liberta a memória alocada para 'last_plays'
    for (int i = 0; i < MaxPlays; i++) {
        free(last_plays[i]);
    }
    free(last_plays);

    // Retorna 0
    return 0;
}