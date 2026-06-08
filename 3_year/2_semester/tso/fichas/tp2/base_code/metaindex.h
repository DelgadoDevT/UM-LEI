#include <glib.h>
#include <stdint.h>
#include <pthread.h>
#include <unistd.h>
#include <fcntl.h>


//Size of keys at the Hashtable
#define KEY_SIZE 100
#define CONTENT_SIZE 10*1024*1024


//struct defining the values of the Hashtable
//NEW content field!
typedef struct filemeta {
	uint64_t size;
	int refs;
	char *content;
} Filemeta;

//structure containing the hashtable structure, global mutex and condition variable
typedef struct index {
	GHashTable *htable;
	pthread_mutex_t mutex;
	pthread_cond_t cond;
} Index;



//Initializes the index structure
//Returns NULL in case of failure and a pointer to the struct otherwise
Index* index_init();

//Adds a new key-value entry (filename-meta) into the Hashtable
//Returns -1 in case of failure (or if the key already exists) and 0 otherwise
int index_add(Index *index, char* filename, Filemeta meta);

//Get the value (meta) for a specific key (filename)
//Returns -1 in case of failure (or if the key does not exists) and 0 otherwise
int index_get(Index *index, char* filename, Filemeta *meta);

//Increment by 1 the number of references for a given key (filename)
//Returns -1 in case of failure (or if the key does not exists)
//and the updated number of references otherwise
int index_increfs(Index *index, char* filename);

//Remove a key-value entry from the Hashtable
//Returns -1 in case of failure (or if the key does not exists) and 0 otherwise
int index_remove(Index* index, char* filename);

//Destroys the index structure
void index_destroy(Index* index);

//Exercice 2.2
//Blocks until the number fo references of a key (filename) reaches a given thredshold (nrefs)
//Returns -1 in case of failure (or if the key does not exists) and 0 otherwise
int index_wait_refs(Index* index, char* filename, int nrefs);

