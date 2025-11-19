<template>
  <div class="container mx-auto max-w-3xl p-4 md:p-8">
    <header class="mb-8">
      <h1 class="text-3xl font-bold text-gray-800">Overzicht Kieskringen per Provincie</h1>
      <p class="text-gray-600 mt-2">Klik op een provincie om de bijbehorende kieskringen te zien.</p>
    </header>

    <!-- Loading Indicator -->
    <div v-if="isLoading" class="text-center p-8 bg-white rounded-xl shadow">
      <p class="text-gray-500 animate-pulse">Data laden...</p>
    </div>

    <!-- Error Message Container -->
    <div v-else-if="error" class="text-center p-8 bg-red-100 text-red-700 rounded-xl shadow">
      <p>Kon de data niet laden: {{ error.message }}</p>
      <p class="text-sm mt-2">Probeer het later opnieuw.</p>
    </div>

    <!-- Province List Container -->
    <!-- We'll use space-y-4 for a list, not a grid -->
    <div v-else-if="provinces.length > 0" class="space-y-4">
      <!--
        Each <details> tag is now styled like a card.
        'overflow-hidden' ensures the rounded corners apply to the content.
      -->
      <details
        v-for="province in provinces"
        :key="province.name"
        class="bg-white rounded-xl shadow p-0 hover:shadow-lg transition-shadow duration-200 overflow-hidden"
      >
        <!--
          The <summary> is the clickable header, styled like the card header.
          We add padding (p-4) here instead of on the <details> tag.
        -->
        <summary class="flex justify-between items-center p-4 cursor-pointer hover:bg-gray-50 transition-colors list-none">
          <!-- Left side: Province Name -->
          <span class="text-lg font-semibold text-gray-800">{{ province.name }}</span>

          <!-- Right side: Badge + Arrow -->
          <div class="flex items-center space-x-3">
            <!-- A badge showing the count, inspired by your example -->
            <span class="text-sm bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
              {{ province.kieskringen.length }} kieskringen
            </span>
            <!-- A rotating chevron arrow -->
            <svg class="w-5 h-5 transition-transform duration-200 transform details-arrow text-gray-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </div>
        </summary>

        <!--
          This is the content that appears when open.
          We style it with a border-top and padding.
        -->
        <div class="p-4 border-t border-gray-200">
          <ul v-if="province.kieskringen && province.kieskringen.length > 0" class="list-disc list-inside space-y-2">
            <li v-for="kieskring in province.kieskringen" :key="kieskring.name" class="text-gray-600">
              {{ kieskring.name }}
            </li>
          </ul>
          <p v-else class="text-gray-500 italic">
            Geen kieskringen gevonden voor deze provincie.
          </p>
        </div>
      </details>
    </div>

    <!-- Empty state -->
    <div v-else class="text-center p-8 bg-white rounded-xl shadow">
      <p class="text-gray-500 text-center">Geen provincies gevonden.</p>
    </div>

  </div>
</template>

<!--
  The <script> section is UNCHANGED.
  It correctly fetches province data, which is what this component needs.
-->
<script setup lang="ts">
// 2. Import 'ref' and 'onMounted' from Vue
import { ref, onMounted } from 'vue';

// 3. Import our function and *type* from the new TS service
//    (Assuming your tsconfig.json handles .ts extensions)
import {getProvincesWithKieskringen} from "@/features/admin/service/ProvinceService.ts";
import {getKiesKringNames} from "@/features/admin/service/KieskringDetails_api.ts";
// 4. Define the component's internal state with explicit types
const provinces = ref<ProvinceDto[]>([]);
const isLoading = ref<boolean>(true);
const error = ref<Error | null>(null);

// 5. Use onMounted to fetch data when the component loads
onMounted(async () => {
  try {
    // Reset state
    error.value = null;
    isLoading.value = true;

    // Call the simple API service
    // The component is now responsible for awaiting the data
    provinces.value = await getProvincesWithKieskringen();

  } catch (err) {
    console.error('Failed to load provinces:', err);
    // We cast the unknown 'err' to an Error type
    error.value = err as Error;
  } finally {
    // This runs whether it succeeded or failed
    isLoading.value = false;
  }
});
</script>

<!--
  We just need a tiny bit of CSS to rotate the arrow,
  as this is hard to do with Tailwind alone.
-->
<style scoped>
/* When the <details> tag is opened, find the element
  with the .details-arrow class inside it and rotate it.
*/
details[open] .details-arrow {
  transform: rotate(180deg);
}
</style>
