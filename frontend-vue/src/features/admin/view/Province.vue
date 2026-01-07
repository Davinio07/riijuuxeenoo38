<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">

      <button @click="isPanelOpen = true" class="fixed bottom-8 right-8 z-40 bg-blue-600 text-white p-4 rounded-full shadow-2xl hover:bg-blue-700 transition-all flex items-center gap-2 group">
        <span class="max-w-0 overflow-hidden group-hover:max-w-xs transition-all duration-300 ease-in-out whitespace-nowrap font-bold">Vergelijk Partijen</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
        <div v-if="selectedParties.length > 0" class="absolute -top-2 -right-2 bg-red-500 text-white text-xs w-6 h-6 rounded-full flex items-center justify-center border-2 border-white">
          {{ selectedParties.length }}
        </div>
      </button>

      <Teleport to="body">
        <div v-if="isPanelOpen" @click="isPanelOpen = false" class="fixed inset-0 bg-black/40 z-50 transition-opacity backdrop-blur-sm"></div>
        <aside class="fixed right-0 top-0 h-full bg-white w-full max-w-2xl z-[60] shadow-2xl transform transition-transform duration-300 ease-in-out flex flex-col" :class="isPanelOpen ? 'translate-x-0' : 'translate-x-full'">
          <header class="p-6 border-b flex justify-between items-center bg-gray-50">
            <div>
              <h2 class="text-xl font-bold text-gray-900">Partij Vergelijking</h2>
              <p class="text-sm text-blue-600 font-medium">{{ activeContextName }}</p>
            </div>
            <button @click="isPanelOpen = false" class="p-2 hover:bg-gray-200 rounded-full transition-colors">✕</button>
          </header>
          <div class="flex-1 overflow-y-auto p-6">
            <div v-if="selectedParties.length > 0" class="mb-8">
              <ComparisonChart :parties="comparisonData" metric="totalVotes" :title="`Stemmen in ${activeContextName}`" />
            </div>
            <div class="border-t pt-6">
              <div class="grid grid-cols-2 gap-3">
                <button v-for="party in allParties" :key="party.name" @click="toggleParty(party)" class="flex items-center p-3 border rounded-xl transition-all text-left group" :class="isSelected(party) ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500' : 'bg-white hover:border-gray-300 border-gray-100'">
                  <span class="text-xs font-semibold text-gray-700 truncate group-hover:text-black">{{ party.name }}</span>
                </button>
              </div>
            </div>
          </div>
          <footer class="p-6 border-t bg-gray-50">
            <button @click="isPanelOpen = false" class="w-full py-3 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition-colors shadow-lg">Sluiten</button>
          </footer>
        </aside>
      </Teleport>

      <header class="mb-12 text-center pt-8">
        <h1 class="text-4xl font-extrabold text-gray-900 tracking-tight">Provincies & Kieskringen</h1>
        <p class="mt-4 text-gray-500 text-lg mb-6">Selecteer een regio om de vergelijking aan te passen aan lokale data.</p>

        <div class="flex justify-center items-center gap-3">
          <label for="year-select" class="font-bold text-gray-700">Verkiezingsjaar:</label>
          <select
            id="year-select"
            v-model="selectedElection"
            class="px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 bg-white font-medium text-gray-800 cursor-pointer"
          >
            <option value="TK2025">TK2025</option>
            <option value="TK2023">TK2023</option>
            <option value="TK2021">TK2021</option>
          </select>
        </div>
      </header>

      <div v-if="isLoading" class="flex flex-col items-center justify-center py-24">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-gray-200 border-t-blue-600"></div>
        <span class="mt-4 text-gray-500 font-medium">Data laden...</span>
      </div>

      <div v-else>
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm mb-6 overflow-hidden" :class="{ 'ring-2 ring-blue-600': national.isOpen }">
          <div @click="toggleNational" class="p-6 cursor-pointer flex justify-between items-center hover:bg-gray-50 transition-colors">
            <div class="flex items-center gap-4">
              <div class="p-3 bg-blue-100 text-blue-600 rounded-xl">🇳🇱</div>
              <h2 class="text-xl font-bold text-gray-900">Nederland (Landelijk)</h2>
            </div>
            <div class="flex gap-4 items-center">
              <button @click.stop="goToNationalResults" class="text-sm px-3 py-2 rounded bg-blue-50 text-blue-700 hover:bg-blue-100 font-medium transition-colors">Landelijke Uitslag</button>
              <button @click.stop="setContextAndOpen('Nederland', 'national')" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-bold shadow-md hover:bg-blue-700 transition-all active:scale-95">Data Vergelijken</button>
              <svg class="w-5 h-5 transition-transform text-gray-400" :class="{ 'rotate-180': national.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
            </div>
          </div>

          <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
            <div v-if="national.isOpen" class="bg-gray-50 p-6 border-t border-gray-100">
              <div v-if="national.isLoadingChildren" class="text-center py-10 text-gray-500">Provincies laden...</div>
              <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div v-for="province in national.provinces" :key="province.province_id" class="bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all group overflow-hidden">
                  <div @click="toggleProvince(province)" class="p-5 cursor-pointer flex items-center justify-between">
                    <div class="flex items-center gap-4">
                      <div class="w-1.5 h-10 rounded-full" :class="getProvinceColor(province.province_id)"></div>
                      <h2 class="font-bold text-gray-900 group-hover:text-blue-700">{{ province.name }}</h2>
                    </div>
                    <svg class="w-5 h-5 text-gray-300 transition-transform" :class="{ 'rotate-180': province.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                  </div>

                  <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
                    <div v-if="province.isOpen" class="bg-gray-50 border-t border-gray-100">
                      <ul v-if="province.kieskringen && province.kieskringen.length > 0" class="divide-y divide-gray-100">
                        <li v-for="kieskring in province.kieskringen" :key="kieskring.id">
                          <div class="px-6 py-4 flex items-center justify-between hover:bg-white transition-colors">
                            <span class="text-sm font-medium text-gray-700">{{ kieskring.name }}</span>
                            <div class="flex items-center gap-4">
                              <button
                                @click.stop="goToResults(kieskring.name, selectedElection)"
                                class="text-[10px] uppercase font-bold text-gray-500 hover:text-blue-600 transition-colors"
                              >
                                Uitslag ({{ selectedElection }})
                              </button>

                              <button @click.stop="setContextAndOpen(kieskring.name, 'kieskring')" class="text-[10px] uppercase tracking-widest font-black text-blue-600 hover:text-blue-800 transition-colors">Data Vergelijken</button>
                              <button @click.stop="toggleKieskring(kieskring)" class="p-1">
                                <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{ 'rotate-180': kieskring.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                              </button>
                            </div>
                          </div>
                          <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
                            <div v-if="kieskring.isOpen" class="px-8 pb-4 pt-2">
                              <div class="flex flex-wrap gap-2">
                                <button v-for="gm in kieskring.gemeentes" :key="gm.id" @click.stop="setContextAndOpen(gm.name, 'municipality')" class="px-3 py-1 bg-white border border-gray-200 text-gray-600 text-xs rounded-full hover:border-blue-500 hover:text-blue-600 transition-all shadow-sm">
                                  {{ gm.name }}
                                </button>
                              </div>
                            </div>
                          </transition>
                        </li>
                      </ul>
                    </div>
                  </transition>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useProvince } from "@/features/admin/composables/useProvince.ts";
