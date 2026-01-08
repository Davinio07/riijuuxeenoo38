<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { getCandidates, getAllPartiesForFilters } from '@/features/admin/service/ScaledElectionResults_api.ts';

interface CandidateData {
  id: number;
  firstName: string;
  lastName: string;
  locality: string;
  gender: string;
  listName: string;
}

interface PartyFilter {
  id: number;
  name: string;
}

// --- State ---
const electionId = ref('TK2023'); // Standaard geselecteerd jaar
const candidates = ref<CandidateData[]>([]);
const parties = ref<PartyFilter[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// Filters
const selectedPartyId = ref<number | null>(null);
const selectedGender = ref<string | null>(null);
const searchQuery = ref('');

// --- Data laden ---
async function loadParties() {
  try {
    // Haal partijen op specifiek voor het gekozen jaar
    parties.value = await getAllPartiesForFilters(electionId.value);
  } catch (err) {
    console.error('Fout bij laden partijen:', err);
  }
}

async function loadCandidates() {
  loading.value = true;
  error.value = null;
  try {
    // Geef electionId mee aan de API service
    candidates.value = await getCandidates(
      electionId.value,
      selectedPartyId.value,
      selectedGender.value
    );
  } catch (err) {
    error.value = 'Kon kandidaten niet ophalen.';
    console.error(err);
  } finally {
    loading.value = false;
  }
}

// --- Watchers ---
// Als het jaartal verandert: reset filters en laad alles opnieuw
watch(electionId, async () => {
  selectedPartyId.value = null;
  searchQuery.value = '';
  await loadParties();
  await loadCandidates();
});

// Als filters veranderen: herlaad de kandidatenlijst
watch([selectedPartyId, selectedGender], () => {
  loadCandidates();
});

// --- Computed ---
// Zoekfilter (client-side bovenop de API filters)
const filteredCandidates = computed(() => {
  if (!searchQuery.value) return candidates.value;
  const q = searchQuery.value.toLowerCase();
  return candidates.value.filter(c =>
    c.firstName.toLowerCase().includes(q) ||
    c.lastName.toLowerCase().includes(q) ||
    c.locality.toLowerCase().includes(q)
  );
});

const totalCandidates = computed(() => filteredCandidates.value.length);

onMounted(async () => {
  await loadParties();
  await loadCandidates();
});
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Kandidatenoverzicht</h1>
        <p class="text-gray-600">Beheer en bekijk kandidaten per verkiezingsjaar</p>
      </div>

      <div class="flex items-center gap-3 bg-white p-2 rounded-xl shadow-sm border">
        <span class="text-sm font-semibold text-gray-500 ml-2">VERKIEZING:</span>
        <select
          v-model="electionId"
          class="bg-gray-50 border-none text-blue-600 font-bold py-2 px-4 rounded-lg focus:ring-2 focus:ring-blue-500"
        >
          <option value="TK2025">Tweede Kamer 2025</option>
          <option value="TK2023">Tweede Kamer 2023</option>
          <option value="TK2021">Tweede Kamer 2021</option>
        </select>
      </div>
    </div>

    <div class="bg-white p-4 rounded-xl shadow-sm border mb-6 flex flex-wrap gap-4 items-center">
      <div class="flex-1 min-w-[200px]">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Zoek op naam of woonplaats..."
          class="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <select v-model="selectedPartyId" class="border rounded-lg px-4 py-2 bg-white">
        <option :value="null">Alle Partijen</option>
        <option v-for="party in parties" :key="party.id" :value="party.id">
          {{ party.name }}
        </option>
      </select>

      <select v-model="selectedGender" class="border rounded-lg px-4 py-2 bg-white">
        <option :value="null">Alle Geslachten</option>
        <option value="male">Man</option>
        <option value="female">Vrouw</option>
      </select>

      <div class="text-sm font-medium text-gray-500">
        {{ totalCandidates }} kandidaten gevonden
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
    </div>

    <div v-else-if="error" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ error }}
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="candidate in filteredCandidates"
        :key="candidate.id"
        class="bg-white border rounded-xl p-5 hover:shadow-md transition-shadow relative overflow-hidden"
      >
        <div class="absolute top-0 right-0 p-3">
    <span
      :class="{
        'bg-blue-100 text-blue-700': candidate.gender === 'male',
        'bg-pink-100 text-pink-700': candidate.gender === 'female',
        'bg-gray-100 text-gray-700': !candidate.gender || (candidate.gender !== 'male' && candidate.gender !== 'female')
      }"
      class="text-[10px] uppercase font-bold px-2 py-1 rounded"
    >
      {{
        candidate.gender === 'male' ? 'Man' :
          candidate.gender === 'female' ? 'Vrouw' : 'Niet bekend'
      }}
    </span>
        </div>
        <h3 class="text-lg font-bold text-gray-900 mb-1">
          {{ candidate.firstName }} {{ candidate.lastName }}
        </h3>

        <div class="space-y-1 text-sm">
          <p class="flex items-center text-gray-600 spacing_can">
            <span class="font-semibold w-20">Partij:</span>
            <span class="text-blue-600 font-medium">{{ candidate.listName || 'Onbekend' }}</span>
          </p>
          <p class="flex items-center text-gray-600 spacing_can">
            <span class="font-semibold w-20">Woonplaats:</span>
            <span>{{ candidate.locality }}</span>
          </p>
        </div>
      </div>
    </div>

    <div v-if="!loading && filteredCandidates.length === 0" class="text-center py-20 text-gray-500">
      Geen kandidaten gevonden voor deze selectie.
    </div>
  </div>
