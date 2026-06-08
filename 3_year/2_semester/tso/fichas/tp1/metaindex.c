#include "metaindex.h"
#include <stdio.h>

//For exercise 1.3 see man pages for
//int pthread_mutex_lock(pthread_mutex_t *mutex)
//int pthread_mutex_unlock(pthread_mutex_t *mutex);
//int pthread_cond_wait(pthread_cond_t *cond, pthread_mutex_t *mutex);
//int pthread_cond_signal(pthread_cond_t *cond);
//int pthread_cond_broadcast(pthread_cond_t *cond);

Index* index_init(){

	//allocate memory for the index
	Index *index = malloc(sizeof(Index));

	//GHashTable initialization
	index->htable = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, g_free);

	//Useful for exercise 1.3
	//mutex variable initialization
	//int pthread_mutex_init(pthread_mutex_t *mutex,const pthread_mutexattr_t *attr);
	//attr can be used to define non-default attributes (e.g., recursive lock)
	pthread_mutex_init(&index->mutex, NULL);

	//Useful for exercise 1.3
	//condition variable initialization
	//int pthread_cond_init(pthread_cond_t *cond, const pthread_condattr_t *attr);
	//attr can be used to define non-default attributes (e.g., recursive lock)
	pthread_cond_init(&index->cond, NULL);
	return index;

}

//Note: remember that memory allocation and copying must be done here
int index_add(Index *index, char* filename, Filemeta meta){
	int rv = 0;
	GHashTable *g = index->htable;

	Filemeta *fm = malloc(sizeof(Filemeta));
	fm->size = meta.size;
	fm->refs = meta.refs;
	fm->content = meta.content;

	pthread_mutex_lock(&index->mutex);
	if (!g_hash_table_insert(g, strdup(filename), fm))
		rv = -1;
	pthread_mutex_unlock(&index->mutex);

	return rv;
}

int index_get(Index *index, char* filename, Filemeta *meta){
	int rv = 0;

	pthread_mutex_lock(&index->mutex);
	Filemeta *fm = (Filemeta*)g_hash_table_lookup(index->htable, filename);
	if (fm == NULL) {
		rv = -1;
	} else {
		*meta = *fm;
	}
	pthread_mutex_unlock(&index->mutex);

	return rv;
}

int index_increfs(Index *index, char* filename){
	int rv = 0;

	pthread_mutex_lock(&index->mutex);
	Filemeta *fm = g_hash_table_lookup(index->htable, filename);
	if (fm == NULL)
		rv = -1;
	else {
		fm->refs += 1;
		rv = fm->refs;
		pthread_cond_signal(&index->cond);
	}
	pthread_mutex_unlock(&index->mutex);

	return rv;
}


//Exercise 1.3
int index_wait_refs(Index* index, char* filename, int nrefs) {
	pthread_mutex_lock(&index->mutex);
	Filemeta *fm = g_hash_table_lookup(index->htable, filename);
	while (fm != NULL && fm->refs < nrefs) {
		pthread_cond_wait(&index->cond, &index->mutex);
		fm = g_hash_table_lookup(index->htable, filename);
	}
	pthread_mutex_unlock(&index->mutex);	
		
	return (fm != NULL) ? 0 : - 1;
}


int index_remove(Index* index, char* filename){
	int rv = 0;

	pthread_mutex_lock(&index->mutex);
	if (!g_hash_table_remove(index->htable, filename))
		rv = -1;
	pthread_cond_signal(&index->cond);
	pthread_mutex_unlock(&index->mutex);

	return rv;
}

void index_destroy(Index* index){

	//destroy hashtable
	g_hash_table_destroy(index->htable);

	//Useful for exercice 1.3
	//destroy mutex and cond variables
	pthread_mutex_destroy(&index->mutex);
    pthread_cond_destroy(&index->cond);

	free(index);
}

// Exercise extra 3
int index_multi_inc_refs(Index* index, char** filenames, int numFiles) {
	int rv = 0;

	pthread_mutex_lock(&index->mutex);
	for (int i = 0; i < numFiles; i++) {
		Filemeta *fm = g_hash_table_lookup(index->htable, filenames[i]);
		if (fm == NULL) {
			rv = -1;
			break;
		}
	}

	if (rv == 0) {
		for (int i = 0; i < numFiles; i++) {
			Filemeta *fm = g_hash_table_lookup(index->htable, filenames[i]);
			fm->refs += 1;
		}
		pthread_cond_broadcast(&index->cond);
	}
	
	pthread_mutex_unlock(&index->mutex);
	
	return rv;
}