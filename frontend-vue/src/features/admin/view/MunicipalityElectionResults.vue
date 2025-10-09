<template>
  <div>
    <h1>Gemeentelijke Uitslagen</h1>

    <div class="search-container" v-if="municipalityNames.length > 0">
      <label for="municipality-select">Kies een gemeente:</label>
      <select id="municipality-select" v-model="selectedMunicipality" @change="fetchResultsForSelectedMunicipality">
        <option v-for="name in municipalityNames" :key="name" :value="name">
          {{ name }}
        </option>
      </select>
    </div>

    <p v-if="loading">Resultaten worden geladen...</p>
    <div v-if="error" class="error">
      {{ error }}
    </div>

    <table v-if="results.length > 0" class="results-table">
      <thead>
      <tr>
        <th>Gemeente</th>
        <th>Partij</th>
        <th>Aantal Stemmen</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(result, index) in results" :key="index">
        <td>{{ result.municipalityName || 'N/A' }}</td>
        <td>{{ result.partyName }}</td>
        <td>{{ result.validVotes.toLocaleString() }}</td>
      </tr>
      </tbody>
    </table>
    <p v-if="!loading && results.length === 0 && !error">
      Selecteer een gemeente om de resultaten te zien.
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getMunicipalityNames, getResultsForMunicipality, type MunicipalityResult } from '../service/MunicipalityElectionResults_api';

const results = ref<MunicipalityResult[]>([]);
const municipalityNames = ref<string[]>([]);
const selectedMunicipality = ref<string | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

/**
 * Fetches results for the currently selected municipality and updates the UI.
 */
async function fetchResultsForSelectedMunicipality() {
  if (!selectedMunicipality.value) return;

  loading.value = true;
  error.value = null;
  try {
    results.value = await getResultsForMunicipality(selectedMunicipality.value);
  } catch (err) {
    error.value = `Fout bij het ophalen van de uitslagen for ${selectedMunicipality.value}.`;
    console.error(err);
    results.value = []; // Clear old results on error
  } finally {
    loading.value = false;
  }
}

/**
 * On component mount, fetch the list of all municipalities.
 * If successful, select the first one and fetch its results.
 */
onMounted(async () => {
  try {
    municipalityNames.value = await getMunicipalityNames();
    if (municipalityNames.value.length > 0) {
      // Automatically select the first municipality and load its data
      selectedMunicipality.value = municipalityNames.value[0];
      await fetchResultsForSelectedMunicipality();
    } else {
      // If no municipalities are found, stop loading and show a message.
      loading.value = false;
    }
  } catch (err) {
    error.value = 'Fout bij het ophalen van de lijst met gemeenten.';
    console.error(err);
    loading.value = false;
  }
});
</script>

<style scoped>
.results-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

.results-table th, .results-table td {
  border: 1px solid #ddd;
  padding: 8px;
}

.results-table th {
  background-color: #f2f2f2;
  text-align: left;
}

.error {
  color: red;
  font-weight: bold;
}

.search-container {
  margin-bottom: 1rem;
}

.search-container label {
  margin-right: 0.5rem;
  font-weight: bold;
}

.search-container select {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ddd;
  min-width: 250px;
}
</style>
