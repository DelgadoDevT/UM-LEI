/* task4.c */
suit naipe(wchar_t card);
value valor(wchar_t card);
int factorial(int n);
int comb(int n, int k);
int checkCode(const void *a, const void *b);
combination setOfCards(card cards[], int size);
combination sequence(card cards[], int size);
combination doubleSequence(card cards[], int size);
int check_combination(card temp[], int size);
void strToCards(wchar_t last_string[], wchar_t last_plays[], card last[], card play[], int size1, int size2);
int countLines(card play[], int size2);
void fillColumn(card play[], int column[], int size2);
void fillPack(card packs[][4], int lines, card play[], int column[]);
int compareCards(card hand1[], card hand2[], int size, int size2);
int all_King(card play[], int size);
int kingException(card last[], int size1);
void printCombs(card far[], int sizec);
void combSets(card cartas[], card far[], int sizec, int N, card last[], int size1, int *control);
void printSets(card last[], int size1, card packs[][4], int lines, int column[]);
int printSetsKing(card last[], int size1, card packs[][4], int lines, int column[]);
void generateSequences(card packs[][4], int nLines, int *nColumns, card *sequence, int index, int size1, int startLine, int startCol, int used[], card last[], int *control);
void printSequences(card last[], int size1, card packs[][4], int lines, int column[]);
int printSequencesKing(card last[], int size1, card packs[][4], int lines, int column[]);
int seeDouble(card sequence[], int size1);
void generateDoubleSequences(card cards[][4], int numLines, int *numColumns, card *sequence, int index, int sequenceSize, int startLine, int startCol, int used[], card last[], int *control, int check);
void printDoubleSequences(card last[], int size1, card packs[][4], int lines, int column[], int check);
int fourEqual(card packs[][4], int lines, int column[]);
void printKings(card last[], card packs[][4], int lines, int column[], int king, int pass);
void printSingle(card last[], int size1, card packs[][4], int lines, int column[]);
int printSingleKing(card last[], int size1, card packs[][4], int lines, int column[]);
void linkPrints(card last[], int size1, card packs[][4], int lines, int column[], int type);
void process(wchar_t last_string[], wchar_t last_plays[]);
int main(void);