<template>
  <div class="min-h-screen bg-gray-50 p-6 transition-all duration-500 ease-in-out">
    <div class="max-w-7xl mx-auto relative">

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
            <div v-else class="text-center py-20 bg-gray-50 rounded-lg border-2 border-dashed border-gray-200 text-gray-500 mb-8">
              <p>Geen partijen geselecteerd.</p>
              <p class="text-xs mt-2">Selecteer partijen hieronder om resultaten voor {{ activeContextName }} te zien.</p>
            </div>

            <div class="border-t pt-6">
              <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4 gap-3">
                <h3 class="font-bold text-gray-800">Selecteer Partijen (max 5)</h3>
                <div class="flex items-center gap-2">
                  <select v-model="sortBy" class="text-xs border border-gray-300 rounded px-2 py-1 bg-white focus:ring-1 focus:ring-blue-500 outline-none">
                    <option value="name-asc">Naam (A-Z)</option>
                    <option value="seats-desc">Zetels (Hoog-Laag)</option>
                  </select>
                  <button @click="resetComparison" class="text-xs text-red-600 hover:underline">Wissen</button>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <button v-for="party in sortedParties" :key="party.name" @click="toggleParty(party)" class="flex items-center p-3 border rounded-xl transition-all text-left group" :class="isSelected(party) ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500' : 'bg-white hover:border-gray-300 border-gray-100'">
                  <div class="mr-3 flex-shrink-0 w-8 h-8 flex items-center justify-center">
                    <img v-if="getPartyLogo(party.name)" :src="getPartyLogo(party.name)!" :alt="party.name" class="w-full h-full object-contain" />
                    <div v-else class="w-4 h-4 rounded-full" :style="{ backgroundColor: getPartyColor(party.name) }"></div>
                  </div>
                  <div class="flex flex-col overflow-hidden">
                    <span class="text-xs font-semibold text-gray-700 truncate group-hover:text-black">{{ party.name }}</span>
                    <span v-if="calculatedSeats[party.name] !== undefined" class="text-[10px] text-gray-500">{{ calculatedSeats[party.name] }} zetels</span>
                  </div>
                </button>
              </div>
            </div>
          </div>
          <footer class="p-6 border-t bg-gray-50">
            <button @click="isPanelOpen = false" class="w-full py-3 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition-colors shadow-lg">Sluiten</button>
          </footer>
        </aside>
      </Teleport>

      <header
        class="mb-8 text-center pt-4 transition-all duration-700 ease-in-out origin-top"
        :class="{ 'opacity-0 h-0 overflow-hidden mb-0 scale-95': national.isOpen || focusedProvinceId }"
      >
        <h1 class="text-4xl font-extrabold text-gray-900 tracking-tight">Provincies & Kieskringen</h1>
        <p class="mt-4 text-gray-500 text-lg mb-6">Selecteer een regio om de vergelijking aan te passen aan lokale data.</p>

        <div class="flex justify-center items-center gap-3">
          <label for="year-select" class="font-bold text-gray-700">Verkiezingsjaar:</label>
          <select id="year-select" v-model="selectedElection" class="px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 bg-white font-medium text-gray-800 cursor-pointer">
            <option value="TK2025">TK2025</option>
            <option value="TK2023">TK2023</option>
            <option value="TK2021">TK2021</option>
          </select>
        </div>
      </header>

      <div v-if="national.isOpen || focusedProvinceId" class="mb-4 animate-fadeIn">
        <button v-if="focusedProvinceId" @click="clearFocus" class="flex items-center text-blue-600 font-semibold hover:text-blue-800 transition-colors bg-white px-4 py-2 rounded-lg shadow-sm border border-gray-200">
          <span class="text-xl mr-2">←</span> Terug naar Nederland
        </button>
        <button v-else @click="toggleNational" class="flex items-center text-gray-600 font-semibold hover:text-gray-900 transition-colors bg-white px-4 py-2 rounded-lg shadow-sm border border-gray-200">
          <span class="text-xl mr-2">←</span> Terug naar startscherm
        </button>
      </div>

      <div v-if="isLoading" class="flex flex-col items-center justify-center py-24">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-gray-200 border-t-blue-600"></div>
        <span class="mt-4 text-gray-500 font-medium">Data laden...</span>
      </div>

      <div v-else>
        <div
          class="bg-white rounded-2xl border border-gray-200 shadow-sm mb-6 overflow-hidden transition-all duration-500 ease-in-out"
          :class="{
            'ring-2 ring-blue-600 shadow-xl': national.isOpen && !focusedProvinceId,
            'ring-0 border-none shadow-none bg-transparent': !!focusedProvinceId
          }"
        >
          <div v-if="!focusedProvinceId" @click="toggleNational" class="p-6 cursor-pointer flex justify-between items-center hover:bg-gray-50 transition-colors">
            <div class="flex items-center gap-4">
              <div class="p-3 bg-blue-100 text-blue-600 rounded-xl transition-transform duration-500" :class="{ 'scale-110': national.isOpen }">🇳🇱</div>
              <div class="flex flex-col">
                <h2 class="text-xl font-bold text-gray-900 transition-all duration-300" :class="{ 'text-2xl text-blue-700': national.isOpen }">Nederland (Landelijk)</h2>
                <p v-if="national.isOpen" class="text-sm text-gray-500 animate-fadeIn">Klik om te sluiten</p>
              </div>
            </div>

            <div class="flex gap-4 items-center" @click.stop>
              <transition name="fade">
                <div v-if="national.isOpen" class="flex gap-2">
                  <button @click="goToNationalResults" class="text-sm px-3 py-2 rounded bg-blue-50 text-blue-700 hover:bg-blue-100 font-medium transition-colors">Landelijke Uitslag</button>
                  <button @click="setContextAndOpen('Nederland', 'national')" class="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-bold shadow-md hover:bg-blue-700 transition-all active:scale-95">Vergelijk</button>
                </div>
              </transition>
              <div @click.stop="toggleNational" class="p-2 cursor-pointer">
                <svg class="w-5 h-5 transition-transform duration-500 text-gray-400" :class="{ 'rotate-180': national.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
              </div>
            </div>
          </div>

          <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
            <div v-if="national.isOpen || focusedProvinceId" class="bg-gray-50 p-6 border-t border-gray-100 relative min-h-[500px]">
              <TransitionGroup
                name="list"
                tag="div"
                class="grid grid-cols-1 gap-6 transition-all duration-500 ease-in-out"
                :class="focusedProvinceId ? 'grid-cols-1' : 'md:grid-cols-2 lg:grid-cols-3'"
              >
                <div
                  v-for="province in national.provinces"
                  :key="province.province_id"
                  v-show="!focusedProvinceId || focusedProvinceId === province.province_id"
                  class="bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all duration-500 group overflow-hidden list-item"
                  :class="{ 'ring-2 ring-blue-500 shadow-xl scale-[1.005]': focusedProvinceId === province.province_id }"
                >
                  <div @click="handleProvinceClick(province)" class="p-5 cursor-pointer flex items-center justify-between">
                    <div class="flex items-center gap-4">
                      <div class="w-1.5 h-10 rounded-full transition-all duration-500" :class="[getProvinceColor(province.province_id), focusedProvinceId === province.province_id ? 'h-16 w-2' : '']"></div>
                      <div>
                        <h2 class="font-bold text-gray-900 group-hover:text-blue-700 transition-colors" :class="focusedProvinceId === province.province_id ? 'text-2xl' : 'text-base'">{{ province.name }}</h2>
                        <span v-if="focusedProvinceId === province.province_id" class="text-gray-500 text-sm">Klik om te sluiten</span>
                      </div>
                    </div>
                    <svg class="w-5 h-5 text-gray-300 transition-transform duration-500" :class="{ 'rotate-180': province.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                  </div>

                  <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
                    <div v-if="province.isOpen" class="bg-gray-50 border-t border-gray-100">
                      <ul v-if="province.kieskringen && province.kieskringen.length > 0" class="divide-y divide-gray-100">
                        <li v-for="kieskring in province.kieskringen" :key="kieskring.id" class="transition-colors hover:bg-white">
                          <div class="px-6 py-4 flex items-center justify-between">
                            <span class="text-sm font-medium text-gray-700">{{ kieskring.name }}</span>
                            <div class="flex items-center gap-4">
                              <button @click.stop="goToResults(kieskring.name, selectedElection)" class="text-[10px] uppercase font-bold text-gray-500 hover:text-blue-600 transition-colors">Uitslag ({{ selectedElection }})</button>
                              <button @click.stop="setContextAndOpen(kieskring.name, 'kieskring')" class="text-[10px] uppercase tracking-widest font-black text-blue-600 hover:text-blue-800 transition-colors">Vergelijk</button>
                              <button @click.stop="toggleKieskring(kieskring)" class="p-1">
                                <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{ 'rotate-180': kieskring.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                              </button>
                            </div>
                          </div>
                          <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
                            <div v-if="kieskring.isOpen" class="px-8 pb-4 pt-2 bg-gray-50/50 shadow-inner">
                              <div class="flex flex-wrap gap-2">
                                <button v-for="gm in kieskring.gemeentes" :key="gm.id" @click.stop="setContextAndOpen(gm.name, 'municipality')" class="px-3 py-1 bg-white border border-gray-200 text-gray-600 text-xs rounded-full hover:border-blue-500 hover:text-blue-600 transition-all shadow-sm">
                                  {{ gm.name }}
                                </button>
                              </div>
                            </div>
                          </transition>
                        </li>
                      </ul>
                      <div v-else-if="province.isLoadingChildren" class="p-4 text-center text-sm text-gray-400">Kieskringen laden...</div>
                    </div>
                  </transition>
                </div>
              </TransitionGroup>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useProvince, type ProvinceUI } from "@/features/admin/composables/useProvince.ts";