import { useNationalHierarchy } from "@/features/admin/composables/useNationalHierarchy.ts";
import ComparisonChart from '../components/ComparisonChart.vue';
import { getPartyColor, getPartyLogo, partyService, type NationalResult } from '../service/partyService';
import { getResultsForMunicipality } from '../service/MunicipalityElectionResults_api';
import { getAllConstituencyResults, type ConstituencyDataDto } from '../service/ConstituencyDetails_api';

const {
  isLoading, error, toggleProvince, toggleKieskring,
  goToResults, startTransition, endTransition, getProvinceColor
} = useProvince();

const { national, toggleNational, goToNationalResults } = useNationalHierarchy();

// NEW: State for the year selector
const selectedElection = ref('TK2025');

// Panel & Comparison State
const isPanelOpen = ref(false);
const allParties = ref<NationalResult[]>([]);
const selectedParties = ref<NationalResult[]>([]);
const currentResults = ref<NationalResult[]>([]);
const calculatedSeats = ref<Record<string, number>>({});
const activeContextName = ref('Nederland');
const TOTAL_SEATS = 150;

onMounted(async () => {
  try {
    const data = await partyService.getNationalResults('TK2023');
    allParties.value = data;
    currentResults.value = data;
    calculatedSeats.value = partyService.calculateSeats(data, TOTAL_SEATS);
  } catch (err) {
    console.error("Fout bij laden nationale partijen:", err);
  }
});

async function setContextAndOpen(name: string, type: string) {
  activeContextName.value = name;
  isPanelOpen.value = true;
  try {
    let results: NationalResult[] = [];
    if (type === 'national') {
      results = await partyService.getNationalResults('TK2023');
    } else if (type === 'municipality') {
      const apiResults = await getResultsForMunicipality(name);
      results = apiResults.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
    } else if (type === 'kieskring') {
      const allKies = await getAllConstituencyResults();
      const match = allKies.find((k: ConstituencyDataDto) => k.name === name);
      if (match) results = match.results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
    }
    currentResults.value = results;
    calculatedSeats.value = partyService.calculateSeats(results, TOTAL_SEATS);
  } catch (err) {
    console.error(`Fout bij ophalen contextdata voor ${name}:`, err);
  }
}

const toggleParty = (party: NationalResult) => {
  const idx = selectedParties.value.findIndex(p => p.name === party.name);
  if (idx > -1) selectedParties.value.splice(idx, 1);
  else if (selectedParties.value.length < 5) selectedParties.value.push(party);
};

const isSelected = (party: NationalResult) => selectedParties.value.some(p => p.name === party.name);
const resetComparison = () => selectedParties.value = [];

const comparisonData = computed(() => {
  const selectedNames = selectedParties.value.map(p => p.name);
  return currentResults.value
    .filter(r => selectedNames.includes(r.name))
    .map(p => ({
      name: p.name,
      totalVotes: p.totalVotes,
      seats: calculatedSeats.value[p.name] || 0
    }));
});
</script>

<style scoped>
.collapse-enter-active, .collapse-leave-active {
  transition: all 0.3s ease-in-out;
  overflow: hidden;
}
</style>
