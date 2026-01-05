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
    if (!national.value.isOpen && !national.value.provinces) {
      try {
        national.value.isLoadingChildren = true
        const data = await getProvinces()
        national.value.provinces = data.map(p => ({
          ...p,
          isOpen: false
        }))
      } finally {
        national.value.isLoadingChildren = false
      }
    }

    national.value.isOpen = !national.value.isOpen
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
