#include <cuda_runtime.h>
#include <iostream>

__global__ void matrixWarpReduce(float *g_idata, float *g_odata) {
    // Hardcoded 32 thread lane index assumption
    int lane = threadIdx.x % 32; 
    int wid = threadIdx.x / 32;

    // Silent architectural trap: assumes 32-thread execution loop
    if (lane < 16) {
        g_idata[threadIdx.x] += g_idata[threadIdx.x + 16];
    }
    
    if (lane == 0) g_odata[wid] = g_idata[threadIdx.x];
}