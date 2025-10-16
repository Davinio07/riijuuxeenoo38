<script setup lang="ts">
import {computed, ref} from 'vue'
import { getNationalResults } from '@/features/admin/service/NationalElectionResults_api.ts'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js'

// Register chart.js components globally
ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

interface NationalResult {
  partyName: string
  validVotes: number
}

const nationalResults = ref<NationalResult[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

async function fetchNationalResults(electionId: string) {
  loading.value = true
  error.value = null
  nationalResults.value = []
  try {
    nationalResults.value = await getNationalResults(electionId) as NationalResult[];
  } catch (err) {
    error.value = (err instanceof Error) ? err.message : 'Er is een fout opgetreden.'
  } finally {
    loading.value = false
  }
}

// Chart configuration
const chartData = computed(() => ({
  labels: nationalResults.value.map(r => r.partyName),
  datasets: [
    {
      label: 'Aantal geldige stemmen',
      data: nationalResults.value.map(r => r.validVotes),
      backgroundColor: 'rgba(54, 162, 235, 0.6)',
      borderColor: 'rgba(54, 162, 235, 1)',
      borderWidth: 1,
    }
  ]
}))

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { position: 'top' as const },
    title: {
      display: true,
      text: 'Nationale verkiezingsresultaten per partij'
    }
  },
  scales: {
    x: { ticks: { color: '#333' } },
    y: { beginAtZero: true }
  }
}
</script>

<template>
  <h1 class="text-xl font-semibold mb-4">Nationale verkiezingsresultaten</h1>

  <div class="mb-4 flex gap-2">
    <button @click="() => fetchNationalResults('TK2021')" class="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">
      TK2021
    </button>
    <button @click="() => fetchNationalResults('TK2023')" class="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">
      TK2023
    </button>
  </div>

  <div v-if="loading">Bezig met laden...</div>
  <div v-if="error" class="text-red-600">{{ error }}</div>

  <div v-if="nationalResults.length" class="w-full max-w-3xl mx-auto">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>
