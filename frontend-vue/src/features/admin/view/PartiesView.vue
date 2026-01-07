<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
  partyService,
  getPartyColor,
  getPartyLogo,
  type NationalResult,
  type PoliticalParty
} from '../service/partyService';
import ComparisonChart from '../components/ComparisonChart.vue';
import LevelSelector from '../components/LevelSelector.vue';

// --- API Imports for the Levels ---
import { getMunicipalityNames, getResultsForMunicipality } from '../service/MunicipalityElectionResults_api';
import { getAllConstituencyResults, type ConstituencyDataDto } from '../service/ConstituencyDetails_api';

// Extend PoliticalParty for sorting
interface SortableParty extends PoliticalParty {
  nationalSeats?: number;
}

const route = useRoute();
const parties = ref<PoliticalParty[]>([]);
const currentResults = ref<NationalResult[]>([]);
const previousResults = ref<Record<string, number>>({}); // For Seat Change
const loading = ref(true);
const loadingChart = ref(false);
const error = ref(false);

const selectedParties = ref<PoliticalParty[]>([]);
const MAX_PARTIES = 8; // Increased for deeper analysis
const TOTAL_SEATS = 150; // Total seats in Dutch House of Representatives
const MAJORITY_THRESHOLD = 76;

// --- Election Year State ---
const availableElections = ['TK2025', 'TK2023', 'TK2021'];
const selectedElection = ref(availableElections[1]); // Default to TK2023

// --- Search & Sort State ---
const searchQuery = ref('');
const sortBy = ref('seats-desc'); // Default to seats-desc for better data priority

// --- Level & Instance State ---
const LEVELS = ['Nationaal', 'Kieskringen', 'Gemeentes'];
const selectedLevel = ref(LEVELS[0]);

// "SubItems" are the specific options (e.g. "Amsterdam", "Utrecht")
const subItems = ref<string[]>([]);
const selectedSubItem = ref<string | null>(null);
const subItemLoading = ref(false);
const pendingSubItem = ref<string | null>(null);

// --- Dynamic Seat Calculation State ---
const calculatedSeats = ref<Record<string, number>>({});

// --- NEW FEATURES LOGIC ---

// 1. Coalition Builder
const coalitionSeats = computed(() => {
  return selectedParties.value.reduce((sum, p) => sum + (calculatedSeats.value[p.name] || 0), 0);
});
const coalitionPercentage = computed(() => (coalitionSeats.value / TOTAL_SEATS) * 100);

// 2. Seat Change Analysis
const getSeatChange = (partyName: string) => {
  const current = calculatedSeats.value[partyName] || 0;
  const previous = previousResults.value[partyName] || 0;
  return current - previous;
};

// 3. Data Export (CSV)
const exportToCSV = () => {
  const rows = [
    ['Partij', 'Stemmen', 'Percentage', 'Zetels'],
    ...displayedParties.value.map(p => [
      p.name,
      currentResults.value.find(r => r.name === p.name)?.totalVotes || 0,
      getVotePercentage(p.name) + '%',
      calculatedSeats.value[p.name] || 0
    ])
  ];
  const csvContent = "data:text/csv;charset=utf-8," + rows.map(e => e.join(",")).join("\n");
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", `verkiezingsdata_${selectedLevel.value}_${selectedElection.value}.csv`);
  document.body.appendChild(link);
  link.click();
};

const getVotePercentage = (partyName: string) => {
  const totalVotes = currentResults.value.reduce((sum, r) => sum + r.totalVotes, 0);
  const partyVotes = currentResults.value.find(r => r.name === partyName)?.totalVotes || 0;
  return totalVotes > 0 ? ((partyVotes / totalVotes) * 100).toFixed(2) : '0.00';
};

// --- Data Loading Logic ---

const loadParties = async () => {
  loading.value = true;
  error.value = false;
  try {
    parties.value = await partyService.getNationalResults(selectedElection.value);

    // Load previous year for seat change analysis
    const prevYearIndex = availableElections.indexOf(selectedElection.value) + 1;
    if (prevYearIndex < availableElections.length) {
      const prevData = await partyService.getNationalResults(availableElections[prevYearIndex]);
      previousResults.value = partyService.calculateSeats(prevData, TOTAL_SEATS);
    } else {
      previousResults.value = {};
    }
  } catch (err) {
    error.value = true;
    console.error('Error loading parties:', err);
  } finally {
    loading.value = false;
  }
};

const prepareLevelData = async (level: string) => {
  subItems.value = [];
  selectedSubItem.value = null;
  currentResults.value = [];
  calculatedSeats.value = {};

  if (level === 'Nationaal') {
    await loadResultsForInstance(null);
    return;
  }

  subItemLoading.value = true;
  try {
    if (level === 'Gemeentes') {
      subItems.value = await getMunicipalityNames();
    } else if (level === 'Kieskringen') {
      const data = await getAllConstituencyResults();
      subItems.value = data.map((k: ConstituencyDataDto) => k.name);
    }
  } catch (err) {
    console.error(`Error loading list for ${level}:`, err);
  } finally {
    subItemLoading.value = false;

    if (pendingSubItem.value && subItems.value.length > 0) {
      const target = pendingSubItem.value.trim().toLowerCase();
      const match = subItems.value.find(item => {
        const val = item.trim().toLowerCase();
        return val === target || val.includes(target) || target.includes(val);
      });

      if (match) {
        selectedSubItem.value = match;
      }
      pendingSubItem.value = null;
    }
  }
};

