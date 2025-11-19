import apiClient from '@/services/api-client';

// --- TypeScript Interface Definitions ---
// (Replaces the JSDoc comments)

export interface KieskringDto {
  kieskring_id: number;
  name: string;
}

export interface ProvinceDto {
  province_id: number;
  name: string;
  kieskringen: KieskringDto[];
}

// --- API Service Function ---

/**
 * Fetches all provinces and their nested kieskringen from the database API.
 * This follows the pattern of your KieskringDetails_api.ts.
 *
 * @returns {Promise<ProvinceDto[]>} A promise that resolves to the list of provinces.
 */
export async function getProvincesWithKieskringen(): Promise<ProvinceDto[]> {
  const endpoint = '/elections/Getprovince';

  // The apiClient handles the fetch, parsing, and error handling.
  // We specify the expected return type for type-hinting.
  return apiClient<ProvinceDto[]>(endpoint);
}
