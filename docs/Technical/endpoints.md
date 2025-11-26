# Endpoint Overview

**Base URL:** `http://localhost:8080/api`

| Route | Auth? | Method | Request (input) | Response (output) | Description |
| --- | --- | --- | --- | --- | --- |
| `/constituencies/results` | --- | `GET` | --- | **200:** `ConstituencyDataDto[]` <br>**500:** `{ error: "Internal server error" }` | Fetches all constituency (kieskring) results aggregated from municipalities. |
| `/elections/municipalities/{municipalityName}` | --- | `GET` | URL param `{ municipalityName }` | **200:** `MunicipalityResultDto[]` <br>**404:** `{ error: "Municipality not found" }` <br>**500:** `{ error: "Internal server error" }` | Gets election results for a single municipality. |
| `/elections/municipalities/all-results` | --- | `GET` | --- | **200:** `MunicipalityDataDto[]` <br>**500:** `{ error: "Internal server error" }` | Retrieves all municipality results in one request. |
| `/elections/TK2023/regions/gemeenten` | --- | `GET` | --- | **200:** `string[]` (municipality names) <br>**500:** `{ error: "Internal server error" }` | Returns municipality names (used for dropdowns). |
| `/nationalResult/{electionId}/national` | --- | `GET` | URL param `{ electionId }` | **200:** `PartyDTO[]` <br>**204:** `[]` <br>**400:** `{ error: "Invalid request" }` <br>**404:** `{ error: "No party data found" }` <br>**5xx:** `{ error: "Service temporarily unavailable" }` | Fetches persisted national party results for the given election. |
| `/provinces` | --- | `GET` | --- | **200:** `ProvinceDto[]` <br>**500:** `{ error: "Internal server error" }` | Fetches all provinces. |
| `/provinces/{provinceId}/kieskringen` | --- | `GET` | URL param `{ provinceId }` | **200:** `KieskringDto[]` <br>**404:** `{ error: "Not found" }` <br>**500:** `{ error: "Internal server error" }` | Fetches constituencies (kieskringen) for a province. |
| `/constituencies/{constituencyId}/municipalities` | --- | `GET` | URL param `{ constituencyId }` | **200:** `GemeenteDto[]` <br>**404:** `{ error: "Not found" }` <br>**500:** `{ error: "Internal server error" }` | Fetches municipalities for a constituency. |
| `/ScaledElectionResults/Result` | --- | `GET` | --- | **200:** `{ message: string }` <br>**500:** `{ error: "Internal server error" }` | Retrieves aggregated/scaled national election results. |
| `/elections/{electionId}/regions/kieskringen` | --- | `GET` | URL param `{ electionId }` | **200:** `[ { regionId, regionName, ... } ]` <br>**404:** `{ error: "Regions not found" }` <br>**500:** `{ error: "Internal server error" }` | Fetches election regions for the given election. |
| `/elections/candidates/db` | --- | `GET` | Optional query params: `partyId`, `gender` | **200:** `CandidateData[]` (mapped from DB) <br>**500:** `{ error: "Internal server error" }` | Retrieves persisted candidates with optional filtering. |
| `/admin/ping` | --- | `GET` | --- | **200:** `{ message: string }` <br>**500:** `{ error: "Internal server error" }` | Checks whether the backend is reachable (health check). |
| `/admin/stats` | Yes (admin) | `GET` | --- | **200:** `DashboardStat[]` <br>**500:** `{ error: "Internal server error" }` | Fetches admin dashboard statistics. |
| `/auth/login` | --- | `POST` | `LoginRequest` `{ username, password }` | **200:** `LoginResponse` (JWT token + metadata) <br>**401:** `{ error: "Invalid credentials" }` <br>**500:** `{ error: "Internal server error" }` | Sends login request and returns JWT on success. |
