<template>
  <div>
    <h1>Gemeentelijke Uitslagen</h1>

    <div class="search-container" v-if="municipalityNames.length > 0">
      <label for="municipality-select">Kies een gemeente:</label>
      <select id="municipality-select" v-model="selectedMunicipality">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getMunicipalityNames, type MunicipalityResult } from '../service/MunicipalityElectionResults_api';

const results = ref<MunicipalityResult[]>([]);
const municipalityNames = ref<string[]>([]);
const selectedMunicipality = ref<string | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

/**
 * On component mount, fetch the list of all municipalities.
 */
onMounted(async () => {
  try {
    municipalityNames.value = await getMunicipalityNames();
    if (municipalityNames.value.length > 0) {
      selectedMunicipality.value = municipalityNames.value[0];
    }
  } catch (err) {
    error.value = 'Fout bij het ophalen van de lijst met gemeenten.';
    console.error(err);
  } finally {
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
