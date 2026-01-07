import apiClient from '@/services/api-client';

// We updated the interfaces to match the new backend DTO structure
export interface ConstituencyResultDto {
  partyName: string;
  validVotes: number;
}

export interface ConstituencyDataDto {
  name: string;
  results: ConstituencyResultDto[];
}

/**
 * Fetches ALL constituency results (aggregated from municipalities).
 * UPDATED: Now accepts an electionId (default TK2025) to fetch specific year data.
 */
export async function getAllConstituencyResults(electionId: string = 'TK2025'): Promise<ConstituencyDataDto[]> {
  try {
    // We inject the electionId into the URL
    const endpoint = `/constituencies/${electionId}/results`;
    return await apiClient<ConstituencyDataDto[]>(endpoint);
  } catch (error) {
    console.error(`API Error when fetching constituency results for ${electionId}:`, error);
    throw error;
  }
}
