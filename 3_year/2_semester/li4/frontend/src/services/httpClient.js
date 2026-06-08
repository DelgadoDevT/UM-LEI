const prepareBody = (body, headers) => {
  if (body == null) return undefined;
  if (body instanceof FormData || body instanceof Blob || typeof body === 'string') {
    return body;
  }

  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  return JSON.stringify(body);
};

const parseResponseBody = async (response, options = {}) => {
  if (response.status === 204) return null;

  const contentType = response.headers.get('content-type') || '';

  // If the caller explicitly wants a blob, or if the server indicates it's a file, return a blob.
  if (options.responseType === 'blob' ||
      contentType.includes('application/pdf') ||
      contentType.includes('text/csv') ||
      contentType.includes('application/zip') ||
      contentType.includes('application/octet-stream')) {
    return response.blob();
  }

  if (contentType.includes('application/json')) {
    return response.json();
  }

  const text = await response.text();
  return text || null;
};

const normalizePath = (path = '') => {
  if (!path) return '';
  return path.startsWith('/') ? path : `/${path}`;
};

export const createApiClient = (basePath) => {
  const request = async (path = '', options = {}) => {
    const { headers: customHeaders, body, ...fetchOptions } = options;
    const headers = new Headers(customHeaders || {});

    if (!headers.has('Accept')) {
      headers.set('Accept', 'application/json');
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 10000); // 10s timeout

    try {
      const response = await fetch(`${basePath}${normalizePath(path)}`, {
        credentials: 'include',
        ...fetchOptions,
        headers,
        body: prepareBody(body, headers),
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      const responseBody = await parseResponseBody(response, options);

      if (!response.ok) {
        const message = typeof responseBody === 'string'
          ? responseBody
          : responseBody?.message || 'Request failed';

        const error = new Error(message);
        error.status = response.status;
        error.body = responseBody;
        throw error;
      }

      // Do not try to unpack blobs or other non-plain objects
      if (responseBody instanceof Blob) {
        return responseBody;
      }

      if (responseBody && typeof responseBody === 'object' && !Array.isArray(responseBody) && 'data' in responseBody) {
        return responseBody.data;
      }

      return responseBody;
    } catch (err) {
      clearTimeout(timeoutId);
      if (err.name === 'AbortError') {
        throw new Error('O pedido expirou (Timeout). Verifique se o servidor está a responder.');
      }
      throw err;
    }
  };

  return {
    request,
    get: (path, options) => request(path, { ...options, method: 'GET' }),
    post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
    put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
    patch: (path, body, options) => request(path, { ...options, method: 'PATCH', body }),
    delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
  };
};
