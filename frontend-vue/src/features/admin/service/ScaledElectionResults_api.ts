import apiClient from '@/services/api-client';

interface JpaCandidate {
  id: number;
  name: string;
  residence: string;
  gender: string;
  partyIdForJson: number | null;
  partyNameForJson: string | null;
}

export interface CandidateData {
  id: number | string | null;
  firstName: string | null;
  lastName: string | null;
  locality: string | null;
  gender: string | null;
  listName: string | null;
  listId: number | null;
  initials?: string | null;
  prefix?: string | null;
  listNumber?: string | null;
  numberOnList?: string | null;
}

export interface PartyFilterData {
  id: number;
  name: string;
}

export async function getAllPartiesForFilters(electionId: string): Promise<PartyFilterData[]> {
  try {
    const endpoint = `/nationalResult/${electionId}/national`;
    // Use apiClient instead of fetch
    const rawParties = await apiClient<any[]>(endpoint);
    return rawParties.map(p => ({ id: p.id, name: p.name }));
  } catch (error) {
    console.error('Error fetching parties for filters:', error);
    return [];
  }
}

export async function ScaledElectionResults(): Promise<string> {
  try {
    // FIX: Removed hardcoded localhost
    const data = await apiClient<{ message: string }>('/ScaledElectionResults/Result');
    return data.message;
  } catch (error) {
    console.error("Fout bij het ophalen van de backend:", error);
    return "Kon geen verbinding maken met de backend.";
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function getProvinces(electionId: string): Promise<any[]> {
  try {
    // FIX: Removed hardcoded localhost and used apiClient
    const endpoint = `/elections/${electionId}/regions/kieskringen`;
    return await apiClient<any[]>(endpoint);
  } catch (error) {
    console.error('Error fetching provinces:', error);
    return [];
  }
}

export async function getCandidates(partyId?: number | null, gender?: string | null): Promise<CandidateData[]> {
  try {
    let url = `/elections/candidates/db`;
    const params = new URLSearchParams();

    if (partyId) params.append('partyId', partyId.toString());
    if (gender) params.append('gender', gender);

    if (params.toString()) url += '?' + params.toString();

    const rawCandidates = await apiClient<JpaCandidate[]>(url);

    return rawCandidates.map(c => {
      const parts = c.name.split(' ');
      const lastName = parts.length > 1 ? parts[parts.length - 1] : c.name;
      const firstName = parts.length > 1 ? parts.slice(0, parts.length - 1).join(' ') : '';

      return {
        id: c.id,
        firstName: firstName,
        lastName: lastName,
        locality: c.residence,
        gender: c.gender,
        listName: c.partyNameForJson,
        listId: c.partyIdForJson,
        initials: null,
        prefix: null,
        listNumber: null,
        numberOnList: null,
      } as CandidateData;
    });

  } catch (error) {
    console.error('Error fetching candidates:', error);
    return [];
  }
}
