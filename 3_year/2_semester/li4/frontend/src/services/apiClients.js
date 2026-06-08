import { createApiClient } from './httpClient';

const localApiTarget = '/api/local';
const globalApiTarget = '/api/global';

export const localApiClient = createApiClient(localApiTarget);
export const globalApiClient = createApiClient(globalApiTarget);
