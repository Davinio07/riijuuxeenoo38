import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useNationalResult } from "@/features/admin/composables/useNationalResult.ts";
import { mount } from '@vue/test-utils'

// Mock Dependencies

// Define all mock functions at the top level
const {
  mockGetPartiesFromDb,
  mockGetChartOptions,
  mockGetPartyColor
} = vi.hoisted(() => ({
  mockGetPartiesFromDb: vi.fn(),
  mockGetChartOptions: vi.fn(e => ({ title: { text: `Chart for ${e}` }})),
  mockGetPartyColor: vi.fn(p => `mock-color-${p.toLowerCase()}`),
}))

vi.mock('@/features/admin/service/NationalElectionResults_api', () => ({
  getPartiesFromDb: mockGetPartiesFromDb,
}))

vi.mock('@/features/admin/components/NationalResultChart.ts', () => ({
  getChartOptions: mockGetChartOptions,
}))

vi.mock('@/features/admin/service/partyService', () => ({
  getPartyColor: mockGetPartyColor,
}))



// Test Helper

// Mounts the composable in a test component to access its reactive state
function mountComposable() {
  let result: ReturnType<typeof useNationalResult> | undefined
  const wrapper = mount({
    setup() {
      result = useNationalResult()
      return { ...result }
    },
    template: '<div></div>',
  })
  // Ensure the composable is initialized and onMounted hook is run
  if (!result) throw new Error("Composable failed to mount.")
  return { wrapper, result }
}


// Test Data
const MOCK_PARTY_DATA = [
  { id: 1, name: 'Partij A', totalVotes: 500000, nationalSeats: 90, votePercentage: 45.00 },
  { id: 2, name: 'Partij B', totalVotes: 300000, nationalSeats: 40, votePercentage: 27.00 },
  { id: 3, name: 'Partij C', totalVotes: 200000, nationalSeats: 20, votePercentage: 18.00 },
  // Total Votes: 1,000,000. Total Seats: 150 (90 + 40 + 20)
]


