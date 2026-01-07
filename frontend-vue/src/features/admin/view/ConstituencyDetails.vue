<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  getAllConstituencyResults,
  type ConstituencyDataDto,
  type ConstituencyResultDto
} from '../service/ConstituencyDetails_api';
import { getPartyColor, getPartyLogo } from '../service/partyService';

const route = useRoute();
const router = useRouter();

// State
const loading = ref(true);
const error = ref<string | null>(null);
const currentConstituency = ref<ConstituencyDataDto | null>(null);
const electionId = ref<string>('TK2025');

// Filter & Sort State
const searchQuery = ref('');
const sortBy = ref('votes-desc'); // Default to highest votes

// --- Computed Properties ---

// Calculate total votes for the current constituency
const totalVotes = computed(() => {
  if (!currentConstituency.value) return 0;
  return currentConstituency.value.results.reduce((sum, r) => sum + r.validVotes, 0);
});

// Process the parties (Filter & Sort)
const displayedParties = computed(() => {
  if (!currentConstituency.value) return [];

  let list = [...currentConstituency.value.results];

  // 1. Filter by Search
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();
    list = list.filter(r => r.partyName.toLowerCase().includes(q));
  }

  // 2. Sort
  return list.sort((a, b) => {
    switch (sortBy.value) {
      case 'votes-desc': return b.validVotes - a.validVotes;
      case 'votes-asc': return a.validVotes - b.validVotes;
      case 'name-asc': return a.partyName.localeCompare(b.partyName);
      case 'name-desc': return b.partyName.localeCompare(a.partyName);
      default: return 0;
    }
  });
});

// --- Helpers ---

const getPercentage = (votes: number) => {
  if (totalVotes.value === 0) return '0.0%';
  return ((votes / totalVotes.value) * 100).toFixed(1) + '%';
};

// --- Data Loading ---

const loadData = async () => {
  loading.value = true;
  error.value = null;

  try {
    // 1. Get Params
    const nameParam = route.query.name as string;
    electionId.value = (route.query.electionId as string) || 'TK2025';

    if (!nameParam) {
      error.value = "Geen kieskring geselecteerd.";
      loading.value = false;
      return;
    }

    // 2. Fetch All Data (API returns all, we find the specific one)
    // Optimization note: Ideally the backend would have a getByName endpoint, but we filter client-side for now.
    const allData = await getAllConstituencyResults(electionId.value);

    // 3. Find the specific constituency
    const match = allData.find(k =>
      k.name.toLowerCase() === nameParam.toLowerCase() ||
      k.name.toLowerCase().includes(nameParam.toLowerCase())
    );

    if (match) {
      currentConstituency.value = match;
    } else {
      error.value = `Kieskring '${nameParam}' niet gevonden in data voor ${electionId.value}.`;
    }

  } catch (err) {
    console.error(err);
    error.value = "Fout bij het ophalen van de data.";
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadData();
});

// Reload if the route changes (e.g. switching year via URL)
watch(() => route.query, loadData);

const goBack = () => {
  router.back();
};
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-300 p-6 md:p-8 font-sans">

    <div v-if="loading" class="flex flex-col items-center justify-center min-h-[50vh]">
      <div class="w-10 h-10 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin mb-4"></div>
      <p class="text-gray-600 font-medium">Uitslagen ophalen...</p>
    </div>

    <div v-else-if="error" class="max-w-2xl mx-auto bg-white p-8 rounded-xl shadow-lg text-center mt-10">
      <div class="text-red-500 text-5xl mb-4">⚠️</div>
      <h2 class="text-xl font-bold text-gray-800 mb-2">Er ging iets mis</h2>
      <p class="text-gray-600 mb-6">{{ error }}</p>
      <button @click="goBack" class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
        Terug
      </button>
    </div>

    <div v-else-if="currentConstituency" class="max-w-[1400px] mx-auto">

      <div class="mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <button @click="goBack" class="text-sm text-blue-600 hover:text-blue-800 font-medium mb-2 flex items-center gap-1 transition-colors">
            ← Terug naar overzicht
          </button>
          <h1 class="text-3xl md:text-4xl font-extrabold text-gray-900 tracking-tight">
            {{ currentConstituency.name }}
          </h1>
          <p class="text-gray-500 mt-1 text-lg">
            Uitslag {{ electionId }} • <span class="font-semibold text-gray-700">{{ totalVotes.toLocaleString() }}</span> uitgebrachte stemmen
          </p>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-md border border-gray-200 p-6">

        <div class="flex flex-col md:flex-row justify-between items-center mb-6 gap-4 border-b border-gray-100 pb-6">
          <h2 class="text-xl font-bold text-gray-800 self-start md:self-center">
            Resultaten per partij
          </h2>

          <div class="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
            <div class="relative w-full sm:w-64">
              <span class="absolute left-3 top-2.5 text-gray-400">🔍</span>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Zoek partij..."
                class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-shadow"
              />
            </div>

            <select
              v-model="sortBy"
              class="px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 focus:ring-2 focus:ring-blue-500 cursor-pointer font-medium text-gray-700"
            >
              <option value="votes-desc">Meeste stemmen</option>
              <option value="votes-asc">Minste stemmen</option>
              <option value="name-asc">Naam (A-Z)</option>
              <option value="name-desc">Naam (Z-A)</option>
            </select>
          </div>
        </div>

        <div v-if="displayedParties.length === 0" class="text-center py-12 bg-gray-50 rounded-lg border border-dashed border-gray-300">
          <p class="text-gray-500 italic">Geen partijen gevonden die voldoen aan de zoekopdracht.</p>
        </div>

        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 animate-fadeIn">
          <div
            v-for="(party, index) in displayedParties"
            :key="party.partyName"
            class="group bg-white border border-gray-200 rounded-xl p-4 hover:shadow-lg hover:border-blue-300 transition-all duration-200 relative overflow-hidden"
          >
            <span class="absolute -right-2 -top-4 text-6xl font-black text-gray-50 opacity-50 group-hover:opacity-10 transition-opacity select-none z-0">
              {{ index + 1 }}
            </span>

            <div class="relative z-10 flex items-start gap-4">
              <div class="flex-shrink-0">
                <div class="w-12 h-12 rounded-lg flex items-center justify-center bg-gray-50 border border-gray-100 p-1">
                  <img
                    v-if="getPartyLogo(party.partyName)"
                    :src="getPartyLogo(party.partyName)!"
                    :alt="party.partyName"
                    class="w-full h-full object-contain"
                  />
                  <div
                    v-else
                    class="w-8 h-8 rounded-full shadow-sm"
                    :style="{ backgroundColor: getPartyColor(party.partyName) }"
                  ></div>
                </div>
              </div>

              <div class="flex-1 min-w-0">
                <h3 class="font-bold text-gray-900 truncate text-lg group-hover:text-blue-600 transition-colors" :title="party.partyName">
                  {{ party.partyName }}
                </h3>

                <div class="mt-2 flex items-baseline gap-2">
                  <span class="text-2xl font-black text-gray-800">
                    {{ getPercentage(party.validVotes) }}
                  </span>
                  <span class="text-sm text-gray-500 font-medium">
                    ({{ party.validVotes.toLocaleString() }} stemmen)
                  </span>
                </div>

                <div class="w-full bg-gray-100 rounded-full h-1.5 mt-3 overflow-hidden">
                  <div
                    class="h-full rounded-full transition-all duration-1000 ease-out"
                    :style="{
                      width: getPercentage(party.validVotes),
                      backgroundColor: getPartyColor(party.partyName)
                    }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fadeIn {
  animation: fadeIn 0.4s ease-out forwards;
}
</style>
