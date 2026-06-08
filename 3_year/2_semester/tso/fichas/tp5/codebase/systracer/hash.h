#ifndef HASH_H
#define HASH_H

#define MAX_BUF_SIZE 		4096

/* Prime numbers used in the XXHash32 algorithm */
static const uint32_t prime32x1 = 2654435761;
static const uint32_t prime32x2 = 2246822519;
static const uint32_t prime32x3 = 3266489917;
static const uint32_t prime32x4 = 668265263;
static const uint32_t prime32x5 = 374761393;

/* Rotate left operation for 32-bit integers */
static uint32_t rotl32_1(uint32_t x)  { return (x << 1) | (x >> (32 - 1)); }
static uint32_t rotl32_7(uint32_t x)  { return (x << 7) | (x >> (32 - 7)); }
static uint32_t rotl32_11(uint32_t x) { return (x << 11) | (x >> (32 - 11)); }
static uint32_t rotl32_12(uint32_t x) { return (x << 12) | (x >> (32 - 12)); }
static uint32_t rotl32_13(uint32_t x) { return (x << 13) | (x >> (32 - 13)); }
static uint32_t rotl32_17(uint32_t x) { return (x << 17) | (x >> (32 - 17)); }
static uint32_t rotl32_18(uint32_t x) { return (x << 18) | (x >> (32 - 18)); }

/* This function provides a limited version of xxhash32 that can be used in eBPF programs. It reads the data from a buffer using bpf_probe_read and calculates the hash value. It also limits the maximum buffer size to 4096 bytes to avoid long processing time in the kernel.
 * Receives a pointer to the buffer (const char *buf), the size of the buffer (long len) and a seed value (uint32_t seed) as arguments.
 * Returns the hash value as a uint32_t.
 */
static uint32_t xxhash32(const char *buf, long len, uint32_t seed)
{
    uint32_t h, k;
    int i = 0, j = 0;

    const unsigned char* data = (const unsigned char*)buf;

    if (len > MAX_BUF_SIZE) return 0;
    if (len >= 16) {
        uint32_t v1 =seed + prime32x1 + prime32x2;
        uint32_t v2 =seed + prime32x2;
        uint32_t v3 =seed + 0;
        uint32_t v4 =seed - prime32x1;

		for (j=0; i <= (len-16) && j < (MAX_BUF_SIZE/16); i += 16, j++) {

            bpf_probe_read(&k, sizeof(k), data);
			v1 += k * prime32x2;
			v1 = rotl32_13(v1) * prime32x1;
            data += sizeof(uint32_t);

            bpf_probe_read(&k, sizeof(k), data);
			v2 += k * prime32x2;
			v2 = rotl32_13(v2) * prime32x1;
            data += sizeof(uint32_t);

            bpf_probe_read(&k, sizeof(k), data);
			v3 += k * prime32x2;
			v3 = rotl32_13(v3) * prime32x1;
            data += sizeof(uint32_t);

            bpf_probe_read(&k, sizeof(k), data);
			v4 += k * prime32x2;
			v4 = rotl32_13(v4) * prime32x1;
            data += sizeof(uint32_t);
		}
        h = rotl32_1(v1) + rotl32_7(v2) + rotl32_12(v3) + rotl32_18(v4);
	} else {
		h = seed + prime32x5;
	}

	h += (uint32_t)len;
	for (j=0; i <= (len-4) && (j < 16); i += 4, j++) {
        bpf_probe_read(&k, sizeof(k), data);
		h += k * prime32x3;
		h = rotl32_17(h) * prime32x4;
        data += sizeof(uint32_t);
	}

	for (j=0; i < len && j < 4; i++, j++) {
        u_int8_t val;
        bpf_probe_read(&val, sizeof(val), data);
		h += val * prime32x5;
		h = rotl32_11(h) * prime32x1;
        data++;
	}

	h ^= h >> 15;
	h *= prime32x2;
	h ^= h >> 13;
	h *= prime32x3;
	h ^= h >> 16;

	return h;
}

#endif