import { useNationalHierarchy } from "@/features/admin/composables/useNationalHierarchy.ts";
import ComparisonChart from '../components/ComparisonChart.vue';
import { getPartyColor, getPartyLogo, partyService, type NationalResult } from '../service/partyService';
import { getResultsForMunicipality } from '../service/MunicipalityElectionResults_api';
import { getAllConstituencyResults, type ConstituencyDataDto } from '../service/ConstituencyDetails_api';

// Composable Hooks
const {
  isLoading, error, toggleProvince, toggleKieskring,
  goToResults, startTransition, endTransition, getProvinceColor
} = useProvince();

const { national, toggleNational, goToNationalResults } = useNationalHierarchy();

// State
const selectedElection = ref('TK2025');
const isPanelOpen = ref(false);
const allParties = ref<NationalResult[]>([]);
const selectedParties = ref<NationalResult[]>([]);
const currentResults = ref<NationalResult[]>([]);
const calculatedSeats = ref<Record<string, number>>({});
const activeContextName = ref('Nederland');
const sortBy = ref('name-asc');
const focusedProvinceId = ref<number | null>(null);
const TOTAL_SEATS = 150;

// Logic
const handleProvinceClick = async (province: ProvinceUI) => {
  if (focusedProvinceId.value === province.province_id) {
    clearFocus();
    if (province.isOpen) await toggleProvince(province);
  } else {
    focusedProvinceId.value = province.province_id;
    if (!province.isOpen) await toggleProvince(province);
  }
};

