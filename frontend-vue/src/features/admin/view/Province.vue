<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">

      <section class="mb-12 bg-white rounded-xl p-6 shadow-sm border border-gray-200">
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
          <div>
            <h2 class="text-xl font-bold text-gray-900">
              Partij Vergelijking: <span class="text-blue-600">{{ activeContextName }}</span>
            </h2>
            <p class="text-sm text-gray-500">
              Selecteer tot 5 partijen uit de lijst om de resultaten in {{ activeContextName }} te vergelijken.
            </p>
          </div>
          <button
            @click="resetComparison"
            class="text-xs px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-600 rounded-md transition-colors"
          >
            Selectie wissen
          </button>
        </div>

        <div v-if="selectedParties.length > 0" class="mb-8">
          <ComparisonChart
            :parties="comparisonData"
            metric="totalVotes"
            :title="`Stemmen in ${activeContextName}`"
          />
        </div>
        <div v-else class="text-center py-12 bg-gray-50 rounded-lg border-2 border-dashed border-gray-200 text-gray-500">
          <p class="mb-2">Geen partijen geselecteerd.</p>
          <p class="text-xs">Klik op partijen hieronder om de vergelijking voor {{ activeContextName }} te starten.</p>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3 mt-6 border-t pt-6">
          <button
            v-for="party in allParties"
            :key="party.name"
            @click="toggleParty(party)"
            class="flex items-center p-2 border rounded-lg transition-all text-xs text-left"
            :class="isSelected(party) ? 'bg-blue-50 border-blue-500 ring-1 ring-blue-500' : 'bg-white hover:bg-gray-50 border-gray-200'"
          >
            <div
              class="w-3 h-3 rounded-full mr-2 flex-shrink-0"
              :style="{ backgroundColor: getPartyColor(party.name) }"
            ></div>
            <span class="truncate font-medium text-gray-700">{{ party.name }}</span>
          </button>
        </div>
      </section>

      <header class="mb-12 text-center">
        <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight sm:text-4xl">
          Provincies & Kieskringen
        </h1>
        <p class="mt-3 max-w-2xl mx-auto text-lg text-gray-500">
          Selecteer een regio om de vergelijking hierboven bij te werken naar lokale resultaten.
        </p>
      </header>

      <div v-if="isLoading" class="flex flex-col items-center justify-center py-24">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-gray-200 border-t-blue-600"></div>
        <span class="mt-4 text-gray-500 font-medium">Data ophalen...</span>
      </div>

      <div v-else-if="error" class="max-w-lg mx-auto bg-white border-l-4 border-red-500 rounded-lg shadow-sm p-6">
        <div class="flex items-center">
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Fout bij laden</h3>
            <div class="mt-1 text-sm text-red-700">{{ error.message }}</div>
          </div>
        </div>
      </div>

      <div v-else>
        <div class="bg-white rounded-xl border shadow-sm mb-6" :class="{ 'ring-2 ring-blue-600': national.isOpen }">
          <div @click="toggleNational" class="p-6 cursor-pointer flex justify-between items-center">
            <h2 class="text-xl font-bold">Nederland</h2>
            <div class="flex gap-3 items-center">
              <button
                @click.stop="setContext('Nederland', 'national')"
                class="text-xs px-3 py-1 rounded bg-blue-600 text-white hover:bg-blue-700 transition-colors"
              >
                Bekijk Data
              </button>
              <svg class="w-5 h-5 transition-transform" :class="{ 'rotate-180': national.isOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>

          <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
            <div v-if="national.isOpen">
              <div v-if="national.isLoadingChildren" class="p-6 text-center">Provincies laden...</div>
              <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 p-6">

                <div
                  v-for="province in national.provinces"
                  :key="province.province_id"
                  class="bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all group"
                  :class="{ 'ring-2 ring-blue-500 border-transparent': province.isOpen }"
                >
                  <div @click="toggleProvince(province)" class="p-6 cursor-pointer flex items-center justify-between relative overflow-hidden">
                    <div class="flex items-center gap-4 relative z-10">
                      <div class="w-1.5 h-10 rounded-full" :class="getProvinceColor(province.province_id)"></div>
                      <div>
                        <h2 class="text-lg font-bold text-gray-900 group-hover:text-blue-700">{{ province.name }}</h2>
                        <span class="text-xs text-gray-400 font-medium">{{ province.isOpen ? 'Klik om te sluiten' : 'Klik voor kieskringen' }}</span>
                      </div>
                    </div>
                    <div class="w-9 h-9 rounded-full flex items-center justify-center transition-all" :class="province.isOpen ? 'bg-blue-100 text-blue-600 rotate-180' : 'bg-gray-50 text-gray-400'">
                      <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                    </div>
                  </div>

                  <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
                    <div v-if="province.isOpen" class="border-t border-gray-100 bg-gray-50/50">
                      <ul v-if="province.kieskringen && province.kieskringen.length > 0" class="divide-y divide-gray-100">

                        <li v-for="kieskring in province.kieskringen" :key="kieskring.id" class="group/item">
                          <div class="px-6 py-4 hover:bg-white transition-colors">
                            <div class="flex items-center justify-between cursor-pointer" @click.stop="toggleKieskring(kieskring)">
                              <span class="text-sm font-semibold text-gray-700">{{ kieskring.name }}</span>
                              <div class="flex items-center gap-3">
                                <button
                                  @click.stop="setContext(kieskring.name, 'kieskring')"
                                  class="text-[10px] uppercase font-bold text-blue-600 hover:underline"
                                >
                                  Bekijk Data
                                </button>
                                <svg class="w-4 h-4 transition-transform" :class="{ 'rotate-180': kieskring.isOpen }" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                              </div>
                            </div>

                            <transition name="collapse" @enter="startTransition" @after-enter="endTransition" @leave="startTransition" @after-leave="endTransition">
                              <div v-if="kieskring.isOpen" class="mt-3 ml-5 pl-4 border-l-2 border-gray-200">

                                <div v-if="kieskring.gemeentes && kieskring.gemeentes.length > 0" class="flex flex-wrap gap-2 py-2">
                                  <button
                                    v-for="gm in kieskring.gemeentes"
                                    :key="gm.id"
                                    @click.stop="setContext(gm.name, 'municipality')"
                                    class="px-3 py-1 bg-white border border-gray-200 text-gray-600 text-xs rounded-full shadow-sm hover:text-blue-600 hover:border-blue-200 transition-all"
                                  >
                                    {{ gm.name }}
                                  </button>
                                </div>
                              </div>
                            </transition>
                          </div>
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
import { getPartyColor, partyService, type NationalResult } from '../service/partyService';
import { getResultsForMunicipality } from '../service/MunicipalityElectionResults_api';
import { getAllConstituencyResults, type ConstituencyDataDto } from '../service/ConstituencyDetails_api';

