// #include <openssl/evp.h>
#include <bzlib.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>

/*
 * FUSE: Filesystem in Userspace
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE
 */

/*
 * Creates files on the underlying file system in response to a FUSE_MKNOD
 * operation
 */
static int mknod_wrapper(int dirfd, const char *path, const char *link,
	int mode, dev_t rdev)
{
	int res;

	if (S_ISREG(mode)) {
		res = openat(dirfd, path, O_CREAT | O_EXCL | O_WRONLY, mode);
		if (res >= 0)
			res = close(res);
	} else if (S_ISDIR(mode)) {
		res = mkdirat(dirfd, path, mode);
	} else if (S_ISLNK(mode) && link != NULL) {
		res = symlinkat(link, dirfd, path);
	} else if (S_ISFIFO(mode)) {
		res = mkfifoat(dirfd, path, mode);
#ifdef __FreeBSD__
	} else if (S_ISSOCK(mode)) {
		struct sockaddr_un su;
		int fd;

		if (strlen(path) >= sizeof(su.sun_path)) {
			errno = ENAMETOOLONG;
			return -1;
		}
		fd = socket(AF_UNIX, SOCK_STREAM, 0);
		if (fd >= 0) {
			/*
			 * We must bind the socket to the underlying file
			 * system to create the socket file, even though
			 * we'll never listen on this socket.
			 */
			su.sun_family = AF_UNIX;
			strncpy(su.sun_path, path, sizeof(su.sun_path));
			res = bindat(dirfd, fd, (struct sockaddr*)&su,
				sizeof(su));
			if (res == 0)
				close(fd);
		} else {
			res = -1;
		}
#endif
	} else {
		res = mknodat(dirfd, path, mode, rdev);
	}

	return res;
}

/* Compresses the input buffer using BZip2 and stores the result in the output buffer.
 * Parameters:
 * - in_buf: Pointer to the input buffer containing the data to be compressed.
 * - in_size: Size of the input buffer in bytes.
 * - out_buf: Pointer to the output buffer where the compressed data will be stored.
 * - out_size: Pointer to an unsigned int that will hold the size of the compressed data after compression.
 * Returns:
 * - 0 on success, -1 on failure.
 * NOTE: to estimate the required size of the output buffer, you can use the formula: out_size >= in_size * 1.01 + 600, as recommended in the BZip2 documentation.
 */
static int compress(char *in_buf, size_t in_size, char *out_buf, unsigned int *out_size) {
	int ret;

    ret = BZ2_bzBuffToBuffCompress(
        out_buf,          /* destination buffer */
        out_size,         /* in: size of dest, out: compressed size */
        in_buf,           /* source buffer */
        in_size,          /* source size */
        9,                /* blockSize100k: 1–9 */
        0,                /* verbosity */
        30                /* workFactor (default) */
    );

    if (ret != BZ_OK) {
        return -1;
    }

	printf("compressed %ld bytes into %d bytes\n", in_size, *out_size);

	return 0;
}

/* Decompresses the input buffer using BZip2 and stores the result in the output buffer.
 * Parameters:
 * - in_buf: Pointer to the input buffer containing the compressed data.
 * - in_size: Size of the input buffer in bytes.
 * - out_buf: Pointer to the output buffer where the decompressed data will be stored.
 * - out_size: Pointer to an unsigned int that will hold the size of the decompressed data after decompression.
 * Returns:
 * - 0 on success, -1 on failure.
 */
static int decompress(char *in_buf, size_t in_size, char *out_buf, unsigned int *out_size) {
	int ret;

	ret = BZ2_bzBuffToBuffDecompress(
        out_buf,        /* destination buffer */
        out_size,      /* in: size of dest, out: decompressed size */
        in_buf,         /* source buffer */
        in_size,        /* compressed size */
        0,              /* small */
        0               /* verbosity */
    );
    if (ret != BZ_OK) {
		return -1;
	}

	printf("decompressed %ld bytes into %d bytes\n", in_size, *out_size);

	return 0;
}