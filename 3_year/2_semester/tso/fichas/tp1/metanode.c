#include "metaindex.h"
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>

//number of Key-value pairs for the workload
#define N_KVP 10
//number of Increment Ops per key
#define I_OPS 1000000
//number of threads operating over the HASTable
#define N_THREADS 10

//Populate the index with key-value pairs (N_KVP)
void populate(Index *index){

	Filemeta m;
	char key[KEY_SIZE];

	for(int i=0; i<N_KVP; i++){

		bzero(key, KEY_SIZE);
		sprintf(key, "%d", i);
		m.size=i;
		m.refs=0;
		m.content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		int res = index_add(index, key, m);
		if(res==-1){
			fprintf(stderr,"error: add key %s failed!\n", key);
			break;
		}
	}
}

//Get and print a range of key-value pairs from the index
void workloadGet(Index *index, int start, int end){

	Filemeta m;
	char key[KEY_SIZE];

	for(int i=start; i<end; i++){

		bzero(key, KEY_SIZE);
		sprintf(key, "%d", i);

		int res = index_get(index, key, &m);
		if(res==-1){
			fprintf(stderr, "error: get key %s failed!\n", key);
			break;
		}
		printf("key %s, size %lu, refs %d, content: %s\n", key, m.size, m.refs, m.content);
	}

	printf("\n\n");

}

//Increment the n times the number of references for key
void workloadInc(Index *index, char* key, int times){

	for(int i=0; i<times; i++){

		int res = index_increfs(index, key);
		if(res==-1){
			fprintf(stderr,"error: update key %s failed!\n", key);
			break;
		}
	}
}

//thread routine - void *(*start_routine)(void *), void *arg
//As argument it receives a pointer to a memory location (the index location in this example)
//As return value, it can provide another memory location for the phtread_join function to collect.
void *thread_inc_routine(void *ptr)
{
	Index *index=ptr;
	workloadInc(index, "0", I_OPS);
	return 0;
}

void *thread_wait_routine(void *ptr) {
	Index *index = ptr;
	index_wait_refs(index, "0", 10000);
	return 0;
}


int main(int argc, char*argv[]){

	//array of pthread structs
	pthread_t threads[N_THREADS];
	pthread_t extraT;
	int j;

	//create index
	Index *index = index_init();

	//populate the index
	populate(index);

	pthread_create(&extraT, NULL, thread_wait_routine, index);
	//create NThreads
    for(j=0; j<N_THREADS; j++){

    	//int pthread_create(pthread_t *thread, const pthread_attr_t *attr, void *(*start_routine)(void *), void *arg);
    	//the thread is created executing start_routine with arg as its sole argument.
    	//attr can be used to specify non-default thread attributed (e.g., priority, scheduling)
    	pthread_create(&threads[j], NULL, thread_inc_routine, index);
    }
	
    for(j=0; j<N_THREADS; j++){

    	// int pthread_join(pthread_t thread, void **value_ptr);
    	// suspends execution of the calling thread until the target thread terminates unless the target thread has already terminated.
    	// value_ptr stores the value returned by the terminated thread
    	pthread_join(threads[j], NULL);
    }
	pthread_join(extraT, NULL);

	//run Get workload
	workloadGet(index,0, N_KVP);

	//destroy the index
	index_destroy(index);

	return 0;
}