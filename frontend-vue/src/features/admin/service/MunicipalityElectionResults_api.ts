import apiClient from '@/services/api-client';

export interface MunicipalityResult {
    municipalityName: string;
    partyName: string;
    validVotes: number;
}

/**
 * Fetches the election results for a single, specific municipality.
 * @param {string} municipalityName - The name of the municipality.
 * @returns {Promise<MunicipalityResult[]>} A promise that resolves to the election results.
 */
export async function getResultsForMunicipality(municipalityName: string): Promise<MunicipalityResult[]> {
    try {
        // Use encodeURIComponent to safely handle names with special characters in the URL
        const endpoint = `/elections/municipalities/${encodeURIComponent(municipalityName)}`;
        const response = await apiClient<MunicipalityResult[]>(endpoint);
        return response;
    } catch (error) {
        console.error('API Error fetching results for municipality:', error);
        throw error;
    }
}

/**
 * Fetches a list of all unique municipality names.
 * @returns {Promise<string[]>} A promise that resolves to an array of municipality names.
 */
export async function getMunicipalityNames(): Promise<string[]> {
    try {
        const response = await apiClient<string[]>('/elections/municipalities/names');
        return response;
    } catch (error) {
        console.error('API Error fetching municipality names:', error);
        throw error;
    }
}
