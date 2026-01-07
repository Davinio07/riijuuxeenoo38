const logger = {
  info: (message: string, context?: object) => console.log(`[INFO] ${message}`, context || ''),
  warn: (message: string, context?: object) => console.warn(`[WARN] ${message}`, context || ''),
  error: (message: string, context?: object) => console.error(`[ERROR] ${message}`, context || ''),
  debug: (message: string, context?: object) => console.debug(`[DEBUG] ${message}`, context || ''),
};

/**
 * Represents an HTTP-specific error.
 * @extends Error
 */
class HttpError extends Error {
  status: number;
  statusText: string;

  constructor(message: string, status: number, statusText: string) {
    super(message);
    this.name = 'HttpError';
    this.status = status;
    this.statusText = statusText;
  }
}

/**
 * Represents the data structure for a Party as returned by the DB endpoint.
 */
export interface PartyDTO {
  id: number;
  name: string;
  totalVotes: number;
  nationalSeats: number;
  votePercentage: number;
}

/**
 * Fetches the persisted party results (incl. votes, seats) for a given election ID.
 * This function makes a GET request to `/api/nationalResult/{electionId}/national`.
 *
 * Returns an array of `PartyDTO`. If the server responds with HTTP 204 (No Content),
 * an empty array is returned.
 *
 * @async
 * @function getPartiesFromDb
 * @param {string} electionId - The unique identifier of the election (e.g., "TK2023").
 * @returns {Promise<PartyDTO[]>} A promise that resolves to an array of party data.
 *
 * @throws {HttpError} When the response contains a known HTTP error status (4xx or 5xx).
 * @throws {Error} When a network or unexpected fetch error occurs.
 */
import apiClient from '@/services/api-client'; // <--- Don't forget this import!

export async function getPartiesFromDb(electionId: string): Promise<PartyDTO[]> {
  // apiClient automatically adds the VITE_API_URL from your .env
  const endpoint = `/nationalResult/${electionId}/national`;

  logger.info(`Fetching persisted party data for election ID: ${electionId}`);

  try {
    const data = await apiClient<PartyDTO[]>(endpoint);

    // apiClient usually throws if not OK, so we can just return data
    if (!data || data.length === 0) {
      logger.info(`Request successful but no party data found for election ID: ${electionId}`);
      return [];
    }

    logger.info(`Successfully retrieved party data for election ID: ${electionId}`);
    return data;

  } catch (error) {
    logger.error('A network or unexpected error occurred during fetch.', { originalError: error });
    // Handle specific errors or rethrow
    throw error;
  }
}
