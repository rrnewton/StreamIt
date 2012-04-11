import java.io.Serializable;
import streamit.library.*;
import streamit.library.io.*;
import streamit.misc.StreamItRandom;
class Complex extends Structure implements Serializable {
  float real;
  float imag;
}
class float2 extends Structure implements Serializable {
  float x;
  float y;
}
class float3 extends Structure implements Serializable {
  float x;
  float y;
  float z;
}
class float4 extends Structure implements Serializable {
  float x;
  float y;
  float z;
  float w;
}
class StreamItVectorLib {
  public static native float2 add2(float2 a, float2 b);
  public static native float3 add3(float3 a, float3 b);
  public static native float4 add4(float4 a, float4 b);
  public static native float2 sub2(float2 a, float2 b);
  public static native float3 sub3(float3 a, float3 b);
  public static native float4 sub4(float4 a, float4 b);
  public static native float2 mul2(float2 a, float2 b);
  public static native float3 mul3(float3 a, float3 b);
  public static native float4 mul4(float4 a, float4 b);
  public static native float2 div2(float2 a, float2 b);
  public static native float3 div3(float3 a, float3 b);
  public static native float4 div4(float4 a, float4 b);
  public static native float2 addScalar2(float2 a, float b);
  public static native float3 addScalar3(float3 a, float b);
  public static native float4 addScalar4(float4 a, float b);
  public static native float2 subScalar2(float2 a, float b);
  public static native float3 subScalar3(float3 a, float b);
  public static native float4 subScalar4(float4 a, float b);
  public static native float2 scale2(float2 a, float b);
  public static native float3 scale3(float3 a, float b);
  public static native float4 scale4(float4 a, float b);
  public static native float2 scaleInv2(float2 a, float b);
  public static native float3 scaleInv3(float3 a, float b);
  public static native float4 scaleInv4(float4 a, float b);
  public static native float sqrtDist2(float2 a, float2 b);
  public static native float sqrtDist3(float3 a, float3 b);
  public static native float sqrtDist4(float4 a, float4 b);
  public static native float dot3(float3 a, float3 b);
  public static native float3 cross3(float3 a, float3 b);
  public static native float2 max2(float2 a, float2 b);
  public static native float3 max3(float3 a, float3 b);
  public static native float2 min2(float2 a, float2 b);
  public static native float3 min3(float3 a, float3 b);
  public static native float2 neg2(float2 a);
  public static native float3 neg3(float3 a);
  public static native float4 neg4(float4 a);
  public static native float2 floor2(float2 a);
  public static native float3 floor3(float3 a);
  public static native float4 floor4(float4 a);
  public static native float2 normalize2(float2 a);
  public static native float3 normalize3(float3 a);
  public static native float4 normalize4(float4 a);
  public static native boolean greaterThan3(float3 a, float3 b);
  public static native boolean lessThan3(float3 a, float3 b);
  public static native boolean equals3(float3 a, float3 b);
}
class Printer extends Filter // MPDChain.str:21
{
  public Printer()
  {
  }
  public void work() { // MPDChain.str:24
    System.out.print(inputChannel.popFloat()); // MPDChain.str:25
    System.out.print(inputChannel.popFloat()); // MPDChain.str:26
  }
  public void init() { // MPDChain.str:21
    setIOTypes(Float.TYPE, Void.TYPE); // MPDChain.str:21
    addSteadyPhase(2, 2, 0, "work"); // MPDChain.str:23
  }
}
class Stump extends Filter // MPDChain.str:30
{
  public Stump()
  {
  }
  public void work() { // MPDChain.str:33
    inputChannel.popFloat(); // MPDChain.str:34
    inputChannel.popFloat(); // MPDChain.str:35
  }
  public void init() { // MPDChain.str:30
    setIOTypes(Float.TYPE, Void.TYPE); // MPDChain.str:30
    addSteadyPhase(2, 2, 0, "work"); // MPDChain.str:32
  }
}
class CFARDetectPipe extends Pipeline // CFARDetect.str:1
{
  public CFARDetectPipe(int rows, int cols)
  {
  }
  public void init(final int rows, final int cols) { // CFARDetect.str:2
    add(new CFARDetectRearrange()); // CFARDetect.str:3
    add(new CFARDetectPipeSplitter()); // CFARDetect.str:4
    add(new CFARDetectFilter(rows, cols)); // CFARDetect.str:5
  }
}
class CFARDetectPipeSplitter extends SplitJoin // CFARDetect.str:8
{
  public CFARDetectPipeSplitter()
  {
  }
  public void init() { // CFARDetect.str:9
    setSplitter(WEIGHTED_ROUND_ROBIN(2, 2, 1)); // CFARDetect.str:10
    add(new CFARDetectSum()); // CFARDetect.str:11
    add(new CFARDetectGuard()); // CFARDetect.str:12
    add(new CFARPusher()); // CFARDetect.str:13
    setJoiner(ROUND_ROBIN(1)); // CFARDetect.str:14
  }
}
class CFARDetectRearrange extends Filter // CFARDetect.str:17
{
  public CFARDetectRearrange()
  {
  }
  public void work() { // CFARDetect.str:21
    float guardNoise = inputChannel.popFloat(); // CFARDetect.str:22
    float sumThresh = inputChannel.popFloat(); // CFARDetect.str:23
    float sumLMax = inputChannel.popFloat(); // CFARDetect.str:24
    float sumDb = inputChannel.popFloat(); // CFARDetect.str:25
    float guardDb = inputChannel.popFloat(); // CFARDetect.str:26
    outputChannel.pushFloat(sumDb); // CFARDetect.str:28
    outputChannel.pushFloat(sumThresh); // CFARDetect.str:29
    outputChannel.pushFloat(guardDb); // CFARDetect.str:30
    outputChannel.pushFloat(guardNoise); // CFARDetect.str:31
    outputChannel.pushFloat(sumLMax); // CFARDetect.str:32
  }
  public void init() { // CFARDetect.str:17
    setIOTypes(Float.TYPE, Float.TYPE); // CFARDetect.str:17
    addSteadyPhase(5, 5, 5, "work"); // CFARDetect.str:20
  }
}
class CFARDetectSum extends Filter // CFARDetect.str:36
{
  public CFARDetectSum()
  {
  }
  public void work() { // CFARDetect.str:39
    float sumDb = inputChannel.popFloat(); // CFARDetect.str:40
    float sumThresh = inputChannel.popFloat(); // CFARDetect.str:41
    if ((sumDb > sumThresh)) { // CFARDetect.str:44
      outputChannel.pushFloat(1); // CFARDetect.str:45
    } else { // CFARDetect.str:48
      outputChannel.pushFloat(0); // CFARDetect.str:49
    } // CFARDetect.str:43
  }
  public void init() { // CFARDetect.str:36
    setIOTypes(Float.TYPE, Float.TYPE); // CFARDetect.str:36
    addSteadyPhase(2, 2, 1, "work"); // CFARDetect.str:38
  }
}
class CFARDetectGuard extends Filter // CFARDetect.str:54
{
  public CFARDetectGuard()
  {
  }
  public void work() { // CFARDetect.str:57
    float guardDb = inputChannel.popFloat(); // CFARDetect.str:58
    float guardNoise = inputChannel.popFloat(); // CFARDetect.str:59
    if ((guardDb > (guardNoise + 15))) { // CFARDetect.str:62
      outputChannel.pushFloat(1); // CFARDetect.str:63
    } else { // CFARDetect.str:66
      outputChannel.pushFloat(0); // CFARDetect.str:67
    } // CFARDetect.str:61
  }
  public void init() { // CFARDetect.str:54
    setIOTypes(Float.TYPE, Float.TYPE); // CFARDetect.str:54
    addSteadyPhase(2, 2, 1, "work"); // CFARDetect.str:56
  }
}
class CFARDetectFilter extends Filter // CFARDetect.str:72
{
  public CFARDetectFilter(int _param_rows, int _param_cols)
  {
  }
  int rows; // CFARDetect.str:72
  int cols; // CFARDetect.str:72
  public void work() { // CFARDetect.str:75
    int currentCol = ((iter() / rows) % cols); // CFARDetect.str:76
    int currentRow = (iter() % rows); // CFARDetect.str:77
    if (((currentCol < 6) || (currentCol >= (cols - 6)))) { // CFARDetect.str:79
      inputChannel.popFloat(); // CFARDetect.str:80
      inputChannel.popFloat(); // CFARDetect.str:81
      inputChannel.popFloat(); // CFARDetect.str:82
      outputChannel.pushFloat(0); // CFARDetect.str:83
    } else { // CFARDetect.str:86
      float sumDet; // CFARDetect.str:87
      sumDet = inputChannel.popFloat(); // CFARDetect.str:87
      float guardDet; // CFARDetect.str:88
      guardDet = inputChannel.popFloat(); // CFARDetect.str:88
      float sumLMax; // CFARDetect.str:89
      sumLMax = inputChannel.popFloat(); // CFARDetect.str:89
      if ((((sumLMax == 1) && (sumDet == 1)) && (guardDet == 0))) { // CFARDetect.str:92
        outputChannel.pushFloat(1); // CFARDetect.str:93
      } else { // CFARDetect.str:96
        outputChannel.pushFloat(0); // CFARDetect.str:97
      } // CFARDetect.str:91
    } // CFARDetect.str:78
  }
  public void init(final int _param_rows, final int _param_cols) { // CFARDetect.str:72
    rows = _param_rows; // CFARDetect.str:72
    cols = _param_cols; // CFARDetect.str:72
    setIOTypes(Float.TYPE, Float.TYPE); // CFARDetect.str:72
    addSteadyPhase(3, 3, 1, "work"); // CFARDetect.str:74
  }
}
class CFARLMaxPipe extends Pipeline // CFARLMax.str:1
{
  public CFARLMaxPipe(int n)
  {
  }
  public void init(final int n) { // CFARLMax.str:2
    add(new LMaxCalc(n, 104)); // CFARLMax.str:3
  }
}
class LMaxPop extends Filter // CFARLMax.str:7
{
  public LMaxPop(int _param_n, int _param_nRows)
  {
  }
  int n; // CFARLMax.str:7
  int nRows; // CFARLMax.str:7
  public void work() { // CFARLMax.str:10
    for (int i = 0; (i < nRows); i++) { // CFARLMax.str:12
      inputChannel.popFloat(); // CFARLMax.str:13
    }; // CFARLMax.str:11
    for (int j = 0; (j < (nRows * n)); j++) { // CFARLMax.str:17
      outputChannel.pushFloat(inputChannel.popFloat()); // CFARLMax.str:18
    }; // CFARLMax.str:16
    for (int i = 0; (i < nRows); i++) { // CFARLMax.str:22
      outputChannel.pushFloat(0); // CFARLMax.str:23
    }; // CFARLMax.str:21
  }
  public void init(final int _param_n, final int _param_nRows) { // CFARLMax.str:7
    n = _param_n; // CFARLMax.str:7
    nRows = _param_nRows; // CFARLMax.str:7
    setIOTypes(Float.TYPE, Float.TYPE); // CFARLMax.str:7
    addSteadyPhase(((n + 1) * nRows), ((n + 1) * nRows), ((n + 1) * nRows), "work"); // CFARLMax.str:9
  }
}
class LMaxCalc extends Filter // CFARLMax.str:28
{
  public LMaxCalc(int _param_n, int _param_nRows)
  {
    setStateful(true);
  }
  float[nRows][3] dataMatrix; // CFARLMax.str:30
  float[(nRows + 2)] dataMaxRow; // CFARLMax.str:31
  int rowIsLMax; // CFARLMax.str:32
  int currentCol; // CFARLMax.str:33
  int lMaxCol; // CFARLMax.str:34
  float maxValue; // CFARLMax.str:35
  int colPos; // CFARLMax.str:36
  int colToChange; // CFARLMax.str:37
  int n; // CFARLMax.str:28
  int nRows; // CFARLMax.str:28
  public void work() { // CFARLMax.str:49
    maxValue = -85; // CFARLMax.str:51
    for (int i = 0; (i < nRows); i++) { // CFARLMax.str:54
      dataMatrix[i][currentCol] = inputChannel.popFloat(); // CFARLMax.str:55
    }; // CFARLMax.str:53
    for (int i = 0; (i < (nRows + 2)); i++) { // CFARLMax.str:59
      dataMaxRow[i] = -85; // CFARLMax.str:60
    }; // CFARLMax.str:58
    if ((colPos == 0)) { // CFARLMax.str:64
      for (int row = 0; (row < nRows); row++) { // CFARLMax.str:66
        for (int col = 0; (col < 3); col++) { // CFARLMax.str:68
          dataMatrix[row][col] = -85; // CFARLMax.str:69
        }; // CFARLMax.str:67
      }; // CFARLMax.str:65
    } else { // CFARLMax.str:73
      if ((colPos == n)) { // CFARLMax.str:74
        for (int changeRow = 0; (changeRow < nRows); changeRow++) { // CFARLMax.str:76
          dataMatrix[changeRow][currentCol] = -85; // CFARLMax.str:77
        }; // CFARLMax.str:75
      } // CFARLMax.str:73
    } // CFARLMax.str:63
    for (int row = 0; (row < nRows); row++) { // CFARLMax.str:82
      for (int col = 0; (col < 3); col++) { // CFARLMax.str:84
        if ((dataMatrix[row][col] > dataMaxRow[(row + 1)])) { // CFARLMax.str:86
          dataMaxRow[(row + 1)] = dataMatrix[row][col]; // CFARLMax.str:87
        } // CFARLMax.str:85
      }; // CFARLMax.str:83
    }; // CFARLMax.str:81
    if ((colPos == 0)) { // CFARLMax.str:93
      for (int i = 0; (i < nRows); i++) { // CFARLMax.str:95
        outputChannel.pushFloat(0); // CFARLMax.str:96
      }; // CFARLMax.str:94
    } else { // CFARLMax.str:100
      for (int lMaxRow = 0; (lMaxRow < nRows); lMaxRow++) { // CFARLMax.str:102
        rowIsLMax = 1; // CFARLMax.str:103
        for (int row = lMaxRow; (row < (lMaxRow + 3)); row++) { // CFARLMax.str:105
          if ((dataMaxRow[row] > dataMatrix[lMaxRow][lMaxCol])) { // CFARLMax.str:108
            rowIsLMax = 0; // CFARLMax.str:109
          } // CFARLMax.str:107
        }; // CFARLMax.str:104
        outputChannel.pushFloat(rowIsLMax); // CFARLMax.str:113
      }; // CFARLMax.str:101
    } // CFARLMax.str:92
    if ((lMaxCol >= 2)) { // CFARLMax.str:118
      lMaxCol = 0; // CFARLMax.str:119
    } else { // CFARLMax.str:122
      lMaxCol++; // CFARLMax.str:123
    } // CFARLMax.str:117
    if ((currentCol >= 2)) { // CFARLMax.str:127
      currentCol = 0; // CFARLMax.str:128
    } else { // CFARLMax.str:131
      currentCol++; // CFARLMax.str:132
    } // CFARLMax.str:126
    if ((colPos >= n)) { // CFARLMax.str:136
      colPos = 0; // CFARLMax.str:137
    } else { // CFARLMax.str:140
      colPos++; // CFARLMax.str:141
    } // CFARLMax.str:135
  }
  public void init(final int _param_n, final int _param_nRows) { // CFARLMax.str:40
    n = _param_n; // CFARLMax.str:39
    nRows = _param_nRows; // CFARLMax.str:39
    setIOTypes(Float.TYPE, Float.TYPE); // CFARLMax.str:28
    addSteadyPhase(nRows, nRows, nRows, "work"); // CFARLMax.str:48
    currentCol = 0; // CFARLMax.str:41
    colPos = 0; // CFARLMax.str:42
    lMaxCol = 2; // CFARLMax.str:43
    maxValue = -85; // CFARLMax.str:44
  }
}
class CopyRearrange extends Filter // CFARLMax.str:147
{
  public CopyRearrange(int _param_n)
  {
  }
  float[(n + 2)] stream; // CFARLMax.str:149
  int n; // CFARLMax.str:147
  public void work() { // CFARLMax.str:158
    for (int i = 1; (i < (n + 1)); i++) { // CFARLMax.str:160
      stream[i] = inputChannel.popFloat(); // CFARLMax.str:161
    }; // CFARLMax.str:159
    for (int i = 0; (i < n); i++) { // CFARLMax.str:165
      outputChannel.pushFloat(stream[i]); // CFARLMax.str:166
      outputChannel.pushFloat(stream[(i + 1)]); // CFARLMax.str:167
      outputChannel.pushFloat(stream[(i + 2)]); // CFARLMax.str:168
    }; // CFARLMax.str:164
  }
  public void init(final int _param_n) { // CFARLMax.str:152
    n = _param_n; // CFARLMax.str:151
    setIOTypes(Float.TYPE, Float.TYPE); // CFARLMax.str:147
    addSteadyPhase(n, n, (n * 3), "work"); // CFARLMax.str:157
    stream[0] = -85; // CFARLMax.str:153
    stream[(n + 1)] = -85; // CFARLMax.str:154
  }
}
class CFARNoiseLevelPipe extends Pipeline // CFARNoiseLevel.str:1
{
  public CFARNoiseLevelPipe(int cols)
  {
  }
  public void init(final int cols) { // CFARNoiseLevel.str:2
    add(new CFARNoiseLevelSplitter(cols)); // CFARNoiseLevel.str:3
    add(new CFARNoiseLevelGuardFirst(cols)); // CFARNoiseLevel.str:4
  }
}
class CFARNoiseLevelGuardFirst extends Filter // CFARNoiseLevel.str:7
{
  public CFARNoiseLevelGuardFirst(int _param_cols)
  {
  }
  int cols; // CFARNoiseLevel.str:7
  public void work() { // CFARNoiseLevel.str:11
    float sumNoise = inputChannel.popFloat(); // CFARNoiseLevel.str:12
    float guardNoise = inputChannel.popFloat(); // CFARNoiseLevel.str:13
    for (int i = 0; (i < (104 * cols)); i++) { // CFARNoiseLevel.str:16
      outputChannel.pushFloat(guardNoise); // CFARNoiseLevel.str:17
      outputChannel.pushFloat(sumNoise); // CFARNoiseLevel.str:18
    }; // CFARNoiseLevel.str:15
  }
  public void init(final int _param_cols) { // CFARNoiseLevel.str:7
    cols = _param_cols; // CFARNoiseLevel.str:7
    setIOTypes(Float.TYPE, Float.TYPE); // CFARNoiseLevel.str:7
    addSteadyPhase(2, 2, ((104 * cols) * 2), "work"); // CFARNoiseLevel.str:10
  }
}
class CFARNoiseLevelSplitter extends SplitJoin // CFARNoiseLevel.str:23
{
  public CFARNoiseLevelSplitter(int cols)
  {
  }
  public void init(final int cols) { // CFARNoiseLevel.str:24
    setSplitter(WEIGHTED_ROUND_ROBIN(1, 1)); // CFARNoiseLevel.str:25
    add(new CFARNoiseLevelCalcPipe(cols)); // CFARNoiseLevel.str:26
    add(new CFARNoiseLevelCalcPipe(cols)); // CFARNoiseLevel.str:27
    setJoiner(ROUND_ROBIN(1)); // CFARNoiseLevel.str:28
  }
}
class CFARNoiseLevelCalcPipe extends Pipeline // CFARNoiseLevel.str:32
{
  public CFARNoiseLevelCalcPipe(int cols)
  {
  }
  public void init(final int cols) { // CFARNoiseLevel.str:33
    add(new CFARNoiseLevelMeanCalc(cols, 104)); // CFARNoiseLevel.str:34
    add(new CFARNoiseLevelMeanCalcGather(cols, 12)); // CFARNoiseLevel.str:35
  }
}
class CFARNoiseLevelMeanCalc extends Filter // CFARNoiseLevel.str:38
{
  public CFARNoiseLevelMeanCalc(int _param_cols, int _param_rows)
  {
    setStateful(true);
  }
  int cols; // CFARNoiseLevel.str:38
  int rows; // CFARNoiseLevel.str:38
  public void work() { // CFARNoiseLevel.str:41
    int column = (iter() % cols); // CFARNoiseLevel.str:42
    float sum = 0; // CFARNoiseLevel.str:43
    if (((column < 6) || (column >= (cols - 6)))) { // CFARNoiseLevel.str:45
      for (int i = 0; (i < rows); i++) { // CFARNoiseLevel.str:47
        inputChannel.popFloat(); // CFARNoiseLevel.str:48
      }; // CFARNoiseLevel.str:46
      outputChannel.pushFloat(0); // CFARNoiseLevel.str:50
    } else { // CFARNoiseLevel.str:53
      for (int i = 0; (i < rows); i++) { // CFARNoiseLevel.str:55
        sum += inputChannel.popFloat(); // CFARNoiseLevel.str:56
      }; // CFARNoiseLevel.str:54
      outputChannel.pushFloat((sum / rows)); // CFARNoiseLevel.str:58
    } // CFARNoiseLevel.str:44
  }
  public void init(final int _param_cols, final int _param_rows) { // CFARNoiseLevel.str:38
    cols = _param_cols; // CFARNoiseLevel.str:38
    rows = _param_rows; // CFARNoiseLevel.str:38
    setIOTypes(Float.TYPE, Float.TYPE); // CFARNoiseLevel.str:38
    addSteadyPhase(rows, rows, 1, "work"); // CFARNoiseLevel.str:40
  }
}
class CFARNoiseLevelMeanCalcGather extends Filter // CFARNoiseLevel.str:63
{
  public CFARNoiseLevelMeanCalcGather(int _param_cols, int _param_notUsed)
  {
  }
  int cols; // CFARNoiseLevel.str:63
  int notUsed; // CFARNoiseLevel.str:63
  public void work() { // CFARNoiseLevel.str:67
    float sum = 0; // CFARNoiseLevel.str:68
    for (int i = 0; (i < cols); i++) { // CFARNoiseLevel.str:70
      sum += inputChannel.popFloat(); // CFARNoiseLevel.str:71
    }; // CFARNoiseLevel.str:69
    outputChannel.pushFloat((sum / (cols - notUsed))); // CFARNoiseLevel.str:73
  }
  public void init(final int _param_cols, final int _param_notUsed) { // CFARNoiseLevel.str:63
    cols = _param_cols; // CFARNoiseLevel.str:63
    notUsed = _param_notUsed; // CFARNoiseLevel.str:63
    setIOTypes(Float.TYPE, Float.TYPE); // CFARNoiseLevel.str:63
    addSteadyPhase(cols, cols, 1, "work"); // CFARNoiseLevel.str:66
  }
}
class CFARPipe extends Pipeline // CFAR.str:1
{
  public CFARPipe(int rows, int cols)
  {
  }
  public void init(final int rows, final int cols) { // CFAR.str:2
    add(new CFARToDecibelFilter()); // CFAR.str:3
    add(new CFARNoiseSplitter(cols)); // CFAR.str:4
    add(new CFARTreshLMaxSplitter(cols)); // CFAR.str:6
    add(new CFARDelayToLMax(rows)); // CFAR.str:7
    add(new CFARDetectPipe(rows, cols)); // CFAR.str:8
  }
}
class CFARDelayToLMax extends SplitJoin // CFAR.str:12
{
  public CFARDelayToLMax(int rows)
  {
  }
  public void init(final int rows) { // CFAR.str:12
    setSplitter(ROUND_ROBIN(1)); // CFAR.str:13
    add(new Delay((rows - 1))); // CFAR.str:14
    add(new Delay((rows - 1))); // CFAR.str:15
    add(new Identity(Float.TYPE)); // CFAR.str:16
    add(new Delay((rows - 1))); // CFAR.str:17
    add(new Delay((rows - 1))); // CFAR.str:18
    setJoiner(ROUND_ROBIN(1)); // CFAR.str:19
  }
}
class Delay extends Filter // CFAR.str:22
{
  public Delay(int _param_N)
  {
  }
  int N; // CFAR.str:22
  public void prework() { // CFAR.str:23
    for (int i = 0; (i < N); i++) { // CFAR.str:24
      outputChannel.pushFloat(0.0f); // CFAR.str:25
    }; // CFAR.str:24
  }
  public void work() { // CFAR.str:28
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:29
  }
  public void init(final int _param_N) { // CFAR.str:22
    N = _param_N; // CFAR.str:22
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:22
    addInitPhase(0, 0, N, "prework"); // CFAR.str:23
    addSteadyPhase(1, 1, 1, "work"); // CFAR.str:28
  }
}
class CFARDelayToLMax_filter extends Filter // CFAR.str:33
{
  public CFARDelayToLMax_filter(int _param_rows)
  {
  }
  float[rows] guardNoise; // CFAR.str:35
  float[rows] thresh; // CFAR.str:36
  float[rows] sumDb; // CFAR.str:37
  float[rows] guardDb; // CFAR.str:38
  int popPos; // CFAR.str:39
  int pushPos; // CFAR.str:40
  int rows; // CFAR.str:33
  public void work() { // CFAR.str:49
    guardNoise[popPos] = inputChannel.popFloat(); // CFAR.str:50
    outputChannel.pushFloat(guardNoise[pushPos]); // CFAR.str:51
    thresh[popPos] = inputChannel.popFloat(); // CFAR.str:52
    outputChannel.pushFloat(thresh[pushPos]); // CFAR.str:53
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:54
    sumDb[popPos] = inputChannel.popFloat(); // CFAR.str:55
    outputChannel.pushFloat(sumDb[pushPos]); // CFAR.str:56
    guardDb[popPos] = inputChannel.popFloat(); // CFAR.str:57
    outputChannel.pushFloat(guardDb[pushPos]); // CFAR.str:58
    popPos++; // CFAR.str:60
    pushPos++; // CFAR.str:61
    if ((popPos >= rows)) { // CFAR.str:64
      popPos = 0; // CFAR.str:65
    } // CFAR.str:63
    if ((pushPos >= rows)) { // CFAR.str:68
      pushPos = 0; // CFAR.str:69
    } // CFAR.str:67
  }
  public void init(final int _param_rows) { // CFAR.str:43
    rows = _param_rows; // CFAR.str:42
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:33
    addSteadyPhase(5, 5, 5, "work"); // CFAR.str:48
    popPos = 0; // CFAR.str:44
    pushPos = 1; // CFAR.str:45
  }
}
class CFARNoiseSplitter extends SplitJoin // CFAR.str:74
{
  public CFARNoiseSplitter(int cols)
  {
  }
  public void init(final int cols) { // CFAR.str:75
    setSplitter(DUPLICATE()); // CFAR.str:76
    add(new CFARNoiseLevelPipe(cols)); // CFAR.str:77
    add(new CFARPusher()); // CFAR.str:78
    setJoiner(ROUND_ROBIN(2)); // CFAR.str:79
  }
}
class CFARTreshLMaxSplitter extends SplitJoin // CFAR.str:82
{
  public CFARTreshLMaxSplitter(int cols)
  {
  }
  public void init(final int cols) { // CFAR.str:83
    setSplitter(WEIGHTED_ROUND_ROBIN(1, 3)); // CFAR.str:84
    add(new CFARPusher()); // CFAR.str:85
    add(new CFARTreshLMaxReplicatePipe(cols)); // CFAR.str:86
    setJoiner(WEIGHTED_ROUND_ROBIN(1, 4)); // CFAR.str:87
  }
}
class CFARDetectSplitter extends SplitJoin // CFAR.str:91
{
  public CFARDetectSplitter(int rows, int cols)
  {
  }
  public void init(final int rows, final int cols) { // CFAR.str:92
    setSplitter(WEIGHTED_ROUND_ROBIN(5, 1)); // CFAR.str:93
    add(new CFARDetectPipe(rows, cols)); // CFAR.str:94
    add(new CFARPusher()); // CFAR.str:95
    setJoiner(ROUND_ROBIN(1)); // CFAR.str:96
  }
}
class CFARTreshLMaxReplicatePipe extends Pipeline // CFAR.str:99
{
  public CFARTreshLMaxReplicatePipe(int cols)
  {
  }
  public void init(final int cols) { // CFAR.str:100
    add(new CFARSumReplicateFilter()); // CFAR.str:101
    add(new CFARTreshLMaxSubSplitter(cols)); // CFAR.str:102
  }
}
class CFARSumReplicateFilter extends Filter // CFAR.str:105
{
  public CFARSumReplicateFilter()
  {
  }
  public void work() { // CFAR.str:108
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:109
    float sumDb = inputChannel.popFloat(); // CFAR.str:110
    outputChannel.pushFloat(sumDb); // CFAR.str:111
    outputChannel.pushFloat(sumDb); // CFAR.str:112
    outputChannel.pushFloat(sumDb); // CFAR.str:113
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:114
  }
  public void init() { // CFAR.str:105
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:105
    addSteadyPhase(3, 3, 5, "work"); // CFAR.str:107
  }
}
class CFARTreshLMaxSubSplitter extends SplitJoin // CFAR.str:118
{
  public CFARTreshLMaxSubSplitter(int cols)
  {
  }
  public void init(final int cols) { // CFAR.str:119
    setSplitter(WEIGHTED_ROUND_ROBIN(2, 1, 2)); // CFAR.str:120
    add(new CFARTreshPipe()); // CFAR.str:121
    add(new CFARLMaxPipe(cols)); // CFAR.str:122
    add(new CFARPusher()); // CFAR.str:123
    setJoiner(WEIGHTED_ROUND_ROBIN(1, 1, 2)); // CFAR.str:125
  }
}
class CFARGuardReplicateFilter extends Filter // CFAR.str:129
{
  public CFARGuardReplicateFilter()
  {
  }
  float sumDb; // CFAR.str:131
  public void work() { // CFAR.str:134
    sumDb = inputChannel.popFloat(); // CFAR.str:135
    outputChannel.pushFloat(sumDb); // CFAR.str:136
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:137
    outputChannel.pushFloat(sumDb); // CFAR.str:138
  }
  public void init() { // CFAR.str:129
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:129
    addSteadyPhase(2, 2, 3, "work"); // CFAR.str:133
  }
}
class ExtractSumDb extends Filter // CFAR.str:142
{
  public ExtractSumDb()
  {
  }
  public void work() { // CFAR.str:145
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:146
    inputChannel.popFloat(); // CFAR.str:147
  }
  public void init() { // CFAR.str:142
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:142
    addSteadyPhase(2, 2, 1, "work"); // CFAR.str:144
  }
}
class ExtractSumComp extends Filter // CFAR.str:151
{
  public ExtractSumComp()
  {
  }
  public void work() { // CFAR.str:154
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:155
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:156
    inputChannel.popFloat(); // CFAR.str:157
    inputChannel.popFloat(); // CFAR.str:158
  }
  public void init() { // CFAR.str:151
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:151
    addSteadyPhase(4, 4, 2, "work"); // CFAR.str:153
  }
}
class CFARPusher extends Filter // CFAR.str:162
{
  public CFARPusher()
  {
  }
  public void work() { // CFAR.str:165
    outputChannel.pushFloat(inputChannel.popFloat()); // CFAR.str:166
  }
  public void init() { // CFAR.str:162
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:162
    addSteadyPhase(1, 1, 1, "work"); // CFAR.str:164
  }
}
class CFARPrinter extends Filter // CFAR.str:170
{
  public CFARPrinter()
  {
  }
  float temp; // CFAR.str:172
  public void work() { // CFAR.str:175
    temp = inputChannel.popFloat(); // CFAR.str:177
    System.out.print(temp); // CFAR.str:178
    outputChannel.pushFloat(temp); // CFAR.str:179
  }
  public void init() { // CFAR.str:170
    setIOTypes(Float.TYPE, Float.TYPE); // CFAR.str:170
    addSteadyPhase(1, 1, 1, "work"); // CFAR.str:174
  }
}
class CFARToDecibelFilter extends Filter // CFARToDecibel.str:1
{
  public CFARToDecibelFilter()
  {
  }
  public void work() { // CFARToDecibel.str:4
    Complex video = new Complex(); // CFARToDecibel.str:5
    video.real = inputChannel.popFloat(); // CFARToDecibel.str:6
    video.imag = inputChannel.popFloat(); // CFARToDecibel.str:7
    float videoOut = (20 * (float)Math.log((float)Math.sqrt(((video.real * video.real) + (video.imag * video.imag))))); // CFARToDecibel.str:8
    outputChannel.pushFloat(videoOut); // CFARToDecibel.str:9
  }
  public void init() { // CFARToDecibel.str:1
    setIOTypes(Float.TYPE, Float.TYPE); // CFARToDecibel.str:1
    addSteadyPhase(2, 2, 1, "work"); // CFARToDecibel.str:3
  }
}
class CFARTreshPipe extends Pipeline // CFARTreshold.str:1
{
  public CFARTreshPipe()
  {
  }
  public void init() { // CFARTreshold.str:2
    add(new CFARTreshSplitter()); // CFARTreshold.str:3
    add(new CFARTreshFilter()); // CFARTreshold.str:4
  }
}
class CFARTreshSplitter extends SplitJoin // CFARTreshold.str:7
{
  public CFARTreshSplitter()
  {
  }
  public void init() { // CFARTreshold.str:8
    setSplitter(ROUND_ROBIN(1)); // CFARTreshold.str:9
    add(new CFARPusher()); // CFARTreshold.str:10
    add(new CFARTreshGofPipe()); // CFARTreshold.str:11
    setJoiner(ROUND_ROBIN(1)); // CFARTreshold.str:12
  }
}
class CFARTreshFilter extends Filter // CFARTreshold.str:15
{
  public CFARTreshFilter()
  {
  }
  public void work() { // CFARTreshold.str:18
    float noiseDb = inputChannel.popFloat(); // CFARTreshold.str:19
    float gofDb = (inputChannel.popFloat() - noiseDb); // CFARTreshold.str:20
    if ((gofDb > 0)) { // CFARTreshold.str:23
      outputChannel.pushFloat(((noiseDb + 15) + gofDb)); // CFARTreshold.str:24
    } else { // CFARTreshold.str:27
      outputChannel.pushFloat((noiseDb + 15)); // CFARTreshold.str:28
    } // CFARTreshold.str:22
  }
  public void init() { // CFARTreshold.str:15
    setIOTypes(Float.TYPE, Float.TYPE); // CFARTreshold.str:15
    addSteadyPhase(2, 2, 1, "work"); // CFARTreshold.str:17
  }
}
class CFARTreshGofPipe extends Pipeline // CFARTreshold.str:33
{
  public CFARTreshGofPipe()
  {
  }
  public void init() { // CFARTreshold.str:34
    add(new CFARTreshReorganize(104)); // CFARTreshold.str:35
    add(new CFARTreshCumSumFilter(139)); // CFARTreshold.str:36
    add(new CFARTreshSubFilter(139)); // CFARTreshold.str:37
    add(new CFARTreshMaxFilter(123)); // CFARTreshold.str:38
  }
}
class CFARTreshReorganize extends Filter // CFARTreshold.str:41
{
  public CFARTreshReorganize(int _param_rows)
  {
  }
  int rows; // CFARTreshold.str:41
  public void work() { // CFARTreshold.str:44
    outputChannel.pushFloat(0); // CFARTreshold.str:45
    for (int i = 2; (i < 19); i++) { // CFARTreshold.str:48
      outputChannel.pushFloat(inputChannel.peekFloat(i)); // CFARTreshold.str:49
    }; // CFARTreshold.str:47
    for (int j = 0; (j < 85); j++) { // CFARTreshold.str:53
      outputChannel.pushFloat(inputChannel.popFloat()); // CFARTreshold.str:54
    }; // CFARTreshold.str:52
    for (int k = 0; (k < 19); k++) { // CFARTreshold.str:58
      outputChannel.pushFloat(inputChannel.peekFloat(k)); // CFARTreshold.str:59
    }; // CFARTreshold.str:57
    for (int l = 0; (l < 17); l++) { // CFARTreshold.str:63
      outputChannel.pushFloat(inputChannel.popFloat()); // CFARTreshold.str:64
    }; // CFARTreshold.str:62
    inputChannel.popFloat(); // CFARTreshold.str:67
    inputChannel.popFloat(); // CFARTreshold.str:68
  }
  public void init(final int _param_rows) { // CFARTreshold.str:41
    rows = _param_rows; // CFARTreshold.str:41
    setIOTypes(Float.TYPE, Float.TYPE); // CFARTreshold.str:41
    addSteadyPhase(rows, rows, (rows + 35), "work"); // CFARTreshold.str:43
  }
}
class CFARTreshCumSumFilter extends Filter // CFARTreshold.str:72
{
  public CFARTreshCumSumFilter(int _param_rows)
  {
    setStateful(true);
  }
  float cumSum; // CFARTreshold.str:74
  int counter; // CFARTreshold.str:75
  int rows; // CFARTreshold.str:72
  public void work() { // CFARTreshold.str:83
    cumSum += inputChannel.popFloat(); // CFARTreshold.str:84
    counter++; // CFARTreshold.str:85
    outputChannel.pushFloat(cumSum); // CFARTreshold.str:86
    if ((counter >= rows)) { // CFARTreshold.str:89
      cumSum = 0; // CFARTreshold.str:90
      counter = 0; // CFARTreshold.str:91
    } // CFARTreshold.str:88
  }
  public void init(final int _param_rows) { // CFARTreshold.str:78
    rows = _param_rows; // CFARTreshold.str:77
    setIOTypes(Float.TYPE, Float.TYPE); // CFARTreshold.str:72
    addSteadyPhase(1, 1, 1, "work"); // CFARTreshold.str:82
    cumSum = 0; // CFARTreshold.str:79
    counter = 0; // CFARTreshold.str:80
  }
}
class CFARTreshSubFilter extends Filter // CFARTreshold.str:96
{
  public CFARTreshSubFilter(int _param_rows)
  {
  }
  int rows; // CFARTreshold.str:96
  public void work() { // CFARTreshold.str:99
    for (int i = 0; (i < (rows - 16)); i++) { // CFARTreshold.str:101
      float peekTemp; // CFARTreshold.str:102
      peekTemp = inputChannel.peekFloat(16); // CFARTreshold.str:102
      outputChannel.pushFloat(((peekTemp - inputChannel.popFloat()) / 16)); // CFARTreshold.str:103
    }; // CFARTreshold.str:100
    for (int j = 0; (j < 16); j++) { // CFARTreshold.str:107
      inputChannel.popFloat(); // CFARTreshold.str:108
    }; // CFARTreshold.str:106
  }
  public void init(final int _param_rows) { // CFARTreshold.str:96
    rows = _param_rows; // CFARTreshold.str:96
    setIOTypes(Float.TYPE, Float.TYPE); // CFARTreshold.str:96
    addSteadyPhase(rows, rows, (rows - 16), "work"); // CFARTreshold.str:98
  }
}
class CFARTreshMaxFilter extends Filter // CFARTreshold.str:113
{
  public CFARTreshMaxFilter(int _param_rows)
  {
  }
  int rows; // CFARTreshold.str:113
  public void work() { // CFARTreshold.str:116
    for (int i = 0; (i < (rows - 19)); i++) { // CFARTreshold.str:118
      float peekTemp; // CFARTreshold.str:119
      peekTemp = inputChannel.peekFloat(19); // CFARTreshold.str:119
      float popTemp; // CFARTreshold.str:120
      popTemp = inputChannel.popFloat(); // CFARTreshold.str:120
      if ((peekTemp > popTemp)) { // CFARTreshold.str:122
        outputChannel.pushFloat(peekTemp); // CFARTreshold.str:123
      } else { // CFARTreshold.str:126
        outputChannel.pushFloat(popTemp); // CFARTreshold.str:127
      } // CFARTreshold.str:121
    }; // CFARTreshold.str:117
    for (int j = 0; (j < 19); j++) { // CFARTreshold.str:132
      inputChannel.popFloat(); // CFARTreshold.str:133
    }; // CFARTreshold.str:131
  }
  public void init(final int _param_rows) { // CFARTreshold.str:113
    rows = _param_rows; // CFARTreshold.str:113
    setIOTypes(Float.TYPE, Float.TYPE); // CFARTreshold.str:113
    addSteadyPhase(rows, rows, (rows - 19), "work"); // CFARTreshold.str:115
  }
}
class CTurnComplex extends SplitJoin // CornerTurn.str:1
{
  public CTurnComplex(int rows, int cols)
  {
  }
  public void init(final int rows, final int cols) { // CornerTurn.str:2
    setSplitter(ROUND_ROBIN(4)); // CornerTurn.str:3
    for (int i = 0; (i < cols); i++) { // CornerTurn.str:5
      add(new Identity(Float.TYPE)); // CornerTurn.str:6
    }; // CornerTurn.str:4
    setJoiner(ROUND_ROBIN((4 * rows))); // CornerTurn.str:8
  }
}
class CTurn extends SplitJoin // CornerTurn.str:11
{
  public CTurn(int rows, int cols)
  {
  }
  public void init(final int rows, final int cols) { // CornerTurn.str:12
    setSplitter(ROUND_ROBIN(1)); // CornerTurn.str:13
    for (int i = 0; (i < cols); i++) { // CornerTurn.str:15
      add(new Identity(Float.TYPE)); // CornerTurn.str:16
    }; // CornerTurn.str:14
    setJoiner(ROUND_ROBIN(rows)); // CornerTurn.str:18
  }
}
class Source extends Filter // DopplerFilter.str:9
{
  public Source()
  {
  }
  float realvalue; // DopplerFilter.str:11
  float imagvalue; // DopplerFilter.str:12
  public void work() { // DopplerFilter.str:19
    outputChannel.pushFloat(realvalue); // DopplerFilter.str:20
    outputChannel.pushFloat(imagvalue); // DopplerFilter.str:21
    realvalue += 0.5f; // DopplerFilter.str:22
    imagvalue += 0.5f; // DopplerFilter.str:23
    if ((realvalue > 8.5f)) { // DopplerFilter.str:25
      realvalue = 1; // DopplerFilter.str:26
      imagvalue = 10; // DopplerFilter.str:27
    } // DopplerFilter.str:24
  }
  public void init() { // DopplerFilter.str:14
    setIOTypes(Void.TYPE, Float.TYPE); // DopplerFilter.str:9
    addSteadyPhase(0, 0, 2, "work"); // DopplerFilter.str:18
    realvalue = 1; // DopplerFilter.str:15
    imagvalue = 10; // DopplerFilter.str:16
  }
}
class DopplerFilter extends SplitJoin // DopplerFilter.str:34
{
  public DopplerFilter(int n)
  {
  }
  public void init(final int n) { // DopplerFilter.str:35
    setSplitter(ROUND_ROBIN(2)); // DopplerFilter.str:36
    add(new VideoPipe(n)); // DopplerFilter.str:37
    add(new VideoPipe(n)); // DopplerFilter.str:38
    setJoiner(ROUND_ROBIN(2)); // DopplerFilter.str:39
  }
}
class VideoPipe extends Pipeline // DopplerFilter.str:42
{
  public VideoPipe(int n)
  {
  }
  public void init(final int n) { // DopplerFilter.str:43
    add(new WeightCalc(n)); // DopplerFilter.str:44
    add(new FFTKernel2(n)); // DopplerFilter.str:45
  }
}
class WeightCalc extends Filter // DopplerFilter.str:53
{
  public WeightCalc(int _param_n)
  {
    setStateful(true);
  }
  float[n] window; // DopplerFilter.str:55
  int windowPos; // DopplerFilter.str:56
  int n; // DopplerFilter.str:53
  public void work() { // DopplerFilter.str:79
    outputChannel.pushFloat((inputChannel.popFloat() * window[windowPos])); // DopplerFilter.str:81
    outputChannel.pushFloat((inputChannel.popFloat() * window[windowPos])); // DopplerFilter.str:82
    windowPos++; // DopplerFilter.str:84
    if ((windowPos >= n)) { // DopplerFilter.str:86
      windowPos = 0; // DopplerFilter.str:87
    } // DopplerFilter.str:85
  }
  public void init(final int _param_n) { // DopplerFilter.str:60
    n = _param_n; // DopplerFilter.str:59
    setIOTypes(Float.TYPE, Float.TYPE); // DopplerFilter.str:53
    addSteadyPhase(2, 2, 2, "work"); // DopplerFilter.str:78
    float normValue = 0.0f; // DopplerFilter.str:61
    for (int i = 0; (i < n); i++) { // DopplerFilter.str:63
      window[i] = (float)Math.sin(((3.141592653589793f * (i + 0.5f)) / n)); // DopplerFilter.str:64
      window[i] = (window[i] * window[i]); // DopplerFilter.str:65
      normValue += (window[i] * window[i]); // DopplerFilter.str:66
    }; // DopplerFilter.str:62
    normValue = (float)Math.sqrt(normValue); // DopplerFilter.str:69
    for (int i = 0; (i < n); i++) { // DopplerFilter.str:72
      window[i] = (window[i] / normValue); // DopplerFilter.str:73
    }; // DopplerFilter.str:71
  }
}
class Float2Complex extends Filter // DopplerFilter.str:92
{
  public Float2Complex()
  {
  }
  Complex c; // DopplerFilter.str:94
  public void work() { // DopplerFilter.str:97
    c.real = inputChannel.popFloat(); // DopplerFilter.str:98
    c.imag = inputChannel.popFloat(); // DopplerFilter.str:99
    outputChannel.push(c); // DopplerFilter.str:100
  }
  public void init() { // DopplerFilter.str:92
    c = new Complex(); // DopplerFilter.str:94
    setIOTypes(Float.TYPE, Complex.class); // DopplerFilter.str:92
    addSteadyPhase(2, 2, 1, "work"); // DopplerFilter.str:96
  }
}
class Complex2Float extends Filter // DopplerFilter.str:104
{
  public Complex2Float()
  {
  }
  Complex c; // DopplerFilter.str:106
  public void work() { // DopplerFilter.str:109
    c = (Complex)inputChannel.pop(); // DopplerFilter.str:110
    outputChannel.pushFloat(c.real); // DopplerFilter.str:111
    outputChannel.pushFloat(c.imag); // DopplerFilter.str:112
  }
  public void init() { // DopplerFilter.str:104
    c = new Complex(); // DopplerFilter.str:106
    setIOTypes(Complex.class, Float.TYPE); // DopplerFilter.str:104
    addSteadyPhase(1, 1, 2, "work"); // DopplerFilter.str:108
  }
}
class CombineDFT extends Filter // FFT2.str:10
{
  public CombineDFT(int _param_n)
  {
  }
  float[n] w; // FFT2.str:13
  int n; // FFT2.str:10
  public void work() { // FFT2.str:31
    int i; // FFT2.str:32
    float[(2 * n)] results; // FFT2.str:33
    for (i = 0; (i < n); i += 2) { // FFT2.str:36
      int i_plus_1; // FFT2.str:46
      i_plus_1 = (i + 1); // FFT2.str:46
      float y0_r; // FFT2.str:48
      y0_r = inputChannel.peekFloat(i); // FFT2.str:48
      float y0_i; // FFT2.str:49
      y0_i = inputChannel.peekFloat(i_plus_1); // FFT2.str:49
      float y1_r; // FFT2.str:51
      y1_r = inputChannel.peekFloat((n + i)); // FFT2.str:51
      float y1_i; // FFT2.str:52
      y1_i = inputChannel.peekFloat((n + i_plus_1)); // FFT2.str:52
      float weight_real; // FFT2.str:56
      weight_real = w[i]; // FFT2.str:56
      float weight_imag; // FFT2.str:57
      weight_imag = w[i_plus_1]; // FFT2.str:57
      float y1w_r; // FFT2.str:59
      y1w_r = ((y1_r * weight_real) - (y1_i * weight_imag)); // FFT2.str:59
      float y1w_i; // FFT2.str:60
      y1w_i = ((y1_r * weight_imag) + (y1_i * weight_real)); // FFT2.str:60
      results[i] = (y0_r + y1w_r); // FFT2.str:62
      results[(i + 1)] = (y0_i + y1w_i); // FFT2.str:63
      results[(n + i)] = (y0_r - y1w_r); // FFT2.str:65
      results[((n + i) + 1)] = (y0_i - y1w_i); // FFT2.str:66
    }; // FFT2.str:35
    for (i = 0; (i < (2 * n)); i++) { // FFT2.str:70
      inputChannel.popFloat(); // FFT2.str:71
      outputChannel.pushFloat(results[i]); // FFT2.str:72
    }; // FFT2.str:69
  }
  public void init(final int _param_n) { // FFT2.str:15
    n = _param_n; // FFT2.str:15
    setIOTypes(Float.TYPE, Float.TYPE); // FFT2.str:10
    addSteadyPhase((2 * n), (2 * n), (2 * n), "work"); // FFT2.str:31
    float wn_r = ((float)((float)Math.cos(((2 * 3.141592654f) / n)))); // FFT2.str:16
    float wn_i = ((float)((float)Math.sin(((-2 * 3.141592654f) / n)))); // FFT2.str:17
    float real = 1; // FFT2.str:18
    float imag = 0; // FFT2.str:19
    float next_real, next_imag; // FFT2.str:20
    for (int i = 0; (i < n); i += 2) { // FFT2.str:21
      w[i] = real; // FFT2.str:22
      w[(i + 1)] = imag; // FFT2.str:23
      next_real = ((real * wn_r) - (imag * wn_i)); // FFT2.str:24
      next_imag = ((real * wn_i) + (imag * wn_r)); // FFT2.str:25
      real = next_real; // FFT2.str:26
      imag = next_imag; // FFT2.str:27
    }; // FFT2.str:21
  }
}
class FFTReorderSimple extends Filter // FFT2.str:79
{
  public FFTReorderSimple(int _param_n)
  {
  }
  int totalData; // FFT2.str:81
  int n; // FFT2.str:79
  public void work() { // FFT2.str:87
    int i; // FFT2.str:88
    for (i = 0; (i < totalData); i += 4) { // FFT2.str:91
      outputChannel.pushFloat(inputChannel.peekFloat(i)); // FFT2.str:92
      outputChannel.pushFloat(inputChannel.peekFloat((i + 1))); // FFT2.str:93
    }; // FFT2.str:90
    for (i = 2; (i < totalData); i += 4) { // FFT2.str:97
      outputChannel.pushFloat(inputChannel.peekFloat(i)); // FFT2.str:98
      outputChannel.pushFloat(inputChannel.peekFloat((i + 1))); // FFT2.str:99
    }; // FFT2.str:96
    for (i = 0; (i < n); i++) { // FFT2.str:103
      inputChannel.popFloat(); // FFT2.str:104
      inputChannel.popFloat(); // FFT2.str:105
    }; // FFT2.str:102
  }
  public void init(final int _param_n) { // FFT2.str:83
    n = _param_n; // FFT2.str:83
    setIOTypes(Float.TYPE, Float.TYPE); // FFT2.str:79
    addSteadyPhase((2 * n), (2 * n), (2 * n), "work"); // FFT2.str:87
    totalData = (2 * n); // FFT2.str:84
  }
}
class FFTReorder extends Pipeline // FFT2.str:111
{
  public FFTReorder(int n)
  {
  }
  public void init(final int n) { // FFT2.str:111
    for (int i = 1; (i < (n / 2)); i *= 2) { // FFT2.str:114
      add(new FFTReorderSimple((n / i))); // FFT2.str:114
    }; // FFT2.str:113
  }
}
class FFTKernel1 extends Pipeline // FFT2.str:119
{
  public FFTKernel1(int n)
  {
  }
  public void init(final int n) { // FFT2.str:119
    if ((n > 2)) { // FFT2.str:121
      add(new AnonFilter_a0(n)); // FFT2.str:122
    } // FFT2.str:121
    add(new CombineDFT(n)); // FFT2.str:129
  }
}
class FFTKernel2 extends SplitJoin // FFT2.str:133
{
  public FFTKernel2(int n)
  {
  }
  public void init(final int n) { // FFT2.str:133
    setSplitter(ROUND_ROBIN((2 * n))); // FFT2.str:135
    for (int i = 0; (i < 2); i++) { // FFT2.str:136
      add(new AnonFilter_a1(n)); // FFT2.str:137
    }; // FFT2.str:136
    setJoiner(ROUND_ROBIN((2 * n))); // FFT2.str:143
  }
}
class FFTKernel3 extends Pipeline // FFT2.str:148
{
  public FFTKernel3(int n)
  {
  }
  public void init(final int n) { // FFT2.str:148
    add(new AnonFilter_a2(n)); // FFT2.str:149
  }
}
class FFTTestSource extends Filter // FFT2.str:157
{
  public FFTTestSource(int _param_N)
  {
  }
  int N; // FFT2.str:157
  public void work() { // FFT2.str:159
    int i; // FFT2.str:160
    outputChannel.pushFloat(0.0f); // FFT2.str:161
    outputChannel.pushFloat(0.0f); // FFT2.str:162
    outputChannel.pushFloat(1.0f); // FFT2.str:163
    outputChannel.pushFloat(0.0f); // FFT2.str:164
    for (i = 0; (i < (2 * (N - 2))); i++) { // FFT2.str:167
      outputChannel.pushFloat(0.0f); // FFT2.str:167
    }; // FFT2.str:166
  }
  public void init(final int _param_N) { // FFT2.str:157
    N = _param_N; // FFT2.str:157
    setIOTypes(Void.TYPE, Float.TYPE); // FFT2.str:157
    addSteadyPhase(0, 0, (2 * N), "work"); // FFT2.str:159
  }
}
class FloatPrinter extends Filter // FFT2.str:172
{
  public FloatPrinter()
  {
  }
  public void work() { // FFT2.str:174
    System.out.println(inputChannel.popFloat()); // FFT2.str:176
  }
  public void init() { // FFT2.str:172
    setIOTypes(Float.TYPE, Void.TYPE); // FFT2.str:172
    addSteadyPhase(1, 1, 0, "work"); // FFT2.str:174
  }
}
class IFFTKernel2 extends Pipeline // IFFTKernel.str:1
{
  public IFFTKernel2(int N)
  {
  }
  public void init(final int N) { // IFFTKernel.str:1
    add(new Conjugate()); // IFFTKernel.str:2
    add(new FFTKernel2(N)); // IFFTKernel.str:3
    add(new Conjugate()); // IFFTKernel.str:4
    add(new DivideByN(N)); // IFFTKernel.str:5
  }
}
class Conjugate extends Filter // IFFTKernel.str:8
{
  public Conjugate()
  {
  }
  public void work() { // IFFTKernel.str:9
    outputChannel.pushFloat(inputChannel.popFloat()); // IFFTKernel.str:10
    outputChannel.pushFloat(-inputChannel.popFloat()); // IFFTKernel.str:11
  }
  public void init() { // IFFTKernel.str:8
    setIOTypes(Float.TYPE, Float.TYPE); // IFFTKernel.str:8
    addSteadyPhase(2, 2, 2, "work"); // IFFTKernel.str:9
  }
}
class DivideByN extends Filter // IFFTKernel.str:15
{
  public DivideByN(int _param_N)
  {
  }
  int N; // IFFTKernel.str:15
  public void work() { // IFFTKernel.str:16
    outputChannel.pushFloat((inputChannel.popFloat() / N)); // IFFTKernel.str:17
  }
  public void init(final int _param_N) { // IFFTKernel.str:15
    N = _param_N; // IFFTKernel.str:15
    setIOTypes(Float.TYPE, Float.TYPE); // IFFTKernel.str:15
    addSteadyPhase(1, 1, 1, "work"); // IFFTKernel.str:16
  }
}
class SumFIRFilter extends Pipeline // FIR.str:1
{
  public SumFIRFilter(int rows)
  {
  }
  public void init(final int rows) { // FIR.str:2
    add(new FIRReset(rows)); // FIR.str:3
    add(new FIRZeroAdder()); // FIR.str:4
    for (int i = 11; (i > 0); i--) { // FIR.str:6
      add(new SingleMultiply(i)); // FIR.str:6
    }; // FIR.str:5
    add(new FIRCleanUp()); // FIR.str:7
    add(new FIRResetClean(rows)); // FIR.str:8
  }
}
class FIRReset extends Filter // FIR.str:11
{
  public FIRReset(int _param_rows)
  {
  }
  int rows; // FIR.str:11
  public void work() { // FIR.str:14
    for (int i = 0; (i < rows); i++) { // FIR.str:16
      outputChannel.pushFloat(inputChannel.popFloat()); // FIR.str:17
      outputChannel.pushFloat(inputChannel.popFloat()); // FIR.str:18
    }; // FIR.str:15
    for (int j = 0; (j < 20); j++) { // FIR.str:22
      outputChannel.pushFloat(0); // FIR.str:23
    }; // FIR.str:21
  }
  public void init(final int _param_rows) { // FIR.str:11
    rows = _param_rows; // FIR.str:11
    setIOTypes(Float.TYPE, Float.TYPE); // FIR.str:11
    addSteadyPhase((rows * 2), (rows * 2), ((rows * 2) + 20), "work"); // FIR.str:13
  }
}
class FIRResetClean extends Filter // FIR.str:28
{
  public FIRResetClean(int _param_rows)
  {
  }
  int rows; // FIR.str:28
  public void work() { // FIR.str:31
    for (int i = 0; (i < rows); i++) { // FIR.str:33
      outputChannel.pushFloat(inputChannel.popFloat()); // FIR.str:34
      outputChannel.pushFloat(inputChannel.popFloat()); // FIR.str:35
    }; // FIR.str:32
    for (int j = 0; (j < 20); j++) { // FIR.str:39
      inputChannel.popFloat(); // FIR.str:40
    }; // FIR.str:38
  }
  public void init(final int _param_rows) { // FIR.str:28
    rows = _param_rows; // FIR.str:28
    setIOTypes(Float.TYPE, Float.TYPE); // FIR.str:28
    addSteadyPhase(((rows * 2) + 20), ((rows * 2) + 20), (rows * 2), "work"); // FIR.str:30
  }
}
class FIRZeroAdder extends Filter // FIR.str:45
{
  public FIRZeroAdder()
  {
  }
  public void work() { // FIR.str:48
    outputChannel.pushFloat(0); // FIR.str:49
    outputChannel.pushFloat(inputChannel.popFloat()); // FIR.str:50
  }
  public void init() { // FIR.str:45
    setIOTypes(Float.TYPE, Float.TYPE); // FIR.str:45
    addSteadyPhase(1, 1, 2, "work"); // FIR.str:47
  }
}
class FIRCleanUp extends Filter // FIR.str:56
{
  public FIRCleanUp()
  {
  }
  public void prework() { // FIR.str:59
    for (int i = 0; (i < 24); i++) { // FIR.str:61
      inputChannel.popFloat(); // FIR.str:62
    }; // FIR.str:60
  }
  public void work() { // FIR.str:66
    outputChannel.pushFloat(inputChannel.popFloat()); // FIR.str:67
    inputChannel.popFloat(); // FIR.str:68
  }
  public void init() { // FIR.str:56
    setIOTypes(Float.TYPE, Float.TYPE); // FIR.str:56
    addInitPhase(24, 24, 0, "prework"); // FIR.str:58
    addSteadyPhase(2, 2, 1, "work"); // FIR.str:65
  }
}
class SingleMultiply extends Filter // FIR.str:72
{
  public SingleMultiply(int _param_wPos)
  {
  }
  float weightNoReal; // FIR.str:74
  float weightNoImag; // FIR.str:75
  float weightNoExp; // FIR.str:76
  float lastReal; // FIR.str:79
  float lastImag; // FIR.str:80
  int wPos; // FIR.str:72
  public void prework() { // FIR.str:120
    outputChannel.pushFloat(inputChannel.peekFloat(0)); // FIR.str:121
    outputChannel.pushFloat(0.0f); // FIR.str:122
    outputChannel.pushFloat(inputChannel.peekFloat(2)); // FIR.str:123
    outputChannel.pushFloat(0.0f); // FIR.str:124
    inputChannel.popFloat(); // FIR.str:125
  }
  public void work() { // FIR.str:128
    outputChannel.pushFloat(((inputChannel.peekFloat(3) + (inputChannel.peekFloat(0) * weightNoReal)) - (inputChannel.peekFloat(2) * weightNoImag))); // FIR.str:129
    outputChannel.pushFloat(inputChannel.peekFloat(0)); // FIR.str:130
    outputChannel.pushFloat(((inputChannel.peekFloat(5) + (inputChannel.peekFloat(0) * weightNoImag)) + (inputChannel.peekFloat(2) * weightNoReal))); // FIR.str:132
    outputChannel.pushFloat(inputChannel.peekFloat(2)); // FIR.str:133
    for (int i = 0; (i < 4); i++) { // FIR.str:135
      inputChannel.popFloat(); // FIR.str:136
    }; // FIR.str:135
  }
  public void init(final int _param_wPos) { // FIR.str:83
    wPos = _param_wPos; // FIR.str:82
    setIOTypes(Float.TYPE, Float.TYPE); // FIR.str:72
    addInitPhase(3, 1, 4, "prework"); // FIR.str:120
    addSteadyPhase(6, 4, 4, "work"); // FIR.str:128
    float real = 0; // FIR.str:84
    float imag = 0; // FIR.str:85
    float nRsPulse = 11; // FIR.str:86
    int noOfTaps = 11; // FIR.str:87
    float normValue = 0; // FIR.str:88
    float[11] weight; // FIR.str:89
    for (int i = noOfTaps; (i > 0); i--) { // FIR.str:93
      weight[(noOfTaps - i)] = (float)Math.sin(((3.141592653589793f * (i - 0.5f)) / nRsPulse)); // FIR.str:94
      weight[(noOfTaps - i)] = (weight[(noOfTaps - i)] * weight[(noOfTaps - i)]); // FIR.str:95
      normValue += (weight[(noOfTaps - i)] * weight[(noOfTaps - i)]); // FIR.str:96
    }; // FIR.str:92
    normValue = (float)Math.sqrt(normValue); // FIR.str:99
    for (int i = 0; (i < noOfTaps); i++) { // FIR.str:102
      weight[i] = (weight[i] / normValue); // FIR.str:103
    }; // FIR.str:101
    weightNoReal = 0; // FIR.str:106
    weightNoImag = ((wPos - (nRsPulse / 2)) - 0.5f); // FIR.str:107
    weightNoImag = (((-2 * 3.141592653589793f) * (weightNoImag * weightNoImag)) / (2 * nRsPulse)); // FIR.str:108
    weightNoExp = (float)Math.exp(weightNoReal); // FIR.str:109
    weightNoReal = (weightNoExp * (float)Math.cos(weightNoImag)); // FIR.str:110
    weightNoImag = (weightNoExp * (float)Math.sin(weightNoImag)); // FIR.str:111
    weightNoReal = (weight[(wPos - 1)] * weightNoReal); // FIR.str:113
    weightNoImag = (weight[(wPos - 1)] * weightNoImag); // FIR.str:114
    weightNoImag = (weightNoImag * -1); // FIR.str:115
  }
}
class GuardSplitter extends SplitJoin // PulseCompressGuard.str:1
{
  public GuardSplitter(int cols)
  {
  }
  public void init(final int cols) { // PulseCompressGuard.str:2
    setSplitter(WEIGHTED_ROUND_ROBIN(0, 2)); // PulseCompressGuard.str:3
    add(new PulsePipe(cols)); // PulseCompressGuard.str:4
    add(new GuardFFTPipe()); // PulseCompressGuard.str:5
    setJoiner(ROUND_ROBIN(2)); // PulseCompressGuard.str:7
  }
}
class PulsePipe extends Pipeline // PulseCompressGuard.str:10
{
  public PulsePipe(int cols)
  {
  }
  public void init(final int cols) { // PulseCompressGuard.str:11
    add(new ValueCreator()); // PulseCompressGuard.str:12
    add(new FFTKernel2(128)); // PulseCompressGuard.str:13
    add(new GuardConj()); // PulseCompressGuard.str:14
  }
}
class GuardIFFTPipe extends Pipeline // PulseCompressGuard.str:18
{
  public GuardIFFTPipe()
  {
  }
  public void init() { // PulseCompressGuard.str:19
    add(new IFFTKernel2(128)); // PulseCompressGuard.str:20
    add(new GuardRemovePadded()); // PulseCompressGuard.str:21
  }
}
class GuardRemovePadded extends Filter // PulseCompressGuard.str:24
{
  public GuardRemovePadded()
  {
  }
  public void work() { // PulseCompressGuard.str:27
    for (int i = 0; (i < 208); i++) { // PulseCompressGuard.str:29
      outputChannel.pushFloat(inputChannel.popFloat()); // PulseCompressGuard.str:30
    }; // PulseCompressGuard.str:28
    for (int i = 208; (i < 256); i++) { // PulseCompressGuard.str:34
      inputChannel.popFloat(); // PulseCompressGuard.str:35
    }; // PulseCompressGuard.str:33
  }
  public void init() { // PulseCompressGuard.str:24
    setIOTypes(Float.TYPE, Float.TYPE); // PulseCompressGuard.str:24
    addSteadyPhase(256, 256, 208, "work"); // PulseCompressGuard.str:26
  }
}
class GuardFFTPipe extends Pipeline // PulseCompressGuard.str:40
{
  public GuardFFTPipe()
  {
  }
  public void init() { // PulseCompressGuard.str:41
    add(new GuardDataPadder()); // PulseCompressGuard.str:42
    add(new FFTKernel2(128)); // PulseCompressGuard.str:43
  }
}
class GuardDataPadder extends Filter // PulseCompressGuard.str:46
{
  public GuardDataPadder()
  {
  }
  public void work() { // PulseCompressGuard.str:49
    for (int i = 0; (i < 10); i++) { // PulseCompressGuard.str:51
      outputChannel.pushFloat(0); // PulseCompressGuard.str:52
    }; // PulseCompressGuard.str:50
    for (int i = 10; (i < 218); i++) { // PulseCompressGuard.str:56
      outputChannel.pushFloat(inputChannel.popFloat()); // PulseCompressGuard.str:57
    }; // PulseCompressGuard.str:55
    for (int i = 218; (i < 256); i++) { // PulseCompressGuard.str:60
      outputChannel.pushFloat(0); // PulseCompressGuard.str:61
    }; // PulseCompressGuard.str:59
  }
  public void init() { // PulseCompressGuard.str:46
    setIOTypes(Float.TYPE, Float.TYPE); // PulseCompressGuard.str:46
    addSteadyPhase(208, 208, 256, "work"); // PulseCompressGuard.str:48
  }
}
class GuardMultiplier extends Filter // PulseCompressGuard.str:66
{
  public GuardMultiplier()
  {
  }
  public void work() { // PulseCompressGuard.str:70
    float pulseReal = inputChannel.popFloat(); // PulseCompressGuard.str:71
    float pulseImag = inputChannel.popFloat(); // PulseCompressGuard.str:72
    float dataReal = inputChannel.popFloat(); // PulseCompressGuard.str:73
    float dataImag = inputChannel.popFloat(); // PulseCompressGuard.str:74
    outputChannel.pushFloat(((pulseReal * dataReal) - (pulseImag * dataImag))); // PulseCompressGuard.str:76
    outputChannel.pushFloat(((pulseReal * dataImag) + (pulseImag * dataReal))); // PulseCompressGuard.str:77
  }
  public void init() { // PulseCompressGuard.str:66
    setIOTypes(Float.TYPE, Float.TYPE); // PulseCompressGuard.str:66
    addSteadyPhase(4, 4, 2, "work"); // PulseCompressGuard.str:69
  }
}
class ValueCreator extends Filter // PulseCompressGuard.str:81
{
  public ValueCreator()
  {
  }
  float nRsPulse; // PulseCompressGuard.str:83
  float[11] weight; // PulseCompressGuard.str:84
  int noOfTaps; // PulseCompressGuard.str:85
  float[11] weightNoReal; // PulseCompressGuard.str:86
  float[11] weightNoImag; // PulseCompressGuard.str:87
  float weightNoExp; // PulseCompressGuard.str:88
  float lastReal; // PulseCompressGuard.str:89
  float lastImag; // PulseCompressGuard.str:90
  float real; // PulseCompressGuard.str:91
  float imag; // PulseCompressGuard.str:92
  float normValue; // PulseCompressGuard.str:93
  float tempReal; // PulseCompressGuard.str:94
  float nFFT; // PulseCompressGuard.str:95
  int pushCounter; // PulseCompressGuard.str:96
  public void work() { // PulseCompressGuard.str:138
    int pushCounter = (iter() % 128); // PulseCompressGuard.str:139
    if ((pushCounter < 11)) { // PulseCompressGuard.str:141
      outputChannel.pushFloat(weightNoReal[pushCounter]); // PulseCompressGuard.str:142
      outputChannel.pushFloat(weightNoImag[pushCounter]); // PulseCompressGuard.str:143
    } else { // PulseCompressGuard.str:146
      outputChannel.pushFloat(0); // PulseCompressGuard.str:147
      outputChannel.pushFloat(0); // PulseCompressGuard.str:148
    } // PulseCompressGuard.str:140
  }
  public void init() { // PulseCompressGuard.str:99
    setIOTypes(Void.TYPE, Float.TYPE); // PulseCompressGuard.str:81
    addSteadyPhase(0, 0, 2, "work"); // PulseCompressGuard.str:137
    lastReal = 0; // PulseCompressGuard.str:100
    lastImag = 0; // PulseCompressGuard.str:101
    real = 0; // PulseCompressGuard.str:102
    imag = 0; // PulseCompressGuard.str:103
    nRsPulse = 11; // PulseCompressGuard.str:104
    noOfTaps = 11; // PulseCompressGuard.str:105
    normValue = 0; // PulseCompressGuard.str:106
    nFFT = 128; // PulseCompressGuard.str:107
    pushCounter = 0; // PulseCompressGuard.str:108
    for (int i = noOfTaps; (i > 0); i--) { // PulseCompressGuard.str:111
      weight[(noOfTaps - i)] = (float)Math.sin(((3.141592653589793f * (i - 0.5f)) / nRsPulse)); // PulseCompressGuard.str:112
      weight[(noOfTaps - i)] = (weight[(noOfTaps - i)] * weight[(noOfTaps - i)]); // PulseCompressGuard.str:113
      normValue += (weight[(noOfTaps - i)] * weight[(noOfTaps - i)]); // PulseCompressGuard.str:114
    }; // PulseCompressGuard.str:110
    normValue = (float)Math.sqrt(normValue); // PulseCompressGuard.str:117
    for (int i = 0; (i < noOfTaps); i++) { // PulseCompressGuard.str:120
      weight[i] = (weight[i] / normValue); // PulseCompressGuard.str:121
      weightNoReal[i] = 0; // PulseCompressGuard.str:123
      weightNoImag[i] = (((i + 1) - (nRsPulse / 2)) - 0.5f); // PulseCompressGuard.str:124
      weightNoImag[i] = (((-2 * 3.141592653589793f) * (weightNoImag[i] * weightNoImag[i])) / (2 * nRsPulse)); // PulseCompressGuard.str:125
      weightNoExp = (float)Math.exp(weightNoReal[i]); // PulseCompressGuard.str:126
      weightNoReal[i] = (weightNoExp * (float)Math.cos(weightNoImag[i])); // PulseCompressGuard.str:127
      weightNoImag[i] = (weightNoExp * (float)Math.sin(weightNoImag[i])); // PulseCompressGuard.str:128
      weightNoReal[i] = (weight[i] * weightNoReal[i]); // PulseCompressGuard.str:130
      weightNoImag[i] = (weight[i] * weightNoImag[i]); // PulseCompressGuard.str:131
    }; // PulseCompressGuard.str:119
  }
}
class GuardConj extends Filter // PulseCompressGuard.str:153
{
  public GuardConj()
  {
  }
  public void work() { // PulseCompressGuard.str:156
    outputChannel.pushFloat(inputChannel.popFloat()); // PulseCompressGuard.str:157
    outputChannel.pushFloat((inputChannel.popFloat() * -1)); // PulseCompressGuard.str:158
  }
  public void init() { // PulseCompressGuard.str:153
    setIOTypes(Float.TYPE, Float.TYPE); // PulseCompressGuard.str:153
    addSteadyPhase(2, 2, 2, "work"); // PulseCompressGuard.str:155
  }
}
class PulseCompression extends SplitJoin // PulseCompression.str:1
{
  public PulseCompression(int rows, int cols)
  {
  }
  public void init(final int rows, final int cols) { // PulseCompression.str:2
    setSplitter(ROUND_ROBIN(2)); // PulseCompression.str:3
    add(new SumFIRFilter(rows)); // PulseCompression.str:4
    add(new GuardPipe(cols)); // PulseCompression.str:5
    setJoiner(ROUND_ROBIN(2)); // PulseCompression.str:6
  }
}
class GuardPipe extends Pipeline // PulseCompression.str:9
{
  public GuardPipe(int cols)
  {
  }
  public void init(final int cols) { // PulseCompression.str:10
    add(new GuardSplitter(cols)); // PulseCompression.str:11
    add(new GuardMultiplier()); // PulseCompression.str:12
    add(new GuardIFFTPipe()); // PulseCompression.str:13
  }
}
class AnonFilter_a0 extends SplitJoin // FFT2.str:122
{
  public AnonFilter_a0(int n)
  {
  }
  public void init(final int n) { // FFT2.str:122
    setSplitter(ROUND_ROBIN(2)); // FFT2.str:123
    add(new FFTKernel1((n / 2))); // FFT2.str:124
    add(new FFTKernel1((n / 2))); // FFT2.str:125
    setJoiner(ROUND_ROBIN(n)); // FFT2.str:126
  }
}
class AnonFilter_a1 extends Pipeline // FFT2.str:137
{
  public AnonFilter_a1(int n)
  {
  }
  public void init(final int n) { // FFT2.str:137
    add(new FFTReorder(n)); // FFT2.str:138
    for (int j = 2; (j <= n); j *= 2) { // FFT2.str:140
      add(new CombineDFT(j)); // FFT2.str:140
    }; // FFT2.str:139
  }
}
class AnonFilter_a2 extends Pipeline // FFT2.str:149
{
  public AnonFilter_a2(int n)
  {
  }
  public void init(final int n) { // FFT2.str:149
    add(new FFTReorder(n)); // FFT2.str:150
    for (int j = 2; (j <= n); j *= 2) { // FFT2.str:152
      add(new CombineDFT(j)); // FFT2.str:152
    }; // FFT2.str:151
  }
}
public class MPDChain extends StreamItPipeline // MPDChain.str:2
{
  public void init() { // MPDChain.str:3
    int cols = 32; // MPDChain.str:5
    int rows = 104; // MPDChain.str:7
    add(new FileReader("video.bin", Float.TYPE)); // MPDChain.str:10
    add(new DopplerFilter(cols)); // MPDChain.str:12
    add(new CTurnComplex(rows, cols)); // MPDChain.str:13
    add(new PulseCompression(rows, cols)); // MPDChain.str:14
    add(new CFARPipe(rows, cols)); // MPDChain.str:15
    add(new Printer()); // MPDChain.str:17
  }
}
