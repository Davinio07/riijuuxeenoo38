/**
 * Interface die een politieke partij vertegenwoordigt (basisgegevens).
 * @interface PoliticalParty
 */
export interface PoliticalParty {
  /** De officieel geregistreerde naam van de partij. */
  name: string;
}

/**
 * Interface die de nationale resultaten van een partij vertegenwoordigt.
 * Komt overeen met het backend datamodel voor stemtotalen.
 * @interface NationalResult
 */
export interface NationalResult {
  /** De naam van de politieke partij. */
  name: string;
  /** Het totaal aantal ontvangen geldige stemmen. */
  totalVotes: number;
}

/**
 * De basis URL voor de nationale resultaten API eindpunten.
 */
const API_BASE_URL = 'http://localhost:8080/api/nationalResult';

/**
 * Een mapping van officiële partijnamen naar hun aangewezen hex-kleurcodes.
 * Bevat grote nationale partijen en specifieke deelnemers van 2021 en 2025.
 * @type {Record<string, string>}
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

  // 2021 Geregistreerde Partijen
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

  // 2025 Nieuwe/Ontbrekende Geregistreerde Partijen
  'Vrede voor Dieren': '#228B22',
  'FNP': '#FFD700',
  'Vrij Verbond': '#87CEEB',
  'DE LINIE': '#2F4F4F',
  'NL PLAN': '#4682B4',
  'ELLECT': '#DA70D6',
  'Partij voor de Rechtsstaat': '#1B4D3E'
};

/**
 * Een mapping van officiële partijnamen naar hun logo URL's.
 * Bestanden moeten worden opgeslagen in de public/logos map.
 * @type {Record<string, string>}
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
  'Piratenpartij - De Groenen': '/logos/piratenpartij.png',
  'Splinter': '/logos/splinter.png',
  'Samen voor Nederland': '/logos/samenvoornederland.png',
  'LP (Libertaire Partij)': '/logos/lp.png',
  'LEF - Voor de Nieuwe Generatie': '/logos/lef.png',
  'Nederland met een PLAN': '/logos/plan.png',
  'PartijvdSport': '/logos/pvds.png',
  'Politieke Partij voor Basisinkomen': '/logos/basisinkomen.png',
  'CODE ORANJE': '/logos/code-oranje.png',
  'Piratenpartij': '/logos/piratenpartij.png',
  'NIDA': '/logos/nida.png',
  'NLBeter': '/logos/nlbeter.png',
  'Lijst Henk Krol': '/logos/henk-krol.png',
  'JONG': '/logos/jong.png',
  'NL PLAN': '/logos/plan.png'
};

/**
 * Haalt de hex-kleurcode op voor een opgegeven partijnaam.
 * Valt terug op een standaard grijs als de partijnaam niet bekend is.
 * @param {string} partyName - De officiële naam van de partij.
 * @returns {string} Hex-kleurcode.
 */
export const getPartyColor = (partyName: string): string => {
  return partyColors[partyName] || '#6B7280';
};

/**
 * Haalt de logo URL op voor een opgegeven partijnaam.
 * @param {string} partyName - De officiële naam van de partij.
 * @returns {string | null} Het URL-pad naar het logo of null indien afwezig.
 */
export const getPartyLogo = (partyName: string): string | null => {
  return partyLogos[partyName] || null;
};

/**
 * Generieke helperfunctie voor het afhandelen van API-verzoeken met fetch.
 * @template T - Het verwachte returntype.
 * @param {string} endpoint - Het API-eindpunt om aan de basis-URL toe te voegen.
 * @returns {Promise<T>} Geparsed JSON-antwoord.
 */
async function fetchFromAPI<T>(endpoint: string): Promise<T> {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`);
    if (!response.ok) {
      const errorBody = await response.text();
      console.error(`API Fout: ${response.status} ${response.statusText}`, errorBody);
      throw new Error(`Ophalen van gegevens mislukt: ${response.status} ${response.statusText}`);
    }
    return await response.json();
  } catch (error) {
    console.error(`Fout in fetchFromAPI:`, error);
    throw error;
  }
}

/**
 * Interne helper om zetelverdeling te berekenen met de methode D'Hondt.
 * @param {NationalResult[]} results - Stemtotalen per partij.
 * @param {number} totalSeats - Het totaal aantal te verdelen zetels.
 * @returns {Record<string, number>} Map van partijnamen naar toegewezen zetelaantallen.
 */
function calculateDHondt(results: NationalResult[], totalSeats: number): Record<string, number> {
  const seatCounts: Record<string, number> = {};
  results.forEach(r => seatCounts[r.name] = 0);

  const activeParties = results
    .filter(r => r.totalVotes > 0)
    .map(r => ({ name: r.name, votes: r.totalVotes, seats: 0 }));

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

/**
 * Service voor het afhandelen van partijgerelateerde gegevens en zetelberekeningen.
 */
export const partyService = {
  /**
   * Haalt nationale verkiezingsresultaten op voor een specifieke verkiezingscyclus.
   * @param {string} electionId - Het ID van de verkiezing (bijv. "TK2021").
   * @returns {Promise<NationalResult[]>} Array van partijresultaten.
   */
  async getNationalResults(electionId: string): Promise<NationalResult[]> {
    return fetchFromAPI<NationalResult[]>(`/${electionId}/national`);
  },

  /**
   * Publieke wrapper voor D'Hondt zetelberekeningslogica.
   * @param {NationalResult[]} results - Stemtotalen per partij.
   * @param {number} [totalSeats=150] - Aantal te verdelen zetels (standaard 150).
   * @returns {Record<string, number>} Toegewezen zetels per partij.
   */
  calculateSeats(results: NationalResult[], totalSeats: number = 150): Record<string, number> {
    return calculateDHondt(results, totalSeats);
  }
};