</template>

<style scoped>
/* --- NEW FILTER STYLES --- */
.filters {
  padding: 1rem 0;
  border-top: 1px solid #e2e8f0;
}
.filter-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #334155;
}
.filter-select {
  padding: 0.4rem 0.8rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  background: #f8fafc;
  cursor: pointer;
  font-size: 0.85rem;
  color: #334155;
  align-self: center;
}
.filter-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}
.filter-group button {
  padding: 0.4rem 0.8rem;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.85rem;
  color: #334155;
}
.filter-group button:hover {
  background: #e2e8f0;
  border-color: #94a3b8;
}
.active-filter {
  background: #0f172a !important;
  color: white !important;
  border-color: #0f172a !important;
}

/* --- EXISTING STYLES --- */
.page { display: grid; gap: 1rem; padding: 1rem; }
.bar { display: grid; gap: .75rem; }
.controls { display: flex; gap: .5rem; flex-wrap: wrap; align-items: center; }
.controls select, .controls button {
  padding: .5rem .6rem; border: 1px solid #cbd5e1; border-radius: .5rem;
}
.controls button {
  border: 1px solid #0f172a; background: #0f172a; color: white; cursor: pointer;
}
.controls button[disabled] { opacity: .6; cursor: not-allowed; }
.state { padding: 1rem; color: #334155; }
.state.error { color: #b91c1c; }
.grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); }
.card {
  display: grid; gap: .5rem; padding: 1rem; border: 1px solid #e2e8f0; border-radius: .8rem;
  background: white; box-shadow: 0 1px 2px rgba(0,0,0,.03);
}
.avatar {
  width: 48px; height: 48px; border-radius: 999px; display: grid; place-items: center;
  font-weight: 700; border: 1px solid #cbd5e1;
}
.name { margin: 0; font-size: 1.1rem; }
.meta { list-style: none; padding: 0; margin: .25rem 0 0 0; display: grid; gap: .25rem; color: #334155; font-size: .95rem; }
.actions { margin-top: .5rem; }
.linklike {
  background: transparent; border: none; padding: 0; color: #0f172a; cursor: pointer; font-weight: 600;
  text-decoration: underline;
}

.modal-backdrop {
  position: fixed; inset: 0; background: rgba(15, 23, 42, .5);
  display: grid; place-items: center; z-index: 1000;
}
.modal {
  width: min(640px, 92vw); background: #fff; border-radius: .9rem; border: 1px solid #e2e8f0;
  box-shadow: 0 10px 30px rgba(0,0,0,.25); display: grid; grid-template-rows: auto 1fr auto;
  max-height: 85vh;
}
.modal-header, .modal-footer { padding: .9rem 1rem; display: flex; align-items: center; justify-content: space-between; }
.modal-title { margin: 0; font-size: 1.15rem; }
.modal-body { padding: 0 1rem 1rem 1rem; overflow: auto; }
.icon-btn {
  border: none; background: transparent; font-size: 1.15rem; cursor: pointer; line-height: 1;
}
.primary {
  padding: .55rem .8rem; border-radius: .6rem; border: 1px solid #0f172a; background: #0f172a; color: #fff; cursor: pointer;
}
.details { list-style: none; padding: 0; margin: 0; display: grid; gap: .4rem; color: #334155; }
.details strong { color: #0f172a; }
.spacing_can{
  justify-content: space-between;
}
</style>
