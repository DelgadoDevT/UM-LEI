#include "metaindex.h"
#include <stdio.h>

//For exercise 2.2 see man pages for
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

	//Useful for exercise 2.2
	//mutex variable initialization
	//int pthread_mutex_init(pthread_mutex_t *mutex,const pthread_mutexattr_t *attr);
	//attr can be used to define non-default attributes (e.g., recursive lock)
	pthread_mutex_init(&index->mutex, NULL);

	//Useful for exercise 2.2
	//condition variable initialization
	//int pthread_cond_init(pthread_cond_t *cond, const pthread_condattr_t *attr);
	//attr can be used to define non-default attributes (e.g., recursive lock)
	pthread_cond_init(&index->cond, NULL);
	return index;

}

//Note: remember that memory allocation and copying must be done here
int index_add(Index *index, char* filename, Filemeta meta){

	int res = -1;

	char *key = strdup(filename);
	if (key == NULL) return res;

	Filemeta *value =  malloc (sizeof(Filemeta));
	if (value == NULL) return res;

	value->content=malloc(CONTENT_SIZE);
	value->size = meta.size;
	value->refs = meta.refs;
	memcpy(value->content, meta.content, CONTENT_SIZE);

	pthread_mutex_lock(&index->mutex);
	if (g_hash_table_insert(index->htable, key, value) == 1)
		res = 0;
	pthread_mutex_unlock(&index->mutex);

	// NEW code to write content to file
	int fd = open(key, O_CREAT | O_TRUNC | O_WRONLY, 0600);
	write(fd, meta.content, CONTENT_SIZE);
	close(fd);

	return res;
}

int index_get(Index *index, char* filename, Filemeta *meta){

	int res = -1;

	pthread_mutex_lock(&index->mutex);
	Filemeta *value = g_hash_table_lookup(index->htable, filename);
	if (value != NULL){

		res = 0;
	
		meta->size = value->size;
		meta->refs = value->refs;
		memcpy(meta->content, value->content, 20);
	}

	pthread_mutex_unlock(&index->mutex);

	return res;

}

int index_increfs(Index *index, char* filename){

	int res = -1;

	pthread_mutex_lock(&index->mutex);
	Filemeta *value = g_hash_table_lookup(index->htable, filename);
	if (value != NULL) {
		value->refs++;
		res = value->refs;
		pthread_cond_broadcast(&index->cond);
	}
	pthread_mutex_unlock(&index->mutex);

	return res;
}


//Exercise 2.2
int index_wait_refs(Index* index, char* filename, int nrefs){

	Filemeta *value;
	int res=0;

	pthread_mutex_lock(&index->mutex);

	value = g_hash_table_lookup(index->htable, filename);
	
	while (value!=NULL && value->refs < nrefs)
		pthread_cond_wait( &index->cond, &index->mutex);

	if(value==NULL) res=-1;
	
	pthread_mutex_unlock(&index->mutex);

	return res;
}


int index_remove(Index* index, char* filename){
	int res = -1;

	pthread_mutex_lock(&index->mutex);
	Filemeta *value = g_hash_table_lookup(index->htable, filename);
	if (value != NULL) {
		if (g_hash_table_remove(index->htable, filename) == 1)
			res = 0;
		free(value->content);
	}
	pthread_mutex_unlock(&index->mutex);

	return res;
}

void index_destroy(Index* index){

	//destroy hashtable
	g_hash_table_destroy(index->htable);

	//Useful for exercise 2.2
	//destroy mutex and cond variables
	pthread_mutex_destroy(&index->mutex);
    pthread_cond_destroy(&index->cond);

	free(index);
	
	
}
