{
type: file_content
fileName: src/features/admin/view/MunicipalityElectionResults.vue
fullContent:
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import {
  getMunicipalityNames,
  getResultsForMunicipality,
  type MunicipalityResult
} from '../service/MunicipalityElectionResults_api';

// State variables
const allMunicipalities = ref<string[]>([]);
const searchQuery = ref('');
const selectedMunicipality = ref<string | null>(null);
const results = ref<MunicipalityResult[]>([]);

// UI State
const loadingNames = ref(true);
const loadingResults = ref(false);
const error = ref<string | null>(null);

/**
 * Filters the list of municipalities based on the user's search query.
 * Case-insensitive.
 */
const filteredMunicipalities = computed(() => {
  if (!searchQuery.value) {
    return allMunicipalities.value;
  }
  const query = searchQuery.value.toLowerCase();
  return allMunicipalities.value.filter(m => m.toLowerCase().includes(query));
});

/**
 * Calculates the total number of votes in the current result set.
 * Used to calculate percentages per party.
 */
const totalVotes = computed(() => {
  return results.value.reduce((sum, item) => sum + item.validVotes, 0);
});

/**
 * Calculates the percentage of votes for a specific party.
 */
function calculatePercentage(votes: number): string {
  if (totalVotes.value === 0) return '0.0%';
  return ((votes / totalVotes.value) * 100).toFixed(1) + '%';
}

/**
 * Fetches the results for a specific municipality when clicked.
 */
async function selectMunicipality(name: string) {
  selectedMunicipality.value = name;
  loadingResults.value = true;
  error.value = null;
  results.value = []; // clear old results

  try {
    // Call the API endpoint we verified earlier
    results.value = await getResultsForMunicipality(name);

    // Sort results by votes (highest first) for better readability
    results.value.sort((a, b) => b.validVotes - a.validVotes);
  } catch (err) {
    console.error(err);
    error.value = `Kon de resultaten voor ${name} niet ophalen.`;
  } finally {
    loadingResults.value = false;
  }
}

/**
 * Resets the view to the selection list.
 */
function clearSelection() {
  selectedMunicipality.value = null;
  searchQuery.value = '';
  results.value = [];
}

onMounted(async () => {
  try {
    // Fetch all unique municipality names on load
    allMunicipalities.value = await getMunicipalityNames();
    // Sort alphabetically for the dropdown/list
    allMunicipalities.value.sort();
  } catch (err) {
    error.value = 'Fout bij het laden van de gemeentelijst.';
    console.error(err);
  } finally {
    loadingNames.value = false;
  }
});
</script>

<template>
  <div class="p-6 max-w-6xl mx-auto min-h-screen">

    <div class="mb-8 text-center">
      <h1 class="text-3xl font-bold text-gray-800 mb-2">Gemeentelijke Uitslagen</h1>
      <p class="text-gray-600">
        Bekijk de verkiezingsresultaten per gemeente.
      </p>
    </div>

    <div v-if="loadingNames" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
    </div>

    <div v-else-if="error" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6" role="alert">
      <p class="font-bold">Foutmelding</p>
      <p>{{ error }}</p>
      <button @click="clearSelection" class="underline mt-2">Terug naar overzicht</button>
    </div>

    <div v-else>

      <div v-if="!selectedMunicipality" class="space-y-6">
        <div class="max-w-md mx-auto relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Zoek een gemeente (bijv. Amsterdam)..."
            class="w-full p-4 pl-12 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
          />
          <span class="absolute left-4 top-4 text-gray-400">🔍</span>
        </div>

        <div v-if="filteredMunicipalities.length > 0" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mt-6">
          <button
            v-for="city in filteredMunicipalities"
            :key="city"
            @click="selectMunicipality(city)"
            class="p-3 bg-white border border-gray-200 rounded hover:bg-blue-50 hover:border-blue-300 hover:shadow-md transition-all text-left truncate"
          >
            {{ city }}
          </button>
        </div>
        <div v-else class="text-center text-gray-500 mt-8">
          Geen gemeenten gevonden die matchen met "{{ searchQuery }}".
        </div>
      </div>

      <div v-else class="animate-fadeIn">
        <button
          @click="clearSelection"
          class="mb-6 flex items-center text-blue-600 hover:text-blue-800 font-medium transition-colors"
        >
          ← Terug naar alle gemeenten
        </button>

        <div class="bg-white rounded-xl shadow-lg overflow-hidden border border-gray-100">
          <div class="p-6 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
            <h2 class="text-2xl font-bold text-gray-800">{{ selectedMunicipality }}</h2>
            <span class="bg-blue-100 text-blue-800 py-1 px-3 rounded-full text-sm font-semibold">
              Totaal stemmen: {{ totalVotes.toLocaleString('nl-NL') }}
            </span>
          </div>

          <div v-if="loadingResults" class="p-12 text-center text-gray-500">
            Resultaten ophalen...
          </div>

          <div v-else>
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Partij</th>
                  <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Stemmen</th>
                  <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Percentage</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/3">Grafiek</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="(row, index) in results" :key="row.partyName" class="hover:bg-gray-50 transition-colors">
                  <td class="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                    {{ index + 1 }}. {{ row.partyName }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-gray-700">
                    {{ row.validVotes.toLocaleString('nl-NL') }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-gray-900 font-bold">
                    {{ calculatePercentage(row.validVotes) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap align-middle">
                    <div class="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                      <div
                        class="bg-blue-600 h-2.5 rounded-full"
                        :style="{ width: calculatePercentage(row.validVotes) }"
                      ></div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
}
