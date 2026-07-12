#include <cuda_runtime.h>

#define SHARED_SIZE 32
#define PADDING 1

__global__ void coalescedMatrixTranspose(float *input, float *output, int width, int height) {
    // Shared memory array with padding to avoid bank conflicts on NVIDIA
    __shared__ float tile[SHARED_SIZE][SHARED_SIZE + PADDING];

    int x = blockIdx.x * blockDim.x + threadIdx.x;
    int y = blockIdx.y * blockDim.y + threadIdx.y;

    if (x < width && y < height) {
        tile[threadIdx.y][threadIdx.x] = input[y * width + x];
        __syncthreads();
        // ... transposition logic ...
        output[x * height + y] = tile[threadIdx.x][threadIdx.y];
    }
}