#ifndef HASHING_H
#define HASHING_H

#include <stddef.h>

#define SHA256_HEX_LEN 65 // 64 hex characters + 1 null terminator

/**
 * @brief Calculates the SHA-256 hash of a data buffer.
 *
 * @param data The input data buffer.
 * @param size The size of the input data.
 * @param output_hash A buffer to store the resulting null-terminated hex string.
 *                    This buffer must be at least SHA256_HEX_LEN bytes long.
 */
void calculate_sha256(const char* data, size_t size, char* output_hash);


#endif // HASHING_H
