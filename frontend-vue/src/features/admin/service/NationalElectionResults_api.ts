import apiClient from '@/services/api-client';

const logger = {
  info: (message: string, context?: object) => console.log(`[INFO] ${message}`, context || ''),
  warn: (message: string, context?: object) => console.warn(`[WARN] ${message}`, context || ''),
  error: (message: string, context?: object) => console.error(`[ERROR] ${message}`, context || ''),
  debug: (message: string, context?: object) => console.debug(`[DEBUG] ${message}`, context || ''),
};

export interface PartyDTO {
  id: number;
  name: string;
  totalVotes: number;
  nationalSeats: number;
  votePercentage: number;
}

export async function getPartiesFromDb(electionId: string): Promise<PartyDTO[]> {
  // FIX: Use the relative path. apiClient will add the 'http://oege...:8400/api' part.
  const endpoint = `/nationalResult/${electionId}/national`;

  logger.info(`Fetching persisted party data for election ID: ${electionId}`);

  try {
    const data = await apiClient<PartyDTO[]>(endpoint);

    if (!data || data.length === 0) {
      logger.info(`Request successful but no party data found for election ID: ${electionId}`);
      return [];
    }

    logger.info(`Successfully retrieved party data for election ID: ${electionId}`);
    return data;

  } catch (error) {
    logger.error('A network or unexpected error occurred during fetch.', { originalError: error });
    // apiClient usually throws an error if status is not 200, so we return empty or rethrow
    throw new Error('Failed to connect to the server.');
  }
}
