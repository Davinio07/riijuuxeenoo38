// partyService.ts

/**
 * Interface representing a political party as returned by the API.
 */
interface PoliticalParty {
  /** The officially registered name of the party. */
  registeredAppellation: string;
}

/**
 * The base URL for the elections API.
 */
const API_BASE_URL = 'http://localhost:8080/api/elections';

/**
 * A mapping of official party names to their designated hex color codes.
 * Based on common colors for Dutch political parties.
 */
export const partyColors: Record<string, string> = {
  'VVD': '#FF6600',
  'D66': '#00A03E',
  'GROENLINKS / Partij van de Arbeid (PvdA)': '#DC143C',
  'PVV (Partij voor de Vrijheid)': '#00529F',
  'CDA': '#00A54F',
  'SP (Socialistische Partij)': '#FF0000',
  'Forum voor Democratie': '#800020',
  'Partij voor de Dieren': '#006F3F',
  'ChristenUnie': '#00A7EB',
  'Volt': '#502379',
  'JA21': '#1C39BB',
  'Staatkundig Gereformeerde Partij (SGP)': '#FE7D00',
  'DENK': '#00CCCC',
  '50PLUS': '#8B008B',
  'BBB': '#5BB12F',
  'BIJ1': '#FFED00',
  'BVNL / Groep Van Haga': '#1E3A8A',
  'Nieuw Sociaal Contract': '#00A3E0',
  'Piratenpartij - De Groenen': '#660099',
  'Splinter': '#FF6B35',
  'Samen voor Nederland': '#FF8C00',
  'LP (Libertaire Partij)': '#FFD700',
  'LEF - Voor de Nieuwe Generatie': '#00CED1',
  'Nederland met een PLAN': '#4682B4',
  'PartijvdSport': '#32CD32',
  'Politieke Partij voor Basisinkomen': '#9370DB',
};

/**
 * Retrieves the hex color code for a given party name.
 *
 * @param {string} partyName - The registered appellation of the party.
 * @returns {string} The corresponding hex color code or a default gray if not found.
 */
export const getPartyColor = (partyName: string): string => {
  return partyColors[partyName] || '#6B7280'; // Default gray if not found
};

/**
 * Service object for handling political party data.
 */
export const partyService = {
  /**
   * Fetches a list of political parties for a specific election.
   *
   * @param {string} electionId - The unique identifier for the election.
   * @returns {Promise<PoliticalParty[]>} A promise that resolves to an array of PoliticalParty objects.
   * @throws {Error} Throws an error if the network request fails, if the server
   * responds with a non-OK status, or if the response is not valid JSON.
   */
  async getParties(electionId: string): Promise<PoliticalParty[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/${electionId}/parties`);

      // Check for non-successful HTTP responses (e.g., 404, 500)
      if (!response.ok) {
        // Try to get more info from the response, if possible
        let errorBody = 'Unknown error';
        try {
          errorBody = await response.text();
        } catch (textError) {
          // Ignore if we can't read the body
        }
        console.error(`API Error: ${response.status} ${response.statusText}`, errorBody);
        throw new Error(`Failed to fetch parties: ${response.status} ${response.statusText}`);
      }

      // Try to parse the JSON response
      const data: PoliticalParty[] = await response.json();
      return data;

    } catch (error) {
      // Log the specific error for debugging
      console.error("Error in getParties service:", error);

      // Handle different types of errors
      if (error instanceof SyntaxError) {
        // This occurs if response.json() fails (malformed JSON)
        throw new Error('Failed to parse server response. Invalid JSON.');
      }

      if (error instanceof TypeError) {
        // This often happens with network errors (e.g., fetch failed to send, CORS issue)
        throw new Error('Network error: Could not connect to the API.');
      }

      // Re-throw the error we already processed (like the !response.ok)
      // or any other unexpected error
      if (error instanceof Error) {
        throw error;
      }

      // Fallback for any other unexpected error type
      throw new Error('An unknown error occurred while fetching parties.');
    }
  }
};
