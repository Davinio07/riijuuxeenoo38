import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getPartiesFromDb, type PartyDTO } from "@/features/admin/service/NationalElectionResults_api.ts";
import type { Mock } from 'vitest';

// --- Typed Fetch Mock Setup ---

// 1. Define the type for the fetch mock function explicitly
// We use the imported Mock type from 'vitest' and the native 'typeof fetch'
const fetchMock: Mock<typeof fetch> = vi.fn();

// 2. Use vi.stubGlobal to replace the global fetch instance
vi.stubGlobal('fetch', fetchMock);

// Mock data
const MOCK_PARTY_DATA: PartyDTO[] = [
  { id: 1, name: 'Party A', totalVotes: 1000, nationalSeats: 5, votePercentage: 50.0 },
  { id: 2, name: 'Party B', totalVotes: 500, nationalSeats: 3, votePercentage: 25.0 },
]

// Helper function for mock responses
// We return a strongly-typed object asserted as a native Response to satisfy the mockResolvedValue requirement.
const createMockResponse = (status: number, body: object = {}, ok: boolean = true): Response => {
  const jsonBody = JSON.stringify(body);

  // Note: Using a type assertion to 'Response' is required here because the object
  // literal does not fully satisfy the deep structure of the native 'Response' class,
  // but it does provide the methods (json, text) and properties (status, ok) used by the API code.
  return {
    statusText: `Status ${status}`,
    ok,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(jsonBody),
    // Add required properties that are not typically used by the code under test,
    // but are part of the 'Response' interface to help satisfy the type system.
    headers: new Headers(),
    url: 'mock-url',
    redirected: false,
    status: status,
    type: 'default',
    clone: () => createMockResponse(status, body, ok) as Response,
  } as Response; // Required Type Assertion to Response
};

describe('getPartiesFromDb', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
    vi.spyOn(console, 'debug').mockImplementation(() => {})
  })

  // Test 1: Successful Fetch (HTTP 200)
  it('should return party data on successful fetch (200 OK)', async () => {
    fetchMock.mockResolvedValue(createMockResponse(200, MOCK_PARTY_DATA, true));

    const result = await getPartiesFromDb('TK2025');

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/nationalResult/TK2025/national',
      expect.anything()
    );
    expect(result).toEqual(MOCK_PARTY_DATA);
  });

  // Test 2: Successful Fetch with No Content (HTTP 204)
  it('should return an empty array on 204 No Content', async () => {
    fetchMock.mockResolvedValue(createMockResponse(204, {}, true));

    const result = await getPartiesFromDb('TK2025');

    expect(result).toEqual([]);
  });

  // Test 3: HTTP 404 (Not Found) Error
  it('should throw HttpError for 404 Not Found', async () => {
    fetchMock.mockResolvedValue(createMockResponse(404, {}, false));

    await expect(getPartiesFromDb('UNKNOWN')).rejects.toThrow(
      "No party data found for election ID 'UNKNOWN'."
    );
    await expect(getPartiesFromDb('UNKNOWN')).rejects.toHaveProperty('status', 404);
  });

  // Test 4: HTTP 400 (Bad Request) Error with JSON body
  it('should throw HttpError for 400 Bad Request and include message from JSON body', async () => {
    const errorBody = { message: 'The election ID format is invalid.' };
    fetchMock.mockResolvedValue(createMockResponse(400, errorBody, false));

    await expect(getPartiesFromDb('INVALID_ID')).rejects.toThrow(
      `Invalid request: ${errorBody.message}`
    );
    await expect(getPartiesFromDb('INVALID_ID')).rejects.toHaveProperty('status', 400);
  });

  // Test 5: HTTP 500 (Server Error) Error
  it('should throw HttpError for 500 Internal Server Error', async () => {
    fetchMock.mockResolvedValue(createMockResponse(500, { message: 'DB Down' }, false));

    await expect(getPartiesFromDb('TK2025')).rejects.toThrow(
      'The service is temporarily unavailable. Please try again later.'
    );
    await expect(getPartiesFromDb('TK2025')).rejects.toHaveProperty('status', 500);
  });

  // Test 6: Network Failure
  it('should throw a generic Error for network failure', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'));

    await expect(getPartiesFromDb('TK2025')).rejects.toThrow(
      'Failed to connect to the server. Please check your network connection.'
    );
    // Should not have HttpError status property
    await expect(getPartiesFromDb('TK2025')).rejects.not.toHaveProperty('status');
  });

  // Test 7: Unhandled HTTP Error (e.g., 401)
  it('should throw generic HttpError for unhandled status codes (e.g., 401)', async () => {
    fetchMock.mockResolvedValue(createMockResponse(401, {}, false));

    await expect(getPartiesFromDb('TK2025')).rejects.toThrow(
      'An unexpected error occurred. Status: 401'
    );
    await expect(getPartiesFromDb('TK2025')).rejects.toHaveProperty('status', 401);
  });
});