const loadResultsForInstance = async (instanceName: string | null) => {
  loadingChart.value = true;
  error.value = false;
  currentResults.value = [];

  try {
    if (selectedLevel.value === 'Nationaal') {
      currentResults.value = await partyService.getNationalResults(selectedElection.value);
    } else if (selectedLevel.value === 'Gemeentes' && instanceName) {
      const results = await getResultsForMunicipality(instanceName);
      currentResults.value = results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
    } else if (selectedLevel.value === 'Kieskringen' && instanceName) {
      const allData = await getAllConstituencyResults();
      const match = allData.find((k: ConstituencyDataDto) => k.name === instanceName);
      if (match) {
        currentResults.value = match.results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
      }
    }

    if (currentResults.value.length > 0) {
      calculatedSeats.value = partyService.calculateSeats(currentResults.value, TOTAL_SEATS);
    } else {
      calculatedSeats.value = {};
    }

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
  const qLevel = route.query.level as string;
  const qName = route.query.name as string;
  if (qLevel && LEVELS.includes(qLevel)) {
    if (qName) pendingSubItem.value = qName;
    selectedLevel.value = qLevel;
  }
});

watch(selectedElection, () => {
  loadParties();
  if (selectedLevel.value === 'Nationaal') {
    loadResultsForInstance(null);
  } else if (selectedSubItem.value) {
    loadResultsForInstance(selectedSubItem.value);
  }
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
    .map((p: NationalResult) => ({
      name: p.name,
      totalVotes: p.totalVotes,
      seats: calculatedSeats.value[p.name] || 0
    }));
});

const displayedParties = computed(() => {
  let list = [...parties.value] as SortableParty[];
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(p => p.name.toLowerCase().includes(q));
  }
  list = list.filter(party => {
    const result = currentResults.value.find(r => r.name === party.name);
    return result && result.totalVotes > 0;
  });

  return list.sort((a: SortableParty, b: SortableParty) => {
    if (sortBy.value === 'name-asc') return a.name.localeCompare(b.name);
    if (sortBy.value === 'name-desc') return b.name.localeCompare(a.name);
    const seatsA = calculatedSeats.value[a.name] || 0;
    const seatsB = calculatedSeats.value[b.name] || 0;
    if (sortBy.value === 'seats-desc') return seatsB - seatsA;
    if (sortBy.value === 'seats-asc') return seatsA - seatsB;
    return 0;
  });
});

const isSelected = (party: PoliticalParty): boolean => {
  return selectedParties.value.some(p => p.name === party.name);
};

const toggleParty = (party: PoliticalParty) => {
  const index = selectedParties.value.findIndex(p => p.name === party.name);
  if (index === -1) {
    if (selectedParties.value.length < MAX_PARTIES) {
      selectedParties.value.push(party);
    }
  } else {
    selectedParties.value.splice(index, 1);
  }
};
</script>

