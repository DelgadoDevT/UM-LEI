#include <stdio.h>
#include <wchar.h>
#include <assert.h>
#include <string.h>                                                                             
#include <locale.h>
#include <stdlib.h>

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

int factorial(int n) {
    int result = 1;
    
    for (int i = 1; i <= n; ++i) {
        result *= i;
    }
    return result;
}

int comb(int n, int k) {
    return factorial(n) / (factorial(k) * factorial(n - k));
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

void crdcpy(card *dest, card *src, int n) {
    for (int i = 0; i < n; ++i) {
        dest[i].tp = src[i].tp;
        dest[i].nu = src[i].nu;
        dest[i].code = src[i].code;
    }
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

void strToCards(wchar_t last_string[], wchar_t last_plays[], card last[], card play[], int size1, int size2) {
    // Converte 'last_string' e 'last_plays' para arrays de estruturas 'card'
    // Percorre cada caractere em 'last_string' e 'last_plays', converte o caractere para um valor numérico e um naipe, e armazena-os em 'last' e 'play', respectivamente
    for (int i = 0; i < size1; i++) {
        last[i].nu = valor(last_string[i]);
        last[i].tp = naipe(last_string[i]);
        last[i].code = last_string[i];
    }

    for (int h = 0; h < size2; h++) {
        play[h].nu = valor(last_plays[h]);
        play[h].tp = naipe(last_plays[h]);
        play[h].code = last_plays[h];
    }

    // Ordena 'last' e 'play' usando a função 'checkCode' como função de comparação
    qsort(last, size1, sizeof(card), checkCode);
    qsort(play, size2, sizeof(card), checkCode);
}

int countLines(card play[], int size2) {
    // Conta o número de linhas em 'play'
    // Se 'size2' é menor ou igual a 1, retorna 'size2'
    if (size2 <= 1) return size2;
    int counter = 1;
    
    // Percorre 'play' e incrementa 'counter' sempre que encontra uma carta com um número diferente da carta anterior
    for (int i = 0; i < size2 - 1; i++) {
        if (play[i].nu != play[i + 1].nu) counter++;
    }
    
    return counter;
}

void fillColumn(card play[], int column[], int size2) {
    // Preenche 'column' com a contagem de cada número em 'play'
    int counter = 1;
    int column_number = 0;

    // Percorre 'play' e incrementa 'counter' sempre que encontra uma carta com o mesmo número da carta anterior
    // Quando encontra uma carta com um número diferente da carta anterior, armazena 'counter' em 'column' e reinicia 'counter'
    for (int i = 0; i < size2 - 1; i++) {          
        if (play[i].nu != play[i + 1].nu) {
            column[column_number] = counter;
            column_number++;
            counter = 1;
        } else counter++;
    }

    column[column_number] = counter;
}

void fillPack(card packs[][4], int lines, card play[], int column[]) { 
    // Preenche 'packs' com as cartas de 'play'
    int pos = 0;

    // Percorre cada linha de 'packs' e preenche a linha com as cartas correspondentes de 'play'
    for (int i = 0; i < lines; i++) {
        for (int k = 0; k < column[i]; k++) {
            packs[i][k] = play[pos];
            pos++;
        }
    }
}

int compareCards(card hand1[], card hand2[], int size, int size2) {
    // Compara as últimas cartas de 'hand1' e 'hand2'
    int last = size - 1;
    int last2 = size2 - 1;

    // Se o número da última carta de 'hand1' é maior que o número da última carta de 'hand2', retorna 1
    if (hand1[last].nu > hand2[last2].nu) {
        return 1;
    } 
    // Se o número da última carta de 'hand1' é menor que o número da última carta de 'hand2', retorna 0
    else if (hand1[last].nu < hand2[last2].nu) { 
        return 0;
    } 
    // Se o naipe da última carta de 'hand1' é maior que o naipe da última carta de 'hand2', retorna 1
    else if (hand1[last].tp > hand2[last2].tp) {
        return 1;
    } 
    // Se o naipe da última carta de 'hand1' é menor que o naipe da última carta de 'hand2', retorna 0
    else if (hand1[last].tp < hand2[last2].tp) {
        return 0;
    }

    // Se as últimas cartas de 'hand1' e 'hand2' são iguais, retorna 2
    return 2;
}

int all_King(card play[], int size) {
    // Verifica se todas as cartas em 'play' são reis
    for (int i = 0; i < size; i++) {
        if (play[i].nu != king) return 1;
    }

    return 0;
}

int kingException(card last[], int size1) {
    // Verifica se as últimas cartas de 'last' são reis
    if (last[size1 - 1].nu == king) {
        if (last[size1 - 2].nu == king) {
            if (last[size1 - 3].nu == king) {
                if (last[size1 - 4].nu == king) return 4;
                else return 2;;
            } else return 1;
        } else {
            return 0;
        }
    }

    return 3;
}  

void printCombs(card far[], int sizec) {
    // Imprime as cartas em 'far'
    for (int i = 0; i < sizec; i++) {
        if (i == (sizec - 1)) {
            printf("%lc\n", far[i].code);
        } else {
            printf("%lc ", far[i].code);
        }
    }
}

void combSets(card cartas[], card far[], int sizec, int N, card last[], int size1, int *control, card result[], int *sizer) {
    // Gera todas as combinações possíveis de 'N' cartas de 'cartas' e verifica se cada combinação é válida
    if (N == 0) {
        if (compareCards(last, far, size1, size1) == 0) {
            *control = 1;
            *sizer = size1;
            crdcpy(result, far, size1);
        }
        return;
    }

    int index = size1 - N; 

    for (int i = 0; i <= sizec - N; i++) {
        far[index] = cartas[i];
        combSets(cartas + i + 1, far, sizec - i - 1, N - 1, last, size1, control, result, sizer);
    }
}

void printSets(card last[], int size1, card packs[][4], int lines, int column[], card result[], int *sizer) {
    // Imprime todos os conjuntos válidos de cartas de 'packs'
    int N, sizec, control = 0;
    card *line = NULL;
    N = size1;

    for (int i = 0; i < lines; i++) {
        sizec = column[i];
        card far[sizec];
        line = malloc(sizeof(card) * sizec);
        for (int h = 0; h < sizec; h++) {
            line[h] = packs[i][h];
        }
        combSets(line, far, sizec, N, last, size1, &control, result, sizer);
        free(line);
    }

    if (control == 0) printf("PASSO\n");
}

int printSetsKing(card last[], int size1, card packs[][4], int lines, int column[], card result[], int *sizer) {
    // Imprime todos os conjuntos válidos de cartas de 'packs' e retorna se algum conjunto válido foi impresso
    int N, sizec, control = 0;
    card *line = NULL;
    N = size1;

    for (int i = 0; i < lines; i++) {
        sizec = column[i];
        card far[sizec];
        line = malloc(sizeof(card) * sizec);
        for (int h = 0; h < sizec; h++) {
            line[h] = packs[i][h];
        }
        combSets(line, far, sizec, N, last, size1, &control, result, sizer);
        free(line);
    }

    return control;
}

void generateSequences(card packs[][4], int nLines, int *nColumns, card *sequence, int index, int size1, int startLine, int startCol, int used[], card last[], int *control, card result[], int *sizer) {
    // Gera todas as sequências possíveis de cartas de 'packs' e verifica se cada sequência é válida
    // Se 'index' é igual a 'size1', verifica se a sequência atual é válida e, em caso afirmativo, imprime a sequência
    if (index == size1) {
        int valid = 1;
        for (int i = 1; i < size1; i++) {
            if (sequence[i].nu != sequence[i - 1].nu + 1) {
                valid = 0;
                break;
            }
        }
        if (valid && compareCards(last, sequence, size1, size1) == 0) {
            *control = 1;
            *sizer = size1;
            crdcpy(result, sequence, size1);
        }
        return;
    }

    // Para cada linha de 'packs', e para cada coluna de cada linha, se a linha não foi usada e a linha é maior que 'startLine' ou a linha é igual a 'startLine' e a coluna é maior ou igual a 'startCol', adiciona a carta à sequência, marca a linha como usada, gera as sequências restantes, e marca a linha como não usada
    for (int line = startLine; line < nLines; line++) {
        int colStart = (line == startLine) ? startCol : 0; 
        for (int col = colStart; col < nColumns[line]; col++) {
            if (!used[line]) {
                sequence[index] = packs[line][col];
                used[line] = 1;
                generateSequences(packs, nLines, nColumns, sequence, index + 1, size1, line, col + 1, used, last, control, result, sizer);
                used[line] = 0;
            }
        }
    }
}

void printSequences(card last[], int size1, card packs[][4], int lines, int column[], card result[], int *sizer) {
    // Imprime todas as sequências válidas de cartas de 'packs'
    // Inicializa 'control' com 0, 'seq' com um array de cartas, e 'used' com um array de zeros
    int control = 0;

    card seq[lines * 4];
    int used[lines];
    for (int i = 0; i < lines; i++) {
        used[i] = 0;
    }

    // Gera todas as sequências de cartas de 'packs' e imprime as sequências válidas
    generateSequences(packs, lines, column, seq, 0, size1, 0, 0, used, last, &control, result, sizer);

    // Se nenhuma sequência válida foi impressa, imprime "PASSO"
    if (control == 0) printf("PASSO\n");
}

int printSequencesKing(card last[], int size1, card packs[][4], int lines, int column[], card result[], int *sizer) {
    // Imprime todas as sequências válidas de cartas de 'packs' e retorna se alguma sequência válida foi impressa
    // Inicializa 'control' com 0, 'seq' com um array de cartas, e 'used' com um array de zeros
    int control = 0;

    card seq[lines * 4];
    int used[lines];
    for (int i = 0; i < lines; i++) {
        used[i] = 0;
    }

    // Gera todas as sequências de cartas de 'packs' e imprime as sequências válidas
    generateSequences(packs, lines, column, seq, 0, size1, 0, 0, used, last, &control, result, sizer);

    // Retorna se alguma sequência válida foi impressa
    return control;
}

int seeDouble(card sequence[], int size1) {
    // Verifica se 'sequence' contém uma sequência dupla
    // Para cada par de cartas em 'sequence', se o número da segunda carta não é igual ao número da primeira carta mais 1, retorna 1
    for (int i = 0; i < size1 - 2; i += 2) {
        if (sequence[i].nu != sequence[i + 2].nu - 1) {
            return 1;
        }
    }
    // Se todas as cartas em 'sequence' formam uma sequência dupla, retorna 0
    return 0;
}

void generateDoubleSequences(card cards[][4], int numLines, int *numColumns, card *sequence, int index, int sequenceSize, int startLine, int startCol, int used[], card last[], int *control, int check, card result[], int *sizer) {
    // Gera todas as sequências duplas possíveis de cartas de 'cards' e verifica se cada sequência dupla é válida
    // Se 'index' é igual a 'sequenceSize', verifica se a sequência dupla atual é válida e, em caso afirmativo, imprime a sequência dupla
    if (index == sequenceSize) {
        if (((seeDouble(sequence, sequenceSize) == 0) && (check == 0 || check == 2)) || ((seeDouble(sequence, sequenceSize) == 0) && (compareCards(last, sequence, sequenceSize, sequenceSize) == 0))) {
            *control = 1;
            *sizer = sequenceSize;
            crdcpy(result, sequence, sequenceSize);
        }
        return;
    }

    // Para cada linha de 'cards', e para cada par de colunas de cada linha, se a linha não foi usada e a linha é maior que 'startLine' ou a linha é igual a 'startLine' e a primeira coluna é maior ou igual a 'startCol', adiciona as cartas à sequência dupla, marca a linha como usada, gera as sequências duplas restantes, e marca a linha como não usada
    for (int line = startLine; line < numLines; line++) {
        for (int col1 = 0; col1 < numColumns[line]; col1++) {
            for (int col2 = col1 + 1; col2 < numColumns[line]; col2++) {
                if (!used[line] && (line > startLine || (line == startLine && col1 >= startCol))) {
                    sequence[index] = cards[line][col1];
                    sequence[index + 1] = cards[line][col2];
                    used[line] = 1;
                    generateDoubleSequences(cards, numLines, numColumns, sequence, index + 2, sequenceSize, line, col1 + 1, used, last, control, check, result, sizer);
                    used[line] = 0;
                }
            }
        }
    }
}

void printDoubleSequences(card last[], int size1, card packs[][4], int lines, int column[], int check, card result[], int *sizer) {
    // Imprime todas as sequências duplas válidas de cartas de 'packs'
    // Inicializa 'control' com 0, 'seq' com um array de cartas, e 'used' com um array de zeros
    int control = 0;

    card seq[lines * 2];
    int used[lines];
    for (int i = 0; i < lines; i++) {
        used[i] = 0;
    }

    // Gera todas as sequências duplas de cartas de 'packs' e imprime as sequências duplas válidas
    generateDoubleSequences(packs, lines, column, seq, 0, size1, 0, 0, used, last, &control, check, result, sizer);

    // Se 'check' é igual a 2, atribui 1 a 'control'
    if (check == 2) control = 1;
    // Se nenhuma sequência dupla válida foi impressa, imprime "PASSO"
    if (control == 0) printf("PASSO\n");
}

int fourEqual(card packs[][4], int lines, int column[]) {
    // Verifica se há quatro cartas iguais em 'packs'
    int sizec = 4;
    int var = 0;
    card far[sizec];

    // Para cada linha de 'packs'
    for (int i = 0; i < lines; i++) {
        // Se a linha contém quatro cartas
        if (column[i] == sizec) { 
            // Copia as cartas para 'far'
            for (int k = 0; k < sizec; k++) {
                far[k] = packs[i][k];
            }
            // Imprime as cartas em 'far'
            printCombs(far, sizec);
            // Atribui 1 a 'var'
            var = 1;
        }
    }

    // Retorna 'var'
    return var;
}

void printKings(card last[], card packs[][4], int lines, int column[], int king, int pass, card result[], int *sizer) {
    // Inicializa algumas variáveis
    int size1, var;
    int check;
    // Se 'pass' é igual a 1, atribui 2 a 'check'. Caso contrário, atribui 0 a 'check'
    if (pass == 1) check = 2; else check = 0;
    
    // Se 'king' é igual a 2
    if (king == 2) {
        // Atribui 10 a 'size1' e imprime as sequências duplas válidas de cartas de 'packs'
        size1 = 10;
        printDoubleSequences(last, size1, packs, lines, column, check, result, sizer);
    }

    // Se 'king' é igual a 1
    if (king == 1) {
        // Atribui 8 a 'size1' e imprime as sequências duplas válidas de cartas de 'packs'
        size1 = 8;
        printDoubleSequences(last, size1, packs, lines, column, check, result, sizer);
    }

    // Se 'king' é igual a 0
    if (king == 0) {
        // Atribui 4 a 'size1' e verifica se há quatro cartas iguais em 'packs'
        size1 = 4;
        var = fourEqual(packs, lines, column);
        // Atribui 6 a 'size1'
        size1 = 6;
        // Se não há quatro cartas iguais em 'packs', imprime as sequências duplas válidas de cartas de 'packs'
        // Caso contrário, imprime as sequências duplas válidas de cartas de 'packs' e atribui 2 a 'check'
        if (var == 0) {
            printDoubleSequences(last, size1, packs, lines, column, check, result, sizer);
        } else { 
            printDoubleSequences(last, size1, packs, lines, column, 2, result, sizer);
        }
    }
}

void printSingle(card last[], int size1, card packs[][4], int lines, int column[], card result[], int *sizer) {
    // Inicializa algumas variáveis
    card play[1];
    int control = 0;

    // Para cada linha de 'packs', e para cada coluna de cada linha
    for (int i = 0; i < lines; i++) {
        for (int k = 0; k < column[i]; k++) {
            // Atribui a carta da linha e coluna atuais a 'play[0]'
            play[0] = packs[i][k];
            // Se a carta em 'play' é maior que a última carta de 'last'
            if (compareCards(last, play, size1, 1) == 0) {
                // Imprime o código da carta e atribui 1 a 'control'
                *sizer = 1;
                result[0] = packs[i][k];
                control = 1;
            }
        }
    }

    // Se nenhuma carta válida foi impressa, imprime "PASSO"
    if (control == 0) printf("PASSO\n");
}

int printSingleKing(card last[], int size1, card packs[][4], int lines, int column[], card result[], int *sizer) {
    // Inicializa algumas variáveis
    card play[1];
    int control = 0;

    // Para cada linha de 'packs', e para cada coluna de cada linha
    for (int i = 0; i < lines; i++) {
        for (int k = 0; k < column[i]; k++) {
            // Atribui a carta da linha e coluna atuais a 'play[0]'
            play[0] = packs[i][k];
            // Se a carta em 'play' é maior que a última carta de 'last'
            if (compareCards(last, play, size1, 1) == 0) {
                // Imprime o código da carta e atribui 1 a 'control'
                *sizer = 1;
                result[0] = packs[i][k];
                control = 1;
            }
        }
    }

    // Retorna se alguma carta válida foi impressa
    return control;
}

int isCard(card result[], int sizer) {
    for (int i = 0; i < sizer; i++) {
        if (result[i].tp > 4 || result[i].tp < 1) return 0;
    }

    return 1;
}

void linkPrints(card last[], int size1, card packs[][4], int lines, int column[], int type) {
    // Inicializa 'pass' com 0
    int pass = 0;
    card *result = malloc(sizeof(card) * MaxInput);
    int sizer;

    // Se todas as cartas em 'last' são reis
    if (all_King(last, size1) == 0) {
        // Verifica se as últimas cartas de 'last' são reis
        int king = kingException(last, size1);
        // Se todas as últimas cartas de 'last' são reis, imprime "PASSO" e retorna
        if (king == 4) {
           printf("PASSO\n");
           return; 
        } 
        // Se nem todas as últimas cartas de 'last' são reis
        if (king < 3) {
            // Dependendo de 'type', imprime os conjuntos, as sequências ou as cartas únicas válidas de 'packs'
            switch (type) {
                case 1:
                    pass = printSetsKing(last, size1, packs, lines, column, result, &sizer);
                    break;
                case 2: 
                    pass = printSequencesKing(last, size1, packs, lines, column, result, &sizer); 
                    break;
                case 0:     
                    pass = printSingleKing(last, size1, packs, lines, column, result, &sizer);
                    break;
            }
            // Imprime as cartas de 'packs' que são reis
            printKings(last, packs, lines, column, king, pass, result, &sizer);
        }
    } else {
        // Se nem todas as cartas em 'last' são reis, dependendo de 'type', imprime os conjuntos, as sequências, as sequências duplas ou as cartas únicas válidas de 'packs'
        switch (type) {
            case 1:
                printSets(last, size1, packs, lines, column, result, &sizer);
                break;
            case 2: 
                printSequences(last, size1, packs, lines, column, result, &sizer); 
                break;
            case 3: 
                printDoubleSequences(last, size1, packs, lines, column, 1, result, &sizer); 
                break;
            case 0:     
                printSingle(last, size1, packs, lines, column, result, &sizer);
                break;
        }
    }
    if(!isCard(result, sizer)) return;
    printCombs(result, sizer);
    free(result);
}

void processComb(wchar_t last_string[], wchar_t last_plays[]) {
    // Inicializa alguns ponteiros
    card *last = NULL;
    card *play = NULL;
    // Calcula o tamanho de 'last_string' e 'last_plays'
    int size1, size2, lines, type;

    size1 = wcslen(last_string);
    size2 = wcslen(last_plays);
    // Aloca memória para 'last' e 'play'
    last = malloc(sizeof(card) * size1);
    play = malloc(sizeof(card) * size2);

    // Converte 'last_string' e 'last_plays' para arrays de estruturas 'card'
    strToCards(last_string, last_plays, last, play, size1, size2);
    // Conta o número de linhas em 'play'
    lines = countLines(play, size2);
    // Inicializa 'packs' e 'column'
    card packs[lines][4];
    int column[lines];
    // Preenche 'column' com a contagem de cada número em 'play'
    fillColumn(play, column, size2);
    // Preenche 'packs' com as cartas de 'play'
    fillPack(packs, lines, play, column);
    // Verifica a combinação de 'last'
    type = check_combination(last, size1);

    // Imprime os conjuntos, as sequências, as sequências duplas ou as cartas únicas válidas de 'packs', dependendo de 'type'
    linkPrints(last, size1, packs, lines, column, type);

    // Liberta a memória alocada para 'last' e 'play'
    free(last);
    free(play); 
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

void isValid2(wchar_t deck_string[], wchar_t *last_plays[]) {
    // Inicializa algumas variáveis e ponteiros
    int control[3] = {0};
    int pass;
    wchar_t *result = malloc(MaxInput * sizeof(wchar_t));

    // Para cada jogada em 'last_plays'
    for (int i = 0; i < 3; i++) {
        // Se a jogada é "PASSO", atribui 1 a 'control[i]'. Caso contrário, atribui 0 a 'control[i]'
        if (wcscmp(last_plays[i], L"PASSO") == 0) control[i] = 1;
        else control[i] = 0;
    }

    for (int l = 0; l < 3; l++) {
        if (control[l] == 0) wcscpy(result, last_plays[l]);
    }

    // Verifica se todas as jogadas em 'last_plays' são "PASSO"
    pass = all_equal(control, 3);
    // Se todas as jogadas em 'last_plays' são "PASSO"
    if (pass == 0) {
        // Lê a próxima jogada para 'play_string' e verifica se 'play_string' é uma jogada válida
        wcscpy(result, L"PASSO");
        processComb(result, deck_string);
    } else {
        // Se nem todas as jogadas em 'last_plays' são "PASSO", verifica se 'result' é uma jogada válida
        processComb(result, deck_string);
    }
}

void processPlays(wchar_t *deck_string, int plays) { 
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
        wcscpy(control_string, L"PASSO");
        processComb(control_string, deck_string);
    } else {
        // Se há mais de uma jogada que não é "PASSO", verifica se 'control_string' é uma jogada válida
        processComb(control_string, deck_string);
    }

    // Liberta a memória alocada para 'control_string' e 'copy_string'
    free(control_string);
    free(copy_string);
}

void processMaxPlays(wchar_t *deck_string, int plays, wchar_t **last_plays) {
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
    isValid2(deck_string, last_plays); 

    // Liberta a memória alocada para 'play_string'
    free(play_string);
}

void processTest(wchar_t *deck_string, int plays, wchar_t **last_plays) {
    // Se o número de jogadas é menor que 'MaxPlays', processa as jogadas. Caso contrário, processa as últimas 'MaxPlays' jogadas
    if (plays < MaxPlays) {
        processPlays(deck_string, plays); 
    } else {
        processMaxPlays(deck_string, plays, last_plays); 
    }
}

int main(void) {
    // Inicializa algumas variáveis e ponteiros
    wchar_t *deck_string = NULL;
    wchar_t **last_plays = malloc(MaxPlays * sizeof(wchar_t*));
    int plays;

    // Define o locale do programa para "C.UTF-8"
    setlocale(LC_CTYPE, "C.UTF-8");

    deck_string = malloc(sizeof(wchar_t) * MaxInput);
    assert(deck_string != NULL);
    
    // Lê o número de jogadas
    if (scanf("%d", &plays) == 1) {
        // Lê as cartas para 'deck_string'
        if (scanf("%ls", deck_string) == 1) {

            // Processa as jogadas do teste
            processTest(deck_string, plays, last_plays);
        }
    }

    // Liberta a memória alocada para 'deck_string'
    free(deck_string);
    // Liberta a memória alocada para 'last_plays'
    for (int i = 0; i < MaxPlays; i++) free(last_plays[i]);
    free(last_plays);

    // Retorna 0
    return 0;
}