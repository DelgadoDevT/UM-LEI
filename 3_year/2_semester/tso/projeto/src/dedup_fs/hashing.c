#include "dedup_fs/hashing.h"
#include <openssl/sha.h>
#include <stdio.h>

/**
 * @brief Computes the SHA-256 hash string for a block of data.
 * 
 * Utilizes the OpenSSL SHA256 function to calculate the raw binary hash,
 * then formats it into a continuous, 64-character, null-terminated hexadecimal string.
 * 
 * @param data Pointer to the raw input data to be hashed.
 * @param size The number of bytes in the data buffer.
 * @param output_hash A pre-allocated buffer (at least 65 bytes long) where
 *                    the resulting hex string will be written.
 */
void calculate_sha256(const char* data, size_t size, char* output_hash) {    
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256((const unsigned char*)data, size, hash);

    for (int i = 0; i < 32; i++) {
        sprintf(&output_hash[i * 2], "%02x", hash[i]);
    }

    output_hash[64] = '\0';
}
