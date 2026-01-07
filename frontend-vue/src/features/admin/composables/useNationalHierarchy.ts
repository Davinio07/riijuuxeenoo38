import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProvinces } from '@/features/admin/service/ProvinceService'
import type { ProvinceUI } from "@/features/admin/composables/useProvince.ts";

export function useNationalHierarchy() {
  const router = useRouter()

  const national = ref<{
    id: 'nl'
    name: string
    isOpen: boolean
    provinces?: ProvinceUI[]
    isLoadingChildren?: boolean
  }>({
    id: 'nl',
    name: 'Nederland',
    isOpen: false
  })

  async function toggleNational() {
    national.value.isOpen = !national.value.isOpen

    if (national.value.isOpen && !national.value.provinces) {
      try {
        national.value.isLoadingChildren = true
        const data = await getProvinces()

        // FIX: Map both the province and its nested kieskringen to UI types
        // to prevent the "is not assignable to type ProvinceUI[]" error.
        national.value.provinces = data.map(p => ({
          ...p,
          isOpen: false,
          kieskringen: p.kieskringen?.map(k => ({
            ...k,
            isOpen: false
          }))
        }))
      } finally {
        national.value.isLoadingChildren = false
      }
    }
  }

  function goToNationalResults() {
    router.push({ path: '/NationalElectionResults' })
  }

  return {
    national,
    toggleNational,
    goToNationalResults
  }
}
