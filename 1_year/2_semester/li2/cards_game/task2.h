/* task2.c */
suit naipe(wchar_t card);
value valor(wchar_t card);
combination setOfCards(card cards[], int size);
combination sequence(card cards[], int size);
combination doubleSequence(card cards[], int size);
int checkCode(const void *a, const void *b);
int check_combination(card temp[], int size);
void copy_array(card cards[], wchar_t input[], int copy_size, int size);
int all_equal(int control[], int n);
void create_last(card cards[], order_cards last_cards[], int tests, int skip);
void show_sequence(card cards[], int copy_size, int tests);
void process(int test_number, int tests, card *cards, int *line_counter, int *control);
void process_input(int lines);
int main(void);