const clearFocus = () => { focusedProvinceId.value = null; };

onMounted(async () => {
  try {
    const data = await partyService.getNationalResults('TK2023');
    allParties.value = data;
    currentResults.value = data;
    calculatedSeats.value = partyService.calculateSeats(data, TOTAL_SEATS);
  } catch (err) {
    console.error("Fout bij laden data:", err);
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
    console.error(`Fout context: ${name}`, err);
  }
}

const toggleParty = (party: NationalResult) => {
  const idx = selectedParties.value.findIndex(p => p.name === party.name);
  if (idx > -1) selectedParties.value.splice(idx, 1);
  else if (selectedParties.value.length < 5) selectedParties.value.push(party);
};

const isSelected = (party: NationalResult) => selectedParties.value.some(p => p.name === party.name);
const resetComparison = () => selectedParties.value = [];

const sortedParties = computed(() => {
  let list = [...allParties.value];
  if (sortBy.value === 'name-asc') return list.sort((a, b) => a.name.localeCompare(b.name));
  if (sortBy.value === 'seats-desc') return list.sort((a, b) => (calculatedSeats.value[b.name] || 0) - (calculatedSeats.value[a.name] || 0));
  return list;
});

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

.list-move, .list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: scale(0.9); }
.list-leave-active { position: absolute; width: 100%; z-index: 0; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.animate-fadeIn { animation: fadeIn 0.4s ease-out forwards; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
