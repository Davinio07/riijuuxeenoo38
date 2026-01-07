/**
 * Interface representing a political party (basic info).
 */
export interface PoliticalParty {
  /** The officially registered name of the party. */
  name: string;
}

/**
 * Interface representing a party's national result.
 */
export interface NationalResult {
  name: string;
  totalVotes: number;
}

/**
 * The base URL for the elections API.
 */
// Uses the .env variable, then adds the specific endpoint path test
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const API_BASE_URL = `${API_URL}/nationalResult`;
/**
 * A mapping of official party names to their designated hex color codes.
 */
export const partyColors: Record<string, string> = {
  // Grote & Bestaande Partijen
  'VVD': '#FF6600',
  'PVV (Partij voor de Vrijheid)': '#00529F',
  'CDA': '#00A54F',
  'D66': '#00A03E',
  'GROENLINKS': '#228B22',
  'GROENLINKS / Partij van de Arbeid (PvdA)': '#DC143C',
  'SP (Socialistische Partij)': '#FF0000',
  'Partij van de Arbeid (P.v.d.A.)': '#DF111A',
  'ChristenUnie': '#00A7EB',
  'Partij voor de Dieren': '#006F3F',
  '50PLUS': '#8B008B',
  'Staatkundig Gereformeerde Partij (SGP)': '#FE7D00',
  'DENK': '#00CCCC',
  'Forum voor Democratie': '#800020',
  'Volt': '#502379',
  'JA21': '#1C39BB',
  'BIJ1': '#FFED00',
  'BBB': '#5BB12F',
  'Nieuw Sociaal Contract': '#00A3E0',
  'Nieuw Sociaal Contract (NSC)': '#00A3E0',
  'Piratenpartij - De Groenen': '#660099',
  'Splinter': '#FF6B35',
  'Samen voor Nederland': '#FF8C00',
  'LP (Libertaire Partij)': '#FFD700',
  'LEF - Voor de Nieuwe Generatie': '#00CED1',
  'Nederland met een PLAN': '#4682B4',
  'PartijvdSport': '#32CD32',
  'Politieke Partij voor Basisinkomen': '#9370DB',
  'BVNL / Groep Van Haga': '#1E3A8A',
  'Belang Van Nederland (BVNL)': '#1E3A8A',

  // 2021 Specifieke Partijen
  'CODE ORANJE': '#FFA500',
  'Piratenpartij': '#660099',
  'NIDA': '#00ADAF',
  'NLBeter': '#242B5C',
  'OPRECHT': '#203E5F',
  'JONG': '#2B2E83',
  'Lijst Henk Krol': '#F0C300',
  'JEZUS LEEFT': '#FF00FF',
  'U-Buntu Connected Front': '#000000',
  'Trots op Nederland (TROTS)': '#000080',
  'Blanco (Zeven, A.J.L.B.)': '#A9A9A9',
  'DE FEESTPARTIJ (DFP)': '#FF69B4',
  'Partij van de Eenheid': '#006400',
  'Vrij en Sociaal Nederland': '#008080',
  'Wij zijn Nederland': '#1F2937',
  'Modern Nederland': '#3B82F6',
  'De Groenen': '#00FF00',
  'Partij voor de Republiek': '#C0C0C0',

  // 2025 Specifieke Partijen
  'Vrede voor Dieren': '#228B22',
  'FNP': '#FFD700',
  'Vrij Verbond': '#87CEEB',
  'DE LINIE': '#2F4F4F',
  'NL PLAN': '#4682B4',
  'ELLECT': '#DA70D6',
  'Partij voor de Rechtsstaat': '#1B4D3E'
};

/**
 * A mapping of official party names to their logo image URLs.
 */
export const partyLogos: Record<string, string> = {
  'VVD': '/logos/vvd.png',
  'D66': '/logos/d66.png',
  'PVV (Partij voor de Vrijheid)': '/logos/pvv.png',
  'GROENLINKS / Partij van de Arbeid (PvdA)': '/logos/gl-pvda.png',
  'CDA': '/logos/cda.png',
  'SP (Socialistische Partij)': '/logos/sp.png',
  'Forum voor Democratie': '/logos/fvd.png',
  'Partij voor de Dieren': '/logos/pvdd.png',
  'ChristenUnie': '/logos/cu.png',
  'Volt': '/logos/volt.png',
  'JA21': '/logos/ja21.png',
  'Staatkundig Gereformeerde Partij (SGP)': '/logos/sgp.png',
  'DENK': '/logos/denk.png',
  '50PLUS': '/logos/50plus.png',
  'BBB': '/logos/bbb.png',
  'BIJ1': '/logos/bij1.png',
  'Nieuw Sociaal Contract': '/logos/nsc.png',
  'Nieuw Sociaal Contract (NSC)': '/logos/nsc.png',
  'BVNL / Groep Van Haga': '/logos/bvnl.png',
  'Belang Van Nederland (BVNL)': '/logos/bvnl.png',
  'Piratenpartij': '/logos/piratenpartij.png',
  'Piratenpartij - De Groenen': '/logos/piratenpartij.png',
  'Splinter': '/logos/splinter.png',
  'LP (Libertaire Partij)': '/logos/lp.png',
  'LEF - Voor de Nieuwe Generatie': '/logos/lef.png',
  'NL PLAN': '/logos/plan.png'
};

export const getPartyColor = (partyName: string): string => {
  return partyColors[partyName] || '#6B7280';
};

export const getPartyLogo = (partyName: string): string | null => {
  return partyLogos[partyName] || null;
};

async function fetchFromAPI<T>(endpoint: string): Promise<T> {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error(`Error in fetchFromAPI:`, error);
    throw error;
  }
}

function calculateDHondt(results: NationalResult[], totalSeats: number): Record<string, number> {
  const seatCounts: Record<string, number> = {};
  results.forEach(r => seatCounts[r.name] = 0);
  const activeParties = results.filter(r => r.totalVotes > 0).map(r => ({ name: r.name, votes: r.totalVotes, seats: 0 }));
  for (let i = 0; i < totalSeats; i++) {
    let maxQuotient = -1;
    let winnerIndex = -1;
    for (let j = 0; j < activeParties.length; j++) {
      const p = activeParties[j];
      const quotient = p.votes / (p.seats + 1);
      if (quotient > maxQuotient) {
        maxQuotient = quotient;
        winnerIndex = j;
      }
    }
    if (winnerIndex !== -1) {
      activeParties[winnerIndex].seats++;
      seatCounts[activeParties[winnerIndex].name]++;
    }
  }
  return seatCounts;
}

export const partyService = {
  async getNationalResults(electionId: string): Promise<NationalResult[]> {
    return fetchFromAPI<NationalResult[]>(`/${electionId}/national`);
  },
  calculateSeats(results: NationalResult[], totalSeats: number = 150): Record<string, number> {
    return calculateDHondt(results, totalSeats);
  }
};