// Existing Hierarchy Logic
const {
  isLoading, error, toggleProvince, toggleKieskring,
  startTransition, endTransition, getProvinceColor, getProvinceBg
} = useProvince();

const { national, toggleNational } = useNationalHierarchy();

// Party Comparison State
const allParties = ref<NationalResult[]>([]);
const selectedParties = ref<NationalResult[]>([]);
const currentContextResults = ref<NationalResult[]>([]);
const activeContextName = ref('Nederland');

onMounted(async () => {
  // Load National data by default
  try {
    const data = await partyService.getNationalResults('TK2023');
    allParties.value = data;
    currentContextResults.value = data;
  } catch (err) {
    console.error("Failed to load initial party data", err);
  }
});

// Context Switching Logic
async function setContext(name: string, type: 'national' | 'municipality' | 'kieskring') {
  activeContextName.value = name;
  try {
    if (type === 'national') {
      currentContextResults.value = await partyService.getNationalResults('TK2023');
    } else if (type === 'municipality') {
      const results = await getResultsForMunicipality(name);
      currentContextResults.value = results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
    } else if (type === 'kieskring') {
      const allKieskringen = await getAllConstituencyResults();
      const match = allKieskringen.find((k: ConstituencyDataDto) => k.name === name);
      if (match) {
        currentContextResults.value = match.results.map(r => ({ name: r.partyName, totalVotes: r.validVotes }));
      }
    }
    // Scroll to chart automatically for better UX
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (err) {
    console.error(`Failed to update context for ${name}`, err);
  }
}

const toggleParty = (party: NationalResult) => {
  const idx = selectedParties.value.findIndex(p => p.name === party.name);
  if (idx > -1) {
    selectedParties.value.splice(idx, 1);
  } else if (selectedParties.value.length < 5) {
    selectedParties.value.push(party);
  }
};

const isSelected = (party: NationalResult) => selectedParties.value.some(p => p.name === party.name);

const resetComparison = () => {
  selectedParties.value = [];
};

const comparisonData = computed(() => {
  const names = selectedParties.value.map(p => p.name);
  return currentContextResults.value.filter(r => names.includes(r.name));
});
</script>

<style scoped>
.collapse-enter-active, .collapse-leave-active {
  transition: height 0.3s ease-in-out, opacity 0.3s ease-in-out;
  overflow: hidden;
}
</style>