<template>
  <div class="p-8 max-w-[1400px] mx-auto min-h-screen bg-gradient-to-br from-gray-50 to-gray-300">
    <div class="flex flex-col md:flex-row justify-between items-center mb-4 gap-4">
      <h1 class="text-4xl sm:text-3xl text-2xl text-gray-900 font-bold">Politieke Partijen</h1>
      <button @click="exportToCSV" class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg font-bold transition flex items-center gap-2">
         Exporteer naar CSV
      </button>
    </div>

    <div v-if="loading" class="text-center p-16 flex flex-col items-center gap-4">
      <div class="w-8 h-8 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin mx-auto"></div>
      <p>Partijen laden...</p>
    </div>

    <div v-else-if="error" class="text-center p-8 text-xl text-red-700 bg-red-100 rounded-lg m-8">
      Failed to load. Please try again.
    </div>

    <div v-else>
      <div class="bg-white rounded-xl p-6 mb-8 shadow-md border border-gray-200 min-h-[200px]">

        <div class="max-w-4xl mx-auto mb-8 bg-gray-50 p-4 rounded-lg border border-gray-100">
          <div class="flex justify-between items-end mb-2">
            <h2 class="text-sm font-bold text-gray-700">Coalitie Meerderheid Tracker</h2>
            <span class="text-lg font-black" :class="coalitionSeats >= MAJORITY_THRESHOLD ? 'text-green-600' : 'text-blue-600'">
              {{ coalitionSeats }} / {{ TOTAL_SEATS }} zetels
            </span>
          </div>
          <div class="w-full bg-gray-200 h-3 rounded-full overflow-hidden relative">
            <div class="h-full transition-all duration-500" :class="coalitionSeats >= MAJORITY_THRESHOLD ? 'bg-green-500' : 'bg-blue-500'" :style="{ width: coalitionPercentage + '%' }"></div>
            <div class="absolute top-0 bottom-0 border-l-2 border-red-500" :style="{ left: (MAJORITY_THRESHOLD / TOTAL_SEATS * 100) + '%' }"></div>
          </div>
          <div class="flex justify-between mt-1 text-[10px] font-bold">
            <span class="text-gray-400">0</span>
            <span class="text-red-500">Meerderheid (76)</span>
            <span class="text-gray-400">150</span>
          </div>
        </div>

        <div class="max-w-4xl mx-auto mb-6 flex flex-col sm:flex-row gap-4 items-center justify-center">
          <div class="flex items-center gap-2">
            <label for="election-year" class="text-sm font-bold text-gray-700">Verkiezingsjaar:</label>
            <select
              id="election-year"
              v-model="selectedElection"
              class="p-2 border border-gray-300 rounded shadow-sm bg-white focus:ring-2 focus:ring-blue-500 outline-none cursor-pointer text-sm font-medium"
            >
              <option v-for="year in availableElections" :key="year" :value="year">
                {{ year }}
              </option>
            </select>
          </div>

          <div class="w-full sm:flex-1">
            <LevelSelector
              v-model="selectedLevel"
              :options="LEVELS"
            />
          </div>
        </div>

        <div v-if="selectedLevel !== 'Nationaal'" class="flex justify-center mb-6">
          <div class="w-full max-w-md">
            <select
              v-model="selectedSubItem"
              :disabled="subItemLoading"
              class="w-full p-2 border border-gray-300 rounded shadow-sm focus:ring-2 focus:ring-blue-500 cursor-pointer"
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
          Vergelijking ({{ selectedPartyCount }} / {{ MAX_PARTIES }}) - {{ selectedSubItem || selectedLevel }} ({{ selectedElection }})
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
            :title="selectedSubItem ? `Stemmen in ${selectedSubItem} (${selectedElection})` : `Stemmen Landelijk (${selectedElection})`"
          />
        </div>
      </div>

      <div class="bg-white rounded-xl p-6 shadow-md border border-gray-200">

        <div class="flex flex-col md:flex-row md:items-center justify-between mb-6 gap-4 border-b border-gray-100 pb-4">
          <h2 class="text-lg font-semibold text-gray-800">
            Kies Partijen
          </h2>

          <div class="flex flex-col sm:flex-row gap-3">
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Zoek partij..."
                class="pl-3 pr-8 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 text-sm w-full sm:w-48"
              />
              <div v-if="searchQuery" @click="searchQuery = ''" class="absolute right-2 top-2.5 cursor-pointer text-gray-400 hover:text-gray-600">✕</div>
            </div>

            <select
              v-model="sortBy"
              class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 text-sm bg-white cursor-pointer"
            >
              <option value="name-asc">Naam (A-Z)</option>
              <option value="name-desc">Naam (Z-A)</option>
              <option value="seats-desc">Zetels (Hoog-Laag)</option>
              <option value="seats-asc">Zetels (Laag-Hoog)</option>
            </select>
          </div>
        </div>

        <div v-if="displayedParties.length === 0" class="text-center py-8 text-gray-500 italic">
          Geen partijen gevonden die voldoen aan je zoekopdracht.
        </div>

        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 animate-fadeIn">
          <button
            v-for="party in displayedParties"
            :key="party.name"
            @click="toggleParty(party)"
            class="flex items-center p-3 border rounded-lg transition-colors text-left hover:shadow-md group relative overflow-hidden"
            :class="isSelected(party)
              ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500'
              : 'bg-white border-gray-200 hover:border-blue-300'"
          >
            <div class="mr-3 flex-shrink-0 w-8 h-8 flex items-center justify-center">
              <img
                v-if="getPartyLogo(party.name)"
                :src="getPartyLogo(party.name)!"
                :alt="party.name"
                class="w-full h-full object-contain"
              />

              <div
                v-else
                class="w-6 h-6 rounded-full border border-gray-100 shadow-sm"
                :style="{ backgroundColor: getPartyColor(party.name) }"
              ></div>
            </div>

            <div class="flex flex-col overflow-hidden w-full">
              <div class="flex justify-between items-center w-full">
                <span class="text-sm font-medium text-gray-700 truncate group-hover:text-gray-900 transition-colors">
                  {{ party.name }}
                </span>
                <span v-if="getSeatChange(party.name) !== 0" class="text-[10px] px-1.5 py-0.5 rounded font-bold" :class="getSeatChange(party.name) > 0 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'">
                  {{ getSeatChange(party.name) > 0 ? '+' : '' }}{{ getSeatChange(party.name) }}
                </span>
              </div>
              <div class="flex justify-between mt-1">
                <span class="text-xs text-blue-600 font-medium">
                  {{ calculatedSeats[party.name] || 0 }} zetels
                </span>
                <span class="text-xs text-gray-500 font-bold">
                  {{ getVotePercentage(party.name) }}%
                </span>
              </div>
            </div>

            <div v-if="isSelected(party)" class="absolute right-1 top-1 text-blue-600">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </div>
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<style>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out;
}
</style>
