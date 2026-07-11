import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { MigrationService, Migration } from './migration.service';

describe('MigrationService', () => {
  let service: MigrationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MigrationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(MigrationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Ensure no pending HTTP requests remain open after the test completes
    httpMock.verify();
  });

  it('should be created successfully', () => {
    expect(service).toBeTruthy();
  });

  it('should send CUDA code to the backend and return the optimized ROCm payload', () => {
    const mockProjectName = 'test_kernel';
    const mockCudaCode = '__global__ void test() { int lane = threadIdx.x % 32; }';
    const mockFileName = 'cuda_kernel.cu'; // Added missing argument
    const mockTier = 'EXPERT';
    const mockResponse: Migration = {
      migrationId: 99,
      projectName: mockProjectName,
      originalCudaCode: mockCudaCode,
      hipifyOutputCode: 'hipified code',
      finalRocmCode: '__global__ void test() { int lane = threadIdx.x % 64; /* Wavefront adjustment */ }',
      wavefrontBugsDetected: 1,
      estimatedSavings: 15000,
      status: 'COMPLETED',
      tokensUsed: 150,
      createdAt: new Date().toISOString()
    };

    // Trigger the service method
    service.processMigration(mockProjectName, mockFileName, mockCudaCode, mockTier).subscribe(response => {
      expect(response).toEqual(mockResponse);
      expect(response.wavefrontBugsDetected).toBe(1);
    });

    // Intercept the outgoing request and validate it
    const req = httpMock.expectOne(`http://localhost:8080/api/migrations/process?projectName=${mockProjectName}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe(mockCudaCode);

    // Provide the dummy data as the simulated backend response
    req.flush(mockResponse);
  });
});
