<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { partyService, getPartyColor, type NationalResult, type PoliticalParty } from '../service/partyService';
import ComparisonChart from '../components/ComparisonChart.vue';
import LevelSelector from '../components/LevelSelector.vue';

// --- API Imports for the 5 Levels ---
import { getMunicipalityNames, getResultsForMunicipality } from '../service/MunicipalityElectionResults_api';
import { getAllKieskringResults } from '../service/KieskringDetails_api';
import { getProvinces } from '../service/ScaledElectionResults_api';

// --- Component State ---
const parties = ref<PoliticalParty[]>([]);
const currentResults = ref<NationalResult[]>([]);
const loading = ref(true);
const loadingChart = ref(false);
const error = ref(false);

const selectedParties = ref<PoliticalParty[]>([]);
const MAX_PARTIES = 5; // Set to 5 to allow broader comparison

// --- New Level & Instance State ---
const LEVELS = ['Nationaal', 'Kieskringen', 'Provincies', 'Gemeentes', 'Stembussen'];
const selectedLevel = ref(LEVELS[0]);

// "SubItems" are the specific options (e.g. "Amsterdam", "Utrecht")
const subItems = ref<string[]>([]);
const selectedSubItem = ref<string | null>(null);
const subItemLoading = ref(false);

// --- Data Loading Logic ---

// 1. Load the master list of parties (for the grid)
const loadParties = async () => {
  loading.value = true;
  error.value = false;
  try {
    parties.value = await partyService.getParties('TK2023');
  } catch (err) {
    error.value = true;
    console.error('Error loading parties:', err);
  } finally {
    loading.value = false;
  }
};

// 2. When Level changes: Load the list of available instances
const prepareLevelData = async (level: string) => {
  subItems.value = [];
  selectedSubItem.value = null;
  currentResults.value = []; // Clear chart

  if (level === 'Nationaal') {
    await loadResultsForInstance(null); // Load national results immediately
    return;
  }

  subItemLoading.value = true;
  try {
    if (level === 'Gemeentes') {
      //
      subItems.value = await getMunicipalityNames();
    } else if (level === 'Kieskringen') {
      //
      const data = await getAllKieskringResults();
      subItems.value = data.map(k => k.name);
    } else if (level === 'Provincies') {
      //
      const data = await getProvinces('TK2023');
      subItems.value = data.map((p: any) => p.name);
    } else if (level === 'Stembussen') {
      console.warn('Polling Station API not implemented');
      subItems.value = ['Stembureau A', 'Stembureau B']; // Mock
    }
  } catch (err) {
    console.error(`Error loading list for ${level}:`, err);
  } finally {
    subItemLoading.value = false;
  }
};

// 3. When Instance changes: Load the chart data
const loadResultsForInstance = async (instanceName: string | null) => {
  loadingChart.value = true;
  error.value = false;
  currentResults.value = [];

  try {
    if (selectedLevel.value === 'Nationaal') {
      //
      currentResults.value = await partyService.getNationalResults('TK2023');
    } else if (selectedLevel.value === 'Gemeentes' && instanceName) {
      //
      const results = await getResultsForMunicipality(instanceName);
      currentResults.value = results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
    } else if (selectedLevel.value === 'Kieskringen' && instanceName) {
      //
      const allData = await getAllKieskringResults();
      const match = allData.find(k => k.name === instanceName);
      if (match) {
        currentResults.value = match.results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
      }
    }
    // Note: Provincies and Stembussen fetching would go here
  } catch (err) {
    error.value = true;
    console.error('Error loading results data:', err);
  } finally {
    loadingChart.value = false;
  }
};

// --- Watchers ---
onMounted(() => {
  loadParties();
});

watch(selectedLevel, (newLevel) => {
  prepareLevelData(newLevel);
}, { immediate: true });

watch(selectedSubItem, (newItem) => {
  if (newItem) loadResultsForInstance(newItem);
});

// --- Computed Properties ---

const selectedPartyCount = computed(() => selectedParties.value.length);

const comparisonData = computed(() => {
  const selectedNames = selectedParties.value.map(p => p.name);
  return currentResults.value
    .filter(p => selectedNames.includes(p.name))
    .map(p => ({ name: p.name, totalVotes: p.totalVotes }));
});

// --- Helper Functions ---

