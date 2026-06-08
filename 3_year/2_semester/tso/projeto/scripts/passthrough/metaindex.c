#include "metaindex.h"
#include <stdio.h>

Index* index_init(){

	//allocate memory for the index
	Index *index = malloc(sizeof(Index));

	//GHashTable initialization
	index->htable = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, g_free);

	//mutex variable initialization
	//int pthread_mutex_init(pthread_mutex_t *mutex,const pthread_mutexattr_t *attr);
	//attr can be used to define non-default attributes (e.g., recursive lock)
	pthread_mutex_init(&index->mutex, NULL);

	return index;

}

//Note: remember that memory allocation and copying must be done here
int index_add(Index *index, char* key, Filemeta meta) {

	int res = -1;

	char *nkey = strdup(key);
	if (nkey == NULL) return res;

	Filemeta *value =  malloc (sizeof(Filemeta));
	if (value == NULL) return res;

	value->real_size = meta.real_size;
	value->compressed_size = meta.compressed_size;

	pthread_mutex_lock(&index->mutex);
	if (g_hash_table_insert(index->htable, nkey, value) == 1)
		res = 0;
	pthread_mutex_unlock(&index->mutex);

	return res;
}

int index_get(Index *index, char* key, Filemeta *meta){

	int res = -1;

	pthread_mutex_lock(&index->mutex);
	Filemeta *value = g_hash_table_lookup(index->htable, key);
	if (value != NULL){
		res = 0;
		meta->real_size = value->real_size;
		meta->compressed_size = value->compressed_size;
	}
	pthread_mutex_unlock(&index->mutex);

	return res;
}

int index_remove(Index* index, char* key){

	int res = -1;

	pthread_mutex_lock(&index->mutex);
	Filemeta *value = g_hash_table_lookup(index->htable, key);
	if (value != NULL) {
		if (g_hash_table_remove(index->htable, key) == 1)
			res = 0;
	}
	pthread_mutex_unlock(&index->mutex);

	return res;
}

void index_destroy(Index* index){

	//destroy hashtable
	g_hash_table_destroy(index->htable);

	//destroy mutex and cond variables
	pthread_mutex_destroy(&index->mutex);

	free(index);
}