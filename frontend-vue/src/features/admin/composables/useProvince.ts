import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getProvinces,
  getConstituenciesForProvince,
  getMunicipalitiesForConstituency,
  type ProvinceDto,
  type KieskringDto,
  type GemeenteDto
} from '@/features/admin/service/ProvinceService'

// UI TYPES
export interface KieskringUI extends KieskringDto {
  isOpen: boolean
  isLoadingGemeentes?: boolean
  gemeentes?: GemeenteDto[]
}

export interface ProvinceUI extends Omit<ProvinceDto, 'kieskringen'> {
  isOpen: boolean
  isLoadingChildren?: boolean
  kieskringen?: KieskringUI[]
}

// COMPOSABLE
export function useProvince() {
  const router = useRouter()

  const provinces = ref<ProvinceUI[]>([])
  const isLoading = ref(true)
  const error = ref<Error | null>(null)

  /* ---------- NAVIGATION ---------- */

  // FIX: Updated to accept 2 arguments to match the template call
  function goToResults(kieskringName: string, election?: string) {
    router.push({
      path: '/kieskring-details',
      query: {
        name: kieskringName,
        election: election // Pass the election year to the details page
      }
    })
  }

  function goToParties(kieskringName: string) {
    router.push({
      path: '/parties',
      query: { level: 'Kieskringen', name: kieskringName }
    })
  }

  function goToMunicipalityParties(municipalityName: string) {
    router.push({
      path: '/parties',
      query: { level: 'Gemeentes', name: municipalityName }
    })
  }

  // DATA LOADING
  onMounted(async () => {
    try {
      isLoading.value = true
      const data = await getProvinces()
      provinces.value = data.map(p => ({ ...p, isOpen: false }))
    } catch (err) {
      error.value = err as Error
    } finally {
      isLoading.value = false
    }
  })

  async function toggleProvince(province: ProvinceUI) {
    province.isOpen = !province.isOpen

    if (province.isOpen && !province.kieskringen) {
      try {
        province.isLoadingChildren = true
        const results = await getConstituenciesForProvince(province.province_id)
        province.kieskringen = results.map(k => ({ ...k, isOpen: false }))
      } finally {
        province.isLoadingChildren = false
      }
    }
  }

  async function toggleKieskring(kieskring: KieskringUI) {
    kieskring.isOpen = !kieskring.isOpen

    if (kieskring.isOpen && !kieskring.gemeentes) {
      try {
        kieskring.isLoadingGemeentes = true
        kieskring.gemeentes = await getMunicipalitiesForConstituency(kieskring.id)
      } finally {
        kieskring.isLoadingGemeentes = false
      }
    }
  }

  // TRANSITIONS
  function startTransition(el: Element) {
    const element = el as HTMLElement
    element.style.height = '0'
    element.style.opacity = '0'
  }

  function endTransition(el: Element) {
    const element = el as HTMLElement
    element.style.height = `${element.scrollHeight}px`
    element.style.opacity = '1'
    element.addEventListener(
      'transitionend',
      () => {
        if (element.style.height !== '0px') element.style.height = 'auto'
      },
      { once: true }
    )
  }

  // COLORS
  const colors = [
    'bg-red-500', 'bg-orange-500', 'bg-amber-500', 'bg-yellow-500',
    'bg-lime-500', 'bg-green-500', 'bg-emerald-500', 'bg-teal-500',
    'bg-cyan-500', 'bg-sky-500', 'bg-blue-500', 'bg-indigo-500'
  ]

  const getProvinceColor = (id: number) => colors[id % colors.length] ?? 'bg-gray-500'

  return {
    provinces,
    isLoading,
    error,
    toggleProvince,
    toggleKieskring,
    goToResults,
    goToParties,
    goToMunicipalityParties,
    startTransition,
    endTransition,
    getProvinceColor
  }
}
