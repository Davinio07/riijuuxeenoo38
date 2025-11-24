<script setup lang="ts">
import { computed } from 'vue';
import { Bar } from 'vue-chartjs';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  type ChartOptions,
  type TooltipItem,
} from 'chart.js';
import { getPartyColor } from '../service/partyService';
import type { PropType } from 'vue';

// Register Chart.js components
ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

// Define component props
// Use a looser type
type PartyChartData = { name: string; totalVotes: number };

const props = defineProps({
  parties: {
    type: Array as PropType<PartyChartData[]>,
    required: true,
  },
  metric: {
    type: String as PropType<'totalVotes' | 'seats' | 'percentage'>,
    required: true,
  },
  title: { type: String, required: true },
});


// Create chart-compatible data
const chartData = computed(() => {
  // FIX 3: Use p.name from the NationalResult for labels
  const labels = props.parties.map(p => p.name);
  const data = props.parties.map(p => {
    if (props.metric === 'totalVotes') return p.totalVotes;
    if (props.metric === 'seats') return (p as any).seats;
    if (props.metric === 'percentage') return (p as any).votePercentage;
    return 0;
  });


  // FIX 5: Use p.name for getting colors
  const backgroundColors = props.parties.map(p => getPartyColor(p.name));

  return {
    labels,
    datasets: [
      {
        label: props.metric === 'totalVotes' ? 'Votes' : props.metric, // Clean up label
        data,
        backgroundColor: backgroundColors,
        borderRadius: 4,
        maxBarThickness: 100,
      },
    ],
  };
});

// Configure chart options
const chartOptions = computed<ChartOptions<'bar'>>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  indexAxis: 'y', // <-- THIS MAKES IT HORIZONTAL
  plugins: {
    legend: {
      display: false, // Hide legend, colors are in the bars
    },
    title: {
      display: true,
      text: props.title,
      font: {
        size: 16,
      },
      padding: {
        bottom: 20,
      }
    },
    tooltip: {
      callbacks: {
        // Format tooltip to use commas for thousands
        label: function (context: TooltipItem<'bar'>) {
          let label = context.dataset.label || '';
          if (label) {
            label += ': ';
          }
          if (context.parsed.x !== null) { // Use 'x' for horizontal chart
            label += new Intl.NumberFormat('nl-NL').format(context.parsed.x);
          }
          return label;
        },
      },
    },
  },
  scales: {
    x: { // <-- X-axis is now the value
      beginAtZero: true,
      ticks: {
        // Format Y-axis to use commas
        callback: function(value: any) {
          return new Intl.NumberFormat('nl-NL').format(value);
        }
      }
    },
    y: { // <-- Y-axis is now the category (party name)
      grid: {
        display: false,
      }
    }
  },
}));
</script>

<template>
  <div class="chart-container">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  /* Adjust height as needed for horizontal bars */
  height: 200px;
  width: 100%;
  padding: 1rem;
  background: #fdfdfd;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

/* Add more height if only one party is selected */
/* FIX: Corrected the unclosed string "[he0"]" to "[height="100"]" */
.chart-container:has(canvas[height="100"]) {
  height: 150px;
}
</style>