// Tests for useNationalResult
describe('useNationalResult', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset the mock implementation for success before each test
    mockGetPartiesFromDb.mockResolvedValue(MOCK_PARTY_DATA)
  })

  // Test 1: Initialization and Data Fetching on Mount
  it('should initialize state correctly and fetch data on mount for the default election', async () => {
    const { result, wrapper } = mountComposable()

    // Initial state check
    expect(result.availableElections.value).toEqual(['TK2025', 'TK2023', 'TK2021'])
    expect(result.selectedElection.value).toBe('TK2025')
    expect(result.loading.value).toBe(true)

    // Wait for onMounted fetch to complete
    await wrapper.vm.$nextTick()

    expect(mockGetPartiesFromDb).toHaveBeenCalledWith('TK2025')
    expect(result.loading.value).toBe(false)
    expect(result.error.value).toBeNull()

    // Check nationalResults/Seats transformation
    expect(result.nationalResults.value.length).toBe(3)
    expect(result.nationalResults.value[0]).toEqual({ partyName: 'Partij A', validVotes: 500000 })
    expect(result.nationalSeats.value).toEqual({ 'Partij A': 90, 'Partij B': 40, 'Partij C': 20 })
  })

  // Test 2: Watcher triggered by selectedElection change
  it('should re-fetch data when selectedElection changes', async () => {
    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    mockGetPartiesFromDb.mockResolvedValue([
      { id: 4, name: 'New Party', totalVotes: 100, nationalSeats: 5, votePercentage: 100 },
    ])

    // Change selected election
    result.selectedElection.value = 'TK2023'
    await wrapper.vm.$nextTick()

    expect(result.loading.value).toBe(true)

    await wrapper.vm.$nextTick()

    expect(mockGetPartiesFromDb).toHaveBeenCalledWith('TK2023')
    expect(result.loading.value).toBe(false)
    expect(result.nationalResults.value.length).toBe(1)
    expect(result.nationalSeats.value).toEqual({ 'New Party': 5 })
  })

  // Test 3: API Error Handling
  it('should set error state when API fetch fails', async () => {
    const errorMessage = 'Custom API Error: Server offline.'
    mockGetPartiesFromDb.mockRejectedValue(new Error(errorMessage))

    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    expect(result.loading.value).toBe(false)
    expect(result.error.value).toBe(errorMessage)
    expect(result.nationalResults.value).toEqual([])
    expect(result.nationalSeats.value).toEqual({})
  })

  // Test 4: Computed Properties - TotalSeats, MajorityThreshold, PartyCount
  it('should correctly calculate totalSeats, majorityThreshold, and partyCount', async () => {
    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    // Total Seats: 90 + 40 + 20 = 150
    expect(result.totalSeats.value).toBe(150)

    // Majority Threshold: floor(150 / 2) + 1 = 76
    expect(result.majorityThreshold.value).toBe(76)

    // Party Count
    expect(result.partyCount.value).toBe(3)
  })

  // Test 5: Computed Properties - LargestParty
  it('should correctly identify the largest party and percentage', async () => {
    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    // Largest Party: Partij A (90 seats)
    expect(result.largestPartyData.value.name).toBe('Partij A')
    expect(result.largestPartyData.value.seats).toBe(90)

    // Percentage: (90 / 150) * 100 = 60.00%
    expect(result.largestPartyPercentage.value).toBe('(60.00%)')
  })

  // Test 6: LargestParty handles zero data
  it('should handle zero seats/parties for largestPartyData and percentage', async () => {
    mockGetPartiesFromDb.mockResolvedValue([])
    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    expect(result.largestPartyData.value).toEqual({ name: 'Geen data', seats: 0 })
    expect(result.largestPartyPercentage.value).toBe('(0.00%)')
  })

  // Test 7: Chart Data Calculation (Labels, Votes, Seats, Colors)
  it('should correctly format chartData, sorting by totalVotes and applying colors', async () => {
    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    // Total Votes: 1,000,000

    expect(result.chartData.value.labels).toEqual(['Partij A', 'Partij B', 'Partij C'])
    expect(mockGetPartyColor).toHaveBeenCalledTimes(3)
    expect(mockGetPartyColor).toHaveBeenCalledWith('Partij A')

    expect(result.chartData.value.datasets[0].data).toEqual([50.00, 30.00, 20.00])
    expect(result.chartData.value.datasets[0].votesData).toEqual([500000, 300000, 200000])
    expect(result.chartData.value.datasets[0].seatsData).toEqual([90, 40, 20])
    expect(result.chartData.value.datasets[0].backgroundColor).toEqual([
      'mock-color-partij a',
      'mock-color-partij b',
      'mock-color-partij c',
    ])
  })

  // Test 8: Chart Data handles zero votes
  it('should handle zero total votes in chartData', async () => {
    mockGetPartiesFromDb.mockResolvedValue([
      { id: 1, name: 'Partij A', totalVotes: 0, nationalSeats: 1, votePercentage: 0 },
      { id: 2, name: 'Partij B', totalVotes: 0, nationalSeats: 1, votePercentage: 0 },
    ])
    const { result, wrapper } = mountComposable()
    await wrapper.vm.$nextTick()

    expect(result.chartData.value.labels).toEqual(['Partij A', 'Partij B'])
    expect(result.chartData.value.datasets[0].data).toEqual([0, 0])
  })

  // Test 9: Exposed Return values
  it('should expose all required properties and computed values', () => {
    const { result } = mountComposable()
    expect(Object.keys(result)).toEqual([
      'availableElections',
      'selectedElection',
      'nationalResults',
      'nationalSeats',
      'loading',
      'error',
      'totalSeats',
      'majorityThreshold',
      'partyCount',
      'largestPartyData',
      'largestPartyPercentage',
      'chartData',
      'chartOptions',
    ])
  })
})