const isSelected = (party: PoliticalParty): boolean => {
  return selectedParties.value.some(p => p.name === party.name);
};

const toggleParty = (party: PoliticalParty) => {
  const index = selectedParties.value.findIndex(p => p.name === party.name);

  if (index === -1) {
    if (selectedParties.value.length < MAX_PARTIES) {
      selectedParties.value.push(party);
    } else {
      console.warn(`Cannot select more than ${MAX_PARTIES} parties.`);
    }
  } else {
    selectedParties.value.splice(index, 1);
  }
};
</script>

<template>
  <div class="p-8 max-w-[1400px] mx-auto min-h-screen bg-gradient-to-br from-gray-50 to-gray-300">
    <h1 class="text-4xl sm:text-3xl text-2xl text-center mb-2 text-gray-900 font-bold">Politieke Partijen</h1>

    <div v-if="loading" class="text-center p-16 flex flex-col items-center gap-4">
      <p>Partijen laden...</p>
    </div>

    <div v-else-if="error" class="text-center p-8 text-xl text-red-700 bg-red-100 rounded-lg m-8">
      Failed to load. Please try again.
    </div>

    <div v-else>
      <div class="bg-white rounded-xl p-6 mb-8 shadow-md border border-gray-200 min-h-[200px]">

        <div class="max-w-4xl mx-auto mb-6">
          <LevelSelector
            v-model="selectedLevel"
            :options="LEVELS"
          />
        </div>

        <div v-if="selectedLevel !== 'Nationaal'" class="flex justify-center mb-6">
          <div class="w-full max-w-md">
            <select
              v-model="selectedSubItem"
              :disabled="subItemLoading"
              class="w-full p-2 border border-gray-300 rounded shadow-sm focus:ring-2 focus:ring-blue-500"
            >
              <option :value="null" disabled>
                {{ subItemLoading ? 'Laden...' : `Kies een ${selectedLevel.slice(0, -1).toLowerCase()}...` }}
              </option>
              <option v-for="item in subItems" :key="item" :value="item">
                {{ item }}
              </option>
            </select>
          </div>
        </div>

        <p class="text-xl font-semibold text-gray-900 mb-6 border-b border-dashed border-gray-200 pb-4">
          Vergelijking ({{ selectedPartyCount }} / {{ MAX_PARTIES }}) - {{ selectedSubItem || selectedLevel }}
        </p>

        <div v-if="loadingChart" class="text-center p-8 text-gray-600">
          <div class="w-8 h-8 border-4 border-gray-200 border-t-blue-500 rounded-full animate-spin mx-auto"></div>
          <p class="mt-2">Resultaten laden...</p>
        </div>

        <div v-else-if="selectedLevel !== 'Nationaal' && !selectedSubItem" class="text-center p-8 text-gray-600 italic bg-gray-50 rounded-lg">
          Selecteer een {{ selectedLevel.slice(0, -1).toLowerCase() }} hierboven om de resultaten te bekijken.
        </div>

        <div v-else-if="selectedPartyCount === 0" class="text-center p-8 text-gray-600 italic bg-gray-50 rounded-lg">
          Selecteer partijen uit de onderstaande lijst om hun resultaten te vergelijken.
        </div>

        <div v-else class="grid grid-cols-1 gap-6 max-w-2xl mx-auto md:max-w-none">
          <ComparisonChart
            :parties="comparisonData"
            metric="totalVotes"
            :title="selectedSubItem ? `Stemmen in ${selectedSubItem}` : 'Stemmen Landelijk'"
          />
        </div>
      </div>

      <div class="bg-white rounded-xl p-6 shadow-md border border-gray-200">
        <h2 class="text-lg font-semibold mb-4">Kies Partijen</h2>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
          <button
            v-for="party in parties"
            :key="party.name"
            @click="toggleParty(party)"
            class="flex items-center p-3 border rounded-lg transition-colors text-left hover:shadow-sm"
            :class="isSelected(party)
              ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500'
              : 'bg-white border-gray-200 hover:border-blue-300'"
          >
            <div
              class="w-4 h-4 rounded-full mr-3 flex-shrink-0"
              :style="{ backgroundColor: getPartyColor(party.name) }"
            ></div>
            <span class="text-sm font-medium text-gray-700 truncate">{{ party.name }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fadeIn {
  animation: fadeIn 0.5s ease-in;
}
</style>
